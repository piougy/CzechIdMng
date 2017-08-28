package eu.bcvsolutions.idm.acc.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.domain.AttributeMapping;
import eu.bcvsolutions.idm.acc.domain.EntityAccount;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
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
import eu.bcvsolutions.idm.acc.exception.ProvisioningException;
import eu.bcvsolutions.idm.acc.repository.AccIdentityAccountRepository;
import eu.bcvsolutions.idm.acc.service.api.AccAccountManagementService;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityAccountService;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningExecutor;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.ic.service.api.IcConnectorFacade;

/**
 * Service for do Identity provisioning
 * 
 * @author svandav
 *
 */
@Service
@Qualifier(value=IdentityProvisioningExecutor.NAME)
public class IdentityProvisioningExecutor extends AbstractProvisioningExecutor<IdmIdentity> {
 
	public static final String NAME = "identityProvisioningService";
	private final AccIdentityAccountService identityAccountService;
	private final AccIdentityAccountRepository identityAccountRepository;
	private final SysRoleSystemService roleSystemService;
	
	@Autowired
	public IdentityProvisioningExecutor(SysSystemMappingService systemMappingService,
			SysSystemAttributeMappingService attributeMappingService, IcConnectorFacade connectorFacade,
			SysSystemService systemService, SysRoleSystemService roleSystemService,
			AccAccountManagementService accountManagementService,
			SysRoleSystemAttributeService roleSystemAttributeService, SysSystemEntityService systemEntityService,
			AccAccountService accountService, AccIdentityAccountService identityAccountService,
			AccIdentityAccountRepository identityAccountRepository,
			ProvisioningExecutor provisioningExecutor,
			EntityEventManager entityEventManager) {
		
		super(systemMappingService, attributeMappingService, connectorFacade, systemService, roleSystemService,
				accountManagementService, roleSystemAttributeService, systemEntityService, accountService,
				provisioningExecutor, entityEventManager);
		
		Assert.notNull(identityAccountService);
		Assert.notNull(roleSystemService);
		Assert.notNull(accountManagementService);
		Assert.notNull(identityAccountRepository);
		
		this.identityAccountService = identityAccountService;
		this.roleSystemService = roleSystemService;
		this.identityAccountRepository = identityAccountRepository;
	}
	
	public void doProvisioning(AccAccount account) {
		Assert.notNull(account);
		//
		identityAccountRepository.findAllByAccount_Id(account.getId())
			.stream()
			.filter(identityAccount -> {
				return identityAccount.isOwnership();
			})
			.forEach((identityAccount) -> {
				doProvisioning(account, identityAccount.getIdentity());
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
	@Override
	public List<AttributeMapping> resolveMappedAttributes(AccAccount account, IdmIdentity entity, SysSystem system, SystemEntityType entityType) {
		IdentityAccountFilter filter = new IdentityAccountFilter();
		filter.setIdentityId(entity.getId());
		filter.setSystemId(system.getId());
		filter.setOwnership(Boolean.TRUE);
		filter.setAccountId(account.getId());

		// All identity account with flag ownership on true
		List<AccIdentityAccount> identityAccounts = identityAccountService.find(filter, null).getContent()
				.stream()
				.map(dto -> {
					return identityAccountRepository.findOne(dto.getId());
				})
				.collect(Collectors.toList());

		// All role system attributes (overloading) for this uid and same system
		List<SysRoleSystemAttribute> roleSystemAttributesAll = findOverloadingAttributesIdentity(entity, system, identityAccounts, entityType);

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
	 * @param idenityAccoutnList
	 * @param operationType
	 * @param entityType
	 * @return
	 */
	@Deprecated
	private List<SysRoleSystemAttribute> findOverloadingAttributesIdentity(IdmIdentity entity, SysSystem system,
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

			if (roleSystems.size() > 1) {
				SysRoleSystem roleSystem = roleSystems.get(0);
				throw new ProvisioningException(AccResultCode.PROVISIONING_DUPLICATE_ROLE_MAPPING,
						ImmutableMap.of("role", roleSystem.getRole().getName(), "system",
								roleSystem.getSystem().getName(), "entityType", entityType));
			}
			if (!roleSystems.isEmpty()) {
				SysRoleSystem roleSystem = roleSystems.get(0);
				RoleSystemAttributeFilter roleSystemAttributeFilter = new RoleSystemAttributeFilter();
				roleSystemAttributeFilter.setRoleSystemId(roleSystem.getId());
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
	protected List<SysRoleSystemAttribute> findOverloadingAttributes(IdmIdentity entity, SysSystem system,
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
