package org.fogbowcloud.saps.engine.core.dispatcher;

import org.fogbowcloud.saps.engine.core.model.ImageTask;
import org.fogbowcloud.saps.engine.core.model.SapsUser;
import org.fogbowcloud.saps.notifier.Ward;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface SubmissionDispatcher {

	void listTasksInDB() throws SQLException, ParseException;

	void addUserInDB(String userEmail, String userName, String userPass, boolean userState,
			boolean userNotify, boolean adminRole) throws SQLException;

	void addTaskNotificationIntoDB(String submissionId, String taskId, String userEmail)
			throws SQLException;

	List<Task> addTasks(SubmissionParameters submissionParameters,
						Map<Date, List<ImageTask>> processedImageTasksGroupedByDate);

	List<Task> addImageTasks(Collection<ImageTask> imageTasks);

	Task addImageTask(ImageTask imageTask) throws SQLException;

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
