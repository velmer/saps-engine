package org.fogbowcloud.saps.engine.core.dispatcher;

import java.util.Date;
import java.util.List;

/**
 * Manages submissions by realizing communication between SAPS instances.
 */
public interface SubmissionManager {

  /**
   * Adds tasks with specified parameters. Checks for existence of already
   * processed tasks in others SAPS instances for reuse.
   * 
   * @param lowerLeftLatitude   Lower left latitude coordinate. 
   * @param lowerLeftLongitude  Lower left longitude coordinate.
   * @param upperRightLatitude  Upper right latitude coordinate.
   * @param upperRightLongitude Upper right longitude coordinate.
   * @param initDate            Interval init date.
   * @param endDate             Interval end date.
   * @param inputGathering      Input gathering source.
   * @param inputPreprocessing  Input preprocessing source.
   * @param algorithmExecution  Algorithm Execution source.
   * @return List of added tasks.
   */
  List<Task> addTasks(String lowerLeftLatitude, String lowerLeftLongitude,
      String upperRightLatitude, String upperRightLongitude, Date initDate,
      Date endDate, String inputGathering, String inputPreprocessing,
      String algorithmExecution);
}