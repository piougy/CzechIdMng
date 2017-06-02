package eu.bcvsolutions.idm.acc.service.impl;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.dto.AccIdentityAccountDto;
import eu.bcvsolutions.idm.acc.dto.filter.IdentityAccountFilter;
import eu.bcvsolutions.idm.acc.entity.AccIdentityAccount;
import eu.bcvsolutions.idm.acc.event.IdentityAccountEvent;
import eu.bcvsolutions.idm.acc.event.IdentityAccountEvent.IdentityAccountEventType;
import eu.bcvsolutions.idm.acc.repository.AccIdentityAccountRepository;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityAccountService;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRoleRepository;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;

/**
 * Identity accounts on target system
 * 
 * @author Radek Tomiška
 * @author Svanda
 *
 */
@Service
public class DefaultAccIdentityAccountService extends
		AbstractReadWriteDtoService<AccIdentityAccountDto, AccIdentityAccount, IdentityAccountFilter> implements AccIdentityAccountService {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultAccIdentityAccountService.class);
	
	private final AccAccountService accountService;
	private final IdmIdentityRoleRepository identityRoleRepository;
	private final EntityEventManager entityEventManager;

	@Autowired
	public DefaultAccIdentityAccountService(
			AccIdentityAccountRepository identityAccountRepository,
			AccAccountService accountService,
			IdmIdentityRoleRepository identityRoleRepository,
			EntityEventManager entityEventManager) {
		super(identityAccountRepository);
		//
		Assert.notNull(accountService);
		Assert.notNull(identityRoleRepository);
		Assert.notNull(entityEventManager);
		//
		this.accountService = accountService;
		this.identityRoleRepository = identityRoleRepository;
		this.entityEventManager = entityEventManager;
	}

	@Override
	@Transactional(readOnly = true)
	public AccIdentityAccount getEntity(Serializable id, BasePermission... permission) {
		// I don't want use excerpt, so I have to do manual load account and
		// identityRole
		AccIdentityAccount ia = super.getEntity(id, permission);
		if (ia != null && ia.getAccount() != null) {
			ia.setAccount(accountService.get(ia.getAccount().getId()));
		}
		if (ia != null && ia.getIdentityRole() != null) {
			ia.setIdentityRole(identityRoleRepository.findOne(ia.getIdentityRole().getId()));
		}
		return ia;
	}
	
	@Override
	public AccIdentityAccountDto save(AccIdentityAccountDto dto, BasePermission... permission) {
		Assert.notNull(dto);
		checkAccess(toEntity(dto, null), permission);
		//
		LOG.debug("Saving identity-account [{}]", dto);
		//
		if (isNew(dto)) { // create
			return entityEventManager.process(new IdentityAccountEvent(IdentityAccountEventType.CREATE, dto)).getContent();
		}
		return entityEventManager.process(new IdentityAccountEvent(IdentityAccountEventType.UPDATE, dto)).getContent();
	}
	

	@Override
	@Transactional
	public void delete(AccIdentityAccountDto dto, BasePermission... permission) {
		this.delete(dto, true, permission);
	}

	@Override
	@Transactional
	public void delete(AccIdentityAccountDto entity, boolean deleteTargetAccount, BasePermission... permission) {
		Assert.notNull(entity);
		checkAccess(this.getEntity(entity.getId()), permission);
		//
		LOG.debug("Deleting identity account [{}]", entity);
		entityEventManager.process(new IdentityAccountEvent(IdentityAccountEventType.DELETE, entity,
				ImmutableMap.of(AccIdentityAccountService.DELETE_TARGET_ACCOUNT_KEY, deleteTargetAccount)));
	}
}
