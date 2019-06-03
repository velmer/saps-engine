package org.fogbowcloud.saps.engine.core.dispatcher;

import org.fogbowcloud.saps.engine.core.model.ImageTask;

import java.util.List;

/**
 * Manages submissions by realizing communication between SAPS instances.
 */
public interface SubmissionManager {

    /**
     * Adds tasks with specified parameters. Checks for existence of already
     * processed tasks in others SAPS instances for reuse.
     *
     * @param submissionParameters Parameters of user submission.
     * @return List of added tasks.
     */
    List<Task> addTasks(SubmissionParameters submissionParameters);

    /**
     * Gets list of processed tasks from all SAPS neighbors from this instance.
     *
     * @param submissionParameters Parameters of user submission.
     * @return List of processed tasks from all SAPS neighbors.
     */
    List<ImageTask> getAllRemotelyProcessedTasks(SubmissionParameters submissionParameters);

    /**
     * Gets list of processed tasks from SAPS neighbor that had its URL specified.
     *
     * @param SAPSNeighborUrl SAPS neighbor URL.
     * @param submissionParameters Parameters of user submission.
     * @return List of processed tasks from SAPS neighbor.
     */
    List<ImageTask> getRemotelyProcessedTasksFromInstance(
            String SAPSNeighborUrl,
            SubmissionParameters submissionParameters);

}