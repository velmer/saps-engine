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

    private Properties properties;
    private SubmissionDispatcher submissionDispatcher;

    public SubmissionManagerImpl(Properties properties, SubmissionDispatcher submissionDispatcher) {
        this.properties = properties;
        this.submissionDispatcher = submissionDispatcher;
    }

    @Override
    public List<Task> addTasks(SubmissionParameters submissionParameters) {
        List<Date> processedDates = new ArrayList<>();
        try {
            List<ImageTask> processedTasks = getRemotelyProcessedTasks(submissionParameters);
            for (ImageTask processedTask : processedTasks) {
                processedTask.setState(ImageTaskState.REMOTELY_ARCHIVED);
            }
            submissionDispatcher.addImageTasks(processedTasks);
            processedDates = processedTasks.stream()
                    .map(ImageTask::getImageDate)
                    .collect(Collectors.toList());
        } catch (Throwable t) {
            LOGGER.error("", t);
        }
        return submissionDispatcher.addTasks(submissionParameters, processedDates);
    }

    /**
     * Gets a list of processed tasks from a SAPS remote instance.
     *
     * @param submissionParameters Parameters of user submission.
     * @return List of processed tasks.
     */
    private List<ImageTask> getRemotelyProcessedTasks(SubmissionParameters submissionParameters) {
        List<ImageTask> processedTasks = new ArrayList<>();
        String[] SAPSNeighborsUrls = getSAPSNeighborsUrls();
        for (String SAPSNeighborsUrl : SAPSNeighborsUrls) {
            try {
                ClientResource clientResource = new ClientResource(SAPSNeighborsUrl);
                Representation response = clientResource.post(submissionParameters, MediaType.APPLICATION_JSON);
                processedTasks = extractTasksList(response);
            } catch (Throwable t) {
                LOGGER.error("Error while getting tasks from SAPS Neighbor with URL: " +
                        SAPSNeighborsUrl + " .", t);
            }
        }
        return processedTasks;
    }

    /**
     * Extract a list of tasks from specified response object.
     *
     * @param response Response containing a list of tasks.
     * @return List of tasks.
     * @throws IOException
     * @throws JSONException
     */
    private List<ImageTask> extractTasksList(Representation response) throws IOException, JSONException {
        List<ImageTask> tasks = new ArrayList<>();
        JsonConverter jsonConverter = new JsonConverter();
        JSONObject responseJson = jsonConverter.toObject(response, JSONObject.class, null);
        JSONArray tasksJsonArray = responseJson.getJSONArray("result");
        for (int i = 0; i < tasksJsonArray.length(); i++) {
            tasks.add(new ImageTask(tasksJsonArray.optJSONObject(i)));
        }
        return tasks;
    }

    /**
     * Returns the URL of a SAPS remote instance.
     *
     * @return URL of a SAPS remote instance.
     */
    private String[] getSAPSNeighborsUrls() {
        String separador = ";";
        String SAPSNeighborsUrls = properties.getProperty(SAPS_NEIGHBORS_URLS);
        return !Objects.isNull(SAPSNeighborsUrls) ? SAPSNeighborsUrls.split(separador)
                : new String[]{};
    }

}