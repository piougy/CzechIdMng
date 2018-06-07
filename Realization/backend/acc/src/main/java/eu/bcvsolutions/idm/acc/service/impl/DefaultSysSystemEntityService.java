package eu.bcvsolutions.idm.acc.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
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
import eu.bcvsolutions.idm.acc.dto.SysProvisioningOperationDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemEntityDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysProvisioningOperationFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemEntityFilter;
import eu.bcvsolutions.idm.acc.entity.SysSystemEntity;
import eu.bcvsolutions.idm.acc.entity.SysSystemEntity_;
import eu.bcvsolutions.idm.acc.repository.AccAccountRepository;
import eu.bcvsolutions.idm.acc.repository.SysProvisioningOperationRepository;
import eu.bcvsolutions.idm.acc.repository.SysSystemEntityRepository;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.ic.api.IcConnectorObject;

/**
 * Entities on target system
 * 
 * @author Radek Tomiška
 *
 */
@Service("sysSystemEntityService")
public class DefaultSysSystemEntityService
		extends AbstractReadWriteDtoService<SysSystemEntityDto, SysSystemEntity, SysSystemEntityFilter>
		implements SysSystemEntityService {

	private final SysSystemEntityRepository repository;
	private final AccAccountRepository accountRepository;
	private final SysProvisioningOperationRepository provisioningOperationRepository;
	private final SysSystemService systemService;

	@Autowired
	public DefaultSysSystemEntityService(SysSystemEntityRepository systemEntityRepository,
			AccAccountRepository accountRepository, SysProvisioningOperationRepository provisioningOperationRepository,
			SysSystemService systemService) {
		super(systemEntityRepository);
		//
		Assert.notNull(accountRepository);
		Assert.notNull(provisioningOperationRepository);
		Assert.notNull(systemService);
		//
		this.repository = systemEntityRepository;
		this.accountRepository = accountRepository;
		this.provisioningOperationRepository = provisioningOperationRepository;
		this.systemService = systemService;
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
	public void delete(SysSystemEntityDto systemEntityDto, BasePermission... permission) {
		Assert.notNull(systemEntityDto);
		//
		SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
		filter.setSystemId(systemEntityDto.getSystem());
		filter.setEntityType(systemEntityDto.getEntityType());
		filter.setSystemEntity(systemEntityDto.getId());
		if (provisioningOperationRepository.find(filter, null).getTotalElements() > 0) {
			SysSystemDto system = DtoUtils.getEmbedded(systemEntityDto, SysSystemEntity_.system);
			throw new ResultCodeException(AccResultCode.SYSTEM_ENTITY_DELETE_FAILED_HAS_OPERATIONS,
					ImmutableMap.of("uid", systemEntityDto.getUid(), "system", system.getName()));
		}
		//
		// clear accounts - only link, can be rebuild
		accountRepository.clearSystemEntity(systemEntityDto.getId());
		//
		super.delete(systemEntityDto, permission);
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

		return this.systemService.readConnectorObject(systemEntity.getSystem(), systemEntity.getUid(), null);
	}
}
