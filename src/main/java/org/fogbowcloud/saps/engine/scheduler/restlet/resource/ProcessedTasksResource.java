package org.fogbowcloud.saps.engine.scheduler.restlet.resource;

import java.util.List;

import org.apache.log4j.Logger;
import org.fogbowcloud.saps.engine.core.dispatcher.SubmissionParameters;
import org.fogbowcloud.saps.engine.core.model.ImageTask;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Post;

/**
 * Responsable for retrieve information about already processed tasks.
 */
public class ProcessedTasksResource extends BaseResource {

	private static final Logger LOGGER = Logger.getLogger(ProcessedTasksResource.class);

	public ProcessedTasksResource() {
		super();
	}

	/**
	 * Returns all processed tasks that matches the execution parameters passed
	 * in {@code representation}.
	 *
	 * @param representation Entity containing execution parameters.
	 * @return List of processed tasks.
	 */
	@Post
	public Representation getProcessedTasksInInterval(Representation representation) {
		Form form = new Form(representation);
		SubmissionParameters submissionParameters = extractSubmissionParameters(form);

		String log = "Recovering processed tasks with settings:\n" +
				"\tLower Left: " + submissionParameters.getLowerLeftLatitude() + ", " + submissionParameters.getLowerLeftLongitude() + "\n" +
				"\tUpper Right: " + submissionParameters.getUpperRightLatitude() + ", " + submissionParameters.getUpperRightLongitude() + "\n" +
				"\tInterval: " + submissionParameters.getInitDate() + " - " + submissionParameters.getEndDate() + "\n" +
				"\tGathering: " + submissionParameters.getInputGathering() + "\n" +
				"\tPreprocessing: " + submissionParameters.getInputPreprocessing() + "\n" +
				"\tAlgorithm: " + submissionParameters.getAlgorithmExecution() + "\n";
		LOGGER.info(log);

		List<ImageTask> tasks = application.searchProcessedTasks(submissionParameters);

		JSONArray arr = new JSONArray();
		for (ImageTask task: tasks) {
			try {
				arr.put(task.toJSON());
			} catch (JSONException e) {
				LOGGER.error("Failed to build JSON object of Image Task", e);
			}
		}

		JSONObject resObj = new JSONObject();
		try {
			resObj.put("result", arr);
		} catch (JSONException e) {
			LOGGER.error("Failed to build response JSON object", e);
		}

		return new StringRepresentation(resObj.toString(), MediaType.APPLICATION_JSON);
	}

}