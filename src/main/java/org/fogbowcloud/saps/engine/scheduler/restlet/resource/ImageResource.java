package org.fogbowcloud.saps.engine.scheduler.restlet.resource;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.log4j.Logger;
import org.fogbowcloud.saps.engine.core.dispatcher.Submission;
import org.fogbowcloud.saps.engine.core.dispatcher.SubmissionParameters;
import org.fogbowcloud.saps.engine.core.dispatcher.Task;
import org.fogbowcloud.saps.engine.core.model.ImageTask;
import org.fogbowcloud.saps.engine.scheduler.restlet.DatabaseApplication;
import org.json.JSONArray;
import org.json.JSONException;
import org.restlet.data.Form;
import org.restlet.data.Header;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.util.Series;

public class ImageResource extends BaseResource {

	private static final Logger LOGGER = Logger.getLogger(ImageResource.class);

	private static final String LOWER_LEFT = "lowerLeft";
	private static final String UPPER_RIGHT = "upperRight";
	private static final String PROCESSING_INIT_DATE = "initialDate";
	private static final String PROCESSING_FINAL_DATE = "finalDate";
	private static final String PROCESSING_INPUT_GATHERING_TAG = "inputGatheringTag";
	private static final String PROCESSING_INPUT_PREPROCESSING_TAG = "inputPreprocessingTag";
	private static final String PROCESSING_ALGORITHM_EXECUTION_TAG = "algorithmExecutionTag";

	private static final String ADD_IMAGES_MESSAGE_OK = "Tasks successfully added";
	private static final String PURGE_MESSAGE_OK = "Tasks purged from database";
	private static final String DAY = "day";
	private static final String FORCE = "force";


	public ImageResource() {
		super();
	}

	@SuppressWarnings("unchecked")
	@Get
	public Representation getTasks() throws Exception {
		Series<Header> series = (Series<Header>) getRequestAttributes()
				.get("org.restlet.http.headers");

		String userEmail = series.getFirstValue(UserResource.REQUEST_ATTR_USER_EMAIL, true);
		String userPass = series.getFirstValue(UserResource.REQUEST_ATTR_USERPASS, true);

		if (!authenticateUser(userEmail, userPass)) {
			throw new ResourceException(HttpStatus.SC_UNAUTHORIZED);
		}

		String taskId = (String) getRequest().getAttributes().get("taskId");


		JSONArray tasksJSON;
		if (taskId != null) {
			LOGGER.info("Getting task");
			LOGGER.debug("TaskID is " + taskId);
			ImageTask imageTask = ((DatabaseApplication) getApplication()).getTask(taskId);
			tasksJSON = new JSONArray();
			try {
				tasksJSON.put(imageTask.toJSON());
			} catch (JSONException e) {
				LOGGER.error("Error while creating JSON from image task " + imageTask, e);
			}
		} else {
			LOGGER.info("Getting all tasks");

			List<ImageTask> listOfTasks = ((DatabaseApplication) getApplication()).getTasks();
			tasksJSON = new JSONArray();

			for (ImageTask imageTask : listOfTasks) {
				try {
					tasksJSON.put(imageTask.toJSON());
				} catch (JSONException e) {
					LOGGER.error("Error while creating JSON from image task " + imageTask, e);
				}
			}
		}

		return new StringRepresentation(tasksJSON.toString(), MediaType.APPLICATION_JSON);
	}

	@Post
	public StringRepresentation insertTasks(Representation entity) {
		Form form = new Form(entity);

		String userEmail = form.getFirstValue(UserResource.REQUEST_ATTR_USER_EMAIL, true);
		String userPass = form.getFirstValue(UserResource.REQUEST_ATTR_USERPASS, true);
		LOGGER.debug("POST with userEmail " + userEmail);
		if (!authenticateUser(userEmail, userPass) || userEmail.equals("anonymous")) {
			throw new ResourceException(HttpStatus.SC_UNAUTHORIZED);
		}

		SubmissionParameters submissionParameters = extractSubmissionParameters(form);

		String log = "Creating new image process with configuration:\n" +
				"\tLower Left: " + submissionParameters.getLowerLeftLatitude() + ", " + submissionParameters.getLowerLeftLongitude() + "\n" +
				"\tUpper Right: " + submissionParameters.getUpperRightLatitude() + ", " + submissionParameters.getUpperRightLongitude() + "\n" +
				"\tInterval: " + submissionParameters.getInitDate() + " - " + submissionParameters.getEndDate() + "\n" +
				"\tGathering: " + submissionParameters.getInputGathering() + "\n" +
				"\tPreprocessing: " + submissionParameters.getInputPreprocessing() + "\n" +
				"\tAlgorithm: " + submissionParameters.getAlgorithmExecution() + "\n";
		LOGGER.info(log);

		try {
			List<Task> createdTasks = application.addTasks(submissionParameters);
			if (application.isUserNotifiable(userEmail)) {
				Submission submission = new Submission(UUID.randomUUID().toString());
				for (Task imageTask : createdTasks) {
					application.addUserNotify(submission.getId(), imageTask.getId(), userEmail);
				}
			}
		} catch (Exception e) {
			LOGGER.debug(e.getMessage(), e);
			throw new ResourceException(HttpStatus.SC_BAD_REQUEST, e);
		}

		return new StringRepresentation(ADD_IMAGES_MESSAGE_OK, MediaType.TEXT_PLAIN);
	}

	@Delete
	public StringRepresentation purgeTask(Representation entity) throws Exception {
		Form form = new Form(entity);

		String userEmail = form.getFirstValue(UserResource.REQUEST_ATTR_USER_EMAIL, true);
		String userPass = form.getFirstValue(UserResource.REQUEST_ATTR_USERPASS, true);

		LOGGER.debug("DELETE with userEmail " + userEmail);

		boolean mustBeAdmin = true;
		if (!authenticateUser(userEmail, userPass, mustBeAdmin)) {
			throw new ResourceException(HttpStatus.SC_UNAUTHORIZED);
		}

		String day = form.getFirstValue(DAY);
		String force = form.getFirstValue(FORCE);

		LOGGER.debug("Purging tasks from day " + day);
		DatabaseApplication application = (DatabaseApplication) getApplication();

		try {
			application.purgeImage(day, force);
		} catch (Exception e) {
			LOGGER.debug(e.getMessage(), e);
			throw new ResourceException(HttpStatus.SC_BAD_REQUEST, e);
		}

		return new StringRepresentation(PURGE_MESSAGE_OK, MediaType.APPLICATION_JSON);
	}
}
