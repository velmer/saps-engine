package org.fogbowcloud.saps.engine.core.dispatcher;

import org.apache.log4j.Logger;
import org.fogbowcloud.saps.engine.core.model.ImageTask;
import org.fogbowcloud.saps.engine.core.model.ImageTaskState;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.MediaType;
import org.restlet.ext.json.JsonConverter;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Concret implementation of {@link SubmissionManager}.
 */
public class SubmissionManagerImpl implements SubmissionManager {

    private static final Logger LOGGER = Logger.getLogger(SubmissionManagerImpl.class);

    private static final String SAPS_NEIGHBORS_URLS = "saps_neighbors_urls";
    private static final String PROCESSED_TASKS_URN = "/archivedTask";

    private Properties properties;
    private SubmissionDispatcher submissionDispatcher;

    public SubmissionManagerImpl(Properties properties, SubmissionDispatcher submissionDispatcher) {
        this.properties = properties;
        this.submissionDispatcher = submissionDispatcher;
    }

    @Override
    public List<Task> addTasks(SubmissionParameters submissionParameters) {
        List<Task> processedTasks = new ArrayList<>();
        List<Date> processedDates = new ArrayList<>();
        try {
            List<ImageTask> processedImageTasks = getAllRemotelyProcessedTasks(submissionParameters);
            if (!processedImageTasks.isEmpty()) {
                for (ImageTask processedTask : processedImageTasks) {
                    processedTask.setState(ImageTaskState.REMOTELY_ARCHIVED);
                }
                processedTasks = submissionDispatcher.addImageTasks(processedImageTasks);
                processedDates = processedImageTasks.stream()
                        .map(ImageTask::getImageDate)
                        .collect(Collectors.toList());
            }
        } catch (Throwable t) {
            LOGGER.error("Error while adding remotely processed tasks.", t);
        }
        List<Task> addedTasks = submissionDispatcher.addTasks(submissionParameters, processedDates);
        List<Task> allAddedTasks = new ArrayList<>();
        allAddedTasks.addAll(processedTasks);
        allAddedTasks.addAll(addedTasks);
        allAddedTasks.sort(Comparator.comparing(task -> task.getImageTask().getImageDate()));
        return allAddedTasks;
    }

    /**
     * Gets list of processed image tasks, ordered by date, from all SAPS
     * neighbors from this instance. If more than one image task is found for
     * the same date, region and satellite, only the first one will be in
     * returned list.
     *
     * @param submissionParameters Parameters of user submission.
     * @return List of processed tasks from all SAPS neighbors.
     */
    private List<ImageTask> getAllRemotelyProcessedTasks(SubmissionParameters submissionParameters) {
        String[] SAPSNeighborsUrls = getSAPSNeighborsUrls();
        List<ImageTask> processedTasks = Arrays.stream(SAPSNeighborsUrls)
                .map(SAPSNeighborsUrl -> getRemotelyProcessedTasksFromInstance(
                        SAPSNeighborsUrl,
                        submissionParameters))
                .flatMap(Collection::stream)
                .sorted(Comparator.comparing(ImageTask::getImageDate))
                .collect(Collectors.toList());
        removeImageTaskDuplicates(processedTasks);
        return processedTasks;
    }

    /**
     * Removes {@link ImageTask} duplicates. Considers that two ImageTasks are
     * duplicates when they have the same date, region and satellite (dataset).
     *
     * Assumes that {@param imageTasks} list is ordered by date.
     *
     * @param imageTasks List of ImageTasks to have its duplicates removed.
     */
    private void removeImageTaskDuplicates(List<ImageTask> imageTasks) {
        for(int i = 0; i < imageTasks.size(); i++) {
            ImageTask current = imageTasks.get(i);
            int j = i + 1;
            while (j < imageTasks.size() && current.getImageDate().equals(imageTasks.get(j).getImageDate())) {
                boolean areDuplicates = current.getRegion().equals(imageTasks.get(j).getRegion())
                        && current.getDataset().equals(imageTasks.get(j).getDataset());
                if (areDuplicates) {
                    imageTasks.remove(j);
                } else {
                    j++;
                }
            }
        }
    }

    /**
     * Gets list of processed tasks from SAPS neighbor that had its URL specified.
     *
     * @param SAPSNeighborUrl SAPS neighbor URL.
     * @param submissionParameters Parameters of user submission.
     * @return List of processed tasks from SAPS neighbor.
     */
    private List<ImageTask> getRemotelyProcessedTasksFromInstance(String SAPSNeighborUrl,
                                                                  SubmissionParameters submissionParameters) {
        List<ImageTask> processedTasks = new ArrayList<>();
        try {
            ClientResource clientResource = new ClientResource(SAPSNeighborUrl + PROCESSED_TASKS_URN);
            Representation response = clientResource.post(submissionParameters, MediaType.APPLICATION_JSON);
            processedTasks = extractTasksList(response);
        } catch (Throwable t) {
            LOGGER.error("Error while getting tasks from SAPS Neighbor.", t);
        }
        return processedTasks;
    }

    /**
     * Extract a list of tasks from specified response object.
     *
     * @param response Response containing a list of tasks.
     * @return List of tasks.
     */
    private List<ImageTask> extractTasksList(Representation response) {
        List<ImageTask> tasks = new ArrayList<>();
        try {
            JsonConverter jsonConverter = new JsonConverter();
            JSONObject responseJson = jsonConverter.toObject(response, JSONObject.class, null);
            JSONArray tasksJsonArray = responseJson.getJSONArray("result");
            for (int i = 0; i < tasksJsonArray.length(); i++) {
                tasks.add(new ImageTask(tasksJsonArray.optJSONObject(i)));
            }
        } catch (JSONException | IOException e) {
            LOGGER.error("Error while extracting tasks from response", e);
        }
        return tasks;
    }

    /**
     * Returns a list of URLs of all SAPS neighbors of this instance.
     *
     * @return list of URLs of all SAPS neighbors of this instance.
     */
    private String[] getSAPSNeighborsUrls() {
        String separator = ";";
        String SAPSNeighborsUrls = properties.getProperty(SAPS_NEIGHBORS_URLS);
        return !Objects.isNull(SAPSNeighborsUrls) ? SAPSNeighborsUrls.split(separator)
                : new String[]{};
    }

}