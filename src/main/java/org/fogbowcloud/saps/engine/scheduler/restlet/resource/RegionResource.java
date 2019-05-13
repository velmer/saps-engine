package org.fogbowcloud.saps.engine.scheduler.restlet.resource;

import java.sql.SQLException;
import java.util.*;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.log4j.Logger;
import org.fogbowcloud.saps.engine.core.dispatcher.SubmissionParameters;
import org.fogbowcloud.saps.engine.core.model.ImageTask;
import org.fogbowcloud.saps.engine.core.model.ImageTaskState;
import org.restlet.data.Form;
import org.restlet.data.Header;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.util.Series;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RegionResource extends BaseResource {

	private static final Logger LOGGER = Logger.getLogger(ImageResource.class);

	private static final String LOWER_LEFT = "lowerLeft";
	private static final String UPPER_RIGHT = "upperRight";
	private static final String PROCESSING_INIT_DATE = "initialDate";
	private static final String PROCESSING_FINAL_DATE = "finalDate";
	private static final String PROCESSING_INPUT_GATHERING_TAG = "inputGatheringTag";
	private static final String PROCESSING_INPUT_PREPROCESSING_TAG = "inputPreprocessingTag";
	private static final String PROCESSING_ALGORITHM_EXECUTION_TAG = "algorithmExecutionTag";

	public RegionResource() {
		super();
	}

	@SuppressWarnings("unchecked")
	@Get
	public Representation getNumberImagesProcessedByRegion() throws SQLException {
		
		Series<Header> series = (Series<Header>) getRequestAttributes()
				.get("org.restlet.http.headers");

		String userEmail = series.getFirstValue(UserResource.REQUEST_ATTR_USER_EMAIL, true);
		String userPass = series.getFirstValue(UserResource.REQUEST_ATTR_USERPASS, true);

		if (!authenticateUser(userEmail, userPass)) {
			throw new ResourceException(HttpStatus.SC_UNAUTHORIZED);
		}
		
		List<ImageTask> imageTasks = this.application.getTasksInState(ImageTaskState.ARCHIVED);
		imageTasks.addAll(this.application.getTasksInState(ImageTaskState.REMOTELY_ARCHIVED));

		Map<String, Integer> regionsFrequency = new HashMap<>();
		for (ImageTask imageTask : imageTasks) {
			String region = imageTask.getRegion();
			if (!regionsFrequency.containsKey(region)) {
				regionsFrequency.put(region, 0);
			}
			regionsFrequency.put(region, regionsFrequency.get(region) + 1);
		}

		JSONArray result = new JSONArray();
		try {
			for (String region : regionsFrequency.keySet()) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("region", region);
				jsonObject.put("count", regionsFrequency.get(region));
				result.put(jsonObject);
			}
		} catch (JSONException e) {
			LOGGER.error("Error while trying creating JSONObject");
		}

		return new StringRepresentation(result.toString(),
				MediaType.APPLICATION_JSON);
	}

	@Post
	public Representation getProcessedImagesInInterval(Representation representation) {
		Form form = new Form(representation);

		String userEmail = form.getFirstValue(UserResource.REQUEST_ATTR_USER_EMAIL, true);
		String userPass = form.getFirstValue(UserResource.REQUEST_ATTR_USERPASS, true);
		if (!authenticateUser(userEmail, userPass) || userEmail.equals("anonymous")) {
			throw new ResourceException(HttpStatus.SC_UNAUTHORIZED);
		}

		SubmissionParameters submissionParameters = extractSubmissionParameters(form);

		String log = "Recovering processed images with settings:\n" +
				"\tLower Left: " + submissionParameters.getLowerLeftLatitude() + ", " + submissionParameters.getLowerLeftLongitude() + "\n" +
				"\tUpper Right: " + submissionParameters.getUpperRightLatitude() + ", " + submissionParameters.getUpperRightLongitude() + "\n" +
				"\tInterval: " + submissionParameters.getInitDate() + " - " + submissionParameters.getEndDate() + "\n" +
				"\tGathering: " + submissionParameters.getInputGathering() + "\n" +
				"\tPreprocessing: " + submissionParameters.getInputPreprocessing() + "\n" +
				"\tAlgorithm: " + submissionParameters.getAlgorithmExecution() + "\n";
		LOGGER.info(log);

		List<ImageTask> tasks = application.searchProcessedTasks(submissionParameters);
		JSONObject resObj = buildJsonResponseFromTaskList(tasks);
		return new StringRepresentation(resObj.toString(), MediaType.APPLICATION_JSON);
	}
}
