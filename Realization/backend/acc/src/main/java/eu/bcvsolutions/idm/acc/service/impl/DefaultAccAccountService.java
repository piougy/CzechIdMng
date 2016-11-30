package eu.bcvsolutions.idm.acc.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.dto.AccountFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.repository.AccAccountRepository;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningService;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
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

	private AccAccountRepository accountRepository;

	@Autowired
	private SysProvisioningService provisioningService;

	@Autowired
	public DefaultAccAccountService(AccAccountRepository accountRepository) {
		super();
		Assert.notNull(accountRepository);

		this.accountRepository = accountRepository;
	}

	@Override
	protected AbstractEntityRepository<AccAccount, AccountFilter> getRepository() {
		return accountRepository;
	}

	@Override
	public void delete(AccAccount entity) {
		super.delete(entity);
		// TODO move to asynchronouse queue
		this.provisioningService.doDeleteProvisioning(entity);
	}
}
