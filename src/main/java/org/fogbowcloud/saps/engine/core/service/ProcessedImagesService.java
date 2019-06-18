package org.fogbowcloud.saps.engine.core.service;

import org.apache.log4j.Logger;
import org.fogbowcloud.saps.engine.core.model.ImageTask;
import org.fogbowcloud.saps.engine.core.pojo.ImageTaskFile;
import org.fogbowcloud.saps.engine.core.pojo.ImageTaskFileList;
import org.fogbowcloud.saps.engine.scheduler.util.SapsPropertiesConstants;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.*;

/**
 * Service the provides operations with processed {@link ImageTask}.
 */
public class ProcessedImagesService {

    private static final Logger LOGGER = Logger.getLogger(ProcessedImagesService.class);

    private static final String TEMP_DIR_URL = "%s?temp_url_sig=%s&temp_url_expires=%s";
    private static final String UNAVAILABLE = "UNAVAILABLE";
    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
    private static final String HTTPS_SCHEME = "https://";
    private static final String FILENAME_QUERY_PARAM = "&filename=";
    private static final String GET = "GET";
    private static final String OBJECT_STORE_ENCRIPTION_FORMAT = "%s\n%s\n%s";

    /**
     * Generates a {@link ImageTaskFileList} object containing every file generated
     * by the processing of {@link ImageTask} that was specified.
     *
     * @param properties Properties that contains Object Store information.
     * @param imageTask  ImageTask that will have its files returned.
     * @return A {@link ImageTaskFileList}.
     */
    public static ImageTaskFileList generateImageTaskFiles(Properties properties, ImageTask imageTask) {
        List<String> filesPaths = ObjectStoreService.getImageTaskFilesPaths(properties, imageTask.getTaskId());
        List<ImageTaskFile> imageTaskFiles = new ArrayList<>();
        for (String filePath: filesPaths) {
            File file = new File(filePath);
            ImageTaskFile imageTaskFile = new ImageTaskFile(filePath, file.getName());
            String imageTaskFileURL;
            try {
                imageTaskFileURL = generateTempURL(properties, imageTaskFile);
            } catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException e) {
                LOGGER.error("Failed to generate download link for file " + filePath, e);
                imageTaskFileURL = UNAVAILABLE;
            }
            imageTaskFile.setURL(imageTaskFileURL);
            imageTaskFiles.add(imageTaskFile);
        }
        return new ImageTaskFileList(imageTask, imageTaskFiles);
    }

    /**
     * Generates a temporary access URL for given {@link ImageTaskFile}.
     *
     * @param properties    Properties that contains Object Store information.
     * @param imageTaskFile File to have its access URL generated.
     * @return Temporary access URL for given {@link ImageTaskFile}.
     */
    private static String generateTempURL(Properties properties, ImageTaskFile imageTaskFile)
            throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        String objectStoreHost = properties
                .getProperty(SapsPropertiesConstants.SWIFT_OBJECT_STORE_HOST);
        String objectStorePath = properties
                .getProperty(SapsPropertiesConstants.SWIFT_OBJECT_STORE_PATH);
        String objectStoreContainer = properties
                .getProperty(SapsPropertiesConstants.SWIFT_OBJECT_STORE_CONTAINER);
        String objectStoreKey = properties
                .getProperty(SapsPropertiesConstants.SWIFT_OBJECT_STORE_KEY);
        String path = generateTempURLPath(
                objectStorePath,
                objectStoreContainer,
                imageTaskFile.getPath(),
                objectStoreKey);
        return HTTPS_SCHEME +
                objectStoreHost +
                path +
                FILENAME_QUERY_PARAM +
                imageTaskFile.getName();
    }

    /**
     * Generates the path of a temporary URL.
     *
     * @param swiftPath Path to Swift Object Store.
     * @param container Container of Swift Object Store.
     * @param filePath  Path to the file that will be accessed.
     * @param key       Key of encryption.
     * @return Path of a temporary URL.
     */
    private static String generateTempURLPath(
            String swiftPath,
            String container,
            String filePath,
            String key)
            throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        Formatter objectStoreFormatter = new Formatter();
        String path = swiftPath + "/" + container + "/" + filePath;
        objectStoreFormatter.format(OBJECT_STORE_ENCRIPTION_FORMAT, GET, Long.MAX_VALUE, path);
        String signature = calculateRFC2104HMAC(objectStoreFormatter.toString(), key);
        objectStoreFormatter.close();
        objectStoreFormatter = new Formatter();
        objectStoreFormatter.format(TEMP_DIR_URL, path, signature, Long.MAX_VALUE);
        String tempURLPath = objectStoreFormatter.toString();
        objectStoreFormatter.close();
        return tempURLPath;
    }

    /**
     * Encrypts the given data with the given key.
     *
     * @param data Data to be encrypted.
     * @param key  Key of encryption.
     * @return The encrypted string.
     */
    private static String calculateRFC2104HMAC(String data, String key)
            throws SignatureException, NoSuchAlgorithmException, InvalidKeyException {
        SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), HMAC_SHA1_ALGORITHM);
        Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
        mac.init(signingKey);
        return parseToHexString(mac.doFinal(data.getBytes()));
    }

    /**
     * Parses a array of bytes to a hexadecimal string.
     *
     * @param bytes Array of bytes to be parsed.
     * @return Hexadecimal string parsed from given array of bytes.
     */
    private static String parseToHexString(byte[] bytes) {
        Formatter formatter = new Formatter();
        for (byte b : bytes) {
            formatter.format("%02x", b);
        }
        String hexString = formatter.toString();
        formatter.close();
        return hexString;
    }

}
