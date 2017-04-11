package eu.bcvsolutions.idm.acc.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.domain.AccountType;
import eu.bcvsolutions.idm.acc.dto.filter.AccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.IdentityAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.RoleSystemAttributeFilter;
import eu.bcvsolutions.idm.acc.dto.filter.RoleSystemFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SystemAttributeMappingFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.entity.AccIdentityAccount;
import eu.bcvsolutions.idm.acc.entity.SysRoleSystem;
import eu.bcvsolutions.idm.acc.entity.SysRoleSystemAttribute;
import eu.bcvsolutions.idm.acc.entity.SysSystemAttributeMapping;
import eu.bcvsolutions.idm.acc.exception.ProvisioningException;
import eu.bcvsolutions.idm.acc.service.api.AccAccountManagementService;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityAccountService;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRoleRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityRoleService;

/**
 * Service for control account management
 * 
 * @author svandav
 *
 */
@Service
public class DefaultAccAccountManagementService implements AccAccountManagementService {

	private final AccAccountService accountService;
	private final SysRoleSystemService roleSystemService;
	private final AccIdentityAccountService identityAccountService;
	private final IdmIdentityRoleRepository identityRoleRepository;
	private final SysRoleSystemAttributeService roleSystemAttributeService;
	private final SysSystemAttributeMappingService systeAttributeMappingService;
	private final IdmIdentityRoleService identityRoleService;

	@Autowired
	public DefaultAccAccountManagementService(SysRoleSystemService roleSystemService, AccAccountService accountService,
			AccIdentityAccountService identityAccountService, IdmIdentityRoleRepository identityRoleRepository,
			SysRoleSystemAttributeService roleSystemAttributeService,
			SysSystemAttributeMappingService systeAttributeMappingService,
			IdmIdentityRoleService identityRoleService) {
		super();
		//
		Assert.notNull(identityAccountService);
		Assert.notNull(roleSystemService);
		Assert.notNull(accountService);
		Assert.notNull(identityRoleRepository);
		Assert.notNull(roleSystemAttributeService);
		Assert.notNull(systeAttributeMappingService);
		Assert.notNull(identityRoleService);
		//
		this.roleSystemService = roleSystemService;
		this.accountService = accountService;
		this.identityAccountService = identityAccountService;
		this.identityRoleRepository = identityRoleRepository;
		this.roleSystemAttributeService = roleSystemAttributeService;
		this.systeAttributeMappingService = systeAttributeMappingService;
		this.identityRoleService = identityRoleService;
	}

	@Override
	public boolean resolveIdentityAccounts(IdmIdentity identity) {
		Assert.notNull(identity);

		IdentityAccountFilter filter = new IdentityAccountFilter();
		filter.setIdentityId(identity.getId());
		Page<AccIdentityAccount> identityAccounts = identityAccountService.find(filter, null);
		List<AccIdentityAccount> identityAccountList = identityAccounts.getContent();

		List<IdmIdentityRole> identityRoles = identityRoleRepository.findAllByIdentityContract_Identity(identity, null);

		boolean provisioningRequired = false;

		if (CollectionUtils.isEmpty(identityRoles) && CollectionUtils.isEmpty(identityAccountList)) {
			// None roles and accounts ... we don't have any to do
			return false;
		}

		List<AccIdentityAccount> identityAccountsToCreate = new ArrayList<>();
		List<AccIdentityAccount> identityAccountsToDelete = new ArrayList<>();

		// Is role valid in this moment
		resolveIdentityAccountForCreate(identity, identityAccountList, identityRoles, identityAccountsToCreate,
				identityAccountsToDelete);

		// Is role invalid in this moment
		resolveIdentityAccountForDelete(identityAccountList, identityRoles, identityAccountsToDelete);

		// Delete invalid identity accounts
		provisioningRequired = !identityAccountsToDelete.isEmpty() ? true : provisioningRequired;
		identityAccountsToDelete.stream().forEach(identityAccount -> {
			identityAccountService.delete(identityAccount);
		});

		// Create new identity accounts
		provisioningRequired = !identityAccountsToCreate.isEmpty() ? true : provisioningRequired;
		identityAccountsToCreate.stream().forEach(identityAccount -> {
			identityAccount.setAccount(accountService.save(identityAccount.getAccount()));
			identityAccountService.save(identityAccount);

		});

		return provisioningRequired;
	}

	/**
	 * Resolve identity account to delete
	 * @param identityAccountList
	 * @param identityRoles
	 * @param identityAccountsToDelete
	 */
	private void resolveIdentityAccountForDelete(List<AccIdentityAccount> identityAccountList,
			List<IdmIdentityRole> identityRoles, List<AccIdentityAccount> identityAccountsToDelete) {

		identityRoles.stream().filter(identityRole -> {
			return !identityRole.isValid();
		}).forEach(identityRole -> {
			// Search IdentityAccounts to delete
			identityAccountList.stream().filter(identityAccount -> {
				return identityRole.equals(identityAccount.getIdentityRole());
			}).forEach(identityAccount -> {
				identityAccountsToDelete.add(identityAccount);
			});
		});
	}

	/**
	 * Resolve Identity account - to create
	 * @param identity
	 * @param identityAccountList
	 * @param identityRoles
	 * @param identityAccountsToCreate
	 * @param identityAccountsToDelete
	 * @param resolvedRolesForCreate
	 */
	private void resolveIdentityAccountForCreate(IdmIdentity identity, List<AccIdentityAccount> identityAccountList,
			List<IdmIdentityRole> identityRoles, List<AccIdentityAccount> identityAccountsToCreate,
			List<AccIdentityAccount> identityAccountsToDelete) {
		
		// Is role valid in this moment
		identityRoles.stream().filter(identityRole -> {
			return identityRole.isValid();
		}).forEach(identityRole -> {
			
			IdmRole role = identityRole.getRole();
			RoleSystemFilter roleSystemFilter = new RoleSystemFilter();
			roleSystemFilter.setRoleId(role.getId());
			List<SysRoleSystem> roleSystems = roleSystemService.find(roleSystemFilter, null).getContent();

			roleSystems.stream().filter(roleSystem -> {

				return !identityAccountList.stream().filter(identityAccount -> {
					if (roleSystem.equals(identityAccount.getRoleSystem())) {
						
						// Has identity account same uid as account?
						String uid = generateUID(identity, roleSystem);
						if (uid.equals(identityAccount.getAccount().getUid())) {
							// Identity account for this role, system and uid is
							// created
							return true;
						}else{
							// We found identityAccount for same identity and roleSystem, but this identityAccount
							// is link to Account with different UID. It's probably means definition of UID (transformation)\
							// on roleSystem was changed. We have to delete this identityAccount. 
							identityAccountsToDelete.add(identityAccount);
						}
					}
					return false;
				}).findFirst().isPresent();

			}).forEach(roleSystem -> {
				// For this system we have to crate new account
				String uid = generateUID(identity, roleSystem);
				
				// We try find account for same uid on same system
				// First we try search same account in list for create new accounts
				Optional<AccIdentityAccount> sameAccountOptional = identityAccountsToCreate.stream().filter(ia -> {
					return ia.getAccount().getUid().equals(uid);
				}).findFirst();
				
				AccAccount account = null;
				if(sameAccountOptional.isPresent()){
					account = sameAccountOptional.get().getAccount();
				}else{
					// If account is not in list accounts to create, then we will search in database
					AccountFilter accountFilter = new AccountFilter();
					accountFilter.setUidId(uid);
					accountFilter.setSystemId(roleSystem.getSystem().getId());
					List<AccAccount> sameAccounts = accountService.find(accountFilter, null).getContent();
					if (CollectionUtils.isEmpty(sameAccounts)) {
						account = new AccAccount();
						account.setUid(uid);
						account.setAccountType(AccountType.PERSONAL);
						account.setSystem(roleSystem.getSystem());
					} else {
						// We use existed account
						account = sameAccounts.get(0);
					}
				}
				AccIdentityAccount identityAccount = new AccIdentityAccount();
				identityAccount.setAccount(account);
				identityAccount.setIdentity(identity);
				identityAccount.setIdentityRole(identityRole);
				identityAccount.setRoleSystem(roleSystem);
				// TODO: Add flag ownership to SystemRole and set here.
				identityAccount.setOwnership(true);

				identityAccountsToCreate.add(identityAccount);
			});
		});
	}

	/**
	 * Return UID for this identity and roleSystem. First will be find and use
	 * transform script from roleSystem attribute. If isn't UID attribute for
	 * roleSystem defined, then will be use default UID attribute handling.
	 * 
	 * @param identity
	 * @param roleSystem
	 * @return
	 */
	@Override
	public String generateUID(IdmIdentity identity, SysRoleSystem roleSystem) {
		// Find attributes for this roleSystem
		RoleSystemAttributeFilter roleSystemAttrFilter = new RoleSystemAttributeFilter();
		roleSystemAttrFilter.setRoleSystemId(roleSystem.getId());
		List<SysRoleSystemAttribute> attributes = roleSystemAttributeService.find(roleSystemAttrFilter, null)
				.getContent();
		List<SysRoleSystemAttribute> attributesUid = attributes.stream().filter(attribute -> {
			return attribute.isUid();
		}).collect(Collectors.toList());

		if (attributesUid.size() > 1) {
			throw new ProvisioningException(AccResultCode.PROVISIONING_ROLE_ATTRIBUTE_MORE_UID, ImmutableMap.of("role",
					roleSystem.getRole().getName(), "system", roleSystem.getSystem().getName()));
		}

		SysRoleSystemAttribute uidRoleAttribute = !attributesUid.isEmpty() ? attributesUid.get(0) : null;

		// If roleSystem UID attribute found, then we use his transformation
		// script.
		if (uidRoleAttribute != null) {
			Object uid = systeAttributeMappingService.transformValueToResource(identity.getUsername(),
					uidRoleAttribute.getTransformScript(), identity, roleSystem.getSystem());
			if (!(uid instanceof String)) {
				throw new ProvisioningException(AccResultCode.PROVISIONING_ATTRIBUTE_UID_IS_NOT_STRING,
						ImmutableMap.of("uid", uid));
			}
			return (String) uid;
		}

		// If roleSystem UID was not found, then we use default UID schema
		// attribute handling
		SystemAttributeMappingFilter systeAttributeMappingFilter = new SystemAttributeMappingFilter();
		systeAttributeMappingFilter.setSystemId(roleSystem.getSystem().getId());
		List<SysSystemAttributeMapping> schemaHandlingAttributes = systeAttributeMappingService
				.find(systeAttributeMappingFilter, null).getContent();
		List<SysSystemAttributeMapping> schemaHandlingAttributesUid = schemaHandlingAttributes.stream()
				.filter(attributeHandling -> {
					return attributeHandling.isUid();
				}).collect(Collectors.toList());

		if (schemaHandlingAttributesUid.size() > 1) {
			throw new ProvisioningException(AccResultCode.PROVISIONING_ATTRIBUTE_MORE_UID,
					ImmutableMap.of("system", roleSystem.getSystem().getName()));
		}
		if (schemaHandlingAttributesUid.isEmpty()) {
			throw new ProvisioningException(AccResultCode.PROVISIONING_ATTRIBUTE_UID_NOT_FOUND,
					ImmutableMap.of("system", roleSystem.getSystem().getName()));
		}

		SysSystemAttributeMapping uidSchemaAttribute = schemaHandlingAttributesUid.get(0);
		Object uid = systeAttributeMappingService.transformValueToResource(identity.getUsername(), uidSchemaAttribute,
				identity);
		if (!(uid instanceof String)) {
			throw new ProvisioningException(AccResultCode.PROVISIONING_ATTRIBUTE_UID_IS_NOT_STRING,
					ImmutableMap.of("uid", uid));
		}
		return (String) uid;
	}

	@Override
	public void deleteIdentityAccount(IdmIdentityRole entity) {
		IdentityAccountFilter filter = new IdentityAccountFilter();
		filter.setIdentityRoleId(entity.getId());
		Page<AccIdentityAccount> identityAccounts = identityAccountService.find(filter, null);
		List<AccIdentityAccount> identityAccountList = identityAccounts.getContent();

		identityAccountList.forEach(identityAccount -> {
			identityAccountService.delete(identityAccount);
		});
	}
}
