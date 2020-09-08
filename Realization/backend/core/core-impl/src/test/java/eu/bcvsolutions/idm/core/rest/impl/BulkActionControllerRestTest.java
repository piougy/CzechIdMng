package eu.bcvsolutions.idm.core.rest.impl;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.CoreModule;
import eu.bcvsolutions.idm.core.api.bulk.action.BulkActionManager;
import eu.bcvsolutions.idm.core.api.bulk.action.IdmBulkAction;
import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BulkActionFilter;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.bulk.action.impl.IdentityChangeUserTypeBulkAction;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;
import eu.bcvsolutions.idm.test.api.AbstractRestTest;
import eu.bcvsolutions.idm.test.api.TestHelper;

/**
 * Bulk action controller tests.
 * - enable / disable
 * - find
 * - test configuration
 * 
 * @author Radek Tomiška
 *
 */
@Transactional
public class BulkActionControllerRestTest extends AbstractRestTest {
	
	@Autowired private BulkActionManager manager;
	@Autowired private IdentityChangeUserTypeBulkAction bulkAction;
	@Autowired private ConfigurationService configurationService;
	
	@Test
	public void testEnable() throws Exception {
		try {
			manager.enable(bulkAction.getId());
			//
			BulkActionFilter filter = new BulkActionFilter();
			filter.setName(IdentityChangeUserTypeBulkAction.NAME);
			List<IdmBulkActionDto> results = find(filter);
			//
			Assert.assertEquals(1, results.size());
			Assert.assertEquals(IdentityChangeUserTypeBulkAction.NAME, results.get(0).getName());
			Assert.assertFalse(results.get(0).isDisabled());
			//
			getMockMvc().perform(put(BaseController.BASE_PATH + "/bulk-actions/" + results.get(0).getId() + "/disable")
					.with(authentication(getAdminAuthentication()))
	                .contentType(TestHelper.HAL_CONTENT_TYPE))
					.andExpect(status().is2xxSuccessful());
			//
			results = find(filter);
			Assert.assertEquals(1, results.size());
			Assert.assertEquals(IdentityChangeUserTypeBulkAction.NAME, results.get(0).getName());
			Assert.assertTrue(results.get(0).isDisabled());
			//
			// enable again
			getMockMvc().perform(put(BaseController.BASE_PATH + "/bulk-actions/" + results.get(0).getId() + "/enable")
					.with(authentication(getAdminAuthentication()))
	                .contentType(TestHelper.HAL_CONTENT_TYPE))
					.andExpect(status().is2xxSuccessful());
			//
			results = find(filter);
			Assert.assertEquals(1, results.size());
			Assert.assertEquals(IdentityChangeUserTypeBulkAction.NAME, results.get(0).getName());
			Assert.assertFalse(results.get(0).isDisabled());
		} finally {
			manager.enable(bulkAction.getId());
		}
	}
	
	@Test
	public void testFindByName() {
		BulkActionFilter filter = new BulkActionFilter();
		filter.setName(IdentityChangeUserTypeBulkAction.NAME);
		List<IdmBulkActionDto> results = find(filter);
		//
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(IdentityChangeUserTypeBulkAction.NAME, results.get(0).getId());
		//
		filter.setName(getHelper().createName()); // not-exists
		results = find(filter);
		Assert.assertTrue(results.isEmpty());
	}
	
	@Test
	public void testFindWithoutFilter() {
		List<IdmBulkActionDto> results = manager.find(null);
		//
		Assert.assertFalse(results.isEmpty());
		Assert.assertTrue(results.stream().anyMatch(a -> IdentityChangeUserTypeBulkAction.NAME.equals(a.getId())));
	}
	
	@Test
	public void testFindByModule() {
		BulkActionFilter filter = new BulkActionFilter();
		filter.setModule(CoreModule.MODULE_ID);
		List<IdmBulkActionDto> results = find(filter);
		//
		results = find(filter);
		Assert.assertFalse(results.isEmpty());
		Assert.assertTrue(results.stream().anyMatch(a -> IdentityChangeUserTypeBulkAction.NAME.equals(a.getId())));
		Assert.assertTrue(results.stream().allMatch(a -> CoreModule.MODULE_ID.equals(a.getModule())));
		//
		filter.setModule(getHelper().createName()); // not-exists
		results = find(filter);
		Assert.assertTrue(results.isEmpty());
	}
	
	@Test
	public void testFindByEntityClass() {
		BulkActionFilter filter = new BulkActionFilter();
		filter.setEntityClass(IdmIdentity.class.getCanonicalName());
		filter.setName(IdentityChangeUserTypeBulkAction.NAME);
		List<IdmBulkActionDto> results = find(filter);
		//
		results = find(filter);
		Assert.assertFalse(results.isEmpty());
		Assert.assertTrue(results.stream().anyMatch(a -> IdentityChangeUserTypeBulkAction.NAME.equals(a.getId())));
		Assert.assertTrue(results.stream().allMatch(a -> a.getEntityClass() == null // generic actions
				|| IdmIdentity.class.getCanonicalName().equals(a.getEntityClass())));
		//
		filter.setEntityClass(getHelper().createName()); // not-exists
		results = find(filter);
		Assert.assertTrue(results.isEmpty());
		//
		filter.setEntityClass(null);
		filter.setText(IdmIdentity.class.getCanonicalName());
		results = find(filter);
		Assert.assertFalse(results.isEmpty());
		Assert.assertTrue(results.stream().anyMatch(a -> IdentityChangeUserTypeBulkAction.NAME.equals(a.getId())));
	}
	
	@Test
	public void testFindByText() {
		BulkActionFilter filter = new BulkActionFilter();
		filter.setText(IdentityChangeUserTypeBulkAction.NAME);
		List<IdmBulkActionDto> results = find(filter);
		//
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(IdentityChangeUserTypeBulkAction.NAME, results.get(0).getId());
		//
		filter.setText(getHelper().createName()); // not-exists
		results = find(filter);
		Assert.assertTrue(results.isEmpty());
	}
	
	@Test
	public void testFindByDesctiption() {
		BulkActionFilter filter = new BulkActionFilter();
		filter.setDescription(IdentityChangeUserTypeBulkAction.DESCRIPTION);
		List<IdmBulkActionDto> results = find(filter);
		//
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(IdentityChangeUserTypeBulkAction.NAME, results.get(0).getId());
		//
		filter.setDescription(getHelper().createName()); // not-exists
		results = find(filter);
		Assert.assertTrue(results.isEmpty());
		//
		filter.setDescription(null);
		filter.setText(IdentityChangeUserTypeBulkAction.DESCRIPTION);
		results = find(filter);
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(IdentityChangeUserTypeBulkAction.NAME, results.get(0).getId());
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void testFindById() {
		BulkActionFilter filter = new BulkActionFilter();
		filter.setId(UUID.randomUUID());
		//
		manager.find(filter);
	}
	
	@Test
	public void testBulkActionConfiguration() {
		int defaultOrder = bulkAction.getOrder();
		//
		try {
			Assert.assertEquals(NotificationLevel.SUCCESS, bulkAction.getLevel());
			Assert.assertFalse(bulkAction.isDeleteAction());
			Assert.assertFalse(bulkAction.isQuickButton());
			Assert.assertNull(bulkAction.getConfigurationValue(ConfigurationService.PROPERTY_ICON));
			Assert.assertNull(bulkAction.getConfigurationValue(ConfigurationService.PROPERTY_LEVEL));
			Assert.assertNull(bulkAction.getConfigurationValue(ConfigurationService.PROPERTY_ORDER));
			//
			getHelper().setConfigurationValue(
					bulkAction.getConfigurationPropertyName(ConfigurationService.PROPERTY_ORDER), 
					String.valueOf(defaultOrder + 1));
			
			;
			getHelper().setConfigurationValue(
					bulkAction.getConfigurationPropertyName(ConfigurationService.PROPERTY_LEVEL), 
					NotificationLevel.ERROR.name());
			
			;
			getHelper().setConfigurationValue(
					bulkAction.getConfigurationPropertyName(ConfigurationService.PROPERTY_ICON), 
					"fa:walking");
			
			;
			getHelper().setConfigurationValue(
					bulkAction.getConfigurationPropertyName(IdmBulkAction.PROPERTY_DELETE_ACTION), 
					true);
			
			;
			getHelper().setConfigurationValue(
					bulkAction.getConfigurationPropertyName(IdmBulkAction.PROPERTY_QUICK_BUTTON), 
					true);
			
			;
			//
			IdmBulkActionDto bulkActionDto = manager.toDto(bulkAction);
			Assert.assertEquals(NotificationLevel.ERROR, bulkActionDto.getLevel());
			Assert.assertTrue(bulkActionDto.isDeleteAction());
			Assert.assertTrue(bulkActionDto.isQuickButton());
			Assert.assertEquals("fa:walking", bulkActionDto.getIcon());
			Assert.assertEquals(defaultOrder + 1, bulkActionDto.getOrder());
		} finally {
			configurationService.deleteValue(bulkAction.getConfigurationPropertyName(ConfigurationService.PROPERTY_ORDER));
			configurationService.deleteValue(bulkAction.getConfigurationPropertyName(ConfigurationService.PROPERTY_ICON));
			configurationService.deleteValue(bulkAction.getConfigurationPropertyName(ConfigurationService.PROPERTY_LEVEL));
			configurationService.deleteValue(bulkAction.getConfigurationPropertyName(IdmBulkAction.PROPERTY_DELETE_ACTION));
			configurationService.deleteValue(bulkAction.getConfigurationPropertyName(IdmBulkAction.PROPERTY_QUICK_BUTTON));
		}
	}
	
	@Test
	public void testBulkActionWrongLevelConfiguration() {
		//
		try {
			Assert.assertEquals(NotificationLevel.SUCCESS, bulkAction.getLevel());
			Assert.assertNull(bulkAction.getConfigurationValue(ConfigurationService.PROPERTY_LEVEL));
			//
			getHelper().setConfigurationValue(
					bulkAction.getConfigurationPropertyName(ConfigurationService.PROPERTY_LEVEL), 
					"wrong-level");
			//
			IdmBulkActionDto bulkActionDto = manager.toDto(bulkAction);
			Assert.assertEquals(NotificationLevel.SUCCESS, bulkActionDto.getLevel());
		} finally {
			configurationService.deleteValue(bulkAction.getConfigurationPropertyName(ConfigurationService.PROPERTY_LEVEL));
		}
	}
	
	protected List<IdmBulkActionDto> find(BulkActionFilter filter) {
		try {
			String response = getMockMvc().perform(get(BaseController.BASE_PATH + "/bulk-actions")
	        		.with(authentication(getAdminAuthentication()))
	        		.params(toQueryParams(filter))
	                .contentType(TestHelper.HAL_CONTENT_TYPE))
					.andExpect(status().isOk())
	                .andExpect(content().contentType(TestHelper.HAL_CONTENT_TYPE))
	                .andReturn()
	                .getResponse()
	                .getContentAsString();
			//
			return toDtos(response, IdmBulkActionDto.class);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to find entities", ex);
		}
	}
}
