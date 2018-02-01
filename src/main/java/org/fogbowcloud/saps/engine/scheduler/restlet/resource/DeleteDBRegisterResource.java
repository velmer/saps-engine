package org.fogbowcloud.saps.engine.scheduler.restlet.resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.log4j.Logger;
import org.restlet.data.Header;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.util.Series;

public class DeleteDBRegisterResource extends BaseResource {

	private static final Logger LOGGER = Logger.getLogger(DeleteDBRegisterResource.class);

	private static final String REQUEST_OK = "All methods with exit status 0";

	public DeleteDBRegisterResource() {
		super();
	}

	@SuppressWarnings("unchecked")
	@Get
	public Representation deleteDB() throws IOException, InterruptedException {

		Series<Header> series = (Series<Header>) getRequestAttributes()
				.get("org.restlet.http.headers");

		String userEmail = series.getFirstValue(UserResource.REQUEST_ATTR_USER_EMAIL, true);
		String userPass = series.getFirstValue(UserResource.REQUEST_ATTR_USERPASS, true);

		if (!authenticateUser(userEmail, userPass)) {
			throw new ResourceException(HttpStatus.SC_UNAUTHORIZED);
		}

		String flow = series.getFirstValue("requestsPerSecond", true);
		String round = series.getFirstValue("round", true);

		boolean allCommandsOk = true;

		Process pb = new ProcessBuilder("/bin/bash", "-c",
				"cp /local/volume/saps-engine/log/dispatcher-lsd.log /local/volume/dispatcher-experiment/dispatcher-lsd-"
						+ flow + "-" + round + ".log").start();

		pb.waitFor();

		LOGGER.info("CP Command :: Exit value: " + pb.exitValue());

		if (pb.exitValue() != 0) {
			allCommandsOk = false;
		}

		pb = new ProcessBuilder("/bin/bash", "-c",
				"echo \"\" > /local/volume/saps-engine/log/dispatcher-lsd.log").start();

		pb.waitFor();

		LOGGER.info("ECHO Command :: Exit value: " + pb.exitValue());

		if (pb.exitValue() != 0) {
			allCommandsOk = false;
		}

		List<String> commandList = new ArrayList<String>();
		commandList.add("DELETE FROM nasa_images;");
		commandList.add("DELETE FROM provenance_data;");
		commandList.add("DELETE FROM states_timestamps;");

		for (String command : commandList) {
			pb = new ProcessBuilder("/bin/bash", "-c", "sudo su sebal -c \"psql -c '" + command + "'\"")
					.start();
			pb.waitFor();
			LOGGER.info("Command: " + command + " :: Exit value: " + pb.exitValue());
			if (pb.exitValue() != 0) {
				allCommandsOk = false;
			}
		}

		if (!allCommandsOk) {
			throw new ResourceException(HttpStatus.SC_METHOD_FAILURE);
		}
		return new StringRepresentation(DeleteDBRegisterResource.REQUEST_OK, MediaType.TEXT_PLAIN);
	}
}
