package eu.bcvsolutions.idm.acc.service.impl;

import java.text.MessageFormat;
import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemEntityDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccAccountFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.entity.AccAccount_;
import eu.bcvsolutions.idm.acc.repository.AccAccountRepository;
import eu.bcvsolutions.idm.acc.repository.AccContractAccountRepository;
import eu.bcvsolutions.idm.acc.repository.AccIdentityAccountRepository;
import eu.bcvsolutions.idm.acc.repository.AccRoleAccountRepository;
import eu.bcvsolutions.idm.acc.repository.AccRoleCatalogueAccountRepository;
import eu.bcvsolutions.idm.acc.repository.AccTreeAccountRepository;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityService;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
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
public class DefaultAccAccountService extends AbstractReadWriteDtoService<AccAccountDto, AccAccount, AccAccountFilter>
		implements AccAccountService {

	private final AccAccountRepository accountRepository;
	private final AccIdentityAccountRepository accIdentityAccountRepository;
	private final AccRoleAccountRepository roleAccountRepository;
	private final AccTreeAccountRepository treeAccountRepository;
	private final AccContractAccountRepository contractAccountRepository;
	private final AccRoleCatalogueAccountRepository roleCatalogueAccountRepository;
	private final ApplicationContext applicationContext;
	private ProvisioningService provisioningService;
	private final SysSystemEntityService systemEntityService;
	private static final Logger LOG = LoggerFactory.getLogger(DefaultAccAccountService.class);

	@Autowired
	public DefaultAccAccountService(AccAccountRepository accountRepository,
			AccIdentityAccountRepository accIdentityAccountRepository, ApplicationContext applicationContext,
			SysSystemEntityService systemEntityService, AccRoleAccountRepository roleAccountRepository,
			AccTreeAccountRepository treeAccountRepository, AccContractAccountRepository contractAccountRepository,
			AccRoleCatalogueAccountRepository roleCatalogueAccountRepository) {
		super(accountRepository);
		//
		Assert.notNull(accIdentityAccountRepository);
		Assert.notNull(accountRepository);
		Assert.notNull(applicationContext);
		Assert.notNull(systemEntityService);
		Assert.notNull(roleAccountRepository);
		Assert.notNull(roleCatalogueAccountRepository);
		Assert.notNull(treeAccountRepository);
		Assert.notNull(contractAccountRepository);

		//
		this.accIdentityAccountRepository = accIdentityAccountRepository;
		this.accountRepository = accountRepository;
		this.applicationContext = applicationContext;
		this.systemEntityService = systemEntityService;
		this.roleAccountRepository = roleAccountRepository;
		this.roleCatalogueAccountRepository = roleCatalogueAccountRepository;
		this.treeAccountRepository = treeAccountRepository;
		this.contractAccountRepository = contractAccountRepository;
	}

	@Override
	protected Page<AccAccount> findEntities(AccAccountFilter filter, Pageable pageable, BasePermission... permission) {
		if (filter == null) {
			return accountRepository.findAll(pageable);
		}
		return accountRepository.find(filter, pageable);
	}

	@Override
	protected AccAccountDto toDto(AccAccount entity, AccAccountDto dto) {
		AccAccountDto newDto = super.toDto(entity, dto);
		//
		// if dto exists add real uid
		if (newDto != null) {
			if (newDto.getSystemEntity() != null) {
				SysSystemEntityDto systemEntity = DtoUtils.getEmbedded(newDto, AccAccount_.systemEntity,
						SysSystemEntityDto.class);
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
	public void delete(AccAccountDto account, BasePermission... permission) {
		delete(account, true, null);
	}

	@Override
	@Transactional
	public void delete(AccAccountDto account, boolean deleteTargetAccount, UUID entityId) {
		Assert.notNull(account);
		// We do not allow delete account in protection
		if (account.isAccountProtectedAndValid()) {
			throw new ResultCodeException(AccResultCode.ACCOUNT_CANNOT_BE_DELETED_IS_PROTECTED,
					ImmutableMap.of("uid", account.getUid()));
		}

		// delete all identity accounts we are calling repository instead service to
		// prevent cycle - we only need clean db in this case
		accIdentityAccountRepository.deleteByAccount(this.getEntity(account.getId()));

		// delete all role accounts we are calling repository instead service to
		// prevent cycle - we only need clean db in this case
		roleAccountRepository.deleteByAccount(this.getEntity(account.getId()));

		// delete all role-catalogue accounts we are calling repository instead service to
		// prevent cycle - we only need clean db in this case
		roleCatalogueAccountRepository.deleteByAccount(this.getEntity(account.getId()));
		
		// delete all tree accounts we are calling repository instead service to
		// prevent cycle - we only need clean db in this case
		treeAccountRepository.deleteByAccount(this.getEntity(account.getId()));
		
		// delete all contract accounts we are calling repository instead service to
		// prevent cycle - we only need clean db in this case
		contractAccountRepository.deleteByAccount(this.getEntity(account.getId()));
		
		//
		super.delete(account);
		// TODO: move to event
		if (deleteTargetAccount) {
			if (provisioningService == null) {
				provisioningService = applicationContext.getBean(ProvisioningService.class);
			}
			if (account.getSystemEntity() != null) {
				SysSystemEntityDto systemEntityDto = systemEntityService.get(account.getSystemEntity());
				if (SystemEntityType.CONTRACT == systemEntityDto.getEntityType()) {
					LOG.warn(MessageFormat.format("Provisioning is not supported for contract now [{0}]!",
							account.getUid()));
					return;
				}
				this.provisioningService.doDeleteProvisioning(account, systemEntityDto.getEntityType(), entityId);
			}
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

		AccAccountFilter filter = new AccAccountFilter();
		filter.setUid(uid);
		filter.setSystemId(systemId);

		List<AccAccountDto> accounts = this.find(filter, null).getContent();
		if (accounts.isEmpty()) {
			return null;
		}
		return accounts.get(0);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<AccAccountDto> findExpired(DateTime expirationDate, Pageable pageable) {
		Assert.notNull(expirationDate);
		//
		return toDtoPage(
				accountRepository.findByEndOfProtectionLessThanAndInProtectionIsTrue(expirationDate, pageable));
	}
}
