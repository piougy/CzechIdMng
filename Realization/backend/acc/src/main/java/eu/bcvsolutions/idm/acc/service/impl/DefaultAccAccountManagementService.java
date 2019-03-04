package eu.bcvsolutions.idm.acc.service.impl;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.domain.AccountType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.AccIdentityAccountDto;
import eu.bcvsolutions.idm.acc.dto.SysRoleSystemAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysRoleSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaObjectClassDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.AccIdentityAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysRoleSystemAttributeFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysRoleSystemFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemAttributeMappingFilter;
import eu.bcvsolutions.idm.acc.entity.AccIdentityAccount_;
import eu.bcvsolutions.idm.acc.entity.SysRoleSystemAttribute_;
import eu.bcvsolutions.idm.acc.entity.SysRoleSystem_;
import eu.bcvsolutions.idm.acc.entity.SysSchemaObjectClass_;
import eu.bcvsolutions.idm.acc.exception.ProvisioningException;
import eu.bcvsolutions.idm.acc.service.api.AccAccountManagementService;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityAccountService;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaObjectClassService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityStateDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.OperationResultDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmEntityStateFilter;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.EntityStateManager;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;

/**
 * Service for control account management. Account management is supported for
 * {@link SystemEntityType#IDENTITY} only.
 * 
 * @author Vít Švanda
 *
 */
@Service
public class DefaultAccAccountManagementService implements AccAccountManagementService {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory
			.getLogger(DefaultAccAccountManagementService.class);
	private final AccAccountService accountService;
	private final SysRoleSystemService roleSystemService;
	private final AccIdentityAccountService identityAccountService;
	private final SysRoleSystemAttributeService roleSystemAttributeService;
	private final SysSystemAttributeMappingService systemAttributeMappingService;
	private final SysSystemMappingService systemMappingService;
	private final SysSchemaObjectClassService schemaObjectClassService;
	@Autowired
	private IdmIdentityRoleService identityRoleService;
	@Autowired
	private EntityStateManager entityStateManager;
	@Autowired
	private EntityEventManager entityEventManager;

	@Autowired
	public DefaultAccAccountManagementService(SysRoleSystemService roleSystemService, AccAccountService accountService,
			AccIdentityAccountService identityAccountService, SysRoleSystemAttributeService roleSystemAttributeService,
			SysSystemAttributeMappingService systemAttributeMappingService,
			SysSystemMappingService systemMappingService, SysSchemaObjectClassService schemaObjectClassService) {
		super();
		//
		Assert.notNull(identityAccountService);
		Assert.notNull(roleSystemService);
		Assert.notNull(accountService);
		Assert.notNull(roleSystemAttributeService);
		Assert.notNull(systemAttributeMappingService);
		Assert.notNull(systemMappingService);
		Assert.notNull(schemaObjectClassService);
		//
		this.roleSystemService = roleSystemService;
		this.accountService = accountService;
		this.identityAccountService = identityAccountService;
		this.roleSystemAttributeService = roleSystemAttributeService;
		this.systemAttributeMappingService = systemAttributeMappingService;
		this.systemMappingService = systemMappingService;
		this.schemaObjectClassService = schemaObjectClassService;
	}

	@Override
	public boolean resolveIdentityAccounts(IdmIdentityDto identity) {
		Assert.notNull(identity);
		// find not deleted identity accounts
		AccIdentityAccountFilter filter = new AccIdentityAccountFilter();
		filter.setIdentityId(identity.getId());
		List<AccIdentityAccountDto> allIdentityAccountList = identityAccountService.find(filter, null).getContent();
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		
		if (CollectionUtils.isEmpty(identityRoles) && CollectionUtils.isEmpty(allIdentityAccountList)) {
			// No roles and accounts ... we don't have anything to do
			return false;
		}
		
		// account with delete accepted states will be removed on the end
		IdmEntityStateFilter identityAccountStatesFilter = new IdmEntityStateFilter();
		identityAccountStatesFilter.setSuperOwnerId(identity.getId());
		identityAccountStatesFilter.setOwnerType(entityStateManager.getOwnerType(AccIdentityAccountDto.class));
		identityAccountStatesFilter.setResultCode(CoreResultCode.DELETED.getCode());
		List<IdmEntityStateDto> identityAccountStates = entityStateManager.findStates(identityAccountStatesFilter, null).getContent();
		List<AccIdentityAccountDto> identityAccountList = allIdentityAccountList //
				.stream() //
				.filter(ia -> {
					return !identityAccountStates //
							.stream() //
							.anyMatch(state -> { 
								return ia.getId().equals(state.getOwnerId());
							});
				}).collect(Collectors.toList());
		
		// create / remove accounts 
		if (!CollectionUtils.isEmpty(identityRoles) || !CollectionUtils.isEmpty(identityAccountList)) {
			List<AccIdentityAccountDto> identityAccountsToCreate = new ArrayList<>();
			List<AccIdentityAccountDto> identityAccountsToDelete = new ArrayList<>();
	
			// Is role valid in this moment
			resolveIdentityAccountForCreate(identity, identityAccountList, identityRoles, identityAccountsToCreate,
					identityAccountsToDelete, false);
	
			// Is role invalid in this moment
			resolveIdentityAccountForDelete(identityAccountList, identityRoles, identityAccountsToDelete);
			
			// Create new identity accounts
			identityAccountsToCreate.forEach(identityAccount -> identityAccountService.save(identityAccount));
	
			// Delete invalid identity accounts
			identityAccountsToDelete.forEach(identityAccount -> identityAccountService.deleteById(identityAccount.getId()));
		}		
		// clear identity accounts marked to be deleted
		identityAccountStates //
			.stream() //
			.forEach(state -> {	// 
				AccIdentityAccountDto deleteIdentityAccount = identityAccountService.get(state.getOwnerId());
				if (deleteIdentityAccount != null) {
					// identity account can be deleted manually.
					identityAccountService.delete(deleteIdentityAccount);
				}
				entityStateManager.deleteState(state);
			});
		
		// Return value is deprecated since version 9.5.0  (is useless)
		return true;
	}
	
	
	@Override
	public List<UUID> resolveNewIdentityRoles(IdmIdentityDto identity, IdmIdentityRoleDto... identityRoles) {
		Assert.notNull(identity);

		if (identityRoles == null || identityRoles.length == 0) {
			// No identity-roles ... we don't have anything to do
			return null;
		}

		List<IdmIdentityRoleDto> identityRolesList = Lists.newArrayList(identityRoles);
		List<AccIdentityAccountDto> identityAccountsToCreate = Lists.newArrayList();

		// Is role valid in this moment
		resolveIdentityAccountForCreate(identity, Lists.newArrayList(), identityRolesList, identityAccountsToCreate,
				Lists.newArrayList(), true);

		// For this account should be executed provisioning
		List<UUID> accounts = Lists.newArrayList();
		// Create new identity accounts
		identityAccountsToCreate.forEach(identityAccount -> {
			AccIdentityAccountDto identityAccountDto = identityAccountService.save(identityAccount);
			accounts.add(identityAccountDto.getAccount());
		});
		return accounts;

	}
	
	@Override
	public  List<UUID>  resolveUpdatedIdentityRoles(IdmIdentityDto identity, IdmIdentityRoleDto... identityRoles) {
		Assert.notNull(identity);

		if (identityRoles == null || identityRoles.length == 0) {
			// No identity-roles ... we don't have anything to do
			return null;
		}
		List<IdmIdentityRoleDto> identityRolesList = Lists.newArrayList(identityRoles);
		// Find identity-accounts for changed identity-roles (using IN predicate)
		List<UUID> identityRoleIds = identityRolesList.stream() //
				.map(IdmIdentityRoleDto::getId) //
				.collect(Collectors.toList()); //
		AccIdentityAccountFilter filter = new AccIdentityAccountFilter();
		filter.setIdentityId(identity.getId());
		filter.setIdentityRoleIds(identityRoleIds);
		List<AccIdentityAccountDto> identityAccountList = identityAccountService.find(filter, null).getContent();

		// create / remove accounts
		List<AccIdentityAccountDto> identityAccountsToCreate = new ArrayList<>();
		List<AccIdentityAccountDto> identityAccountsToDelete = new ArrayList<>();

		// Is role valid in this moment
		resolveIdentityAccountForCreate(identity, identityAccountList, identityRolesList, identityAccountsToCreate,
				identityAccountsToDelete, false);

		// Is role invalid in this moment
		resolveIdentityAccountForDelete(identityAccountList, identityRolesList, identityAccountsToDelete);

		// For this account should be executed provisioning
		List<UUID> accounts = Lists.newArrayList();
		
		// Create new identity accounts
		identityAccountsToCreate.forEach(identityAccount -> {
			AccIdentityAccountDto identityAccountDto = identityAccountService.save(identityAccount);
			accounts.add(identityAccountDto.getAccount());
		});

		// Delete invalid identity accounts
		identityAccountsToDelete.forEach(identityAccount -> {
			identityAccountService.deleteById(identityAccount.getId());
			accounts.add(identityAccount.getAccount());
		});
		
		return accounts;
	}

	/**
	 * Resolve identity account to delete
	 * 
	 * @param identityAccountList
	 * @param identityRoles
	 * @param identityAccountsToDelete
	 */
	private void resolveIdentityAccountForDelete(List<AccIdentityAccountDto> identityAccountList,
			List<IdmIdentityRoleDto> identityRoles, List<AccIdentityAccountDto> identityAccountsToDelete) {

		identityRoles.stream().filter(identityRole -> {
			return !identityRole.isValid();
		}).forEach(identityRole -> {
			// Search IdentityAccounts to delete

			// Identity-account is not removed (even if that identity-role is invalid) if
			// the role-system has enabled forward account management and identity-role will
			// be valid in the future.
			identityAccountList.stream() //
					.filter(identityAccount -> identityRole.getId().equals(identityAccount.getIdentityRole())) //
					.filter(identityAccount -> identityAccount.getRoleSystem() == null
							|| !( ((SysRoleSystemDto) DtoUtils
									.getEmbedded(identityAccount, AccIdentityAccount_.roleSystem))
									.isForwardAccountManagemen() && identityRole.isValidNowOrInFuture())) //
					.forEach(identityAccount -> {
						identityAccountsToDelete.add(identityAccount);
					});
		});
	}

	/**
	 * Resolve Identity account - to create
	 * 
	 * @param identity
	 * @param identityAccountList
	 * @param identityRoles
	 * @param identityAccountsToCreate
	 * @param identityAccountsToDelete
	 * @param resolvedRolesForCreate
	 */
	private void resolveIdentityAccountForCreate(IdmIdentityDto identity,
			List<AccIdentityAccountDto> identityAccountList, List<IdmIdentityRoleDto> identityRoles,
			List<AccIdentityAccountDto> identityAccountsToCreate, List<AccIdentityAccountDto> identityAccountsToDelete,
			boolean onlyCreateNew) {

		identityRoles.forEach(identityRole -> {
			UUID role = identityRole.getRole();
			SysRoleSystemFilter roleSystemFilter = new SysRoleSystemFilter();
			roleSystemFilter.setRoleId(role);
			List<SysRoleSystemDto> roleSystems = roleSystemService.find(roleSystemFilter, null).getContent();

			// Is role valid in this moment or
			// role-system has enabled forward account management (identity-role have to be
			// valid in the future)
			roleSystems.stream()
					.filter(roleSystem -> (identityRole.isValid()
							|| (roleSystem.isForwardAccountManagemen() && identityRole.isValidNowOrInFuture()))) //
					.forEach(roleSystem -> {

						String uid = generateUID(identity, roleSystem);

						// Check on change of UID is not executed if all given identity-roles are new
						if (!onlyCreateNew) {
							// Check identity-account for that role-system on change the definition of UID
							checkOnChangeUID(uid, roleSystem, identityAccountList, identityAccountsToDelete);
						}

						// Try to find identity-account for this identity-role. If exists and doesn't in
						// list of identity-account to delete, then we are done.
						AccIdentityAccountDto existsIdentityAccount = findAlreadyExistsIdentityAccount(
								identityAccountList, identityAccountsToDelete, identityRole);

						if (existsIdentityAccount != null) {
							return;
						}

						// For this system we need to create new (or found exists) account
						AccAccountDto account = createAccountByRoleSystem(uid, identity, roleSystem,
								identityAccountsToCreate);
						if (account == null) {
							return;
						}

						// Prevent to create the same identity account
						if (identityAccountList.stream().filter(identityAccount -> {
							return identityAccount.getAccount().equals(account.getId())
									&& identityRole.getId().equals(identityAccount.getIdentityRole())
									&& roleSystem.getId().equals(identityAccount.getRoleSystem());
						}).count() == 0) {
							AccIdentityAccountDto identityAccount = new AccIdentityAccountDto();
							identityAccount.setAccount(account.getId());
							identityAccount.setIdentity(identity.getId());
							identityAccount.setIdentityRole(identityRole.getId());
							identityAccount.setRoleSystem(roleSystem.getId());
							identityAccount.setOwnership(true);
							identityAccount.getEmbedded().put(AccIdentityAccount_.account.getName(), account);

							identityAccountsToCreate.add(identityAccount);
						}

					});
		});
	}

	/**
	 * Return UID for this identity and roleSystem. First will be find and use
	 * transform script from roleSystem attribute. If isn't UID attribute for
	 * roleSystem defined, then will be use default UID attribute handling.
	 * 
	 * @param entity
	 * @param roleSystem
	 * @return
	 */
	@Override
	public String generateUID(AbstractDto entity, SysRoleSystemDto roleSystem) {
	
		// Find attributes for this roleSystem
		SysRoleSystemAttributeFilter roleSystemAttrFilter = new SysRoleSystemAttributeFilter();
		roleSystemAttrFilter.setRoleSystemId(roleSystem.getId());
		roleSystemAttrFilter.setIsUid(Boolean.TRUE);

		List<SysRoleSystemAttributeDto> attributesUid = roleSystemAttributeService
				.find(roleSystemAttrFilter, null) //
				.getContent(); //
		
		if (attributesUid.size() > 1) {
			IdmRoleDto roleDto = DtoUtils.getEmbedded(roleSystem, SysRoleSystem_.role);
			SysSystemDto systemDto = DtoUtils.getEmbedded(roleSystem, SysRoleSystem_.system);
			throw new ProvisioningException(AccResultCode.PROVISIONING_ROLE_ATTRIBUTE_MORE_UID,
					ImmutableMap.of("role", roleDto.getCode(), "system", systemDto.getName()));
		}

		SysRoleSystemAttributeDto uidRoleAttribute = !attributesUid.isEmpty() ? attributesUid.get(0) : null;

		// If roleSystem UID attribute found, then we use his transformation
		// script.
		if (uidRoleAttribute != null) {
			// Default values (values from schema attribute handling)
			SysSystemAttributeMappingDto systemAttributeMapping = DtoUtils.getEmbedded(uidRoleAttribute,
					SysRoleSystemAttribute_.systemAttributeMapping.getName(), SysSystemAttributeMappingDto.class);
			
			uidRoleAttribute.setSchemaAttribute(systemAttributeMapping.getSchemaAttribute());
			uidRoleAttribute.setTransformFromResourceScript(systemAttributeMapping.getTransformFromResourceScript());
			Object uid = systemAttributeMappingService.getAttributeValue(null, entity, uidRoleAttribute);
			if (uid == null) {
				SysSystemDto systemEntity = DtoUtils.getEmbedded(roleSystem, SysRoleSystem_.system);
				throw new ProvisioningException(AccResultCode.PROVISIONING_GENERATED_UID_IS_NULL,
						ImmutableMap.of("system", systemEntity.getName()));
			}
			if (!(uid instanceof String)) {
				throw new ProvisioningException(AccResultCode.PROVISIONING_ATTRIBUTE_UID_IS_NOT_STRING,
						ImmutableMap.of("uid", uid));
			}
			return (String) uid;
		}

		// If roleSystem UID was not found, then we use default UID schema
		// attribute handling
		SysSystemAttributeMappingFilter attributeMappingFilter = new SysSystemAttributeMappingFilter();
		attributeMappingFilter.setSystemMappingId(roleSystem.getSystemMapping());
		attributeMappingFilter.setIsUid(Boolean.TRUE);
		attributeMappingFilter.setDisabledAttribute(Boolean.FALSE);
		List<SysSystemAttributeMappingDto> defaultUidAttributes = systemAttributeMappingService
				.find(attributeMappingFilter, null).getContent();
		if(defaultUidAttributes.size() == 1) {
			return systemAttributeMappingService.generateUid(entity, defaultUidAttributes.get(0));
		}
		
		// Default UID attribute was not correctly found, getUidAttribute method will be throw exception.
		// This is good time for loading the system (is used in exception message)
		SysSystemMappingDto mapping = systemMappingService.get(roleSystem.getSystemMapping());
		SysSchemaObjectClassDto objectClassDto = schemaObjectClassService.get(mapping.getObjectClass());
		SysSystemDto system = DtoUtils.getEmbedded(objectClassDto, SysSchemaObjectClass_.system);
		systemAttributeMappingService.getUidAttribute(defaultUidAttributes, system);
		// Exception occurred
		return null;
	}

	@Override
	@Transactional
	public List<UUID> deleteIdentityAccount(IdmIdentityRoleDto entity) {
		AccIdentityAccountFilter filter = new AccIdentityAccountFilter();
		filter.setIdentityRoleId(entity.getId());
		List<UUID> accountIds = Lists.newArrayList();
		
		identityAccountService.find(filter, null).getContent() //
				.forEach(identityAccount -> { //
					accountIds.add(identityAccount.getAccount());
					identityAccountService.delete(identityAccount);
				});
		return accountIds;
	}
	
	@Override
	@Transactional
	public void deleteIdentityAccount(EntityEvent<IdmIdentityRoleDto> event) {
		Assert.notNull(event);
		IdmIdentityRoleDto identityRole = event.getContent();
		Assert.notNull(identityRole);
		Assert.notNull(identityRole.getId());
		//
		if (event.getRootId() == null || !entityEventManager.isRunnable(event.getRootId())) {
			// role is deleted without request or without any parent ... we need to remove account synchronously
			List<UUID> accountIds = deleteIdentityAccount(identityRole);
			// We needs accounts which were connected to deleted identity-role in next
			// processor (we want to execute provisioning only for that accounts)
			event.getProperties().put(ACCOUNT_IDS_FOR_DELETED_IDENTITY_ROLE, (Serializable) accountIds);
			return;
		}
		// Role is deleted in bulk (e.g. role request) - account management has to be called outside
		// we just mark identity account to be deleted and remove identity role
		AccIdentityAccountFilter filter = new AccIdentityAccountFilter();
		filter.setIdentityRoleId(identityRole.getId());

		identityAccountService.find(filter, null).getContent() //
				.forEach(identityAccount -> { //
					identityAccount.setIdentityRole(null);

					// Create entity state for identity account.
					IdmEntityStateDto stateDeleted = new IdmEntityStateDto();
					stateDeleted.setSuperOwnerId(identityAccount.getIdentity());
					stateDeleted.setResult(new OperationResultDto.Builder(OperationState.RUNNING)
							.setModel(new DefaultResultModel(CoreResultCode.DELETED)).build());
					entityStateManager.saveState(identityAccount, stateDeleted);
					//
					identityAccountService.save(identityAccount);
				});
	}
	
	/**
	 * Check identity-account for that role-system on change the definition of UID
	 * 
	 * @param roleSystem
	 * @param identityAccountList
	 * @param identityAccountsToDelete
	 */
	private void checkOnChangeUID(String uid, SysRoleSystemDto roleSystem,
			List<AccIdentityAccountDto> identityAccountList, List<AccIdentityAccountDto> identityAccountsToDelete) {
		identityAccountList.forEach(identityAccount -> { //
			if (roleSystem.getId().equals(identityAccount.getRoleSystem())) {
				// Has identity account same UID as account?
				AccAccountDto account = AccIdentityAccountService.getEmbeddedAccount(identityAccount);
				if (!uid.equals(account.getUid())) {
					// We found identityAccount for same identity and roleSystem, but this
					// identityAccount is link to Account with different UID. It's probably means
					// definition of UID
					// (transformation) on roleSystem was changed. We have to delete this
					// identityAccount.
					identityAccountsToDelete.add(identityAccount);
				}
			}
		});
	}

	/**
	 * Create Account by given roleSystem
	 * 
	 * @param identity
	 * @param roleSystem
	 * @param identityAccountsToCreate
	 * @return
	 */
	private AccAccountDto createAccountByRoleSystem(String uid, IdmIdentityDto identity, SysRoleSystemDto roleSystem,
			List<AccIdentityAccountDto> identityAccountsToCreate) {

		// We try find account for same UID on same system
		// First we try to search same account in list for create new accounts
		AccIdentityAccountDto sameAccount = identityAccountsToCreate.stream() //
				.filter(identityAccountToCreate -> {
					AccAccountDto account = DtoUtils.getEmbedded(identityAccountToCreate, AccIdentityAccount_.account.getName(), AccAccountDto.class);
					
					return account.getUid().equals(uid) 
							&& roleSystem.getId().equals(identityAccountToCreate.getRoleSystem());
				}).findFirst() //
				.orElse(null); //

		if (sameAccount != null) {
			return DtoUtils.getEmbedded(sameAccount, AccIdentityAccount_.account.getName(), AccAccountDto.class);
		}
		
		// If account is not in the list accounts to create, then we will search in
		// database

		// Account management - can be the account created? - execute the script on the
		// system mapping
		SysSystemDto system = DtoUtils.getEmbedded(roleSystem, SysRoleSystem_.system);
		SysSystemMappingDto mapping = systemMappingService.findProvisioningMapping(system.getId(),
				SystemEntityType.IDENTITY);
		if (mapping == null) {
			return null;
		}
		if (!this.canBeAccountCreated(uid, identity, mapping, system)) {
			LOG.info(MessageFormat.format(
					"For entity [{0}] and entity type [{1}] cannot be created the account (on system [{2}]),"
							+ " because script \"Can be account created\" on the mapping returned \"false\"!",
					identity.getCode(), SystemEntityType.IDENTITY, system.getName()));
			return null;
		}

		AccAccountFilter accountFilter = new AccAccountFilter();
		accountFilter.setUid(uid);
		accountFilter.setSystemId(roleSystem.getSystem());
		List<AccAccountDto> sameAccounts = accountService.find(accountFilter, null).getContent();
		if (CollectionUtils.isEmpty(sameAccounts)) {
			// Create and persist new account
			return createAccount(uid, roleSystem);
		} else {
			// We use existed account
			return sameAccounts.get(0);
		}

	}

	private AccAccountDto createAccount(String uid, SysRoleSystemDto roleSystem) {
		AccAccountDto account = new AccAccountDto();
		account.setUid(uid);
		account.setEntityType(SystemEntityType.IDENTITY);
		account.setAccountType(AccountType.PERSONAL);
		account.setSystem(roleSystem.getSystem());
		
		return accountService.save(account);
	}

	private boolean canBeAccountCreated(String uid, IdmIdentityDto dto, SysSystemMappingDto mapping,
			SysSystemDto system) {
		return systemMappingService.canBeAccountCreated(uid, dto, mapping.getCanBeAccountCreatedScript(), system);
	}
	
	/**
	 * Try to find identity-account for this identity-role. If exists and doesn't in
	 * list of identity-account to delete, then we are done.
	 * 
	 * @param identityAccountList
	 * @param identityAccountsToDelete
	 * @param identityRole
	 * @return
	 */
	private AccIdentityAccountDto findAlreadyExistsIdentityAccount(List<AccIdentityAccountDto> identityAccountList,
			List<AccIdentityAccountDto> identityAccountsToDelete, IdmIdentityRoleDto identityRole) {
		AccIdentityAccountDto existsIdentityAccount = identityAccountList.stream().filter(identityAccount -> {
			if (identityRole.getId().equals(identityAccount.getIdentityRole())
					&& !identityAccountsToDelete.contains(identityAccount)) {
				return true;
			}
			return false;
		}).findFirst().orElse(null);
		return existsIdentityAccount;
	}
}
