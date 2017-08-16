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
import eu.bcvsolutions.idm.acc.dto.AccTreeAccountDto;
import eu.bcvsolutions.idm.acc.dto.filter.TreeAccountFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccount_;
import eu.bcvsolutions.idm.acc.entity.AccTreeAccount;
import eu.bcvsolutions.idm.acc.entity.AccTreeAccount_;
import eu.bcvsolutions.idm.acc.entity.SysRoleSystem_;
import eu.bcvsolutions.idm.acc.entity.SysSystem_;
import eu.bcvsolutions.idm.acc.repository.AccTreeAccountRepository;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccTreeAccountService;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode_;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * Tree accounts on target system
 * 
 * @author Svanda
 *
 */
@Service("accTreeAccountService")
public class DefaultAccTreeAccountService
		extends AbstractReadWriteDtoService<AccTreeAccountDto, AccTreeAccount, TreeAccountFilter>
		implements AccTreeAccountService {

	private final AccAccountService accountService;

	@Autowired
	public DefaultAccTreeAccountService(AccTreeAccountRepository repository,
			AccAccountService accountService) {
		super(repository);
		//
		Assert.notNull(accountService);
		//
		this.accountService = accountService;
	}

	@Override
	@Transactional
	public void delete(AccTreeAccountDto dto, BasePermission... permission) {
		this.delete(dto, true, permission);
	}

	@Override
	@Transactional
	public void delete(AccTreeAccountDto entity, boolean deleteTargetAccount, BasePermission... permission) {
		Assert.notNull(entity);
		super.delete(entity, permission);

		UUID account = entity.getAccount();
		// We check if exists another (ownership) identityAccounts, if not
		// then
		// we will delete account
		TreeAccountFilter filter = new TreeAccountFilter();
		filter.setAccountId(account);
		filter.setOwnership(Boolean.TRUE);

		List<AccTreeAccountDto> treeAccounts = this.find(filter, null).getContent();
		boolean moreTreeAccounts = treeAccounts.stream().filter(treeAccount -> {
			return treeAccount.isOwnership() && !treeAccount.equals(entity);
		}).findAny().isPresent();

		if (!moreTreeAccounts && entity.isOwnership()) {
			// We delete all tree accounts first
			treeAccounts.forEach(identityAccount -> {
				super.delete(identityAccount);
			});
			// Finally we can delete account
			accountService.delete(accountService.get(account), deleteTargetAccount, entity.getEntity());
		}
	}

	@Override
	protected List<Predicate> toPredicates(Root<AccTreeAccount> root, CriteriaQuery<?> query, CriteriaBuilder builder, TreeAccountFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		if(filter.getAccountId() != null) {
			predicates.add(builder.equal(root.get(AccTreeAccount_.account).get(AccAccount_.id), filter.getAccountId()));
		}
		if(filter.getTreeNodeId() != null) {
			predicates.add(builder.equal(root.get(AccTreeAccount_.treeNode).get(IdmTreeNode_.id), filter.getTreeNodeId()));
		}
		if(filter.getRoleSystemId() != null) {
			predicates.add(builder.equal(root.get(AccTreeAccount_.roleSystem).get(SysRoleSystem_.systemMapping), filter.getRoleSystemId()));
		}
		if(filter.getSystemId() != null) {
			predicates.add(builder.equal(root.get(AccTreeAccount_.account).get(AccAccount_.system).get(SysSystem_.id), filter.getSystemId()));
		}
		if(filter.isOwnership() != null) {
			predicates.add(builder.equal(root.get(AccTreeAccount_.ownership), filter.isOwnership()));
		}
		return predicates;
	}

	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(AccGroupPermission.TREEACCOUNT, getEntityClass());
	}
}
