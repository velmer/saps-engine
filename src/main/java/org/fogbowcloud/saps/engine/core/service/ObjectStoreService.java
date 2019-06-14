//package org.fogbowcloud.saps.engine.core.service;
//
//import org.apache.http.HttpResponse;
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.methods.HttpGet;
//import org.apache.http.client.utils.URIBuilder;
//import org.apache.http.impl.client.HttpClients;
//import org.apache.http.util.EntityUtils;
//import org.apache.log4j.Logger;
//import org.fogbowcloud.saps.engine.core.model.ImageTask;
//import org.fogbowcloud.saps.engine.scheduler.util.SapsPropertiesConstants;
//
//import java.io.IOException;
//import java.net.URI;
//import java.net.URISyntaxException;
//import java.util.*;
//
///**
// * Service that provides operations for communication with the Object Store.
// */
//public class ObjectStoreService {
//
//    private static final Logger LOGGER = Logger.getLogger(ObjectStoreService.class);
//
//    private static final String HTTPS_SCHEME = "https";
//    private static final String PROJECT_ID = "projectId";
//    private static final String USER_ID = "userId";
//    private static final String PASSWORD = "password";
//    private static final String AUTH_URL = "authUrl";
//    private static final String PATH_PARAM = "path";
//    private static final String X_AUTH_TOKEN = "X-Auth-Token";
//    private static final String HTTP_RESPONSE_SEPARATOR = "\n";
//    private static final String ARCHIVER_PATH = "archiver/";
//    private static final String DATA_OUTPUT_PATH = "/data/output/";
//
//    /**
//     * Returns all the paths for every file generated by the {@link ImageTask}
//     * that had its ID specified.
//     *
//     * @param properties  Properties that contains Object Store information.
//     * @param imageTaskId ImageTask's ID.
//     * @return List of paths.
//     */
//    public static List<String> getImageTaskFilesPaths(Properties properties, String imageTaskId) {
//        try {
//            HttpClient client = HttpClients.createDefault();
//            HttpGet httpget = prepareObjectStoreRequest(properties, imageTaskId);
//            HttpResponse response = client.execute(httpget);
//            return parseHttpResponse(response);
//        } catch (IOException | URISyntaxException e) {
//            LOGGER.error("", e);
//            return Collections.emptyList();
//        }
//
//    }
//
//    /**
//     * Parses the given {@link HttpResponse} to a list of Strings.
//     *
//     * @param response Response to be parsed.
//     * @return List of Strings parsed from response.
//     * @throws IOException
//     */
//    private static List<String> parseHttpResponse(HttpResponse response) throws IOException {
//        return Arrays.asList(EntityUtils.toString(response.getEntity()).split(HTTP_RESPONSE_SEPARATOR));
//    }
//
//    /**
//     * Prepares a {@link HttpGet} request for the Object Store.
//     *
//     * @param properties  Properties that contains Object Store information.
//     * @param imageTaskId ImageTask's ID.
//     * @return A {@link HttpGet} request.
//     * @throws URISyntaxException
//     */
//    private static HttpGet prepareObjectStoreRequest(Properties properties,
//                                                     String imageTaskId) throws URISyntaxException {
//        String objectStoreHost = properties.getProperty(SapsPropertiesConstants.SWIFT_OBJECT_STORE_HOST);
//        String objectStorePath = properties.getProperty(SapsPropertiesConstants.SWIFT_OBJECT_STORE_PATH);
//        String objectStoreContainer = properties.getProperty(SapsPropertiesConstants.SWIFT_OBJECT_STORE_CONTAINER);
//        String pathParamValue = ARCHIVER_PATH + imageTaskId + DATA_OUTPUT_PATH;
//        URI uri = new URIBuilder()
//                .setScheme(HTTPS_SCHEME)
//                .setHost(objectStoreHost)
//                .setPath(objectStorePath + "/" + objectStoreContainer)
//                .addParameter(PATH_PARAM, pathParamValue)
//                .build();
//        LOGGER.debug("Getting list of files for task " + imageTaskId + " from " + uri);
//        HttpGet httpget = new HttpGet(uri);
//        Token token = getKeystoneToken(properties);
//        httpget.addHeader(X_AUTH_TOKEN, token.getAccessId());
//        return httpget;
//    }
//
//    /**
//     * Returns a Keystone Token for the given properties.
//     *
//     * @param properties Properties that contains Object Store information.
//     * @return Keystone Token.
//     */
//    private static Token getKeystoneToken(Properties properties) {
//        Map<String, String> credentials = new HashMap<>();
//        credentials.put(PROJECT_ID, properties.getProperty(SapsPropertiesConstants.SWIFT_PROJECT_ID));
//        credentials.put(USER_ID, properties.getProperty(SapsPropertiesConstants.SWIFT_USER_ID));
//        credentials.put(PASSWORD, properties.getProperty(SapsPropertiesConstants.SWIFT_PASSWORD));
//        credentials.put(AUTH_URL, properties.getProperty(SapsPropertiesConstants.SWIFT_AUTH_URL));
//        KeystoneV3IdentityPlugin keystone = new KeystoneV3IdentityPlugin(properties);
//        return keystone.createToken(credentials);
//    }
//}
