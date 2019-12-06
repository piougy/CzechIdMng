package eu.bcvsolutions.idm.acc.service.impl;

import java.util.List;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.acc.dto.AccIdentityRoleAccountDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccIdentityRoleAccountFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccount_;
import eu.bcvsolutions.idm.acc.entity.AccIdentityRoleAccount;
import eu.bcvsolutions.idm.acc.entity.AccIdentityRoleAccount_;
import eu.bcvsolutions.idm.acc.entity.SysSystem_;
import eu.bcvsolutions.idm.acc.event.AccountEvent;
import eu.bcvsolutions.idm.acc.event.AccountEvent.AccountEventType;
import eu.bcvsolutions.idm.acc.repository.AccIdentityRoleAccountRepository;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityRoleAccountService;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole_;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * Identity-role-accounts on target system
 * 
 * @author Svanda
 *
 */
@Service
public class DefaultAccIdentityRoleAccountService
		extends AbstractReadWriteDtoService<AccIdentityRoleAccountDto, AccIdentityRoleAccount, AccIdentityRoleAccountFilter>
		implements AccIdentityRoleAccountService {

	private final AccAccountService accountService;

	@Autowired
	public DefaultAccIdentityRoleAccountService(AccIdentityRoleAccountRepository repository, AccAccountService accountService) {
		super(repository);
		//
		Assert.notNull(accountService, "Service is required.");
		//
		this.accountService = accountService;
	}

	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(AccGroupPermission.IDENTITYROLEACCOUNT, getEntityClass());
	}

	@Override
	@Transactional
	public void delete(AccIdentityRoleAccountDto dto, BasePermission... permission) {
		this.delete(dto, true, permission);
	}

	@Override
	@Transactional
	public void delete(AccIdentityRoleAccountDto entity, boolean deleteTargetAccount, BasePermission... permission) {
		Assert.notNull(entity, "Entity is required.");
		super.delete(entity, permission);

		UUID account = entity.getAccount();
		// We check if exists another (ownership) identityAccounts, if not
		// then
		// we will delete account
		AccIdentityRoleAccountFilter filter = new AccIdentityRoleAccountFilter();
		filter.setAccountId(account);
		filter.setOwnership(Boolean.TRUE);

		List<AccIdentityRoleAccountDto> entityAccounts = this.find(filter, null).getContent();
		boolean moreEntityAccounts = entityAccounts.stream().filter(treeAccount -> {
			return treeAccount.isOwnership() && !treeAccount.equals(entity);
		}).findAny().isPresent();

		if (!moreEntityAccounts && entity.isOwnership()) {
			// We delete all entity accounts first
			entityAccounts.forEach(identityAccount -> {
				super.delete(identityAccount);
			});
			// Finally we can delete account
			accountService.publish(new AccountEvent(AccountEventType.DELETE, accountService.get(account),
					ImmutableMap.of(AccAccountService.DELETE_TARGET_ACCOUNT_PROPERTY, deleteTargetAccount,
							AccAccountService.ENTITY_ID_PROPERTY, entity.getEntity())));
		}
	}

	@Override
	protected List<Predicate> toPredicates(Root<AccIdentityRoleAccount> root, CriteriaQuery<?> query,
			CriteriaBuilder builder, AccIdentityRoleAccountFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		//
		if (filter.getAccountId() != null) {
			predicates.add(
					builder.equal(root.get(AccIdentityRoleAccount_.account).get(AccAccount_.id), filter.getAccountId()));
		}
		if (filter.getIdentityRoleId() != null) {
			predicates.add(builder.equal(root.get(AccIdentityRoleAccount_.identityRole).get(IdmIdentityRole_.id),
					filter.getIdentityRoleId()));
		}
		if (filter.getSystemId() != null) {
			predicates
					.add(builder.equal(root.get(AccIdentityRoleAccount_.account).get(AccAccount_.system).get(SysSystem_.id),
							filter.getSystemId()));
		}
		if (filter.isOwnership() != null) {
			predicates.add(builder.equal(root.get(AccIdentityRoleAccount_.ownership), filter.isOwnership()));
		}
		//
		return predicates;
	}
}
