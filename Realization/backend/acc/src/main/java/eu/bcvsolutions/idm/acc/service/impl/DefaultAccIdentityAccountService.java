package eu.bcvsolutions.idm.acc.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.dto.IdentityAccountFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.entity.AccIdentityAccount;
import eu.bcvsolutions.idm.acc.repository.AccIdentityAccountRepository;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityAccountService;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningService;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;

/**
 * Identity accounts on target system
 * 
 * @author Radek Tomiška
 *
 */
@Service
public class DefaultAccIdentityAccountService
		extends AbstractReadWriteEntityService<AccIdentityAccount, IdentityAccountFilter>
		implements AccIdentityAccountService {

	private AccIdentityAccountRepository identityAccountRepository;
	private AccAccountService accountService;
	
	@Autowired
	private ApplicationContext applicationContext;
	private SysProvisioningService provisioningService;

	@Autowired
	public DefaultAccIdentityAccountService(AccIdentityAccountRepository identityAccountRepository,
			AccAccountService accountService) {
		super();
		Assert.notNull(identityAccountRepository);
		Assert.notNull(accountService);

		this.identityAccountRepository = identityAccountRepository;
		this.accountService = accountService;
	}

	@Override
	protected AbstractEntityRepository<AccIdentityAccount, IdentityAccountFilter> getRepository() {
		return identityAccountRepository;
	}
	
	@Override
	public AccIdentityAccount save(AccIdentityAccount entity) {
		AccIdentityAccount account =  super.save(entity);
		if(this.provisioningService == null){
			this.provisioningService = applicationContext.getBean(SysProvisioningService.class);
		}
		this.provisioningService.doProvisioning(account);
		return account;
	}

	@Override
	public void delete(AccIdentityAccount entity) {
		Assert.notNull(entity);

		AccAccount account = entity.getAccount();
		// We check if exists another (ownership) identityAccounts, if not then
		// we will delete account
		boolean moreIdentityAccounts = account.getIdentityAccounts().stream().filter(identityAccount -> {
			return identityAccount.isOwnership() && !identityAccount.equals(entity);
		}).findAny().isPresent();

		if (!moreIdentityAccounts) {
			// We delete all identity accounts first
			account.getIdentityAccounts().forEach(identityAccount -> {
				super.delete(identityAccount);
			});
			// Finally we can delete account
			accountService.delete(account);
		} else {
			super.delete(entity);
		}
	}
}
