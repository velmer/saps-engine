package org.fogbowcloud.saps.engine.core.dispatcher;

import java.util.Date;
import java.util.List;

/**
 * Concret implementation of {@link SubmissionManager}.
 */
public class SubmissionManagerImpl implements SubmissionManager {

  // TODO: Replace static string by config txt file
  private static final String REMOTE_INSTANCE_URL = "";

  @Override
  public List<Task> addTasks(SubmissionParameters submissionParameters) {
    String remoteInstanceUrl = getRemoteInstanceUrl();
    return null;
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