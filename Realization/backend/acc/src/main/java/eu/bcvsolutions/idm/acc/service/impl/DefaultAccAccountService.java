package eu.bcvsolutions.idm.acc.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.dto.filter.AccountFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.repository.AccAccountRepository;
import eu.bcvsolutions.idm.acc.repository.AccIdentityAccountRepository;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningService;
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
	
	private final AccAccountRepository accountRepository;
	private final AccIdentityAccountRepository accIdentityAccountRepository;
	@Autowired
	private ApplicationContext applicationContext;
	private ProvisioningService provisioningService;

	@Autowired
	public DefaultAccAccountService(AccAccountRepository accountRepository,
			AccIdentityAccountRepository accIdentityAccountRepository) {
		super(accountRepository);
		//
		Assert.notNull(accIdentityAccountRepository);
		Assert.notNull(accountRepository);
		//
		this.accIdentityAccountRepository = accIdentityAccountRepository;
		this.accountRepository = accountRepository;
	}

	@Override
	@Transactional
	public AccAccount save(AccAccount entity) {
 		return super.save(entity);
	}

	@Override
	@Transactional
	public void delete(AccAccount account) {
		delete(account, true);
	}

	@Override
	@Transactional
	public void delete(AccAccount account, boolean deleteTargetAccount) {
		Assert.notNull(account);
		//
		// delete all identity accounts
		// we are calling repository instead service to prevent cycle - we only
		// need clean db in this case
		accIdentityAccountRepository.deleteByAccount(account);
		//
		super.delete(account);
		// TODO: move to event
		if (deleteTargetAccount) {
			if (provisioningService == null) {
				provisioningService = applicationContext.getBean(ProvisioningService.class);
			}
			this.provisioningService.doDeleteProvisioning(account, SystemEntityType.IDENTITY);
		}
	}

	@Override
	public List<AccAccount> getAccouts(UUID systemId, UUID identityId) {
		return accountRepository.findAccountBySystemAndIdentity(identityId, systemId);
	}

}
