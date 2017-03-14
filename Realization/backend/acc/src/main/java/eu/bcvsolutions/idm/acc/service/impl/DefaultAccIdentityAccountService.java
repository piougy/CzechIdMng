package eu.bcvsolutions.idm.acc.service.impl;

import java.io.Serializable;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.dto.filter.IdentityAccountFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.entity.AccIdentityAccount;
import eu.bcvsolutions.idm.acc.repository.AccIdentityAccountRepository;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityAccountService;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityRoleService;

/**
 * Identity accounts on target system
 * 
 * @author Radek Tomiška
 *
 */
@Service
public class DefaultAccIdentityAccountService extends
		AbstractReadWriteEntityService<AccIdentityAccount, IdentityAccountFilter> implements AccIdentityAccountService {

	private final AccAccountService accountService;
	private final IdmIdentityRoleService identityRoleService;

	@Autowired
	public DefaultAccIdentityAccountService(AccIdentityAccountRepository identityAccountRepository,
			AccAccountService accountService, IdmIdentityRoleService identityRoleService) {
		super(identityAccountRepository);
		//
		Assert.notNull(accountService);
		Assert.notNull(identityRoleService);
		//
		this.accountService = accountService;
		this.identityRoleService = identityRoleService;
	}

	@Override
	@Transactional(readOnly = true)
	public AccIdentityAccount get(Serializable id) {
		// I don't want use excerpt, so I have to do manual load account and
		// identityRole
		AccIdentityAccount ia = super.get(id);
		if (ia != null && ia.getAccount() != null) {
			ia.setAccount(accountService.get(ia.getAccount().getId()));
		}
		if (ia != null && ia.getIdentityRole() != null) {
			ia.setIdentityRole(identityRoleService.get(ia.getIdentityRole().getId()));
		}
		return ia;
	}

	@Override
	@Transactional
	public void delete(AccIdentityAccount entity) {
		this.delete(entity, true);
	}

	@Override
	@Transactional
	public void delete(AccIdentityAccount entity, boolean deleteTargetAccount) {
		Assert.notNull(entity);
		super.delete(entity);

		AccAccount account = entity.getAccount();
		// We check if exists another (ownership) identityAccounts, if not
		// then
		// we will delete account
		IdentityAccountFilter filter = new IdentityAccountFilter();
		filter.setAccountId(account.getId());
		filter.setOwnership(Boolean.TRUE);

		List<AccIdentityAccount> identityAccounts = this.find(filter, null).getContent();
		boolean moreIdentityAccounts = identityAccounts.stream().filter(identityAccount -> {
			return identityAccount.isOwnership() && !identityAccount.equals(entity);
		}).findAny().isPresent();

		if (!moreIdentityAccounts && entity.isOwnership()) {
			// We delete all identity accounts first
			identityAccounts.forEach(identityAccount -> {
				super.delete(identityAccount);
			});
			// Finally we can delete account
			accountService.delete(account, deleteTargetAccount);
		}
	}
}
