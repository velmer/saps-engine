package org.fogbowcloud.saps.engine.core.dispatcher;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.fogbowcloud.saps.engine.core.database.ImageDataStore;
import org.fogbowcloud.saps.engine.core.database.JDBCImageDataStore;
import org.fogbowcloud.saps.engine.core.model.ImageTask;
import org.fogbowcloud.saps.engine.core.model.ImageTaskState;
import org.fogbowcloud.saps.engine.core.model.SapsUser;
import org.fogbowcloud.saps.engine.core.repository.USGSNasaRepository;
import org.fogbowcloud.saps.engine.core.util.DatasetUtil;
import org.fogbowcloud.saps.engine.core.util.DateUtil;
import org.fogbowcloud.saps.notifier.Ward;

public class SubmissionDispatcherImpl implements SubmissionDispatcher {
    public static final int DEFAULT_PRIORITY = 0;
    private final JDBCImageDataStore imageStore;
    private Properties properties;
    private USGSNasaRepository repository;

    private static final Logger LOGGER = Logger.getLogger(SubmissionDispatcherImpl.class);

    public SubmissionDispatcherImpl(JDBCImageDataStore imageStore, USGSNasaRepository repository) {
        this.imageStore = imageStore;
        this.repository = repository;
    }

    public SubmissionDispatcherImpl(Properties properties) throws SQLException {
        this.properties = properties;
        this.imageStore = new JDBCImageDataStore(properties);
        this.repository = new USGSNasaRepository(properties);
    }

    @Override
    public void addUserInDB(String userEmail, String userName, String userPass, boolean userState,
                            boolean userNotify, boolean adminRole) throws SQLException {
        try {
            imageStore.addUser(userEmail, userName, userPass, userState, userNotify, adminRole);
        } catch (SQLException e) {
            LOGGER.error("Error while adding user " + userEmail + " in Catalogue", e);
            throw new SQLException(e);
        }
    }

    @Override
    public void updateUserState(String userEmail, boolean userState) throws SQLException {
        try {
            imageStore.updateUserState(userEmail, userState);
        } catch (SQLException e) {
            LOGGER.error("Error while adding user " + userEmail + " in Catalogue", e);
            throw new SQLException(e);
        }
    }

    @Override
    public SapsUser getUser(String userEmail) {
        try {
            return imageStore.getUser(userEmail);
        } catch (SQLException e) {
            LOGGER.error("Error while trying to get Sebal User with email: " + userEmail + ".", e);
        }
        return null;
    }

    @Override
    public void addTaskNotificationIntoDB(String submissionId, String taskId, String userEmail)
            throws SQLException {
        try {
            imageStore.addUserNotification(submissionId, taskId, userEmail);
        } catch (SQLException e) {
            LOGGER.error("Error while adding task " + taskId + " notification for user " + userEmail
                    + " in Catalogue", e);
        }
    }

    @Override
    public void removeUserNotification(String submissionId, String taskId, String userEmail)
            throws SQLException {
        try {
            imageStore.removeNotification(submissionId, taskId, userEmail);
        } catch (SQLException e) {
            LOGGER.error("Error while removing task " + taskId + " notification for user "
                    + userEmail + " from Catalogue", e);
        }
    }

    @Override
    public boolean isUserNotifiable(String userEmail) throws SQLException {
        try {
            return imageStore.isUserNotifiable(userEmail);
        } catch (SQLException e) {
            LOGGER.error("Error while verifying user notify", e);
        }

        return false;
    }

    @Override
    public void setTasksToPurge(String day, boolean force) throws SQLException, ParseException {
        List<ImageTask> tasksToPurge = force ? imageStore.getAllTasks()
                : imageStore.getIn(ImageTaskState.ARCHIVED);

        for (ImageTask imageTask : tasksToPurge) {
            long date = 0;
            try {
                date = parseStringToDate(day).getTime();
            } catch (ParseException e) {
                LOGGER.error("Error while parsing string to date", e);
            }
            if (isBeforeDay(date, imageTask.getUpdateTime())) {
                imageTask.setStatus(ImageTask.PURGED);

                imageStore.updateImageTask(imageTask);
                imageTask.setUpdateTime(imageStore.getTask(imageTask.getTaskId()).getUpdateTime());
            }
        }
    }

    protected Date parseStringToDate(String day) throws ParseException {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        java.util.Date date = format.parse(day);
        java.sql.Date sqlDate = new java.sql.Date(date.getTime());
        return sqlDate;
    }

    protected boolean isBeforeDay(long date, Timestamp imageTaskDay) {
        return (imageTaskDay.getTime() <= date);
    }

    // FIXME is it necessaty ? "System.out.println" ?
    @Override
    public void listTasksInDB() throws SQLException, ParseException {
        List<ImageTask> allImageTask = imageStore.getAllTasks();
        for (int i = 0; i < allImageTask.size(); i++) {
            System.out.println(allImageTask.get(i).toString());
        }
    }

    @Override
    public List<Task> addTasks(SubmissionParameters submissionParameters, List<Date> processedDates) {
        Set<String> regions = repository.getRegionsFromArea(
                submissionParameters.getLowerLeftLatitude(),
                submissionParameters.getLowerLeftLongitude(),
                submissionParameters.getUpperRightLatitude(),
                submissionParameters.getUpperRightLongitude()
        );
        List<Date> datesToProcess = DateUtil.getDateListFromInterval(
                submissionParameters.getInitDate(),
                submissionParameters.getEndDate());
        // Filter already processed dates
        datesToProcess = datesToProcess.stream()
                .filter(processedDates::contains)
                .collect(Collectors.toList());
        List<Task> createdTasks = new ArrayList<>();
        for (Date currentDate : datesToProcess) {
            List<Task> createdTasksForCurrentDate = addTasksForDate(
                    currentDate,
                    submissionParameters,
                    regions
            );
            createdTasks.addAll(createdTasksForCurrentDate);
        }
        return createdTasks;
    }

    /**
     * Adds all the needed tasks for specified date. It will add a task for every
     * satellite in operation in specified date, for every specified region.
     *
     * @param date                 Date of processing.
     * @param submissionParameters Submission parameters specified by user.
     * @param regions              Regions to be processed.
     * @return List of added tasks.
     */
    private List<Task> addTasksForDate(Date date,
                                       SubmissionParameters submissionParameters,
                                       Set<String> regions) {
        LOGGER.debug("Adding tasks for date: " + date);
        int startingYear = DateUtil.calendarFromDate(date).get(Calendar.YEAR);
        List<String> satellitesInOperation = DatasetUtil.getSatsInOperationByYear(startingYear);
        List<Task> createdTasksForDate = new ArrayList<>();
        for (String satellite : satellitesInOperation) {
            for (String region : regions) {
                try {
                    ImageTask imageTask = addImageTask(date, submissionParameters, region, satellite);
                    Task task = new Task(UUID.randomUUID().toString());
                    task.setImageTask(imageTask);
                    createdTasksForDate.add(task);
                } catch (SQLException e) {
                    LOGGER.error("Error while adding image to database", e);
                }
            }
        }
        return createdTasksForDate;
    }

    /**
     * Adds a ImageTask to ImageStore for specified date, region and satellite.
     *
     * @param date                 Date of processing.
     * @param submissionParameters Submission parameters specified by user.
     * @param region               Region to be processed.
     * @param satellite            Satellite that provide the landsat image.
     * @return Added ImageTask.
     * @throws SQLException
     */
    private ImageTask addImageTask(Date date,
                                   SubmissionParameters submissionParameters,
                                   String region,
                                   String satellite) throws SQLException {
        String imageTaskId = UUID.randomUUID().toString();
        ImageTask imageTask = getImageStore().addImageTask(
                imageTaskId,
                satellite,
                region,
                date,
                ImageDataStore.NONE,
                DEFAULT_PRIORITY,
                submissionParameters.getInputGathering(),
                submissionParameters.getInputPreprocessing(),
                submissionParameters.getAlgorithmExecution()
        );
        getImageStore().addStateStamp(imageTaskId, ImageTaskState.CREATED,
                getImageStore().getTask(imageTaskId).getUpdateTime());
        getImageStore().dispatchMetadataInfo(imageTaskId);
        return imageTask;
    }

    @Override
    public void addImageTasks(Collection<ImageTask> imageTasks) {
        for (ImageTask imageTask: imageTasks) {
            try {
                addImageTask(imageTask);
            } catch (SQLException e) {
                LOGGER.error("", e);
            }
        }
    }

    @Override
    public void addImageTask(ImageTask imageTask) throws SQLException {
        imageStore.addImageTask(imageTask);
    }

    public List<ImageTask> getTaskListInDB() throws SQLException, ParseException {
        return imageStore.getAllTasks();
    }

    @Override
    public List<Ward> getUsersToNotify() throws SQLException {
        List<Ward> wards = imageStore.getUsersToNotify();
        return wards;
    }

    public ImageTask getTaskInDB(String taskId) throws SQLException {
        List<ImageTask> allTasks = imageStore.getAllTasks();

        for (ImageTask imageTask : allTasks) {
            if (imageTask.getTaskId().equals(taskId)) {
                return imageTask;
            }
        }

        return null;
    }

    public List<ImageTask> getTasksInState(ImageTaskState imageState) throws SQLException {
        return this.imageStore.getIn(imageState);
    }

    public JDBCImageDataStore getImageStore() {
        return imageStore;
    }

    public Properties getProperties() {
        return properties;
    }

    @Override
    public List<ImageTask> searchProcessedTasks(SubmissionParameters submissionParameters) {
        List<ImageTask> filteredTasks = new ArrayList<>();
        Set<String> regions = new HashSet<>(repository.getRegionsFromArea(
                submissionParameters.getLowerLeftLatitude(),
                submissionParameters.getLowerLeftLongitude(),
                submissionParameters.getUpperRightLatitude(),
                submissionParameters.getUpperRightLongitude()
        ));

        for (String region : regions) {
            List<ImageTask> iTasks;
            try {
                iTasks = getImageStore().getProcessedImages(
                        region,
                        submissionParameters.getInitDate(),
                        submissionParameters.getEndDate(),
                        submissionParameters.getInputGathering(),
                        submissionParameters.getInputPreprocessing(),
                        submissionParameters.getAlgorithmExecution()
                );
                filteredTasks.addAll(iTasks);
            } catch (SQLException e) {
                String builder = "Failed to load images with configuration:\n" +
                        "\tRegion: " + region + "\n" +
                        "\tInterval: " + submissionParameters.getInitDate() + " - " + submissionParameters.getEndDate() + "\n" +
                        "\tGathering: " + submissionParameters.getInputGathering() + "\n" +
                        "\tPreprocessing: " + submissionParameters.getInputPreprocessing() + "\n" +
                        "\tAlgorithm: " + submissionParameters.getAlgorithmExecution() + "\n";
                LOGGER.error(builder, e);
            }
        }
        return filteredTasks;
    }
}