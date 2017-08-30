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
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemEntityDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccountFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.repository.AccAccountRepository;
import eu.bcvsolutions.idm.acc.repository.AccIdentityAccountRepository;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityService;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;

/**
 * Accounts on target system
 * 
 * TODO: event processing - see account delete
 * 
 * @author Radek Tomiška
 *
 */
@Service("accAccountService")
public class DefaultAccAccountService extends AbstractReadWriteDtoService<AccAccountDto, AccAccount, AccountFilter>
		implements AccAccountService {
	
	private final AccAccountRepository accountRepository;
	private final AccIdentityAccountRepository accIdentityAccountRepository;
	private final ApplicationContext applicationContext;
	private ProvisioningService provisioningService;
	private final SysSystemEntityService systemEntityService;

	@Autowired
	public DefaultAccAccountService(
			AccAccountRepository accountRepository,
			AccIdentityAccountRepository accIdentityAccountRepository,
			ApplicationContext applicationContext,
			SysSystemEntityService systemEntityService) {
		super(accountRepository);
		//
		Assert.notNull(accIdentityAccountRepository);
		Assert.notNull(accountRepository);
		Assert.notNull(applicationContext);
		Assert.notNull(systemEntityService);
		//
		this.accIdentityAccountRepository = accIdentityAccountRepository;
		this.accountRepository = accountRepository;
		this.applicationContext = applicationContext;
		this.systemEntityService = systemEntityService;
	}
	
	@Override
	protected AccAccountDto toDto(AccAccount entity, AccAccountDto dto) {
		AccAccountDto newDto = super.toDto(entity, dto);
		//
		// if dto exists add real uid
		if (newDto != null) {
			if (newDto.getSystemEntity() != null) {
				SysSystemEntityDto systemEntity = systemEntityService.get(newDto.getSystemEntity());
				newDto.setRealUid(systemEntity.getUid());
			} else {
				// If system entity do not exist, then return uid from account.
				newDto.setRealUid(newDto.getUid());
			}
		}
		return newDto;
	}

	@Override
	@Transactional
	public AccAccountDto save(AccAccountDto entity, BasePermission... permission) {
 		return super.save(entity, permission);
	}

	@Override
	@Transactional
	public void delete(AccAccountDto account, BasePermission... permission) {
		delete(account, true, null);
	}

	@Override
	@Transactional
	public void delete(AccAccountDto account, boolean deleteTargetAccount, UUID entityId) {
		Assert.notNull(account);
		// We do not allow delete account in protection
		if(account.isAccountProtectedAndValid()){
			throw new ResultCodeException(AccResultCode.ACCOUNT_CANNOT_BE_DELETED_IS_PROTECTED,
					ImmutableMap.of("uid", account.getUid()));
		}
		
		// delete all identity accounts
		// we are calling repository instead service to prevent cycle - we only
		// need clean db in this case
		accIdentityAccountRepository.deleteByAccount(this.getEntity(account.getId()));
		//
		super.delete(account);
		// TODO: move to event
		if (deleteTargetAccount) {
			if (provisioningService == null) {
				provisioningService = applicationContext.getBean(ProvisioningService.class);
			}
			SysSystemEntityDto systemEntityDto = systemEntityService.get(account.getSystemEntity());
			this.provisioningService.doDeleteProvisioning(account, systemEntityDto.getEntityType(), entityId);
		}
	}

	@Override
	public List<AccAccountDto> getAccounts(UUID systemId, UUID identityId) {
		return toDtos(accountRepository.findAccountBySystemAndIdentity(identityId, systemId), true);
	}
	
	@Override
	public AccAccountDto getAccount(String uid, UUID systemId) {
		Assert.notNull(uid, "UID cannot be null!");
		Assert.notNull(systemId, "System ID cannot be null!");
		
		AccountFilter filter = new AccountFilter();
		filter.setUid(uid);
		filter.setSystemId(systemId);
		
		List<AccAccountDto> accounts = this.find(filter, null).getContent();
		if(accounts.isEmpty()){
			return null;
		}
		return accounts.get(0);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<AccAccountDto> findExpired(DateTime expirationDate, Pageable pageable) {
		Assert.notNull(expirationDate);
		//
		return toDtoPage(accountRepository.findByEndOfProtectionLessThanAndInProtectionIsTrue(expirationDate, pageable));
	}
}
