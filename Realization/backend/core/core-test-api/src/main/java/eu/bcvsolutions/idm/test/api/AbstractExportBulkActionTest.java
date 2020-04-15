package eu.bcvsolutions.idm.test.api;

import java.util.List;
import java.util.function.Consumer;

import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Sets;
import java.util.Map;

import eu.bcvsolutions.idm.core.api.bulk.action.AbstractExportBulkAction;
import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.domain.ExportImportType;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.IdmExportImportDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmExportImportFilter;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.service.IdmExportImportService;
import eu.bcvsolutions.idm.core.api.service.ImportManager;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.ecm.api.dto.IdmAttachmentDto;
import eu.bcvsolutions.idm.core.ecm.api.service.AttachmentManager;

/**
 * Abstract class for testing export and import bulk actions
 *
 * @author Ondrej Husnik
 *
 */

public class AbstractExportBulkActionTest extends AbstractBulkActionTest {

	@Autowired
	private IdmExportImportService exportImportService;
	@Autowired
	private AttachmentManager attachmentManager;
	@Autowired
	private ImportManager importManager;
	
	public static final String EXECUTE_BEFORE_DTO_DELETE = "EXECUTE_BEFORE_DTO_DELETE";

	
	/**
	 * Provides export and following import operation for supplied dto
	 * 
	 * @param dto 
	 * @param actionName
	 * @return
	 */
	protected <DTO extends AbstractDto> IdmExportImportDto executeExportAndImport(DTO dto, String actionName) {
		return executeExportAndImport(dto, actionName, null);
	}

	/**
	 * Provides export and following import operation for supplied dto
	 * It accepts a map of methods in order to supply
	 * necessary operation between individual steps. 
	 * 
	 * @param dto
	 * @param actionName
	 * @param execute
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected <DTO extends AbstractDto> IdmExportImportDto executeExportAndImport(DTO dto, String actionName, Map<String, Consumer<DTO>> execute) {
		String batchName = getHelper().createName();
		Class<? extends BaseEntity> entityClass = getLookupService().getEntityClass(dto.getClass());

		// Bulk action preparation
		IdmBulkActionDto bulkAction = findBulkAction(entityClass, actionName);
		bulkAction.setIdentifiers(Sets.newHashSet(dto.getId()));
		bulkAction.getProperties().put(AbstractExportBulkAction.PROPERTY_NAME, batchName);
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, 1l, null, null);

		// Export batch is created
		IdmExportImportFilter exportImportFilter = new IdmExportImportFilter();
		exportImportFilter.setText(batchName);
		List<IdmExportImportDto> batches = exportImportService.find(exportImportFilter, null).getContent();
		Assert.assertEquals(1, batches.size());
		IdmExportImportDto batch = batches.get(0);
		Assert.assertEquals(OperationState.EXECUTED, batch.getResult().getState());
		Assert.assertNotNull(batch.getData());

		// Find export batch as attachment
		List<IdmAttachmentDto> attachments = attachmentManager//
				.getAttachments(batch.getId(),getLookupService().getEntityClass(IdmExportImportDto.class).getCanonicalName(), null)//
				.getContent();//
		Assert.assertEquals(1, attachments.size());
		IdmAttachmentDto attachment = attachments.get(0);

		// Upload import
		IdmExportImportDto importBatch = importManager.uploadImport(attachment.getName(), attachment.getName(),
				attachmentManager.getAttachmentData(attachment.getId()));
		Assert.assertNotNull(importBatch);
		Assert.assertEquals(batch.getName(), importBatch.getName());
		Assert.assertEquals(ExportImportType.IMPORT, importBatch.getType());

		// Get a service corresponding to the DTO type
		ReadWriteDtoService<BaseDto, BaseFilter> service = ((ReadWriteDtoService<BaseDto, BaseFilter>) getLookupService()
				.getDtoService(dto.getClass()));
		
		// Execute supplied action before original dto deletion
		if (execute != null && execute.containsKey(EXECUTE_BEFORE_DTO_DELETE)) {
			execute.get(EXECUTE_BEFORE_DTO_DELETE).accept(dto);
		}
		
		// Original dto deletion
		service.delete(dto);
		Assert.assertNull(service.get(dto.getId()));

		// Execute import
		importBatch = importManager.executeImport(importBatch, false);
		Assert.assertNotNull(importBatch);
		Assert.assertEquals(batch.getName(), importBatch.getName());
		Assert.assertEquals(ExportImportType.IMPORT, importBatch.getType());
		Assert.assertEquals(OperationState.EXECUTED, importBatch.getResult().getState());
		return importBatch;
	}
}
