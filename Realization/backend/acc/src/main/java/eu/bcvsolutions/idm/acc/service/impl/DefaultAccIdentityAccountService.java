package eu.bcvsolutions.idm.acc.service.impl;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.dto.AccIdentityAccountDto;
import eu.bcvsolutions.idm.acc.dto.filter.IdentityAccountFilter;
import eu.bcvsolutions.idm.acc.entity.AccIdentityAccount;
import eu.bcvsolutions.idm.acc.repository.AccIdentityAccountRepository;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityAccountService;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
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

	private final AccAccountService accountService;
	private final IdmIdentityRoleRepository identityRoleRepository;

	@Autowired
	public DefaultAccIdentityAccountService(AccIdentityAccountRepository identityAccountRepository,
			AccAccountService accountService, IdmIdentityRoleRepository identityRoleRepository) {
		super(identityAccountRepository);
		//
		Assert.notNull(accountService);
		Assert.notNull(identityRoleRepository);
		//
		this.accountService = accountService;
		this.identityRoleRepository = identityRoleRepository;
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
	@Transactional
	public void delete(AccIdentityAccountDto dto, BasePermission... permission) {
		this.delete(dto, true, permission);
	}

	@Override
	@Transactional
	public void delete(AccIdentityAccountDto entity, boolean deleteTargetAccount, BasePermission... permission) {
		Assert.notNull(entity);
		super.delete(entity, permission);

		UUID account = entity.getAccount();
		// We check if exists another (ownership) identityAccounts, if not
		// then
		// we will delete account
		IdentityAccountFilter filter = new IdentityAccountFilter();
		filter.setAccountId(account);
		filter.setOwnership(Boolean.TRUE);

		List<AccIdentityAccountDto> identityAccounts = this.find(filter, null).getContent();
		boolean moreIdentityAccounts = identityAccounts.stream().filter(identityAccount -> {
			return identityAccount.isOwnership() && !identityAccount.equals(entity);
		}).findAny().isPresent();

		if (!moreIdentityAccounts && entity.isOwnership()) {
			// We delete all identity accounts first
			identityAccounts.forEach(identityAccount -> {
				super.delete(identityAccount);
			});
			// Finally we can delete account
			accountService.delete(accountService.get(account), deleteTargetAccount);
		}
	}
}
