package org.fogbowcloud.saps.engine.core.dispatcher;

import org.fogbowcloud.saps.engine.core.model.ImageTask;
import org.fogbowcloud.saps.engine.core.model.ImageTaskState;
import org.fogbowcloud.saps.engine.core.util.DateUtil;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 *
 */
public class SubmissionManagerImplTest {

    private static final String SAPS_NEIGHBOR_1_URL = "saps_neighbor_1_url";
    private static final String SAPS_NEIGHBOR_2_URL = "saps_neighbor_2_url";
    private static final String SAPS_NEIGHBOR_3_URL = "saps_neighbor_3_url";

    private Properties properties;
    private SubmissionManagerImpl submissionManagerImpl;
    private SubmissionDispatcher submissionDispatcher;

    @Before
    public void setUp() {
        properties = new Properties();
        String sapsNeighborsUrls = String.join(";",
                Arrays.asList(SAPS_NEIGHBOR_1_URL, SAPS_NEIGHBOR_2_URL, SAPS_NEIGHBOR_3_URL));
        properties.setProperty(SubmissionManagerImpl.SAPS_NEIGHBORS_URLS, sapsNeighborsUrls);

        submissionDispatcher = Mockito.mock(SubmissionDispatcher.class);
        submissionManagerImpl = new SubmissionManagerImpl(properties, submissionDispatcher);
    }

    @Test
    public void testAddTasksWithoutProcessedTasks() throws SQLException {
        SubmissionParameters submissionParameters = getDefaultSubmissionParameters();

        Mockito.spy(submissionManagerImpl);
        Mockito.when(submissionManagerImpl.getAllRemotelyProcessedTasks(submissionParameters))
            .thenReturn(Collections.emptyList());
        Mockito.stub(submissionDispatcher.addTasks(submissionParameters, Collections.emptyList()));
        Mockito.doNothing().when(submissionDispatcher).addImageTasks(Collections.emptyList());

        submissionManagerImpl.addTasks(submissionParameters);

        Mockito.verify(submissionDispatcher.addTasks(submissionParameters, Collections.emptyList()));
        Mockito.verify(submissionDispatcher).addImageTasks(Collections.emptyList());
    }

    @Test
    public void testAddTasksWithConsecutiveProcessedTasks() throws SQLException {
        SubmissionParameters submissionParameters = getDefaultSubmissionParameters();
        Date initDate = submissionParameters.getInitDate();
        int amountProcessedTasks = 10;
        List<ImageTask> processedTasks = generateImageTaskList(initDate, amountProcessedTasks);
        List<Date> processedDates = processedTasks.stream()
                .map(ImageTask::getImageDate)
                .collect(Collectors.toList());

        Mockito.spy(submissionManagerImpl);
        Mockito.when(submissionManagerImpl.getAllRemotelyProcessedTasks(submissionParameters))
                .thenReturn(processedTasks);
        Mockito.stub(submissionDispatcher.addTasks(submissionParameters, processedDates));
        Mockito.doNothing().when(submissionDispatcher).addImageTasks(processedTasks);

        submissionManagerImpl.addTasks(submissionParameters);

        Mockito.verify(submissionDispatcher.addTasks(submissionParameters, processedDates));
        Mockito.verify(submissionDispatcher).addImageTasks(processedTasks);
    }

    private List<ImageTask> generateImageTaskList(Date initDate, int length) {
        Calendar endCalendar = DateUtil.calendarFromDate(initDate);
        endCalendar.add(Calendar.DAY_OF_MONTH, length);
        Date endDate = endCalendar.getTime();
        return  generateImageTaskList(initDate, endDate);
    }

    private List<ImageTask> generateImageTaskList(Date initDate, Date endDate) {
        return DateUtil.getDateListFromInterval(initDate, endDate)
                .stream()
                .map(this::createImageTaskWithDate)
                .collect(Collectors.toList());
    }

    private SubmissionParameters getDefaultSubmissionParameters() {
        return new SubmissionParameters(
                "lowerLeftLatitude",
                "lowerLeftLongitude",
                "UpperRightLatitude",
                "UpperRightLongitude",
                DateUtil.buildDate(2014, 6, 12),
                DateUtil.buildDate(2014, 7, 13),
                "default_script",
                "default_pre-script",
                "default_algorithm"
        );
    }

    private ImageTask createImageTaskWithDate(Date date) {
        return new ImageTask(
                "taskId",
                "dataset",
                "region", date,
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

}