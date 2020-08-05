package eu.bcvsolutions.idm.core.rest.impl;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.domain.AutomaticRoleAttributeRuleType;
import eu.bcvsolutions.idm.core.api.domain.RecursionType;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleTreeNodeDto;
import eu.bcvsolutions.idm.core.api.repository.filter.FilterManager;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoControllerRestTest;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.test.api.TestHelper;

/**
 * Automatic role by tree structure rest tests.
 * - filters
 * 
 * FIXME: request are created in new transcation => transactional test (active operations) cannot be tested with AbstractReadWriteDtoControllerRestTest.
 * 
 * @author Radek Tomiška
 *
 */
public class IdmRoleTreeNodeControllerRestTest extends AbstractReadWriteDtoControllerRestTest<IdmRoleTreeNodeDto> {

	@Autowired private IdmRoleTreeNodeController controller;
	@Autowired private ConfigurationService configurationService;
	
	@Override
	protected AbstractReadWriteDtoController<IdmRoleTreeNodeDto, ?> getController() {
		return controller;
	}

	@Override
	protected IdmRoleTreeNodeDto prepareDto() {
		IdmRoleTreeNodeDto dto = new IdmRoleTreeNodeDto();
		dto.setName(getHelper().createName());
		dto.setRole(getHelper().createRole().getId());
		dto.setTreeNode(getHelper().createTreeNode().getId());
		dto.setRecursionType(RecursionType.NO);
		//
		return dto;
	}
	
	@Test
	public void testFindByName() {
		IdmRoleTreeNodeDto roleOne = prepareDto();
		roleOne.setName(getHelper().createName());
		IdmRoleTreeNodeDto roleOneCreated = createDto(roleOne);
		createDto(); // other
		//
		MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
		parameters.add("name", roleOne.getName());
		List<IdmRoleTreeNodeDto> roles = find(parameters);
		Assert.assertEquals(1, roles.size());
		Assert.assertTrue(roles.stream().anyMatch(r -> r.getId().equals(roleOneCreated.getId())));
	}
	
	@Test
	public void testFindByConcept() {
		MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
		parameters.add("concept", Boolean.TRUE.toString());
		//
		try {
			getMockMvc().perform(get(getFindQuickUrl())
	        		.with(authentication(getAdminAuthentication()))
	        		.params(parameters)
	                .contentType(TestHelper.HAL_CONTENT_TYPE))
					.andExpect(status().isNotImplemented());
		} catch (Exception ex) {
			throw new RuntimeException("Failed to find entities", ex);
		}
	}
	
	@Test
	public void testFindByHasRules() {
		MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
		parameters.add("hasRules", Boolean.TRUE.toString());
		//
		try {
			getMockMvc().perform(get(getFindQuickUrl())
	        		.with(authentication(getAdminAuthentication()))
	        		.params(parameters)
	                .contentType(TestHelper.HAL_CONTENT_TYPE))
					.andExpect(status().isNotImplemented());
		} catch (Exception ex) {
			throw new RuntimeException("Failed to find entities", ex);
		}
	}
	
	@Test
	public void testFindByRuleType() {
		MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
		parameters.add("ruleType", AutomaticRoleAttributeRuleType.IDENTITY.name());
		//
		try {
			getMockMvc().perform(get(getFindQuickUrl())
	        		.with(authentication(getAdminAuthentication()))
	        		.params(parameters)
	                .contentType(TestHelper.HAL_CONTENT_TYPE))
					.andExpect(status().isNotImplemented());
		} catch (Exception ex) {
			throw new RuntimeException("Failed to find entities", ex);
		}
	}
	
	@Test
	public void testFindByConceptWithoutFilterCheck() {
		try {
			configurationService.setBooleanValue(
					FilterManager.PROPERTY_CHECK_SUPPORTED_FILTER_ENABLED, 
					false);
			createDto();
			//
			MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
			parameters.add("concept", Boolean.TRUE.toString());
			List<IdmRoleTreeNodeDto> roles = find(parameters);
			//
			Assert.assertFalse(roles.isEmpty());
		} finally {
			configurationService.setBooleanValue(
					FilterManager.PROPERTY_CHECK_SUPPORTED_FILTER_ENABLED, 
					FilterManager.DEFAULT_CHECK_SUPPORTED_FILTER_ENABLED);
		}
	}

	@Override
	protected boolean supportsPatch() {
		return false;
	}
	
	@Override
	protected boolean supportsPut() {
		return false;
	}
	
	@Override
	protected boolean supportsPost() {
		return false;
	}
	
	@Override
	protected boolean supportsDelete() {
		return false;
	}
}
