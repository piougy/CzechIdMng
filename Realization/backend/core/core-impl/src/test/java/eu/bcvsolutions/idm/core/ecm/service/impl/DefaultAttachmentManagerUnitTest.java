package eu.bcvsolutions.idm.core.ecm.service.impl;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.modelmapper.ModelMapper;

import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.ecm.api.config.AttachmentConfiguration;
import eu.bcvsolutions.idm.core.ecm.api.dto.IdmAttachmentDto;
import eu.bcvsolutions.idm.core.ecm.entity.IdmAttachment;
import eu.bcvsolutions.idm.core.ecm.repository.IdmAttachmentRepository;
import eu.bcvsolutions.idm.core.model.entity.IdmProfile;
import eu.bcvsolutions.idm.test.api.AbstractVerifiableUnitTest;

/**
 * Attachment manager unit tests
 * - test closing streams after attachment is saved or updated
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultAttachmentManagerUnitTest extends AbstractVerifiableUnitTest {

	@Mock private IdmAttachmentRepository repository;
	@Mock private LookupService lookupService;
	@Mock private AttachmentConfiguration attachmentConfiguration;
	@Spy 
	private ModelMapper modelMapper = new ModelMapper();
	@InjectMocks
	private DefaultAttachmentManager attachmentManager;
	
	@Test
	public void testAttachmentInputStreamIsClosedAfterSave() throws IOException {		
		when(attachmentConfiguration.getStoragePath()).thenReturn("target");
		InputStream inputStreamSpy = Mockito.spy(IOUtils.toInputStream("mock"));
		//
		IdmProfile owner = new IdmProfile(UUID.randomUUID());
		IdmAttachmentDto attachment = new IdmAttachmentDto();
		attachment.setName("mock");
		attachment.setInputData(inputStreamSpy);
		//
		attachmentManager.saveAttachment(owner, attachment);
		// 
		Assert.assertNull(attachment.getInputData());
		Mockito.verify(inputStreamSpy).close();
		Mockito.verify(repository).saveAndFlush(Mockito.any(IdmAttachment.class));
		Mockito.verify(attachmentConfiguration, times(2)).getStoragePath();
	}
	
	@Test
	public void testAttachmentInputStreamIsClosedAfterUpdate() throws IOException {	
		when(attachmentConfiguration.getStoragePath()).thenReturn("target");
		when(repository.saveAndFlush(any())).thenReturn(new IdmAttachment());
		InputStream inputStreamSpy = Mockito.spy(IOUtils.toInputStream("mock"));
		//
		IdmProfile owner = new IdmProfile(UUID.randomUUID());
		IdmAttachmentDto attachment = new IdmAttachmentDto();
		attachment.setId(UUID.randomUUID());
		attachment.setName("mock");
		attachment.setInputData(inputStreamSpy);
		attachment.setOwnerId(owner.getId());
		attachment.setOwnerType("mock");
		//
		attachmentManager.updateAttachment(attachment);
		// 
		Assert.assertNull(attachment.getInputData());
		Mockito.verify(inputStreamSpy).close();
		Mockito.verify(repository).saveAndFlush(Mockito.any(IdmAttachment.class));
		Mockito.verify(repository).findOne(Mockito.any(UUID.class));
		Mockito.verify(attachmentConfiguration, times(2)).getStoragePath();
	}
	
	@Test
	public void testAttachmentInputStreamIsClosedAfterIOUtilsToByteArray() throws IOException {		
		InputStream inputStreamSpy = Mockito.spy(IOUtils.toInputStream("mock"));
		// used in acc module internally
		IOUtils.toByteArray(inputStreamSpy);
		IOUtils.closeQuietly(inputStreamSpy);
		//
		Mockito.verify(inputStreamSpy, times(1)).close();
	}
	
	@Test
	public void testAttachmentInputStreamIsClosedAfterIOUtilsToString() throws IOException {		
		InputStream inputStreamSpy = Mockito.spy(IOUtils.toInputStream("mock"));
		// used in acc module internally
		IOUtils.toString(inputStreamSpy);
		IOUtils.closeQuietly(inputStreamSpy);
		//
		Mockito.verify(inputStreamSpy, times(1)).close();
	}
}
