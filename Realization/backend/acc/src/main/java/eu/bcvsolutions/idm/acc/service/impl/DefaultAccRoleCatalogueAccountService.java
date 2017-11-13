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
import eu.bcvsolutions.idm.acc.dto.AccRoleCatalogueAccountDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccRoleCatalogueAccountFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccount_;
import eu.bcvsolutions.idm.acc.entity.AccRoleCatalogueAccount;
import eu.bcvsolutions.idm.acc.entity.AccRoleCatalogueAccount_;
import eu.bcvsolutions.idm.acc.entity.SysSystem_;
import eu.bcvsolutions.idm.acc.event.AccountEvent;
import eu.bcvsolutions.idm.acc.event.AccountEvent.AccountEventType;
import eu.bcvsolutions.idm.acc.repository.AccRoleCatalogueAccountRepository;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccRoleCatalogueAccountService;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogue_;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * Role catalogue accounts on target system
 * 
 * @author Svanda
 *
 */
@Service
public class DefaultAccRoleCatalogueAccountService
		extends AbstractReadWriteDtoService<AccRoleCatalogueAccountDto, AccRoleCatalogueAccount, AccRoleCatalogueAccountFilter>
		implements AccRoleCatalogueAccountService {

	private final AccAccountService accountService;

	@Autowired
	public DefaultAccRoleCatalogueAccountService(AccRoleCatalogueAccountRepository repository,
			AccAccountService accountService) {
		super(repository);
		//
		Assert.notNull(accountService);
		//
		this.accountService = accountService;
	}
	
	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(AccGroupPermission.ROLECATALOGUEACCOUNT, getEntityClass());
	}

	@Override
	@Transactional
	public void delete(AccRoleCatalogueAccountDto dto, BasePermission... permission) {
		this.delete(dto, true, permission);
	}

	@Override
	@Transactional
	public void delete(AccRoleCatalogueAccountDto entity, boolean deleteTargetAccount, BasePermission... permission) {
		Assert.notNull(entity);
		super.delete(entity, permission);

		UUID account = entity.getAccount();
		// We check if exists another (ownership) identityAccounts, if not
		// then
		// we will delete account
		AccRoleCatalogueAccountFilter filter = new AccRoleCatalogueAccountFilter();
		filter.setAccountId(account);
		filter.setOwnership(Boolean.TRUE);

		List<AccRoleCatalogueAccountDto> entityAccounts = this.find(filter, null).getContent();
		boolean moreEntityAccounts = entityAccounts.stream().filter(treeAccount -> {
			return treeAccount.isOwnership() && !treeAccount.equals(entity);
		}).findAny().isPresent();

		if (!moreEntityAccounts && entity.isOwnership()) {
			// We delete all tree accounts first
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
	protected List<Predicate> toPredicates(Root<AccRoleCatalogueAccount> root, CriteriaQuery<?> query, CriteriaBuilder builder, AccRoleCatalogueAccountFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		if(filter.getAccountId() != null) {
			predicates.add(builder.equal(root.get(AccRoleCatalogueAccount_.account).get(AccAccount_.id), filter.getAccountId()));
		}
		if(filter.getRoleCatalogueId() != null) {
			predicates.add(builder.equal(root.get(AccRoleCatalogueAccount_.roleCatalogue).get(IdmRoleCatalogue_.id), filter.getRoleCatalogueId()));
		}
		if(filter.getSystemId() != null) {
			predicates.add(builder.equal(root.get(AccRoleCatalogueAccount_.account).get(AccAccount_.system).get(SysSystem_.id), filter.getSystemId()));
		}
		if(filter.isOwnership() != null) {
			predicates.add(builder.equal(root.get(AccRoleCatalogueAccount_.ownership), filter.isOwnership()));
		}
		return predicates;
	}
}
