package eu.bcvsolutions.idm.core.model.service.impl;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.IdmExportImportDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmExportImportFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmImportLogFilter;
import eu.bcvsolutions.idm.core.api.service.AbstractEventableDtoService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmExportImportService;
import eu.bcvsolutions.idm.core.api.service.IdmImportLogService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.ecm.api.service.AttachmentManager;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmExportImport;
import eu.bcvsolutions.idm.core.model.entity.IdmExportImport_;
import eu.bcvsolutions.idm.core.model.repository.IdmExportImportRepository;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.entity.IdmLongRunningTask_;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * CRUD for export and import data
 * 
 * @author Vít Švanda
 *
 */
@Service("exportImportService")
public class DefaultIdmExportImportService 
	extends AbstractEventableDtoService<IdmExportImportDto, IdmExportImport, IdmExportImportFilter>
	implements IdmExportImportService {
	
	@Autowired
	private AttachmentManager attachmentManager;
	@Autowired
	private IdmImportLogService importLogService;

	@Autowired
	public DefaultIdmExportImportService(
			IdmExportImportRepository repository,
			EntityEventManager entityEventManager) {
		super(repository, entityEventManager);
	}
	
	
	@Override
	@Transactional
	public void deleteInternal(IdmExportImportDto dto) {
		Assert.notNull(dto, "Batch cannot be null for delete!");
		Assert.notNull(dto.getId(), "Batch ID cannot be null for delete!");

		// delete attachments
		attachmentManager.deleteAttachments(dto);
		// Delete all logs for this batch.
		IdmImportLogFilter logFilter = new IdmImportLogFilter();
		logFilter.setBatchId(dto.getId());
		importLogService.findIds(logFilter, null)//
				.getContent()//
				.forEach(logId -> {
					importLogService.deleteById(logId);
				});

		super.deleteInternal(dto);
	}
	
	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(CoreGroupPermission.EXPORTIMPORT, getEntityClass());
	}
	
	@Override
	public InputStream download(IdmExportImportDto batch) {
		Assert.notNull(batch, "Batch cannot be null!");
		Assert.notNull(batch.getData(), "Batch must contains attachment ID!");
		
		return attachmentManager.getAttachmentData(batch.getData());
	}
	
	
	@Override
	protected IdmExportImportDto toDto(IdmExportImport entity, IdmExportImportDto dto, IdmExportImportFilter filter) {
		dto = super.toDto(entity, dto, filter);
		if (dto != null && dto.getLongRunningTask() != null) {
			IdmLongRunningTaskDto lrt = DtoUtils.getEmbedded(dto, IdmExportImport_.longRunningTask.getName(),
					IdmLongRunningTaskDto.class);
			if (lrt != null) {
				dto.setResult(lrt.getResult());
			}
		}

		return dto;
	}
	
	@Override
	public boolean supportsToDtoWithFilter() {
		return true;
	}
	
	@Override
	protected List<Predicate> toPredicates(Root<IdmExportImport> root, CriteriaQuery<?> query, CriteriaBuilder builder,
			IdmExportImportFilter filter) {
		List<Predicate> predicates =  super.toPredicates(root, query, builder, filter);
		//
		if (StringUtils.isNotEmpty(filter.getText())) {
			predicates.add(builder.like(builder.lower(root.get(IdmExportImport_.name)), "%" + filter.getText().toLowerCase() + "%"));
		}
		UUID longRunningTaskId = filter.getLongRunningTaskId();
		if (longRunningTaskId != null) {
			predicates.add(builder.equal(root.get(IdmExportImport_.longRunningTask).get(IdmLongRunningTask_.id), longRunningTaskId));
		}
		if (filter.getType() != null) {
			predicates.add(builder.equal(root.get(IdmExportImport_.type), filter.getType()));
		}
		//
		return predicates;
	}
	


}
