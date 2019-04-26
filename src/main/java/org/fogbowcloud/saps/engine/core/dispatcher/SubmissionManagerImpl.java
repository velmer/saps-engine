package org.fogbowcloud.saps.engine.core.dispatcher;

import java.util.Date;
import java.util.List;

/**
 * Concret implementation of {@link SubmissionManager}.
 */
public class SubmissionManagerImpl implements SubmissionManager {

  @Override
  public List<Task> addTasks(String lowerLeftLatitude, String lowerLeftLongitude,
      String upperRightLatitude, String upperRightLongitude,
      Date initDate, Date endDate, String inputGathering,
      String inputPreprocessing, String algorithmExecution) {
    return null;
  }
  
}