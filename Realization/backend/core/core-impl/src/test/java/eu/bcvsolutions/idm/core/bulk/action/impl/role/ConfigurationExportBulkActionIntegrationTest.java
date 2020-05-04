package eu.bcvsolutions.idm.core.bulk.action.impl.role;


import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;


import eu.bcvsolutions.idm.core.api.dto.IdmConfigurationDto;
import eu.bcvsolutions.idm.core.api.service.IdmConfigurationService;
import eu.bcvsolutions.idm.core.bulk.action.impl.configuration.ConfigurationExportBulkAction;
import eu.bcvsolutions.idm.test.api.AbstractExportBulkActionTest;

/**
 * Export configuration integration test
 * 
 * @author Vít Švanda
 *
 */
public class ConfigurationExportBulkActionIntegrationTest extends AbstractExportBulkActionTest {

	@Autowired
	private IdmConfigurationService configurationService;

	@Before
	public void login() {
		loginAsAdmin();
	}

	@After
	public void logout() {
		super.logout();
	}

	@Test
	public void testExportAndImportConfiguration() {
		IdmConfigurationDto originalConfiguration = new IdmConfigurationDto(this.getHelper().createName(),
				this.getHelper().createName());
		originalConfiguration = configurationService.save(originalConfiguration);

		// Make export, upload and import
		executeExportAndImport(originalConfiguration, ConfigurationExportBulkAction.NAME);

		originalConfiguration = configurationService.get(originalConfiguration.getId());
		Assert.assertNotNull(originalConfiguration);
	}
	
	@Test
	public void testExportAndImportConfigurationConfidential() {
		IdmConfigurationDto originalConfiguration = new IdmConfigurationDto(this.getHelper().createName(),
				this.getHelper().createName());
		originalConfiguration.setConfidential(true);
		originalConfiguration = configurationService.save(originalConfiguration);

		// Make export, upload and import
		executeExportAndImport(originalConfiguration, ConfigurationExportBulkAction.NAME);

		IdmConfigurationDto newConfiguration = configurationService.get(originalConfiguration.getId());
		Assert.assertNotNull(newConfiguration);
		Assert.assertEquals(null, newConfiguration.getValue());
	}

}
