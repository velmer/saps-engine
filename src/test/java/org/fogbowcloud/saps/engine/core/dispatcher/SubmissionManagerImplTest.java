package org.fogbowcloud.saps.engine.core.dispatcher;

import org.fogbowcloud.saps.engine.core.model.ImageTask;
import org.fogbowcloud.saps.engine.core.model.ImageTaskState;
import org.fogbowcloud.saps.engine.core.util.DateUtil;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Tests of {@link SubmissionManagerImpl}.
 */
public class SubmissionManagerImplTest {

    private static final String SAPS_NEIGHBOR_1_URL = "saps_neighbor_1_url";
    private static final String SAPS_NEIGHBOR_2_URL = "saps_neighbor_2_url";
    private static final String SAPS_NEIGHBOR_3_URL = "saps_neighbor_3_url";

    private SubmissionManagerImpl submissionManagerImplSpy;
    private SubmissionDispatcher submissionDispatcher;

    /**
     * Sets up the global properties and fields needed to this test suit
     * execute properly.
     */
    @Before
    public void setUp() {
        Properties properties = new Properties();
        String sapsNeighborsUrls = String.join(";",
                Arrays.asList(SAPS_NEIGHBOR_1_URL, SAPS_NEIGHBOR_2_URL, SAPS_NEIGHBOR_3_URL));
        properties.setProperty(SubmissionManagerImpl.SAPS_NEIGHBORS_URLS, sapsNeighborsUrls);

        submissionDispatcher = Mockito.mock(SubmissionDispatcher.class);
        SubmissionManagerImpl submissionManagerImpl = new SubmissionManagerImpl(properties, submissionDispatcher);
        submissionManagerImplSpy = Mockito.spy(submissionManagerImpl);
    }

    /**
     * Tests if when there aren't any processed tasks, tasks are created locally
     * for the whole date interval defined by the SubmissionParameters.
     */
    @Test
    public void testAddTasksWithoutProcessedTasks() {
        SubmissionParameters submissionParameters = getDefaultSubmissionParameters();
        List<Task> addedTasks = createTaskList(
                submissionParameters.getInitDate(),
                submissionParameters.getEndDate());

        doReturn(Collections.emptyList())
                .when(submissionManagerImplSpy)
                .getAllRemotelyProcessedTasks(submissionParameters);
        when(submissionDispatcher.addTasks(submissionParameters, Collections.emptyList()))
                .thenReturn(addedTasks);

        List<Task> addedTasksActual = submissionManagerImplSpy.addTasks(submissionParameters);

        verify(submissionDispatcher).addTasks(submissionParameters, Collections.emptyList());
        verify(submissionDispatcher, never()).addImageTasks(Collections.emptyList());
        assertEquals(addedTasks, addedTasksActual);
    }

    /**
     * Tests if when an error occurs while getting processed tasks, tasks are
     * created locally for the whole date interval defined by the SubmissionParameters.
     */
    @Test
    public void testAddTasksWithErrorWhileGettingProcessedTasks() {
        SubmissionParameters submissionParameters = getDefaultSubmissionParameters();
        List<Date> processedDates = Collections.emptyList();
        List<Task> addedTasks = createTaskList(
                submissionParameters.getInitDate(),
                submissionParameters.getEndDate());

        doThrow(new RuntimeException())
                .when(submissionManagerImplSpy)
                .getAllRemotelyProcessedTasks(submissionParameters);
        when(submissionDispatcher.addTasks(submissionParameters, processedDates))
                .thenReturn(addedTasks);

        List<Task> addedTasksActual = submissionManagerImplSpy.addTasks(submissionParameters);

        verify(submissionDispatcher).addTasks(submissionParameters, processedDates);
        verify(submissionDispatcher, never()).addImageTasks(any());
        assertEquals(addedTasks, addedTasksActual);
    }

    /**
     * Tests the addition of Tasks when requested processed tasks from neighbors
     * have dates that are consecutive.
     */
    @Test
    public void testAddTasksWithConsecutiveProcessedTasks() {
        SubmissionParameters submissionParameters = getDefaultSubmissionParameters();
        List<ImageTask> processedImageTasks = createImageTaskList(
                DateUtil.buildDate(2014, 5, 12),
                DateUtil.buildDate(2014, 5, 22));
        List<Task> processedTasks = createTaskList(processedImageTasks);
        List<Date> processedDates = processedImageTasks.stream()
                .map(ImageTask::getImageDate)
                .collect(Collectors.toList());
        List<Task> addedTasks = createTaskList(
                DateUtil.buildDate(2014, 5, 23),
                DateUtil.buildDate(2014, 6, 13));

        doReturn(processedImageTasks)
                .when(submissionManagerImplSpy)
                .getAllRemotelyProcessedTasks(submissionParameters);
        doReturn(processedTasks)
                .when(submissionDispatcher)
                .addImageTasks(processedImageTasks);
        when(submissionDispatcher.addTasks(submissionParameters, processedDates))
                .thenReturn(addedTasks);

        List<Task> allAddedTasks = submissionManagerImplSpy.addTasks(submissionParameters);
        List<Task> expectedAllAddedTasks = new ArrayList<>();
        expectedAllAddedTasks.addAll(addedTasks);
        expectedAllAddedTasks.addAll(processedTasks);
        expectedAllAddedTasks.sort(Comparator.comparing(task -> task.getImageTask().getImageDate()));

        verify(submissionDispatcher).addTasks(submissionParameters, processedDates);
        verify(submissionDispatcher).addImageTasks(processedImageTasks);
        assertEquals(expectedAllAddedTasks, allAddedTasks);
    }

    /**
     * Tests the addition of Tasks when requested processed tasks from neighbors
     * have dates that aren't consecutive.
     */
    @Test
    public void testAddTasksWithNonConsecutiveProcessedTasks() {
        SubmissionParameters submissionParameters = getDefaultSubmissionParameters();
        List<ImageTask> processedImageTasks = new ArrayList<>();
        processedImageTasks.addAll(createImageTaskList(
                DateUtil.buildDate(2014, 5, 12),
                DateUtil.buildDate(2014, 5, 17)));
        processedImageTasks.addAll(createImageTaskList(
                DateUtil.buildDate(2014, 5, 28),
                DateUtil.buildDate(2014, 6, 2)));
        processedImageTasks.addAll(createImageTaskList(
                DateUtil.buildDate(2014, 6, 6),
                DateUtil.buildDate(2014, 6, 9)));

        List<Task> processedTasks = createTaskList(processedImageTasks);
        List<Date> processedDates = processedImageTasks.stream()
                .map(ImageTask::getImageDate)
                .collect(Collectors.toList());

        List<Task> addedTasks = new ArrayList<>();
        addedTasks.addAll(createTaskList(
                DateUtil.buildDate(2014, 5, 18),
                DateUtil.buildDate(2014, 5, 27)));
        addedTasks.addAll(createTaskList(
                DateUtil.buildDate(2014, 6, 3),
                DateUtil.buildDate(2014, 6, 5)));
        addedTasks.addAll(createTaskList(
                DateUtil.buildDate(2014, 6, 10),
                DateUtil.buildDate(2014, 6, 13)));

        doReturn(processedImageTasks)
                .when(submissionManagerImplSpy)
                .getAllRemotelyProcessedTasks(submissionParameters);
        doReturn(processedTasks)
                .when(submissionDispatcher)
                .addImageTasks(processedImageTasks);
        when(submissionDispatcher.addTasks(submissionParameters, processedDates))
                .thenReturn(addedTasks);

        List<Task> allAddedTasks = submissionManagerImplSpy.addTasks(submissionParameters);
        List<Task> expectedAllAddedTasks = new ArrayList<>();
        expectedAllAddedTasks.addAll(addedTasks);
        expectedAllAddedTasks.addAll(processedTasks);
        expectedAllAddedTasks.sort(Comparator.comparing(task -> task.getImageTask().getImageDate()));

        verify(submissionDispatcher).addTasks(submissionParameters, processedDates);
        verify(submissionDispatcher).addImageTasks(processedImageTasks);
        assertEquals(expectedAllAddedTasks, allAddedTasks);
    }

    /**
     * Tests if returns a empty list when there aren't any neighbors.
     */
    @Test
    public void testGetAllRemotelyProcessedTasksWithoutNeighbors() {
        SubmissionParameters submissionParameters = getDefaultSubmissionParameters();

        doReturn(new String[]{})
                .when(submissionManagerImplSpy)
                .getSAPSNeighborsUrls();

        List<ImageTask> allProcessedImageTasksActual = submissionManagerImplSpy
                .getAllRemotelyProcessedTasks(submissionParameters);

        verify(submissionManagerImplSpy).getSAPSNeighborsUrls();
        verify(submissionManagerImplSpy, never())
                .getRemotelyProcessedTasksFromInstance(SAPS_NEIGHBOR_1_URL, submissionParameters);
        assertEquals(Collections.emptyList(), allProcessedImageTasksActual);
    }

    /**
     * Tests if returns a empty list when all neighbors don't have any processed
     * tasks for the provided {@link SubmissionParameters}.
     */
    @Test
    public void testGetAllRemotelyProcessedTasksWithoutProcessedTasks() {
        SubmissionParameters submissionParameters = getDefaultSubmissionParameters();

        doReturn(new String[]{ SAPS_NEIGHBOR_1_URL })
                .when(submissionManagerImplSpy)
                .getSAPSNeighborsUrls();
        doReturn(Collections.emptyList())
                .when(submissionManagerImplSpy)
                .getRemotelyProcessedTasksFromInstance(SAPS_NEIGHBOR_1_URL, submissionParameters);

        List<ImageTask> allProcessedImageTasksActual = submissionManagerImplSpy
                .getAllRemotelyProcessedTasks(submissionParameters);

        verify(submissionManagerImplSpy).getSAPSNeighborsUrls();
        verify(submissionManagerImplSpy)
                .getRemotelyProcessedTasksFromInstance(SAPS_NEIGHBOR_1_URL, submissionParameters);
        assertEquals(Collections.emptyList(), allProcessedImageTasksActual);
    }

    /**
     * Tests if all tasks from different neighbors are returned when there aren't
     * duplicates.
     */
    @Test
    public void testGetAllRemotelyProcessedTasksWithoutDateOverlapping() {
        SubmissionParameters submissionParameters = getDefaultSubmissionParameters();
        List<ImageTask> processedImageTasks1 = createImageTaskList(
                DateUtil.buildDate(2014, 5, 12),
                DateUtil.buildDate(2014, 5, 17));
        List<ImageTask> processedImageTasks2 = createImageTaskList(
                DateUtil.buildDate(2014, 5, 28),
                DateUtil.buildDate(2014, 6, 2));
        List<ImageTask> processedImageTasks3 = createImageTaskList(
                DateUtil.buildDate(2014, 6, 6),
                DateUtil.buildDate(2014, 6, 9));
        List<ImageTask> allProcessedImageTasksExpected = new ArrayList<>();
        allProcessedImageTasksExpected.addAll(processedImageTasks1);
        allProcessedImageTasksExpected.addAll(processedImageTasks2);
        allProcessedImageTasksExpected.addAll(processedImageTasks3);

        doReturn(processedImageTasks1)
                .when(submissionManagerImplSpy)
                .getRemotelyProcessedTasksFromInstance(SAPS_NEIGHBOR_1_URL, submissionParameters);
        doReturn(processedImageTasks2)
                .when(submissionManagerImplSpy)
                .getRemotelyProcessedTasksFromInstance(SAPS_NEIGHBOR_2_URL, submissionParameters);
        doReturn(processedImageTasks3)
                .when(submissionManagerImplSpy)
                .getRemotelyProcessedTasksFromInstance(SAPS_NEIGHBOR_3_URL, submissionParameters);

        List<ImageTask> allProcessedImageTasksActual = submissionManagerImplSpy
                .getAllRemotelyProcessedTasks(submissionParameters);

        verify(submissionManagerImplSpy).getSAPSNeighborsUrls();
        verify(submissionManagerImplSpy)
                .getRemotelyProcessedTasksFromInstance(SAPS_NEIGHBOR_1_URL, submissionParameters);
        verify(submissionManagerImplSpy)
                .getRemotelyProcessedTasksFromInstance(SAPS_NEIGHBOR_2_URL, submissionParameters);
        verify(submissionManagerImplSpy)
                .getRemotelyProcessedTasksFromInstance(SAPS_NEIGHBOR_3_URL, submissionParameters);
        assertEquals(allProcessedImageTasksExpected, allProcessedImageTasksActual);
    }

    /**
     * Tests if when getting duplicated tasks from different neighbors, only one
     * of those (the first one found) is returned in the list of processed tasks.
     */
    @Test
    public void testGetAllRemotelyProcessedTasksWithDateOverlapping() {
        SubmissionParameters submissionParameters = getDefaultSubmissionParameters();
        List<ImageTask> processedImageTasks1 = createImageTaskList(
                DateUtil.buildDate(2014, 5, 12),
                DateUtil.buildDate(2014, 5, 25));
        List<ImageTask> processedImageTasks2 = createImageTaskList(
                DateUtil.buildDate(2014, 5, 20),
                DateUtil.buildDate(2014, 6, 7));
        List<ImageTask> processedImageTasks3 = createImageTaskList(
                DateUtil.buildDate(2014, 6, 3),
                DateUtil.buildDate(2014, 6, 9));
        List<ImageTask> allProcessedImageTasksExpected = createImageTaskList(
                DateUtil.buildDate(2014, 5, 12),
                DateUtil.buildDate(2014, 6, 9)
        );

        doReturn(processedImageTasks1)
                .when(submissionManagerImplSpy)
                .getRemotelyProcessedTasksFromInstance(SAPS_NEIGHBOR_1_URL, submissionParameters);
        doReturn(processedImageTasks2)
                .when(submissionManagerImplSpy)
                .getRemotelyProcessedTasksFromInstance(SAPS_NEIGHBOR_2_URL, submissionParameters);
        doReturn(processedImageTasks3)
                .when(submissionManagerImplSpy)
                .getRemotelyProcessedTasksFromInstance(SAPS_NEIGHBOR_3_URL, submissionParameters);

        List<ImageTask> allProcessedImageTasksActual = submissionManagerImplSpy
                .getAllRemotelyProcessedTasks(submissionParameters);

        verify(submissionManagerImplSpy).getSAPSNeighborsUrls();
        verify(submissionManagerImplSpy)
                .getRemotelyProcessedTasksFromInstance(SAPS_NEIGHBOR_1_URL, submissionParameters);
        verify(submissionManagerImplSpy)
                .getRemotelyProcessedTasksFromInstance(SAPS_NEIGHBOR_2_URL, submissionParameters);
        verify(submissionManagerImplSpy)
                .getRemotelyProcessedTasksFromInstance(SAPS_NEIGHBOR_3_URL, submissionParameters);
        assertEquals(allProcessedImageTasksExpected, allProcessedImageTasksActual);
    }

    /**
     * Tests if when getting tasks with same date, but with different regions,
     * from different neighbors, all of them are returned.
     */
    @Test
    public void testGetAllRemotelyProcessedTasksWithDateOverlappingWithoutRegionOverlapping() {
        SubmissionParameters submissionParameters = getDefaultSubmissionParameters();
        List<ImageTask> processedImageTasks1 = createImageTaskList(
                DateUtil.buildDate(2014, 5, 12),
                DateUtil.buildDate(2014, 5, 25));
        List<ImageTask> processedImageTasks2 = createImageTaskList(
                DateUtil.buildDate(2014, 5, 20),
                DateUtil.buildDate(2014, 6, 7));
        List<ImageTask> processedImageTasks3 = createImageTaskList(
                DateUtil.buildDate(2014, 6, 3),
                DateUtil.buildDate(2014, 6, 9));
        processedImageTasks2 = processedImageTasks2.stream()
                .map(imageTask -> { imageTask.setRegion("differentRegion"); return imageTask; })
                .collect(Collectors.toList());
        List<ImageTask> allProcessedImageTasksExpected = new ArrayList<>();
        allProcessedImageTasksExpected.addAll(processedImageTasks1);
        allProcessedImageTasksExpected.addAll(processedImageTasks2);
        allProcessedImageTasksExpected.addAll(processedImageTasks3);
        allProcessedImageTasksExpected.sort(Comparator.comparing(ImageTask::getImageDate));

        doReturn(processedImageTasks1)
                .when(submissionManagerImplSpy)
                .getRemotelyProcessedTasksFromInstance(SAPS_NEIGHBOR_1_URL, submissionParameters);
        doReturn(processedImageTasks2)
                .when(submissionManagerImplSpy)
                .getRemotelyProcessedTasksFromInstance(SAPS_NEIGHBOR_2_URL, submissionParameters);
        doReturn(processedImageTasks3)
                .when(submissionManagerImplSpy)
                .getRemotelyProcessedTasksFromInstance(SAPS_NEIGHBOR_3_URL, submissionParameters);

        List<ImageTask> allProcessedImageTasksActual = submissionManagerImplSpy
                .getAllRemotelyProcessedTasks(submissionParameters);

        verify(submissionManagerImplSpy).getSAPSNeighborsUrls();
        verify(submissionManagerImplSpy)
                .getRemotelyProcessedTasksFromInstance(SAPS_NEIGHBOR_1_URL, submissionParameters);
        verify(submissionManagerImplSpy)
                .getRemotelyProcessedTasksFromInstance(SAPS_NEIGHBOR_2_URL, submissionParameters);
        verify(submissionManagerImplSpy)
                .getRemotelyProcessedTasksFromInstance(SAPS_NEIGHBOR_3_URL, submissionParameters);
        assertEquals(allProcessedImageTasksExpected, allProcessedImageTasksActual);
    }

    /**
     * Creates a list of {@link ImageTask}. Will create a {@link ImageTask} with
     * date from {@param initDate} to {@param endDate}.
     *
     * @param initDate Init date.
     * @param endDate End date.
     * @return List of ImageTasks.
     */
    private List<ImageTask> createImageTaskList(Date initDate, Date endDate) {
        return DateUtil.getDateListFromInterval(initDate, endDate)
                .stream()
                .map(this::createImageTaskWithDate)
                .collect(Collectors.toList());
    }

    /**
     * Creates a list of {@link Task}. Will create a {@link Task} for each
     * ImageTask created with date from {@param initDate} to {@param endDate}.
     *
     * @param initDate Init date.
     * @param endDate End date.
     * @return List of Tasks.
     */
    private List<Task> createTaskList(Date initDate, Date endDate) {
        return createTaskList(createImageTaskList(initDate, endDate));
    }

    /**
     * Creates a list of {@link Task} from specified list of ImageTasks.
     * Will create a {@link Task} for each ImageTask in {@param imageTask}.
     *
     * @param imageTasks List of ImageTasks.
     * @return List of Tasks.
     */
    private List<Task> createTaskList(List<ImageTask> imageTasks) {
        return imageTasks.stream()
                .map(this::createTaskWithImageTask)
                .collect(Collectors.toList());
    }

    /**
     * Returns a default {@link SubmissionParameters}. It will have init date of
     * 12/06/2014 and end date of 13/07/2014.
     *
     * @return Default SubmissionParameters.
     */
    private SubmissionParameters getDefaultSubmissionParameters() {
        return createSubmissionParameters(DateUtil.buildDate(2014, 5, 12),
                DateUtil.buildDate(2014, 6, 13));
    }

    /**
     * Creates a {@link SubmissionParameters} with specified initDate and endDate.
     *
     * @param initDate Init date of SubmissionParameters that will be created.
     * @param endDate  End date of SubmissionParameters that will be created.
     * @return Created SubmissionParameters.
     */
    private SubmissionParameters createSubmissionParameters(Date initDate, Date endDate) {
        return new SubmissionParameters(
                "lowerLeftLatitude",
                "lowerLeftLongitude",
                "upperRightLatitude",
                "upperRightLongitude",
                initDate,
                endDate,
                "default_script",
                "default_pre-script",
                "default_algorithm"
        );
    }

    /**
     * Creates a {@link ImageTask} with the specified date.
     *
     * @param date Date of ImageTask that will be created.
     * @return Created ImageTask.
     */
    private ImageTask createImageTaskWithDate(Date date) {
        return new ImageTask(
                "imageTaskId",
                "dataset",
                "region",
                date,
                "downloadLink",
                ImageTaskState.CREATED,
                "federationMember",
                1,
                "stationId",
                "inputGatheringTag",
                "inputPreprocessingTag",
                "algorithmExecutionTag",
                "archiverVersion",
                "blowoutVersion",
                new Timestamp(date.getTime()),
                new Timestamp(date.getTime()),
                "status",
                null);
    }

    /**
     * Creates a {@link Task} with the specified {@link ImageTask}.
     *
     * @param imageTask ImageTask of Task that will be created.
     * @return Created Task.
     */
    private Task createTaskWithImageTask(ImageTask imageTask) {
        return new Task("id", imageTask);
    }

}