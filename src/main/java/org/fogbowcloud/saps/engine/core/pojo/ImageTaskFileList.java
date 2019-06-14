package org.fogbowcloud.saps.engine.core.pojo;

import org.fogbowcloud.saps.engine.core.model.ImageTask;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Represents a list of {@link ImageTaskFile} for a specific {@link ImageTask}.
 */
public class ImageTaskFileList {

    private static final String IMAGE_TASK_ID = "imageTaskId";
    private static final String REGION = "region";
    private static final String COLLECTION_TIER_NAME = "collectionTierName";
    private static final String IMAGE_DATE = "imageDate";
    private static final String NAME = "name";
    private static final String URL = "url";
    private static final String FILES = "files";
    private static final String STATUS = "status";
    private static final String UNAVAILABLE = "UNAVAILABLE";

    private ImageTask imageTask;
    private List<ImageTaskFile> imageTaskFiles;

    public ImageTaskFileList(ImageTask imageTask, List<ImageTaskFile> imageTaskFiles) {
        this.imageTask = imageTask;
        this.imageTaskFiles = imageTaskFiles;
    }

    public ImageTaskFileList(JSONObject jsonObject) throws JSONException {
        ImageTask imageTask = new ImageTask(jsonObject.getString(IMAGE_TASK_ID),
                jsonObject.getString(REGION),
                (Date) jsonObject.get(IMAGE_DATE)
        );
        List<ImageTaskFile> imageTaskFiles = new ArrayList<>();
        JSONArray filesJSONArray = jsonObject.getJSONArray(FILES);
        for (int i = 0; i < filesJSONArray.length(); i++) {
            JSONObject imageTaskFileJSON = filesJSONArray.optJSONObject(i);
            imageTaskFiles.add(new ImageTaskFile(
                    null,
                    imageTaskFileJSON.getString(NAME),
                    imageTaskFileJSON.getString(URL)
            ));
        }
        this.imageTask = imageTask;
        this.imageTaskFiles = imageTaskFiles;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(IMAGE_TASK_ID, imageTask.getTaskId());
        try {
            jsonObject.put(REGION, imageTask.getRegion());
            jsonObject.put(IMAGE_DATE, imageTask.getImageDate());
            jsonObject.put(COLLECTION_TIER_NAME, imageTask.getCollectionTierName());
            JSONArray filesJSONArray = new JSONArray();
            for (ImageTaskFile imageTaskFile : imageTaskFiles) {
                JSONObject imageTaskFileJSONObject = new JSONObject();
                imageTaskFileJSONObject.put(NAME, imageTaskFile.getName());
                imageTaskFileJSONObject.put(URL, imageTaskFile.getURL());
                filesJSONArray.put(imageTaskFileJSONObject);
            }
            jsonObject.put(FILES, filesJSONArray);
        } catch (JSONException e) {
            jsonObject.put(STATUS, UNAVAILABLE);
            throw e;
        }
        return jsonObject;
    }
}
