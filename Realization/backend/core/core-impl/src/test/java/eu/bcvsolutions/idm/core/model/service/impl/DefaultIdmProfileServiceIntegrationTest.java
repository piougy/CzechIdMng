package eu.bcvsolutions.idm.core.model.service.impl;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmProfileDto;
import eu.bcvsolutions.idm.core.ecm.api.dto.IdmAttachmentDto;
import eu.bcvsolutions.idm.core.ecm.api.service.AttachmentManager;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Profile service tests
 * 
 * @author Radek Tomiška
 *
 */
@Transactional
public class DefaultIdmProfileServiceIntegrationTest extends AbstractIntegrationTest {
	
	@Autowired private ApplicationContext context;
	@Autowired private AttachmentManager attachmentManager;
	//
	private DefaultIdmProfileService service;

	@Before
	public void init() {
		service = context.getAutowireCapableBeanFactory().createBean(DefaultIdmProfileService.class);
	}
	
	@Test
	public void testReferentialIntegrity() {
		IdmIdentityDto identity = getHelper().createIdentity();
		IdmProfileDto profile = getHelper().createProfile(identity);
		//
		IdmAttachmentDto attachment = new IdmAttachmentDto();
		attachment.setName("something");
		attachment.setInputData(IOUtils.toInputStream("data"));
		attachment.setMimetype("text/plain");
		attachment = attachmentManager.saveAttachment(profile, attachment);
		//
		Assert.assertNotNull(attachmentManager.get(attachment.getId()));
		//
		service.delete(profile);
		//
		Assert.assertNull(attachmentManager.get(attachment.getId()));
	}

}
