package eu.bcvsolutions.idm.acc.service.impl;

import eu.bcvsolutions.idm.acc.service.api.SysSchemaObjectClassService;
import eu.bcvsolutions.idm.ic.api.IcObjectClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.domain.ProvisioningOperation;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningArchiveDto;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningBatchDto;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningOperationDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemEntityDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysProvisioningOperationFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemEntityFilter;
import eu.bcvsolutions.idm.acc.entity.SysSystemEntity;
import eu.bcvsolutions.idm.acc.entity.SysSystemEntity_;
import eu.bcvsolutions.idm.acc.repository.SysSystemEntityRepository;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningBatchService;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningOperationService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.ic.api.IcConnectorObject;

/**
 * Entities on target system.
 * 
 * @author Radek Tomiška
 *
 */
@Service("sysSystemEntityService")
public class DefaultSysSystemEntityService
		extends AbstractReadWriteDtoService<SysSystemEntityDto, SysSystemEntity, SysSystemEntityFilter>
		implements SysSystemEntityService {

	private final SysSystemEntityRepository repository;
	//
	// TODO: after transformation to events can be this removed
	@Autowired @Lazy private SysProvisioningOperationService provisioningOperationService;
	@Autowired @Lazy private AccAccountService accountService;
	@Autowired @Lazy private SysSchemaObjectClassService schemaService;
	@Autowired private SysProvisioningBatchService batchService;
	@Autowired private SysSystemService systemService;

	@Autowired
	public DefaultSysSystemEntityService(SysSystemEntityRepository systemEntityRepository) {
		super(systemEntityRepository);
		//
		this.repository = systemEntityRepository;
	}

	@Override
	protected Page<SysSystemEntity> findEntities(SysSystemEntityFilter filter, Pageable pageable,
			BasePermission... permission) {
		if (filter == null) {
			return repository.findAll(pageable);
		}
		return repository.find(filter, pageable);
	}

	@Override
	@Transactional
	public void delete(SysSystemEntityDto systemEntity, BasePermission... permission) {
		Assert.notNull(systemEntity, "System entity is required.");
		//
		SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
		filter.setSystemId(systemEntity.getSystem());
		filter.setEntityType(systemEntity.getEntityType());
		filter.setSystemEntity(systemEntity.getId());
		// TODO: transform this behavior to events
		if (provisioningOperationService.count(filter) > 0) {
			SysSystemDto system = DtoUtils.getEmbedded(systemEntity, SysSystemEntity_.system);
			throw new ResultCodeException(AccResultCode.SYSTEM_ENTITY_DELETE_FAILED_HAS_OPERATIONS,
					ImmutableMap.of("uid", systemEntity.getUid(), "system", system.getName()));
		}
		//
		// clear accounts - only link, can be rebuild
		AccAccountFilter accountFilter = new AccAccountFilter();
		accountFilter.setSystemEntityId(systemEntity.getId());
		accountService
			.find(accountFilter, null)
			.forEach(account -> {
				account.setSystemEntity(null);
				accountService.save(account);
			});
		//
		// clear batches
		SysProvisioningBatchDto batch = batchService.findBatch(systemEntity.getId());
		if (batch != null) {
			batchService.delete(batch);
		}
		//
		super.delete(systemEntity, permission);
	}

	@Override
	@Transactional(readOnly = true)
	public SysSystemEntityDto getBySystemAndEntityTypeAndUid(SysSystemDto system, SystemEntityType entityType,
			String uid) {
		return toDto(repository.findOneBySystem_IdAndEntityTypeAndUid(system.getId(), entityType, uid));
	}

	@Override
	@Transactional(readOnly = true)
	public SysSystemEntityDto getByProvisioningOperation(ProvisioningOperation operation) {
		if (operation instanceof SysProvisioningOperationDto) {
			return this.get(((SysProvisioningOperationDto) operation).getSystemEntity());
		}
		if (operation instanceof SysProvisioningArchiveDto) {
			return toDto(repository.findOneBySystem_IdAndEntityTypeAndUid(operation.getSystem(),
					operation.getEntityType(), ((SysProvisioningArchiveDto) operation).getSystemEntityUid()));
		}
		return null;
	}

	@Override
	public IcConnectorObject getConnectorObject(SysSystemEntityDto systemEntity, BasePermission... permissions) {
		Assert.notNull(systemEntity, "System entity cannot be null!");
		this.checkAccess(systemEntity, permissions);
		// Find first mapping for entity type and system, from the account and return his object class.
		IcObjectClass icObjectClass = schemaService.findByAccount(systemEntity.getSystem(), systemEntity.getEntityType());
		
		return this.systemService.readConnectorObject(systemEntity.getSystem(), systemEntity.getUid(), icObjectClass);
	}
}
