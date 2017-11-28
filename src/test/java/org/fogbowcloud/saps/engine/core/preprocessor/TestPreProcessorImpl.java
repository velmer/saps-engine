package org.fogbowcloud.saps.engine.core.preprocessor;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Properties;

import org.fogbowcloud.saps.engine.core.database.ImageDataStore;
import org.fogbowcloud.saps.engine.core.database.JDBCImageDataStore;
import org.fogbowcloud.saps.engine.core.model.ImageTask;
import org.fogbowcloud.saps.engine.core.model.ImageTaskState;
import org.fogbowcloud.saps.engine.scheduler.core.exception.SapsException;
import org.fogbowcloud.saps.engine.scheduler.util.SapsPropertiesConstants;
import org.fogbowcloud.saps.engine.util.ExecutionScriptTag;
import org.fogbowcloud.saps.engine.util.ExecutionScriptTagUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

public class TestPreProcessorImpl {

	private static final String DEFAULT_EXPORT_PATH = "/local/exports";
	private Properties properties;
	private PreProcessorImpl preProcessor;
	private ImageDataStore imageStore;
	private ImageTask imageTask;
	
	@Rule
    public ExpectedException thrown = ExpectedException.none();

	@Before
	public void setUp() throws Exception {
		this.properties = new Properties();
		this.properties.put(SapsPropertiesConstants.SAPS_EXPORT_PATH, DEFAULT_EXPORT_PATH);
		this.properties.put(SapsPropertiesConstants.SAPS_CONTAINER_LINKED_PATH, "/home/ubuntu/");
		this.imageStore = Mockito.mock(JDBCImageDataStore.class);

		Date date = new Date(10000854);
		String federationMember = "fake-fed-member";

		this.imageTask = new ImageTask("task-id-1", "LT5", "region-53", date, "link1",
				ImageTaskState.CREATED, federationMember, 0, "NE", "NE", "Default", "NE",
				"NE", "NE", new Timestamp(date.getTime()), new Timestamp(date.getTime()),
				"available", "");

		this.preProcessor = Mockito.spy(new PreProcessorImpl(this.properties, this.imageStore));
	}

	public void testPreProcessImage() throws Exception {
		String hostPath = "fake-host-path";
		String containerPath = "fake-container-path";

		ExecutionScriptTag execScriptTag = new ExecutionScriptTag(
				this.imageTask.getInputPreprocessingTag(), "fogbow/preprocessor", "preprocessor",
				ExecutionScriptTagUtil.PRE_PROCESSING);

		Mockito.doReturn(execScriptTag).when(this.preProcessor).getContainerImageTags(imageTask);
		Mockito.doNothing().when(this.preProcessor).getDockerImage(execScriptTag);

		Mockito.doReturn(hostPath).when(this.preProcessor).getHostPath(imageTask);
		Mockito.doReturn(containerPath).when(this.preProcessor).getContainerPath();

		Mockito.doNothing().when(this.preProcessor).createPreProcessingHostPath(hostPath);

		String containerId = "fake-container-id";
		Mockito.doReturn(containerId).when(this.preProcessor).raiseContainer(execScriptTag,
				imageTask, hostPath, containerPath);
		
		thrown.expect(Exception.class);

		this.preProcessor.preProcessImage(this.imageTask);
	}

	@Test
	public void testGetContainerImageTags() throws SapsException {

		ExecutionScriptTag execScriptTag = new ExecutionScriptTag(
				this.imageTask.getInputPreprocessingTag(), "fogbow/preprocessor", "preprocessor",
				ExecutionScriptTagUtil.PRE_PROCESSING);

		assertEquals(this.preProcessor.getContainerImageTags(this.imageTask), execScriptTag);
	}

	@Test
	public void testGetHostPath() {

		String hostPath = DEFAULT_EXPORT_PATH + File.separator 
				+ this.imageTask.getTaskId() + "/data/preprocessing";

		assertEquals(hostPath, this.preProcessor.getHostPath(this.imageTask));
	}
	
	@Test
	public void testAddStateStamp() throws SQLException {
		String imageTaskId = "imageTaskId";
		
		Mockito.when(this.imageStore.getTask(Mockito.eq(imageTaskId))).thenReturn(null);		
		this.preProcessor.addStateStamp(imageTaskId);
		
		Mockito.verify(this.imageStore, Mockito.never()).addStateStamp(
				Mockito.anyString(), Mockito.any(ImageTaskState.class), Mockito.any(Timestamp.class));
	}
	
	@Test
	public void testAddStateStampWithTaskNull() throws SQLException {
		Mockito.when(this.imageStore.getTask(Mockito.anyString())).thenReturn(null);
		
		String imageTaskId = "imageTaskId";
		try {
			this.preProcessor.addStateStamp(imageTaskId);			
		} catch (Exception e) {
			Assert.fail();
		}
	}

}
