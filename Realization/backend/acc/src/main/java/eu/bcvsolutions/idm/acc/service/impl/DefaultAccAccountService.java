package eu.bcvsolutions.idm.acc.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.dto.AccountFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.repository.AccAccountRepository;
import eu.bcvsolutions.idm.acc.repository.AccIdentityAccountRepository;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningService;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;

/**
 * Accounts on target system
 * 
 * @author Radek Tomiška
 *
 */
@Service
public class DefaultAccAccountService extends AbstractReadWriteEntityService<AccAccount, AccountFilter>
		implements AccAccountService {

	private final AccIdentityAccountRepository accIdentityAccountRepository;
	private final SysProvisioningService provisioningService;

	@Autowired
	public DefaultAccAccountService(
			AccAccountRepository accountRepository,
			AccIdentityAccountRepository accIdentityAccountRepository,
			SysProvisioningService provisioningService) {
		super(accountRepository);
		//
		Assert.notNull(accIdentityAccountRepository);
		Assert.notNull(provisioningService);
		//
		this.accIdentityAccountRepository = accIdentityAccountRepository;
		this.provisioningService = provisioningService;
	}

	@Override
	@Transactional
	public void delete(AccAccount account) {
		Assert.notNull(account);
		//
		// delete all identity accounts
		// we are calling repository instead service to prevent cycle - we only need clean db in this case
		accIdentityAccountRepository.deleteByAccount(account);
		//
		super.delete(account);
		// TODO move to asynchronouse queue
		this.provisioningService.doDeleteProvisioning(account);
	}
}
