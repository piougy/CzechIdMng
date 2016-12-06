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

	private AccAccountService accountService;
	private SysProvisioningService provisioningService;
	
	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	public DefaultAccIdentityAccountService(AccIdentityAccountRepository identityAccountRepository,
			AccAccountService accountService) {
		super(identityAccountRepository);
		//
		Assert.notNull(accountService);
		//
		this.accountService = accountService;
	}
	
	@Override
	public AccIdentityAccount save(AccIdentityAccount entity) {
		AccIdentityAccount account =  super.save(entity);		
		getProvisioningService().doProvisioning(account);
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
	
	/**
	 * TODO: remove this lazy injection after provisioning event event will be done
	 * @return
	 */
	public SysProvisioningService getProvisioningService() {
		if(provisioningService == null){
			provisioningService = applicationContext.getBean(SysProvisioningService.class);
		}
		return provisioningService;
	}
}
