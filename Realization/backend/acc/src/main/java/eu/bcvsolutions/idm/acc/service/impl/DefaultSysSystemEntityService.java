package eu.bcvsolutions.idm.acc.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.dto.SystemEntityFilter;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystemEntity;
import eu.bcvsolutions.idm.acc.repository.AccAccountRepository;
import eu.bcvsolutions.idm.acc.repository.SysSystemEntityRepository;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityService;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;

/**
 * Entities on target system
 * 
 * @author Radek Tomiška
 *
 */
@Service
public class DefaultSysSystemEntityService extends AbstractReadWriteEntityService<SysSystemEntity, SystemEntityFilter> implements SysSystemEntityService {

	private final AccAccountRepository accountRepository;
	private final SysSystemEntityRepository repository;
	
	@Autowired
	public DefaultSysSystemEntityService(
			SysSystemEntityRepository systemEntityRepository,
			AccAccountRepository accountRepository) {
		super(systemEntityRepository);
		//
		Assert.notNull(accountRepository);
		//
		this.repository = systemEntityRepository;
		this.accountRepository = accountRepository;
	}
	
	@Override
	@Transactional
	public void delete(SysSystemEntity systemEntity) {
		Assert.notNull(systemEntity);
		//
		// clear accounts - only link, can be rebuild
		accountRepository.clearSystemEntity(systemEntity);
		//
		super.delete(systemEntity);
	}

	@Override
	public SysSystemEntity getBySystemAndEntityTypeAndUid(SysSystem system, SystemEntityType entityType, String uid) {
		return repository.findOneBySystemAndEntityTypeAndUid(system, entityType, uid);
	}
}
