package org.fogbowcloud.saps.engine.scheduler.restlet.resource;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;
import org.fogbowcloud.saps.engine.core.dispatcher.SubmissionParameters;
import org.fogbowcloud.saps.engine.core.model.ImageTask;
import org.fogbowcloud.saps.engine.core.model.SapsUser;
import org.fogbowcloud.saps.engine.scheduler.restlet.DatabaseApplication;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class BaseResource extends ServerResource {

	private static final Logger LOGGER = Logger.getLogger(BaseResource.class);

	private static final String LOWER_LEFT = "lowerLeft";
	private static final String UPPER_RIGHT = "upperRight";
	private static final String PROCESSING_INIT_DATE = "initialDate";
	private static final String PROCESSING_FINAL_DATE = "finalDate";
	private static final String PROCESSING_INPUT_GATHERING_TAG = "inputGatheringTag";
	private static final String PROCESSING_INPUT_PREPROCESSING_TAG = "inputPreprocessingTag";
	private static final String PROCESSING_ALGORITHM_EXECUTION_TAG = "algorithmExecutionTag";
	private static final int LATITUDE_INDEX = 0;
	private static final int LONGITUDE_INDEX = 1;

	protected DatabaseApplication application;

	public BaseResource() {
		application = (DatabaseApplication) getApplication();
	}

	protected boolean authenticateUser(String userEmail, String userPass) {
		return authenticateUser(userEmail, userPass, false);
	}

	protected boolean authenticateUser(String userEmail, String userPass, boolean mustBeAdmin) {
		if (userEmail == null || userEmail.isEmpty() || userPass == null || userPass.isEmpty()) {
			LOGGER.error("User email or user password was null.");
			return false;
		}

		SapsUser user = application.getUser(userEmail);
		String md5Pass = DigestUtils.md5Hex(userPass);
		if (user != null && user.getUserPassword().equals(md5Pass) && user.getActive()) {
			if (mustBeAdmin && !user.getAdminRole()) {
				// the user must be an admin and the logged user is not
				LOGGER.error("Admin level account needed for this action.");
				return false;
			}
			return true;
		}
		LOGGER.error("No user with this email or password mismatch.");
		return false;
	}

	/**
	 * Extracts a {@link SubmissionParameters} from specified form.
	 *
	 * @param form Form that holds the parameters to be extracted.
	 * @return Extracted submission parameters.
	 */
	SubmissionParameters extractSubmissionParameters(Form form) {
		String lowerLeftLatitude;
		String lowerLeftLongitude;
		String upperRightLatitude;
		String upperRightLongitude;
		try {
			lowerLeftLatitude = extractCoordinate(form, LOWER_LEFT, LATITUDE_INDEX);
			lowerLeftLongitude = extractCoordinate(form, LOWER_LEFT, LONGITUDE_INDEX);
			upperRightLatitude = extractCoordinate(form, UPPER_RIGHT, LATITUDE_INDEX);
			upperRightLongitude = extractCoordinate(form, UPPER_RIGHT, LONGITUDE_INDEX);
		} catch (Throwable t) {
			LOGGER.error("Failed to parse coordinates.", t);
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "All coordinates must be informed.");
		}

		Date initDate;
		Date endDate;
		try {
			initDate = extractDate(form, PROCESSING_INIT_DATE);
			endDate = extractDate(form, PROCESSING_FINAL_DATE);
		} catch (Throwable t) {
			LOGGER.error("Failed to parse dates.", t);
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "All dates must be informed.");
		}

		String inputGathering = form.getFirstValue(PROCESSING_INPUT_GATHERING_TAG);
		if (inputGathering.isEmpty()) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Input Gathering must be informed.");
		}
		String inputPreprocessing = form.getFirstValue(PROCESSING_INPUT_PREPROCESSING_TAG);
		if (inputPreprocessing.isEmpty()) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Input Preprocessing must be informed.");
		}
		String algorithmExecution = form.getFirstValue(PROCESSING_ALGORITHM_EXECUTION_TAG);
		if (algorithmExecution.isEmpty()) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Algorithm Execution must be informed.");
		}

		return new SubmissionParameters(
				lowerLeftLatitude,
				lowerLeftLongitude,
				upperRightLatitude,
				upperRightLongitude,
				initDate,
				endDate,
				inputPreprocessing,
				inputGathering,
				algorithmExecution
		);
	}

	String extractCoordinate(Form form, String name, int index) {
		String data[] = form.getValuesArray(name + "[]");
		return data[index];
	}

	Date extractDate(Form form, String name) throws ParseException {
		String data = form.getFirstValue(name);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		return dateFormat.parse(data);
	}

	/**
	 * Builds a response as JSONObject from specified list of tasks. The built
	 * object will have only the property "result", which carries a JSONArray
	 * of tasks that were parsed to JSONObjects.
	 *
	 * @param tasks Tasks to be returned inside "result" property of built
	 *              JSONObject.
	 * @return Response as JSONObject.
	 */
	JSONObject buildJsonResponseFromTaskList(List<ImageTask> tasks) {
		JSONArray tasksJsonArray = new JSONArray();
		for (ImageTask task: tasks) {
			try {
				tasksJsonArray.put(task.toJSON());
			} catch (JSONException e) {
				LOGGER.error("Failed to build JSON object of Image Task", e);
			}
		}

		JSONObject resObj = new JSONObject();
		try {
			resObj.put("result", tasksJsonArray);
		} catch (JSONException e) {
			LOGGER.error("Failed to build response JSON object", e);
		}
		return resObj;
	}
}
