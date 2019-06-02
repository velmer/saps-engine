package org.fogbowcloud.saps.engine.core.dispatcher;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Properties;

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
    public void addTasks() {
    }

}