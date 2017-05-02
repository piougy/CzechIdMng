package eu.bcvsolutions.idm.acc.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.domain.AttributeMapping;
import eu.bcvsolutions.idm.acc.domain.EntityAccount;
import eu.bcvsolutions.idm.acc.domain.ProvisioningOperationType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.dto.AccIdentityAccountDto;
import eu.bcvsolutions.idm.acc.dto.EntityAccountDto;
import eu.bcvsolutions.idm.acc.dto.filter.EntityAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.IdentityAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.RoleSystemAttributeFilter;
import eu.bcvsolutions.idm.acc.dto.filter.RoleSystemFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.entity.AccIdentityAccount;
import eu.bcvsolutions.idm.acc.entity.SysRoleSystem;
import eu.bcvsolutions.idm.acc.entity.SysRoleSystemAttribute;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystemEntity;
import eu.bcvsolutions.idm.acc.entity.SysSystemMapping;
import eu.bcvsolutions.idm.acc.exception.ProvisioningException;
import eu.bcvsolutions.idm.acc.repository.AccIdentityAccountRepository;
import eu.bcvsolutions.idm.acc.service.api.AccAccountManagementService;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityAccountService;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningExecutor;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningService;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.model.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.ic.service.api.IcConnectorFacade;

/**
 * Service for do identity provisioning
 * 
 * @author svandav
 *
 */
@Service
@Qualifier(value=IdentityProvisioningExecutor.NAME)
public class IdentityProvisioningExecutor extends AbstractProvisioningExecutor<IdmIdentity> {
 
	public static final String NAME = "identityProvisioningService";
	private final AccIdentityAccountRepository identityAccountRepository;
	private final AccIdentityAccountService identityAccountService;
	private final SysRoleSystemService roleSystemService;
	private final AccAccountManagementService accountManagementService;
	
	@Autowired
	public IdentityProvisioningExecutor(SysSystemMappingService systemMappingService,
			SysSystemAttributeMappingService attributeMappingService, IcConnectorFacade connectorFacade,
			SysSystemService systemService, SysRoleSystemService roleSystemService,
			AccAccountManagementService accountManagementService,
			SysRoleSystemAttributeService roleSystemAttributeService, SysSystemEntityService systemEntityService,
			AccAccountService accountService, AccIdentityAccountRepository identityAccountRepository,
			ProvisioningExecutor provisioningExecutor, AccIdentityAccountService identityAccountService) {
		super(systemMappingService, attributeMappingService, connectorFacade, systemService, roleSystemService,
				accountManagementService, roleSystemAttributeService, systemEntityService, accountService,
				provisioningExecutor);		
		Assert.notNull(identityAccountRepository);
		Assert.notNull(roleSystemService);
		Assert.notNull(accountManagementService);
		Assert.notNull(identityAccountService);
		//
		this.identityAccountRepository = identityAccountRepository;
		this.roleSystemService = roleSystemService;
		this.accountManagementService = accountManagementService;
		this.identityAccountService = identityAccountService;
	}
	
	public void doProvisioning(AccAccount account) {
		Assert.notNull(account);

		IdentityAccountFilter filter = new IdentityAccountFilter();
		filter.setAccountId(account.getId());
		Page<AccIdentityAccount> identityAccounts = identityAccountRepository.find(filter, null);
		List<AccIdentityAccount> idenittyAccoutnList = identityAccounts.getContent();
		if (idenittyAccoutnList == null) {
			return;
		}
		idenittyAccoutnList.stream().filter(identityAccount -> {
			return identityAccount.isOwnership();
		}).forEach((identityAccount) -> {
			doProvisioning(account, identityAccount.getIdentity());
		});
	}

	@Override
	public void changePassword(IdmIdentity identity, PasswordChangeDto passwordChange) {
		Assert.notNull(identity);
		Assert.notNull(passwordChange);

		IdentityAccountFilter filter = new IdentityAccountFilter();
		filter.setIdentityId(identity.getId());
		Page<AccIdentityAccount> identityAccounts = identityAccountRepository.find(filter, null);
		List<AccIdentityAccount> identityAccountList = identityAccounts.getContent();
		if (identityAccountList == null) {
			return;
		}
		
		// Distinct by accounts
		List<AccAccount> accounts = new ArrayList<>();
		identityAccountList.stream().filter(identityAccount -> {
			return identityAccount.isOwnership() && (passwordChange.isAll()
					|| passwordChange.getAccounts().contains(identityAccount.getId().toString()));
		}).forEach(identityAccount -> {
			if (!accounts.contains(identityAccount.getAccount())) {
				accounts.add(identityAccount.getAccount());
			}
		});

		accounts.forEach(account -> {
			// find uid from system entity or from account
			String uid = account.getUid();
			SysSystem system = account.getSystem();
			SysSystemEntity systemEntity = account.getSystemEntity();
			//
			// Find mapped attributes (include overloaded attributes)
			List<AttributeMapping> finalAttributes = resolveMappedAttributes(uid, account, identity, system, systemEntity.getEntityType());
			if (CollectionUtils.isEmpty(finalAttributes)) {
				return;
			}
			
			// We try find __PASSWORD__ attribute in mapped attributes
			Optional<? extends AttributeMapping> attriubuteHandlingOptional = finalAttributes.stream()
					.filter((attribute) -> {
						return ProvisioningService.PASSWORD_SCHEMA_PROPERTY_NAME.equals(attribute.getSchemaAttribute().getName());
					}).findFirst();
			if (!attriubuteHandlingOptional.isPresent()) {
				throw new ProvisioningException(AccResultCode.PROVISIONING_PASSWORD_FIELD_NOT_FOUND,
						ImmutableMap.of("uid", uid));
			}
			AttributeMapping mappedAttribute = attriubuteHandlingOptional.get();

			doProvisioningForAttribute(systemEntity, mappedAttribute, passwordChange.getNewPassword(),
					ProvisioningOperationType.UPDATE, identity);
		});
	}


	/**
	 * Return all mapped attributes for this account (include overloaded attributes)
	 * 
	 * @param uid
	 * @param account
	 * @param entity
	 * @param system
	 * @param entityType
	 * @return
	 */
	public List<AttributeMapping> resolveMappedAttributes(String uid, AccAccount account, IdmIdentity entity, SysSystem system, SystemEntityType entityType) {
		IdentityAccountFilter filter = new IdentityAccountFilter();
		filter.setIdentityId(entity.getId());
		filter.setSystemId(system.getId());
		filter.setOwnership(Boolean.TRUE);
		filter.setAccountId(account.getId());
		Page<AccIdentityAccount> identityAccounts = identityAccountRepository.find(filter, null);
		List<AccIdentityAccount> idenityAccoutnList = identityAccounts.getContent();
		if (idenityAccoutnList == null) {
			return null;
		}
		// All identity account with flag ownership on true

		// All role system attributes (overloading) for this uid and same system
		List<SysRoleSystemAttribute> roleSystemAttributesAll = findOverloadingAttributesIdentity(uid, entity, system, idenityAccoutnList, entityType);

		// All default mapped attributes from system
		List<? extends AttributeMapping> defaultAttributes = findAttributeMappings(system, entityType);

		// Final list of attributes use for provisioning
		return compileAttributes(defaultAttributes, roleSystemAttributesAll, entityType);
	}
	
	/**
	 * Return list of all overloading attributes for given identity, system and
	 * uid
	 * 
	 * @param identityAccount
	 * @param uid
	 * @param idenityAccoutnList
	 * @param operationType
	 * @param entityType
	 * @return
	 */
	@Deprecated
	private List<SysRoleSystemAttribute> findOverloadingAttributesIdentity(String uid, IdmIdentity entity, SysSystem system,
			List<? extends EntityAccount> idenityAccoutnList, SystemEntityType entityType) {

		List<SysRoleSystemAttribute> roleSystemAttributesAll = new ArrayList<>();

		idenityAccoutnList.stream().filter(ia -> {
			return ((AccIdentityAccount)ia).getIdentityRole() != null && ia.getAccount().getSystem() != null
					&& ia.getAccount().getSystem().equals(system) 
					&& ia.isOwnership();
		}).forEach((identityAccountInner) -> {
			// All identity account with same system and with filled
			// identityRole

			IdmIdentityRole identityRole = ((AccIdentityAccount)identityAccountInner).getIdentityRole();
			RoleSystemFilter roleSystemFilter = new RoleSystemFilter();
			roleSystemFilter.setRoleId(identityRole.getRole().getId());
			roleSystemFilter.setSystemId(identityAccountInner.getAccount().getSystem().getId());
			List<SysRoleSystem> roleSystems = roleSystemService.find(roleSystemFilter, null).getContent();

			List<SysRoleSystem> roleSystemsForSameAccount = roleSystems.stream().filter(roleSystem -> {
				String roleSystemUID = accountManagementService.generateUID(entity, roleSystem);

				SysSystemMapping systemMapping = roleSystem.getSystemMapping();
				return (SystemOperationType.PROVISIONING == systemMapping.getOperationType()
						&& entityType == systemMapping.getEntityType() && roleSystemUID.equals(uid));
			}).collect(Collectors.toList());

			if (roleSystemsForSameAccount.size() > 1) {
				throw new ProvisioningException(AccResultCode.PROVISIONING_DUPLICATE_ROLE_MAPPING,
						ImmutableMap.of("role", roleSystemsForSameAccount.get(0).getRole().getName(), "system",
								roleSystemsForSameAccount.get(0).getSystem().getName(), "entityType", entityType));
			}
			if (!roleSystemsForSameAccount.isEmpty()) {
				RoleSystemAttributeFilter roleSystemAttributeFilter = new RoleSystemAttributeFilter();
				roleSystemAttributeFilter.setRoleSystemId(roleSystemsForSameAccount.get(0).getId());
				List<SysRoleSystemAttribute> roleAttributes = roleSystemAttributeService
						.find(roleSystemAttributeFilter, null).getContent();

				if (!CollectionUtils.isEmpty(roleAttributes)) {
					roleSystemAttributesAll.addAll(roleAttributes);
				}
			}

		});

		return roleSystemAttributesAll;
	}

	/**
	 * Can use after transform identityAccount to DTO
	 */
	@Override
	protected List<SysRoleSystemAttribute> findOverloadingAttributes(String uid, IdmIdentity entity, SysSystem system,
			List<? extends EntityAccountDto> idenityAccoutnList, SystemEntityType entityType) {
		return null;
	}

	@Override
	protected EntityAccountFilter createEntityAccountFilter() {
		return new IdentityAccountFilter();
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected ReadWriteDtoService getEntityAccountService() {
		return identityAccountService;
	}

	@Override
	protected EntityAccountDto createEntityAccountDto() {
		return new AccIdentityAccountDto();
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected ReadWriteDtoService getEntityService() {
		return null; // We don't have DTO service for IdmIdentity now.
	}

	@Override
	public boolean supports(SystemEntityType delimiter) {
		return SystemEntityType.IDENTITY == delimiter;
	}

}
