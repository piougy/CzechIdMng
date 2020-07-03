package eu.bcvsolutions.idm.core.audit.rest.impl;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.audit.dto.IdmAuditDto;
import eu.bcvsolutions.idm.core.api.audit.service.IdmAuditService;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.test.api.AbstractRestTest;
import eu.bcvsolutions.idm.test.api.TestHelper;

/**
 * Audit controller rest test.
 * 
 * @author Radek Tomiška
 *
 */
public class IdmAuditControllerRestTest extends AbstractRestTest {

	// @Autowired private IdmAuditController controller;
	@Autowired private IdmAuditService service;

	protected IdmAuditDto prepareDto() {
		return new IdmAuditDto();
	}
	
	@Test
	@Transactional
	public void testFindByLongId() {
		UUID entityId = UUID.randomUUID();
		//
		IdmAuditDto auditDto = prepareDto();
		auditDto.setEntityId(entityId);
		auditDto = service.save(auditDto);
		//
		MultiValueMap<String, String> filter = new LinkedMultiValueMap<>();
		filter.set(BaseEntity.PROPERTY_ID, String.valueOf(auditDto.getId()));
		//
		List<IdmAuditDto> find = find(filter, "/audits/search/quick");
		//
		Assert.assertEquals(1, find.size());
		Assert.assertEquals(entityId, find.get(0).getEntityId());
	}
	
	@Test
	public void testFindEntityByUsername() throws Exception {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		MultiValueMap<String, String> filter = new LinkedMultiValueMap<>();
		filter.set("ownerType", IdmIdentity.class.getCanonicalName());
		filter.set(IdmIdentity_.username.getName(), identity.getUsername());
		//
		List<IdmAuditDto> find = find(filter, "/audits/search/entity");
		//
		Assert.assertFalse(find.isEmpty());
		Assert.assertTrue(find.stream().allMatch(a -> a.getOwnerId().equals(identity.getId().toString())));
		//
		// test get revision
		String response = getMockMvc().perform(get(BaseController.BASE_PATH + "/audits/" + find.get(0).getId())
        		.with(authentication(getAdminAuthentication()))
        		.params(filter)
                .contentType(TestHelper.HAL_CONTENT_TYPE))
				.andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
		// FIXME: jsou deserialization doesn't work ...
//		IdmAuditDto audit = getMapper().convertValue(response, IdmAuditDto.class);
//		//
//		Assert.assertNotNull(audit);
//		Assert.assertEquals(identity.getId().toString(), audit.getOwnerId());
		// ugly workaround:
		Assert.assertTrue(response.contains(identity.getId().toString()));
		
	}
	
	// get previous version
	
	/**
	 * Find audit entities
	 * 
	 * @param filter raw map => data filter is not used in audit
	 * @return
	 */
	protected List<IdmAuditDto> find(MultiValueMap<String, String> filter, String searchName) {
		try {
			String response = getMockMvc().perform(get(BaseController.BASE_PATH + searchName)
	        		.with(authentication(getAdminAuthentication()))
	        		.params(filter)
	                .contentType(TestHelper.HAL_CONTENT_TYPE))
					.andExpect(status().isOk())
	                .andExpect(content().contentType(TestHelper.HAL_CONTENT_TYPE))
	                .andReturn()
	                .getResponse()
	                .getContentAsString();
			//
			return toDtos(response, IdmAuditDto.class);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to find entities", ex);
		}
	}
}
