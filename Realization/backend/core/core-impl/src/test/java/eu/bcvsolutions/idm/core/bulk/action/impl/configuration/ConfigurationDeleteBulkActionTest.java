package eu.bcvsolutions.idm.core.bulk.action.impl.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmConfigurationDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmConfiguration;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.test.api.AbstractBulkActionTest;

/**
 * Integration tests for {@link IdmConfigurationDeleteBulkAction}
 *
 * @author Ondrej Husnik
 *
 */

public class ConfigurationDeleteBulkActionTest extends AbstractBulkActionTest {

	final private String commonNameRoot = "commonNameRoot";
	final int testCount = 5;

	@Before
	public void login() {
		IdmIdentityDto adminIdentity = this.createUserWithAuthorities(IdmBasePermission.DELETE, IdmBasePermission.READ);
		loginAsNoAdmin(adminIdentity.getUsername());
	}

	@After
	public void logout() {
		super.logout();
	}
	
	@Test
	public void processBulkActionByIds() {
		List<IdmConfigurationDto> configs = createConfigurationItems(testCount);

		IdmBulkActionDto bulkAction = this.findBulkAction(IdmConfiguration.class, ConfigurationDeleteBulkAction.NAME);
		Set<UUID> ids = this.getIdFromList(configs);
		bulkAction.setIdentifiers(this.getIdFromList(configs));
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, (long)testCount, null, null);

		for (UUID id : ids) {
			IdmConfigurationDto identityDto = configurationService.get(id);
			assertNull(identityDto);
		}
	}
	
	@Test
	public void processBulkActionByFilter() {
		List<IdmConfigurationDto> configs = createConfigurationItems(testCount);

		DataFilter filter = new DataFilter(IdmConfigurationDto.class);
		filter.setText(commonNameRoot);

		List<IdmConfigurationDto> checkConfigs = configurationService.find(filter, null).getContent();
		assertEquals(testCount, checkConfigs.size());

		IdmBulkActionDto bulkAction = this.findBulkAction(IdmConfiguration.class, ConfigurationDeleteBulkAction.NAME);
		bulkAction.setTransformedFilter(filter);
		bulkAction.setFilter(toMap(filter));
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, (long)testCount, null, null);

		for (IdmConfigurationDto config : configs) {
			IdmConfigurationDto dto = configurationService.get(config.getId());
			assertNull(dto);
		}
	}
	
		
	private List<IdmConfigurationDto> createConfigurationItems(int count) {
		List<IdmConfigurationDto> configurations = new ArrayList<IdmConfigurationDto>();
		for (int i = 0; i < count; ++i) {
			IdmConfigurationDto configuration = new IdmConfigurationDto();
			configuration.setName(commonNameRoot + getHelper().createName());
			configuration = configurationService.save(configuration);
			configurations.add(configuration);
		}
		return configurations;
	}
}
