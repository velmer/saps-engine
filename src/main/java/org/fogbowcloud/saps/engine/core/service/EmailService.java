package org.fogbowcloud.saps.engine.core.service;

import org.fogbowcloud.saps.engine.scheduler.util.SapsPropertiesConstants;
//import org.fogbowcloud.saps.notifier.GoogleMail;

import java.util.Properties;

/**
 * Service to handle Email operations.
 */
public class EmailService {

    /**
     * Asynchronously sends an email with specified parameters and default
     * no-reply email and password.
     *
     * @param recipientEmail Recipient email.
     * @param title          Email title.
     * @param message        Email message.
     */
    public static void sendEmail(
            Properties properties,
            String recipientEmail,
            String title,
            String message) {
        sendEmail(
                properties.getProperty(SapsPropertiesConstants.NO_REPLY_EMAIL),
                properties.getProperty(SapsPropertiesConstants.NO_REPLY_PASS),
                recipientEmail,
                title,
                message
        );
    }

    /**
     * Asynchronously sends an email with specified parameters.
     *
     * @param username       Username of sender account.
     * @param password       Password of sender account.
     * @param recipientEmail Recipient email.
     * @param title          Email title.
     * @param message        Email message.
     */
    public static void sendEmail(
            String username,
            String password,
            String recipientEmail,
            String title,
            String message) {
//        Thread emailThread = new Thread(() -> GoogleMail.Send(
//                username,
//                password,
//                recipientEmail,
//                title,
//                message
//        ));
//        emailThread.start();
    }
}
