package eu.bcvsolutions.idm.core.bulk.action.impl.role;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Sets;

import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.domain.ExportImportType;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.IdmConfigurationDto;
import eu.bcvsolutions.idm.core.api.dto.IdmExportImportDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmExportImportFilter;
import eu.bcvsolutions.idm.core.api.service.IdmConfigurationService;
import eu.bcvsolutions.idm.core.api.service.IdmExportImportService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.api.service.ImportManager;
import eu.bcvsolutions.idm.core.bulk.action.impl.configuration.ConfigurationExportBulkAction;
import eu.bcvsolutions.idm.core.ecm.api.dto.IdmAttachmentDto;
import eu.bcvsolutions.idm.core.ecm.api.service.AttachmentManager;
import eu.bcvsolutions.idm.core.model.entity.IdmConfiguration;
import eu.bcvsolutions.idm.core.model.entity.IdmExportImport;
import eu.bcvsolutions.idm.test.api.AbstractBulkActionTest;

/**
 * Export configuration integration test
 * 
 * @author Vít Švanda
 *
 */
public class ConfigurationExportBulkActionIntegrationTest extends AbstractBulkActionTest {

	@Autowired
	private IdmRoleService roleService;
	@Autowired
	private IdmExportImportService exportImportService;
	@Autowired
	private AttachmentManager attachmentManager;
	@Autowired
	private ImportManager importManager;
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
		executeExportAndImport(originalConfiguration);

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
		executeExportAndImport(originalConfiguration);

		IdmConfigurationDto newConfiguration = configurationService.get(originalConfiguration.getId());
		Assert.assertNotNull(newConfiguration);
		Assert.assertEquals(null, newConfiguration.getValue());
	}

	private IdmExportImportDto executeExportAndImport(IdmConfigurationDto configuration) {
		String batchName = getHelper().createName();
		IdmBulkActionDto bulkAction = findBulkAction(IdmConfiguration.class, ConfigurationExportBulkAction.NAME);
		bulkAction.setIdentifiers(Sets.newHashSet(configuration.getId()));
		bulkAction.getProperties().put(ConfigurationExportBulkAction.PROPERTY_NAME, batchName);
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);

		checkResultLrt(processAction, 1l, null, null);

		IdmExportImportFilter exportImportFilter = new IdmExportImportFilter();
		exportImportFilter.setText(batchName);
		List<IdmExportImportDto> batches = exportImportService.find(exportImportFilter, null).getContent();
		Assert.assertEquals(1, batches.size());
		IdmExportImportDto batch = batches.get(0);
		Assert.assertEquals(OperationState.EXECUTED, batch.getResult().getState());
		Assert.assertNotNull(batch.getData());

		List<IdmAttachmentDto> attachments = attachmentManager//
				.getAttachments(batch.getId(), IdmExportImport.class.getCanonicalName(), null)//
				.getContent();//
		Assert.assertEquals(1, attachments.size());
		IdmAttachmentDto attachment = attachments.get(0);

		// Upload import
		IdmExportImportDto importBatch = importManager.uploadImport(attachment.getName(), attachment.getName(),
				attachmentManager.getAttachmentData(attachment.getId()));
		Assert.assertNotNull(importBatch);
		Assert.assertEquals(batch.getName(), importBatch.getName());
		Assert.assertEquals(ExportImportType.IMPORT, importBatch.getType());

		// Delete original configuration
		configurationService.delete(configuration);
		Assert.assertNull(roleService.get(configuration.getId()));

		// Execute import
		importBatch = importManager.executeImport(importBatch, false);
		Assert.assertNotNull(importBatch);
		Assert.assertEquals(batch.getName(), importBatch.getName());
		Assert.assertEquals(ExportImportType.IMPORT, importBatch.getType());
		Assert.assertEquals(OperationState.EXECUTED, importBatch.getResult().getState());
		return importBatch;
	}
}
