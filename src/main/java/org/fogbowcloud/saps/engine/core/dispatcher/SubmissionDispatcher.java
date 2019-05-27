package org.fogbowcloud.saps.engine.core.dispatcher;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.fogbowcloud.saps.engine.core.model.ImageTask;
import org.fogbowcloud.saps.engine.core.model.SapsUser;
import org.fogbowcloud.saps.notifier.Ward;

public interface SubmissionDispatcher {

	void listTasksInDB() throws SQLException, ParseException;

	void addUserInDB(String userEmail, String userName, String userPass, boolean userState,
			boolean userNotify, boolean adminRole) throws SQLException;

	void addTaskNotificationIntoDB(String submissionId, String taskId, String userEmail)
			throws SQLException;

	List<Task> addTasks(SubmissionParameters submissionParameters, List<Date> processedDates) throws IOException, ParseException, SQLException;

	void addImageTasks(Collection<ImageTask> imageTasks) throws SQLException;

	void addImageTask(ImageTask imageTask) throws SQLException;

	List<Ward> getUsersToNotify() throws SQLException;

	SapsUser getUser(String userEmail);

	void setTasksToPurge(String day, boolean forceRemoveNonFetched)
			throws SQLException, ParseException;

	void removeUserNotification(String submissionId, String taskId, String userEmail)
			throws SQLException;

	void updateUserState(String userEmail, boolean userState) throws SQLException;

	boolean isUserNotifiable(String userEmail) throws SQLException;

	List<ImageTask> searchProcessedTasks(SubmissionParameters submissionParameters);
}
