package org.fogbowcloud.saps.engine.core.dispatcher;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;

import org.json.JSONException;

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
     * @throws IOException
     * @throws JSONException
     * @throws ParseException
     * @throws SQLException
     */
    List<Task> addTasks(SubmissionParameters submissionParameters) throws IOException, JSONException, ParseException, SQLException;

}