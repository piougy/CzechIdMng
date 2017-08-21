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

import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.acc.dto.AccRoleAccountDto;
import eu.bcvsolutions.idm.acc.dto.filter.RoleAccountFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccount_;
import eu.bcvsolutions.idm.acc.entity.AccRoleAccount;
import eu.bcvsolutions.idm.acc.entity.AccRoleAccount_;
import eu.bcvsolutions.idm.acc.entity.SysSystem_;
import eu.bcvsolutions.idm.acc.repository.AccRoleAccountRepository;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccRoleAccountService;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.model.entity.IdmRole_;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * Role accounts on target system
 * 
 * @author Svanda
 *
 */
@Service
public class DefaultAccRoleAccountService
		extends AbstractReadWriteDtoService<AccRoleAccountDto, AccRoleAccount, RoleAccountFilter>
		implements AccRoleAccountService {

	private final AccAccountService accountService;

	@Autowired
	public DefaultAccRoleAccountService(AccRoleAccountRepository repository,
			AccAccountService accountService) {
		super(repository);
		//
		Assert.notNull(accountService);
		//
		this.accountService = accountService;
	}

	@Override
	@Transactional
	public void delete(AccRoleAccountDto dto, BasePermission... permission) {
		this.delete(dto, true, permission);
	}

	@Override
	@Transactional
	public void delete(AccRoleAccountDto entity, boolean deleteTargetAccount, BasePermission... permission) {
		Assert.notNull(entity);
		super.delete(entity, permission);

		UUID account = entity.getAccount();
		// We check if exists another (ownership) identityAccounts, if not
		// then
		// we will delete account
		RoleAccountFilter filter = new RoleAccountFilter();
		filter.setAccountId(account);
		filter.setOwnership(Boolean.TRUE);

		List<AccRoleAccountDto> entityAccounts = this.find(filter, null).getContent();
		boolean moreEntityAccounts = entityAccounts.stream().filter(treeAccount -> {
			return treeAccount.isOwnership() && !treeAccount.equals(entity);
		}).findAny().isPresent();

		if (!moreEntityAccounts && entity.isOwnership()) {
			// We delete all tree accounts first
			entityAccounts.forEach(identityAccount -> {
				super.delete(identityAccount);
			});
			// Finally we can delete account
			accountService.delete(accountService.get(account), deleteTargetAccount, entity.getEntity());
		}
	}
	
	@Override
	protected List<Predicate> toPredicates(Root<AccRoleAccount> root, CriteriaQuery<?> query, CriteriaBuilder builder,
			RoleAccountFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		if(filter.getAccountId() != null) {
			predicates.add(builder.equal(root.get(AccRoleAccount_.account).get(AccAccount_.id), filter.getAccountId()));
		}
		if(filter.getRoleId() != null) {
			predicates.add(builder.equal(root.get(AccRoleAccount_.role).get(IdmRole_.id), filter.getRoleId()));
		}
		if(filter.getSystemId() != null) {
			predicates.add(builder.equal(root.get(AccRoleAccount_.account).get(AccAccount_.system).get(SysSystem_.id), filter.getSystemId()));
		}
		if(filter.getOwnership() != null) {
			predicates.add(builder.equal(root.get(AccRoleAccount_.ownership), filter.getOwnership()));
		}
		
		return predicates;
	}

	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(AccGroupPermission.ROLEACCOUNT, getEntityClass());
	}
}
