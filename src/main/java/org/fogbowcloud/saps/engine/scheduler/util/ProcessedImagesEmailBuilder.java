package org.fogbowcloud.saps.engine.scheduler.util;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.fogbowcloud.saps.engine.core.model.ImageTask;
import org.fogbowcloud.saps.engine.core.pojo.ImageTaskFileList;
import org.fogbowcloud.saps.engine.core.service.ProcessedImagesService;
import org.fogbowcloud.saps.engine.scheduler.restlet.DatabaseApplication;
import org.fogbowcloud.saps.notifier.GoogleMail;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.mail.MessagingException;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

public class ProcessedImagesEmailBuilder implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(ProcessedImagesEmailBuilder.class);

    private DatabaseApplication application;
    private Properties properties;
    private String userEmail;
    private List<String> images;

    public ProcessedImagesEmailBuilder(
            DatabaseApplication databaseApplication,
            Properties properties,
            String userEmail,
            List<String> images) {
        this.application = databaseApplication;
        this.properties = properties;
        this.userEmail = userEmail;
        this.images = images;
    }

    @Override
    public void run() {
        StringBuilder builder = new StringBuilder();
        builder.append("Creating email for user ");
        builder.append(userEmail);
        builder.append(" with images:\n");
        for (String str: images) {
            builder.append(str).append("\n");
        }
        LOGGER.info(builder.toString());
        StringBuilder errorBuilder = new StringBuilder();
        JSONArray tasklist = generateAllTasksJsons(errorBuilder);
        sendTaskEmail(errorBuilder, tasklist);
        sendErrorEmail(errorBuilder);
    }

    JSONArray generateAllTasksJsons(StringBuilder errorBuilder) {
        JSONArray tasklist = new JSONArray();
        for (String str: images) {
            try {
                tasklist.put(generateTaskEmailJson(properties, str));
            } catch (SQLException e) {
                LOGGER.error("Failed to fetch image from database.", e);
                errorBuilder.append("Failed to fetch image from database.").append("\n")
                        .append(ExceptionUtils.getStackTrace(e)).append("\n");
            } catch (JSONException e) {
                LOGGER.error("Failed to create task json.", e);
                errorBuilder.append("Failed to create task json.").append("\n")
                        .append(ExceptionUtils.getStackTrace(e)).append("\n");
            }
        }
        return tasklist;
    }

    private void sendTaskEmail(StringBuilder errorBuilder, JSONArray tasklist) {
        try {
            GoogleMail.Send(
                    properties.getProperty(SapsPropertiesConstants.NO_REPLY_EMAIL),
                    properties.getProperty(SapsPropertiesConstants.NO_REPLY_PASS),
                    userEmail,
                    "[SAPS] Filter results",
                    tasklist.toString(2)
            );
        } catch (MessagingException | JSONException e) {
            LOGGER.error("Failed to send email with images download links.", e);
            errorBuilder
                    .append("Failed to send email with images download links.").append("\n")
                    .append(ExceptionUtils.getStackTrace(e)).append("\n");
        }
    }

    private void sendErrorEmail(StringBuilder errorBuilder) {
        if (!errorBuilder.toString().isEmpty()) {
            try {
                GoogleMail.Send(
                        properties.getProperty(SapsPropertiesConstants.NO_REPLY_EMAIL),
                        properties.getProperty(SapsPropertiesConstants.NO_REPLY_PASS),
                        "sebal.no.reply@gmail.com",
                        "[SAPS] Errors during image temporary link creation",
                        errorBuilder.toString()
                );
            } catch (MessagingException e) {
                LOGGER.error("Failed to send email with errors to admins.", e);
            }
        }
    }

    JSONObject generateTaskEmailJson(Properties properties, String imageTaskId)
            throws SQLException, JSONException {
        ImageTask task = application.getTask(imageTaskId);
        ImageTaskFileList imageTaskFileList = ProcessedImagesService
                .generateImageTaskFiles(properties, task);
        return imageTaskFileList.toJSON();
    }

}
