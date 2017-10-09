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
import eu.bcvsolutions.idm.acc.dto.AccContractAccountDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccContractAccountFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccount_;
import eu.bcvsolutions.idm.acc.entity.AccContractAccount;
import eu.bcvsolutions.idm.acc.entity.AccContractAccount_;
import eu.bcvsolutions.idm.acc.entity.SysSystem_;
import eu.bcvsolutions.idm.acc.repository.AccContractAccountRepository;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccContractAccountService;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract_;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * Contract-accounts on target system
 * 
 * @author Svanda
 *
 */
@Service
public class DefaultAccContractAccountService
		extends AbstractReadWriteDtoService<AccContractAccountDto, AccContractAccount, AccContractAccountFilter>
		implements AccContractAccountService {

	private final AccAccountService accountService;

	@Autowired
	public DefaultAccContractAccountService(AccContractAccountRepository repository,
			AccAccountService accountService) {
		super(repository);
		//
		Assert.notNull(accountService);
		//
		this.accountService = accountService;
	}
	
	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(AccGroupPermission.CONTRACTACCOUNT, getEntityClass());
	}

	@Override
	@Transactional
	public void delete(AccContractAccountDto dto, BasePermission... permission) {
		this.delete(dto, true, permission);
	}

	@Override
	@Transactional
	public void delete(AccContractAccountDto entity, boolean deleteTargetAccount, BasePermission... permission) {
		Assert.notNull(entity);
		super.delete(entity, permission);

		UUID account = entity.getAccount();
		// We check if exists another (ownership) identityAccounts, if not
		// then
		// we will delete account
		AccContractAccountFilter filter = new AccContractAccountFilter();
		filter.setAccountId(account);
		filter.setOwnership(Boolean.TRUE);

		List<AccContractAccountDto> entityAccounts = this.find(filter, null).getContent();
		boolean moreEntityAccounts = entityAccounts.stream().filter(treeAccount -> {
			return treeAccount.isOwnership() && !treeAccount.equals(entity);
		}).findAny().isPresent();

		if (!moreEntityAccounts && entity.isOwnership()) {
			// We delete all entity accounts first
			entityAccounts.forEach(identityAccount -> {
				super.delete(identityAccount);
			});
			// Finally we can delete account
			accountService.delete(accountService.get(account), deleteTargetAccount, entity.getEntity());
		}
	}
	
	@Override
	protected List<Predicate> toPredicates(Root<AccContractAccount> root, CriteriaQuery<?> query, CriteriaBuilder builder,
			AccContractAccountFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		//
		if(filter.getAccountId() != null) {
			predicates.add(builder.equal(root.get(AccContractAccount_.account).get(AccAccount_.id), filter.getAccountId()));
		}
		if(filter.getContractId() != null) {
			predicates.add(builder.equal(root.get(AccContractAccount_.contract).get(IdmIdentityContract_.id), filter.getContractId()));
		}
		if(filter.getSystemId() != null) {
			predicates.add(builder.equal(root.get(AccContractAccount_.account).get(AccAccount_.system).get(SysSystem_.id), filter.getSystemId()));
		}
		if(filter.isOwnership() != null) {
			predicates.add(builder.equal(root.get(AccContractAccount_.ownership), filter.isOwnership()));
		}
		//
		return predicates;
	}
}
