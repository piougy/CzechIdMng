package eu.bcvsolutions.idm.acc.service.impl;

import java.io.Serializable;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.acc.dto.AccIdentityAccountDto;
import eu.bcvsolutions.idm.acc.dto.filter.IdentityAccountFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccount_;
import eu.bcvsolutions.idm.acc.entity.AccIdentityAccount;
import eu.bcvsolutions.idm.acc.entity.AccIdentityAccount_;
import eu.bcvsolutions.idm.acc.entity.SysRoleSystem_;
import eu.bcvsolutions.idm.acc.entity.SysSystem_;
import eu.bcvsolutions.idm.acc.event.IdentityAccountEvent;
import eu.bcvsolutions.idm.acc.event.IdentityAccountEvent.IdentityAccountEventType;
import eu.bcvsolutions.idm.acc.repository.AccAccountRepository;
import eu.bcvsolutions.idm.acc.repository.AccIdentityAccountRepository;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityAccountService;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.model.entity.IdmRole_;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRoleRepository;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * Identity accounts on target system
 * 
 * @author Radek Tomiška
 * @author Svanda
 *
 */
@Service("accIdentityAccountService")
public class DefaultAccIdentityAccountService extends
		AbstractReadWriteDtoService<AccIdentityAccountDto, AccIdentityAccount, IdentityAccountFilter> implements AccIdentityAccountService {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultAccIdentityAccountService.class);
	
	private final IdmIdentityRoleRepository identityRoleRepository;
	private final EntityEventManager entityEventManager;
	private final AccAccountRepository accountRepository;

	@Autowired
	public DefaultAccIdentityAccountService(
			AccIdentityAccountRepository identityAccountRepository,
			IdmIdentityRoleRepository identityRoleRepository,
			EntityEventManager entityEventManager,
			AccAccountRepository accountRepository) {
		super(identityAccountRepository);
		//
		Assert.notNull(identityRoleRepository);
		Assert.notNull(entityEventManager);
		Assert.notNull(accountRepository);
		//
		this.identityRoleRepository = identityRoleRepository;
		this.entityEventManager = entityEventManager;
		this.accountRepository = accountRepository;
	}
	
	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(AccGroupPermission.IDENTITYACCOUNT, getEntityClass());
	}

	@Override
	@Transactional(readOnly = true)
	public AccIdentityAccount getEntity(Serializable id, BasePermission... permission) {
		// I don't want use excerpt, so I have to do manual load account and
		// identityRole
		AccIdentityAccount ia = super.getEntity(id, permission);
		if (ia != null && ia.getAccount() != null) {
			ia.setAccount(accountRepository.findOne(ia.getAccount().getId()));
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
		this.delete(entity, deleteTargetAccount, false, permission);
	}
	
	@Override
	@Transactional
	public void forceDelete(AccIdentityAccountDto dto, BasePermission... permission) {
		this.delete(dto, true, true,permission);
	}

	private void delete(AccIdentityAccountDto entity, boolean deleteTargetAccount, boolean forceDelete, BasePermission... permission) {
		Assert.notNull(entity);
		checkAccess(this.getEntity(entity.getId()), permission);
		//
		LOG.debug("Deleting identity account [{}]", entity);
		entityEventManager.process(new IdentityAccountEvent(IdentityAccountEventType.DELETE, entity,
				ImmutableMap.of(AccIdentityAccountService.DELETE_TARGET_ACCOUNT_KEY, deleteTargetAccount,
						AccIdentityAccountService.FORCE_DELETE_OF_IDENTITY_ACCOUNT_KEY, forceDelete)));
	}
	
	@Override
	protected List<Predicate> toPredicates(Root<AccIdentityAccount> root, CriteriaQuery<?> query,
			CriteriaBuilder builder, IdentityAccountFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		if (filter.getAccountId() != null) {
			predicates.add(builder.equal(root.get(AccIdentityAccount_.account).get(AccAccount_.id), filter.getAccountId()));
		}
		if (filter.getSystemId() != null) {
			predicates.add(builder.equal(root.get(AccIdentityAccount_.account).get(AccAccount_.system).get(SysSystem_.id), filter.getSystemId()));
		}	
		if (filter.getIdentityId() != null) {
			predicates.add(builder.equal(root.get(AccIdentityAccount_.identity).get(IdmIdentity_.id), filter.getIdentityId()));
		}		
		if (filter.getRoleId() != null || filter.getIdentityRoleId() != null) {
			Join<AccIdentityAccount, IdmIdentityRole> identityRole = root.join(AccIdentityAccount_.identityRole, JoinType.LEFT);
			if (filter.getRoleId() != null) {
				predicates.add(builder.equal(identityRole.get(IdmIdentityRole_.role).get(IdmRole_.id), filter.getRoleId()));
			}
			if (filter.getIdentityRoleId() != null) {
				predicates.add(builder.equal(identityRole.get(IdmIdentityRole_.id), filter.getIdentityRoleId()));
			}
		}
		if (filter.getRoleSystemId() != null) {
			predicates.add(builder.equal(root.get(AccIdentityAccount_.roleSystem).get(SysRoleSystem_.id), filter.getRoleSystemId()));
		}
		if (filter.isOwnership() != null) {
			predicates.add(builder.equal(root.get(AccIdentityAccount_.ownership), filter.isOwnership()));
		}
		//
		return predicates;
	}
}
