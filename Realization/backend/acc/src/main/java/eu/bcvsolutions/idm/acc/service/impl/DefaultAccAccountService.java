package eu.bcvsolutions.idm.acc.service.impl;

import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.dto.filter.AccountFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.repository.AccAccountRepository;
import eu.bcvsolutions.idm.acc.repository.AccIdentityAccountRepository;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningService;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;

/**
 * Accounts on target system
 * 
 * TODO: dto, event processing - see account delete
 * 
 * @author Radek Tomiška
 *
 */
@Service("accAccountService")
public class DefaultAccAccountService extends AbstractReadWriteEntityService<AccAccount, AccountFilter>
		implements AccAccountService {
	
	private final AccAccountRepository accountRepository;
	private final AccIdentityAccountRepository accIdentityAccountRepository;
	private final ApplicationContext applicationContext;
	private ProvisioningService provisioningService;

	@Autowired
	public DefaultAccAccountService(
			AccAccountRepository accountRepository,
			AccIdentityAccountRepository accIdentityAccountRepository,
			ApplicationContext applicationContext) {
		super(accountRepository);
		//
		Assert.notNull(accIdentityAccountRepository);
		Assert.notNull(accountRepository);
		Assert.notNull(applicationContext);
		//
		this.accIdentityAccountRepository = accIdentityAccountRepository;
		this.accountRepository = accountRepository;
		this.applicationContext = applicationContext;
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
		// We do not allow delete account in protection
		if(account.isAccountProtectedAndValid()){
			throw new ResultCodeException(AccResultCode.ACCOUNT_CANNOT_BE_DELETED_IS_PROTECTED, ImmutableMap.of("uid", account.getUid()));
		}
		
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
	public List<AccAccount> getAccounts(UUID systemId, UUID identityId) {
		return accountRepository.findAccountBySystemAndIdentity(identityId, systemId);
	}
	
	@Override
	public AccAccount getAccount(String uid, UUID systemId) {
		Assert.notNull(uid, "UID cannot be null!");
		Assert.notNull(systemId, "System ID cannot be null!");
		
		AccountFilter filter = new AccountFilter();
		filter.setUid(uid);
		filter.setSystemId(systemId);
		
		List<AccAccount> accounts = this.find(filter, null).getContent();
		if(accounts.isEmpty()){
			return null;
		}
		return accounts.get(0);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<AccAccount> findExpired(DateTime expirationDate, Pageable pageable) {
		Assert.notNull(expirationDate);
		//
		return accountRepository.findByEndOfProtectionLessThanAndInProtectionIsTrue(expirationDate, pageable);
	}
}
