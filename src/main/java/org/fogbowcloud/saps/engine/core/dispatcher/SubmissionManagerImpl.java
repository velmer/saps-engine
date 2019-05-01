package org.fogbowcloud.saps.engine.core.dispatcher;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.fogbowcloud.saps.engine.core.model.ImageTask;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.MediaType;
import org.restlet.ext.json.JsonConverter;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

/**
 * Concret implementation of {@link SubmissionManager}.
 */
public class SubmissionManagerImpl implements SubmissionManager {

  // TODO: Replace static string by config txt file
  private static final String REMOTE_INSTANCE_URL = "";

  private SubmissionDispatcher submissionDispatcher;

  public SubmissionManagerImpl(SubmissionDispatcher submissionDispatcher) {
    this.submissionDispatcher = submissionDispatcher;
  }

  @Override
  public List<Task> addTasks(SubmissionParameters submissionParameters) throws IOException, JSONException, ParseException {
    List<ImageTask> processedTasks = getRemotelyProcessedTasks(submissionParameters);
    List<Date> datesToExclude = processedTasks.stream()
      .map(ImageTask::getImageDate)
      .collect(Collectors.toList());
    // TODO: Insert on ServiceCatalog the list {@code processedTasks}
    return submissionDispatcher.fillDB(submissionParameters, datesToExclude);
  }

  /**
   * Gets a list of processed tasks from a SAPS remote instance.
   * 
   * @param submissionParameters Parameters of user submission.
   * @return List of processed tasks.
   * @throws IOException
   * @throws JSONException
   */
  private List<ImageTask> getRemotelyProcessedTasks(SubmissionParameters submissionParameters)
      throws IOException, JSONException {
    String remoteInstanceUrl = getRemoteInstanceUrl();
    ClientResource clientResource = new ClientResource(remoteInstanceUrl);
    Representation response = clientResource.post(submissionParameters, MediaType.APPLICATION_JSON);
    List<ImageTask> processedTasks = extractTasksList(response);
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
  private String getRemoteInstanceUrl() {
    return REMOTE_INSTANCE_URL;
  }
  
}