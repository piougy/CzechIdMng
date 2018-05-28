package eu.bcvsolutions.idm.core.model.repository.filter;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.repository.filter.FilterBuilder;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Filter manager
 * - configured filter
 * - disabled filter
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultFilterManagerIntegrationTest extends AbstractIntegrationTest {
	
	@Autowired private ApplicationContext context;
	@Autowired private ConfigurationService configurationService;
	//
	private DefaultFilterManager filterManager;
	
	@Before
	public void init() {
		filterManager = context.getAutowireCapableBeanFactory().createBean(DefaultFilterManager.class);
	}
	
	@Test
	public void testGetNotExistBuilder() {
		Assert.assertNull(filterManager.getBuilder(IdmIdentity.class, "w-r-o-n-g"));
	}
	
	@Test
	public void testGetDefaultConfiguredSubordinatesBuilder() {
		FilterBuilder<IdmIdentity, DataFilter> filter = filterManager.getBuilder(IdmIdentity.class, IdmIdentityFilter.PARAMETER_SUBORDINATES_FOR);
		//
		Assert.assertNotNull(filter);
		Assert.assertFalse(filter.isDisabled());
	}
	
	@Test
	public void testDisableSubordinatesBuilder() {
		IdmIdentityDto subordinate = getHelper().createIdentity((GuardedString) null);
		IdmIdentityDto manager = getHelper().createIdentity((GuardedString) null);
		getHelper().createContractGuarantee(getHelper().getPrimeContract(subordinate.getId()).getId(), manager.getId());
		//
		FilterBuilder<IdmIdentity, DataFilter> filter = filterManager.getBuilder(IdmIdentity.class, IdmIdentityFilter.PARAMETER_SUBORDINATES_FOR);
		String enabledPropertyName = filter.getConfigurationPropertyName(ConfigurationService.PROPERTY_ENABLED);
		String implProperty = filter.getConfigurationPropertyName(ConfigurationService.PROPERTY_IMPLEMENTATION);
		String currentFilterBeanName = filter.getConfigurationValue(ConfigurationService.PROPERTY_IMPLEMENTATION);
		//
		Assert.assertNotNull(filter);
		Assert.assertFalse(filter.isDisabled());
		IdmIdentityFilter identityFilter = new IdmIdentityFilter();
		identityFilter.setSubordinatesFor(manager.getId());
		identityFilter.setIncludeGuarantees(true);
		List<IdmIdentityDto> subordinates = getHelper().getService(IdmIdentityService.class).find(identityFilter, null).getContent();
		Assert.assertEquals(1, subordinates.size());
		Assert.assertEquals(subordinate.getId(), subordinates.get(0).getId());
		//
		try {
			configurationService.setBooleanValue(enabledPropertyName, false);
			//
			filter = filterManager.getBuilder(IdmIdentity.class, IdmIdentityFilter.PARAMETER_SUBORDINATES_FOR);
			Assert.assertNotNull(filter);
			Assert.assertTrue(filter.isDisabled());
			//
			subordinates = getHelper().getService(IdmIdentityService.class).find(identityFilter, null).getContent();
			Assert.assertTrue(subordinates.isEmpty());
			//
			// configure different filter - should be disabled to
			configurationService.setValue(implProperty, GuaranteeSubordinatesFilter.BEAN_NAME);
			filter = filterManager.getBuilder(IdmIdentity.class, IdmIdentityFilter.PARAMETER_SUBORDINATES_FOR);
			//
			Assert.assertNotNull(filter);
			Assert.assertTrue(filter.isDisabled());
			Assert.assertEquals(GuaranteeSubordinatesFilter.BEAN_NAME, filter.getConfigurationValue(ConfigurationService.PROPERTY_IMPLEMENTATION));
			//
			subordinates = getHelper().getService(IdmIdentityService.class).find(identityFilter, null).getContent();
			Assert.assertTrue(subordinates.isEmpty());
		} finally {
			configurationService.setBooleanValue(enabledPropertyName, true);
			configurationService.setValue(implProperty, currentFilterBeanName);
		}
	}

}
