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
 *
 */
public class SubmissionManagerImplTest {

    private static final String SAPS_NEIGHBOR_1_URL = "saps_neighbor_1_url";
    private static final String SAPS_NEIGHBOR_2_URL = "saps_neighbor_2_url";
    private static final String SAPS_NEIGHBOR_3_URL = "saps_neighbor_3_url";

    private Properties properties;
    private SubmissionManagerImpl submissionManagerImplSpy;
    private SubmissionDispatcher submissionDispatcher;

    @Before
    public void setUp() {
        properties = new Properties();
        String sapsNeighborsUrls = String.join(";",
                Arrays.asList(SAPS_NEIGHBOR_1_URL, SAPS_NEIGHBOR_2_URL, SAPS_NEIGHBOR_3_URL));
        properties.setProperty(SubmissionManagerImpl.SAPS_NEIGHBORS_URLS, sapsNeighborsUrls);

        submissionDispatcher = Mockito.mock(SubmissionDispatcher.class);
        SubmissionManagerImpl submissionManagerImpl = new SubmissionManagerImpl(properties, submissionDispatcher);
        submissionManagerImplSpy = Mockito.spy(submissionManagerImpl);
    }

    @Test
    public void testAddTasksWithoutProcessedTasks() {
        SubmissionParameters submissionParameters = getDefaultSubmissionParameters();
        List<Task> addedTasks = generateTaskList(
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

    @Test
    public void testAddTasksWithErrorWhileGettingProcessedTasks() throws Exception {
        SubmissionParameters submissionParameters = getDefaultSubmissionParameters();
        List<Date> processedDates = Collections.emptyList();
        List<Task> addedTasks = generateTaskList(
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

    @Test
    public void testAddTasksWithConsecutiveProcessedTasks() {
        SubmissionParameters submissionParameters = getDefaultSubmissionParameters();
        List<ImageTask> processedImageTasks = generateImageTaskList(
                DateUtil.buildDate(2014, 5, 12),
                DateUtil.buildDate(2014, 5, 22));
        List<Task> processedTasks = generateTaskList(processedImageTasks);
        List<Date> processedDates = processedImageTasks.stream()
                .map(ImageTask::getImageDate)
                .collect(Collectors.toList());
        List<Task> addedTasks = generateTaskList(
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

    @Test
    public void testAddTasksWithNonConsecutiveProcessedTasks() {
        SubmissionParameters submissionParameters = getDefaultSubmissionParameters();
        List<ImageTask> processedImageTasks = new ArrayList<>();
        processedImageTasks.addAll(generateImageTaskList(
                DateUtil.buildDate(2014, 5, 12),
                DateUtil.buildDate(2014, 5, 17)));
        processedImageTasks.addAll(generateImageTaskList(
                DateUtil.buildDate(2014, 5, 28),
                DateUtil.buildDate(2014, 6, 2)));
        processedImageTasks.addAll(generateImageTaskList(
                DateUtil.buildDate(2014, 6, 6),
                DateUtil.buildDate(2014, 6, 9)));

        List<Task> processedTasks = generateTaskList(processedImageTasks);
        List<Date> processedDates = processedImageTasks.stream()
                .map(ImageTask::getImageDate)
                .collect(Collectors.toList());

        List<Task> addedTasks = new ArrayList<>();
        addedTasks.addAll(generateTaskList(
                DateUtil.buildDate(2014, 5, 18),
                DateUtil.buildDate(2014, 5, 27)));
        addedTasks.addAll(generateTaskList(
                DateUtil.buildDate(2014, 6, 3),
                DateUtil.buildDate(2014, 6, 5)));
        addedTasks.addAll(generateTaskList(
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

    @Test
    public void testGetAllRemotelyProcessedTasksWithoutNeighbors() {
        SubmissionParameters submissionParameters = getDefaultSubmissionParameters();

        doReturn(new String[]{})
                .when(submissionManagerImplSpy)
                .getSAPSNeighborsUrls();

        List<ImageTask> allProcessedImageTasksActual = submissionManagerImplSpy
                .getAllRemotelyProcessedTasks(submissionParameters);

        assertEquals(Collections.emptyList(), allProcessedImageTasksActual);
    }

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

        assertEquals(Collections.emptyList(), allProcessedImageTasksActual);
    }

    @Test
    public void testGetAllRemotelyProcessedTasksWithoutDateOverlapping() {
        SubmissionParameters submissionParameters = getDefaultSubmissionParameters();
        List<ImageTask> processedImageTasks1 = generateImageTaskList(
                DateUtil.buildDate(2014, 5, 12),
                DateUtil.buildDate(2014, 5, 17));
        List<ImageTask> processedImageTasks2 = generateImageTaskList(
                DateUtil.buildDate(2014, 5, 28),
                DateUtil.buildDate(2014, 6, 2));
        List<ImageTask> processedImageTasks3 = generateImageTaskList(
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

        assertEquals(allProcessedImageTasksExpected, allProcessedImageTasksActual);
    }

    @Test
    public void testGetAllRemotelyProcessedTasksWithDateOverlapping() {
        SubmissionParameters submissionParameters = getDefaultSubmissionParameters();
        List<ImageTask> processedImageTasks1 = generateImageTaskList(
                DateUtil.buildDate(2014, 5, 12),
                DateUtil.buildDate(2014, 5, 25));
        List<ImageTask> processedImageTasks2 = generateImageTaskList(
                DateUtil.buildDate(2014, 5, 20),
                DateUtil.buildDate(2014, 6, 7));
        List<ImageTask> processedImageTasks3 = generateImageTaskList(
                DateUtil.buildDate(2014, 6, 3),
                DateUtil.buildDate(2014, 6, 9));
        List<ImageTask> allProcessedImageTasksExpected = generateImageTaskList(
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

        assertEquals(allProcessedImageTasksExpected, allProcessedImageTasksActual);
    }

    @Test
    public void testGetAllRemotelyProcessedTasksWithDateOverlappingWithoutRegionOverlapping() {
        SubmissionParameters submissionParameters = getDefaultSubmissionParameters();
        List<ImageTask> processedImageTasks1 = generateImageTaskList(
                DateUtil.buildDate(2014, 5, 12),
                DateUtil.buildDate(2014, 5, 25));
        List<ImageTask> processedImageTasks2 = generateImageTaskList(
                DateUtil.buildDate(2014, 5, 20),
                DateUtil.buildDate(2014, 6, 7));
        List<ImageTask> processedImageTasks3 = generateImageTaskList(
                DateUtil.buildDate(2014, 6, 3),
                DateUtil.buildDate(2014, 6, 9));
        processedImageTasks2 = processedImageTasks2.stream()
                .map(imageTask -> { imageTask.setRegion("differentRegion"); return imageTask; })
                .collect(Collectors.toList());
        List<ImageTask> allProcessedImageTasksExpected = generateImageTaskList(
                DateUtil.buildDate(2014, 5, 12),
                DateUtil.buildDate(2014, 5, 25)
        );
        allProcessedImageTasksExpected.addAll(processedImageTasks2);
        allProcessedImageTasksExpected.addAll(generateImageTaskList(
                DateUtil.buildDate(2014, 6, 3),
                DateUtil.buildDate(2014, 6, 9)));
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

        assertEquals(allProcessedImageTasksExpected, allProcessedImageTasksActual);
    }

    private List<ImageTask> generateImageTaskList(Date initDate, int length) {
        Calendar endCalendar = DateUtil.calendarFromDate(initDate);
        // Subtracts 1 because initDate is included in date interval
        endCalendar.add(Calendar.DAY_OF_MONTH, length - 1);
        Date endDate = endCalendar.getTime();
        return generateImageTaskList(initDate, endDate);
    }

    private List<ImageTask> generateImageTaskList(Date initDate, Date endDate) {
        return DateUtil.getDateListFromInterval(initDate, endDate)
                .stream()
                .map(this::createImageTaskWithDate)
                .collect(Collectors.toList());
    }

    private List<Task> generateTaskList(Date initDate, int length) {
        Calendar endCalendar = DateUtil.calendarFromDate(initDate);
        // Subtracts 1 because initDate is included in date interval
        endCalendar.add(Calendar.DAY_OF_MONTH, length - 1);
        Date endDate = endCalendar.getTime();
        return generateTaskList(initDate, endDate);
    }

    private List<Task> generateTaskList(Date initDate, Date endDate) {
        return generateTaskList(generateImageTaskList(initDate, endDate));
    }

    private List<Task> generateTaskList(List<ImageTask> imageTasks) {
        return imageTasks.stream()
                .map(this::createTaskWithImageTask)
                .collect(Collectors.toList());
    }

    private SubmissionParameters getDefaultSubmissionParameters() {
        return new SubmissionParameters(
                "lowerLeftLatitude",
                "lowerLeftLongitude",
                "upperRightLatitude",
                "upperRightLongitude",
                DateUtil.buildDate(2014, 5, 12),
                DateUtil.buildDate(2014, 6, 13),
                "default_script",
                "default_pre-script",
                "default_algorithm"
        );
    }

    private ImageTask createImageTaskWithDate(Date date) {
        return new ImageTask(
                "taskId",
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

    private Task createTaskWithImageTask(ImageTask imageTask) {
        return new Task("id", imageTask);
    }

}