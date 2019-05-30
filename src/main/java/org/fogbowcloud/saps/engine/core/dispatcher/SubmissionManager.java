package org.fogbowcloud.saps.engine.core.dispatcher;

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

}