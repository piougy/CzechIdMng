package eu.bcvsolutions.idm.acc.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.domain.AttributeMapping;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.AccIdentityAccountDto;
import eu.bcvsolutions.idm.acc.dto.EntityAccountDto;
import eu.bcvsolutions.idm.acc.dto.SysRoleSystemAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysRoleSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccIdentityAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysRoleSystemAttributeFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysRoleSystemFilter;
import eu.bcvsolutions.idm.acc.entity.AccIdentityAccount_;
import eu.bcvsolutions.idm.acc.entity.SysRoleSystem_;
import eu.bcvsolutions.idm.acc.exception.ProvisioningException;
import eu.bcvsolutions.idm.acc.repository.AccIdentityAccountRepository;
import eu.bcvsolutions.idm.acc.service.api.AccAccountManagementService;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityAccountService;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningExecutor;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaObjectClassService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.ic.service.api.IcConnectorFacade;

/**
 * Service for do Identity provisioning
 * 
 * @author svandav
 * @author Radek Tomiška
 */
@Service
@Qualifier(value=IdentityProvisioningExecutor.NAME)
public class IdentityProvisioningExecutor extends AbstractProvisioningExecutor<IdmIdentityDto> {
 
	public static final String NAME = "identityProvisioningService";
	private final AccIdentityAccountService identityAccountService;
	private final AccIdentityAccountRepository identityAccountRepository;
	private final SysRoleSystemService roleSystemService;
	private final IdmRoleService roleService;
	private final IdmIdentityService identityService;
	
	@Autowired
	public IdentityProvisioningExecutor(
			SysSystemMappingService systemMappingService,
			SysSystemAttributeMappingService attributeMappingService, 
			IcConnectorFacade connectorFacade,
			SysSystemService systemService, 
			SysRoleSystemService roleSystemService,
			AccAccountManagementService accountManagementService,
			SysRoleSystemAttributeService roleSystemAttributeService, 
			SysSystemEntityService systemEntityService,
			AccAccountService accountService, 
			AccIdentityAccountService identityAccountService,
			AccIdentityAccountRepository identityAccountRepository,
			ProvisioningExecutor provisioningExecutor,
			EntityEventManager entityEventManager,
			SysSchemaObjectClassService schemaObjectClassService,
			SysSchemaAttributeService schemaAttributeService,
			SysSystemAttributeMappingService systemAttributeMappingService,
			IdmRoleService roleService,
			IdmIdentityService identityService) {
		
		super(systemMappingService, attributeMappingService, connectorFacade, systemService, roleSystemService,
				accountManagementService, roleSystemAttributeService, systemEntityService, accountService,
				provisioningExecutor, entityEventManager, schemaAttributeService, schemaObjectClassService,
				systemAttributeMappingService, roleService);
		//
		Assert.notNull(identityAccountService);
		Assert.notNull(roleSystemService);
		Assert.notNull(identityAccountRepository);
		Assert.notNull(roleService);
		Assert.notNull(identityService);
		//
		this.identityAccountService = identityAccountService;
		this.roleSystemService = roleSystemService;
		this.identityAccountRepository = identityAccountRepository;
		this.roleService = roleService;
		this.identityService = identityService;
	}
	
	public void doProvisioning(AccAccountDto account) {
		Assert.notNull(account);
		//
		AccIdentityAccountFilter filter = new AccIdentityAccountFilter();
		filter.setAccountId(account.getId());
		identityAccountService.find(filter, null).getContent()
			.stream()
			.filter(identityAccount -> {
				return identityAccount.isOwnership();
			})
			.forEach((identityAccount) -> {
				doProvisioning(account, DtoUtils.getEmbedded(identityAccount, AccIdentityAccount_.identity, IdmIdentityDto.class));
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
	public List<AttributeMapping> resolveMappedAttributes(AccAccountDto account, IdmIdentityDto entity, SysSystemDto system, SystemEntityType entityType) {
		AccIdentityAccountFilter filter = new AccIdentityAccountFilter();
		filter.setIdentityId(entity.getId());
		filter.setSystemId(system.getId());
		filter.setOwnership(Boolean.TRUE);
		filter.setAccountId(account.getId());

		// All identity account with flag ownership on true
		List<AccIdentityAccountDto> identityAccounts = identityAccountService.find(filter, null).getContent();

		// All role system attributes (overloading) for this uid and same system
		List<SysRoleSystemAttributeDto> roleSystemAttributesAll = findOverloadingAttributes(entity, system, identityAccounts, entityType);

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
	@Override
	protected List<SysRoleSystemAttributeDto> findOverloadingAttributes(IdmIdentityDto entity, SysSystemDto system,
			List<? extends EntityAccountDto> idenityAccoutnList, SystemEntityType entityType) {
		List<SysRoleSystemAttributeDto> roleSystemAttributesAll = new ArrayList<>();

		idenityAccoutnList.stream().filter(ia -> {
			AccAccountDto account = DtoUtils.getEmbedded((AccIdentityAccountDto)ia, AccIdentityAccount_.account, AccAccountDto.class);
			return ((AccIdentityAccountDto)ia).getIdentityRole() != null && account.getSystem() != null
					&& account.getSystem().equals(system.getId())
					&& ia.isOwnership();
		}).forEach((identityAccountInner) -> {
			AccIdentityAccountDto identityAccount = (AccIdentityAccountDto)identityAccountInner;
			// All identity account with same system and with filled
			// identityRole
			AccAccountDto account = DtoUtils.getEmbedded(identityAccount, AccIdentityAccount_.account, AccAccountDto.class);
			IdmIdentityRoleDto identityRole = DtoUtils.getEmbedded(identityAccount, AccIdentityAccount_.identityRole, IdmIdentityRoleDto.class);
			SysRoleSystemFilter roleSystemFilter = new SysRoleSystemFilter();
			roleSystemFilter.setRoleId(identityRole.getRole());
			roleSystemFilter.setSystemId(account.getSystem());
			List<SysRoleSystemDto> roleSystems = roleSystemService.find(roleSystemFilter, null).getContent();

			if (roleSystems.size() > 1) {
				SysRoleSystemDto roleSystem = roleSystems.get(0);
				IdmRoleDto roleDto = roleService.get(roleSystem.getRole());
				SysSystemDto systemDto = DtoUtils.getEmbedded(roleSystem, SysRoleSystem_.system, SysSystemDto.class);
				throw new ProvisioningException(AccResultCode.PROVISIONING_DUPLICATE_ROLE_MAPPING,
						ImmutableMap.of("role", roleDto.getName(), "system",
								systemDto.getName(), "entityType", entityType));
			}
			if (!roleSystems.isEmpty()) {
				SysRoleSystemDto roleSystem = roleSystems.get(0);
				SysRoleSystemAttributeFilter roleSystemAttributeFilter = new SysRoleSystemAttributeFilter();
				roleSystemAttributeFilter.setRoleSystemId(roleSystem.getId());
				List<SysRoleSystemAttributeDto> roleAttributes = roleSystemAttributeService
						.find(roleSystemAttributeFilter, null).getContent();

				if (!CollectionUtils.isEmpty(roleAttributes)) {
					roleSystemAttributesAll.addAll(roleAttributes);
				}
			}

		});

		return roleSystemAttributesAll;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected AccIdentityAccountFilter createEntityAccountFilter() {
		return new AccIdentityAccountFilter();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	protected AccIdentityAccountService getEntityAccountService() {
		return identityAccountService;
	}

	@Override
	protected EntityAccountDto createEntityAccountDto() {
		return new AccIdentityAccountDto();
	}

	@Override
	protected ReadWriteDtoService<IdmIdentityDto, ?> getService() {
		return identityService;
	}
}
