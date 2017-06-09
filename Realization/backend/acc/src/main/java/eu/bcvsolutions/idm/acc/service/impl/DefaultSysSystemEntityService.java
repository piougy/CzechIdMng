package eu.bcvsolutions.idm.acc.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.dto.filter.SystemEntityFilter;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystemEntity;
import eu.bcvsolutions.idm.acc.repository.AccAccountRepository;
import eu.bcvsolutions.idm.acc.repository.SysProvisioningOperationRepository;
import eu.bcvsolutions.idm.acc.repository.SysSystemEntityRepository;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityService;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;

/**
 * Entities on target system
 * 
 * @author Radek Tomiška
 *
 */
@Service("sysSystemEntityService")
public class DefaultSysSystemEntityService extends AbstractReadWriteEntityService<SysSystemEntity, SystemEntityFilter> implements SysSystemEntityService {

	private final SysSystemEntityRepository repository;
	private final AccAccountRepository accountRepository;
	private final SysProvisioningOperationRepository provisioningOperationRepository;
	
	@Autowired
	public DefaultSysSystemEntityService(
			SysSystemEntityRepository systemEntityRepository,
			AccAccountRepository accountRepository,
			SysProvisioningOperationRepository provisioningOperationRepository) {
		super(systemEntityRepository);
		//
		Assert.notNull(accountRepository);
		Assert.notNull(provisioningOperationRepository);
		//
		this.repository = systemEntityRepository;
		this.accountRepository = accountRepository;
		this.provisioningOperationRepository = provisioningOperationRepository;
	}
	
	@Override
	@Transactional
	public void delete(SysSystemEntity systemEntity) {
		Assert.notNull(systemEntity);
		//
		if (provisioningOperationRepository.countBySystemEntity(systemEntity) > 0) {
			throw new ResultCodeException(AccResultCode.SYSTEM_ENTITY_DELETE_FAILED_HAS_OPERATIONS, ImmutableMap.of("uid", systemEntity.getUid(), "system", systemEntity.getSystem().getName()));
		}
		//
		// clear accounts - only link, can be rebuild
		accountRepository.clearSystemEntity(systemEntity);
		//
		super.delete(systemEntity);
	}

	@Override
	public SysSystemEntity getBySystemAndEntityTypeAndUid(SysSystem system, SystemEntityType entityType, String uid) {
		return repository.findOneBySystem_IdAndEntityTypeAndUid(system.getId(), entityType, uid);
	}
}
