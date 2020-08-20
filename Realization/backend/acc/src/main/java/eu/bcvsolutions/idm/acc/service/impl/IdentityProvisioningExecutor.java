package eu.bcvsolutions.idm.acc.service.impl;

import eu.bcvsolutions.idm.acc.domain.MappingContext;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.acc.domain.AssignedRoleDto;
import eu.bcvsolutions.idm.acc.domain.AttributeMapping;
import eu.bcvsolutions.idm.acc.domain.AttributeMappingStrategyType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.AccIdentityAccountDto;
import eu.bcvsolutions.idm.acc.dto.EntityAccountDto;
import eu.bcvsolutions.idm.acc.dto.SysRoleSystemAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysRoleSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccIdentityAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysRoleSystemAttributeFilter;
import eu.bcvsolutions.idm.acc.entity.AccIdentityAccount_;
import eu.bcvsolutions.idm.acc.entity.SysRoleSystemAttribute_;
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
import eu.bcvsolutions.idm.core.api.dto.AbstractIdmAutomaticRoleDto;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.IdmContractPositionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleCompositionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityContractFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormValue_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.ic.service.api.IcConnectorFacade;

/**
 * Service for do Identity provisioning
 * 
 * @author Vít Švanda
 * @author Radek Tomiška
 */
@Service
@Qualifier(value = IdentityProvisioningExecutor.NAME)
public class IdentityProvisioningExecutor extends AbstractProvisioningExecutor<IdmIdentityDto> {

	public static final String NAME = "identityProvisioningService";
	public final static String ASSIGNED_ROLES_FIELD = "assignedRoles";
	public final static String ASSIGNED_ROLES_FOR_SYSTEM_FIELD = "assignedRolesForSystem";

	private final AccIdentityAccountService identityAccountService;
	private final IdmIdentityService identityService;
	private final AccAccountManagementService accountManagementService;
	@Autowired
	private IdmIdentityRoleService identityRoleService;
	@Autowired
	private IdmIdentityContractService identityContractService;
	@Autowired
	private LookupService lookupService;

	@Autowired
	public IdentityProvisioningExecutor(SysSystemMappingService systemMappingService,
			SysSystemAttributeMappingService attributeMappingService, IcConnectorFacade connectorFacade,
			SysSystemService systemService, SysRoleSystemService roleSystemService,
			AccAccountManagementService accountManagementService,
			SysRoleSystemAttributeService roleSystemAttributeService, SysSystemEntityService systemEntityService,
			AccAccountService accountService, AccIdentityAccountService identityAccountService,
			ProvisioningExecutor provisioningExecutor, EntityEventManager entityEventManager,
			SysSchemaObjectClassService schemaObjectClassService, SysSchemaAttributeService schemaAttributeService,
			SysSystemAttributeMappingService systemAttributeMappingService, IdmRoleService roleService,
			IdmIdentityService identityService) {

		super(systemMappingService, attributeMappingService, connectorFacade, systemService, roleSystemService,
				roleSystemAttributeService, systemEntityService, accountService, provisioningExecutor,
				entityEventManager, schemaAttributeService, schemaObjectClassService, systemAttributeMappingService,
				roleService);
		//
		Assert.notNull(identityAccountService, "Service is required.");
		Assert.notNull(roleSystemService, "Service is required.");
		Assert.notNull(roleService, "Service is required.");
		Assert.notNull(identityService, "Service is required.");
		Assert.notNull(accountManagementService, "Service is required.");
		//
		this.identityAccountService = identityAccountService;
		this.identityService = identityService;
		this.accountManagementService = accountManagementService;
	}

	@Override
	public void doProvisioning(AccAccountDto account) {
		Assert.notNull(account, "Account is required.");
		//
		AccIdentityAccountFilter filter = new AccIdentityAccountFilter();
		filter.setAccountId(account.getId());
		identityAccountService.find(filter, null).getContent().stream().filter(identityAccount -> {
			return identityAccount.isOwnership();
		}).forEach((identityAccount) -> {
			doProvisioning(account,
					DtoUtils.getEmbedded(identityAccount, AccIdentityAccount_.identity, IdmIdentityDto.class));
		});
	}

	@Override
	/**
	 * Identities have own implementation of ACM
	 */
	public boolean accountManagement(IdmIdentityDto dto) {
		return accountManagementService.resolveIdentityAccounts(dto);
	}

	/**
	 * Return list of all overloading attributes for given identity, system and uid
	 * 
	 * @param entity
	 * @param system
	 * @param entityType
	 * @param account
	 * @return
	 */
	@Override
	protected List<SysRoleSystemAttributeDto> findOverloadingAttributes(IdmIdentityDto entity, SysSystemDto system,
			AccAccountDto account, SystemEntityType entityType) {
		
		SysSystemMappingDto mapping = getMapping(system, entityType);
		
		List<SysRoleSystemAttributeDto> roleSystemAttributesAll = new ArrayList<>();

		if(mapping == null) {
			return roleSystemAttributesAll;
		}
		// Search overridden attributes for this account (searching via
		// identity-accounts -> identity-roles -> role-systems ->
		// role-system-attributes)
		
		// !Strange behavior - attributes are searching via assigned identity-accounts,
		// it means in some case aren't returns all overridden attributes for this
		// system and assigned roles! For example, when UID is changed, then exists old
		// and new accounts in same time and we cannot return overridden UID attribute
		// for original account. 
		// TODO: Investigate and prevent situation when exists two accounts in same time (change the UID).
		SysRoleSystemAttributeFilter roleSystemAttributeFilter = new SysRoleSystemAttributeFilter();
		roleSystemAttributeFilter.setSystemMappingId(mapping.getId());
		roleSystemAttributeFilter.setIdentityId(entity.getId());
		roleSystemAttributeFilter.setAccountId(account.getId());
		List<SysRoleSystemAttributeDto> roleAttributes = roleSystemAttributeService
				.find(roleSystemAttributeFilter, null).getContent();

		if (!CollectionUtils.isEmpty(roleAttributes)) {
			roleSystemAttributesAll.addAll(roleAttributes);
		}
		
		return roleSystemAttributesAll;
	}

	@Override
	protected Object getAttributeValue(String uid, IdmIdentityDto dto, AttributeMapping attribute,
			SysSystemDto system, MappingContext mappingContext) {
		
		if (attribute instanceof SysRoleSystemAttributeDto) {
			SysRoleSystemAttributeDto roleSystemAttributeDto = (SysRoleSystemAttributeDto) attribute;
			if (roleSystemAttributeDto.isSkipValueIfExcluded() &&
					(AttributeMappingStrategyType.MERGE == roleSystemAttributeDto.getStrategyType() ||
					AttributeMappingStrategyType.AUTHORITATIVE_MERGE == roleSystemAttributeDto.getStrategyType())) {
				
				// Get ID of the role
				Assert.notNull(roleSystemAttributeDto.getRoleSystem(), "SysRoleSystem cannot be null!");	
				SysRoleSystemDto roleSystemDto = DtoUtils.getEmbedded(roleSystemAttributeDto,
						SysRoleSystemAttribute_.roleSystem.getName(), SysRoleSystemDto.class, (SysRoleSystemDto) null);
				if(roleSystemDto == null) {
					roleSystemDto = roleSystemService.get(roleSystemAttributeDto.getId());
				}
				UUID roleId = roleSystemDto.getRole();
				Assert.notNull(roleId, "Role cannot be null!");
				
				// Find count of NOT excluded contracts for this identity and role
				IdmIdentityContractFilter contractFilter = new IdmIdentityContractFilter();
				contractFilter.setIdentity(dto.getId());
				contractFilter.setExcluded(Boolean.FALSE);
				contractFilter.setRoleId(roleId);
				// If exists some not excluded contract, then value will be not skipped!
				long countOfNotExcludedContracts = identityContractService.count(contractFilter);
				if (countOfNotExcludedContracts == 0) {
					contractFilter.setExcluded(Boolean.TRUE);
					// For skip the value must exist at least one excluded contract
					long countOfexcludedContracts = identityContractService.count(contractFilter);
					if (countOfexcludedContracts >= 0) {
						return null;
					}
				}
				
			}
		}

		// If assigned roles fields are mapped, then we will searching and convert
		// identity-roles to list of AssignedRoleDtos (including values of EAV for 
		// identity-roles). That list will be input for that fields.
		if (attribute != null //
				&& (ASSIGNED_ROLES_FIELD.equals(attribute.getIdmPropertyName()) //
						|| ASSIGNED_ROLES_FOR_SYSTEM_FIELD.equals(attribute.getIdmPropertyName()) //
				)) { //
			assertNotNull(dto.getId());

			IdmIdentityRoleFilter identityRoleFilter = new IdmIdentityRoleFilter();
			identityRoleFilter.setIdentityId(dto.getId());
			identityRoleFilter.setValid(Boolean.TRUE);
			List<IdmIdentityRoleDto> identityRoles = identityRoleService
					.find(identityRoleFilter,
							PageRequest.of(0, Integer.MAX_VALUE, Sort.by(IdmIdentityRole_.created.getName())))
					.getContent();
			List<IdmIdentityRoleDto> identityRolesToProcess;

			if (ASSIGNED_ROLES_FOR_SYSTEM_FIELD.equals(attribute.getIdmPropertyName())) {
				// For ASSIGNED_ROLES_FOR_SYSTEM_FIELD we will convert only identity-roles for
				// that identity and given system
				assertNotNull(system.getId());

				List<IdmIdentityRoleDto> identityRolesForSystem = Lists.newArrayList();
				AccIdentityAccountFilter identityAccountFilter = new AccIdentityAccountFilter();
				identityAccountFilter.setIdentityId(dto.getId());
				identityAccountFilter.setSystemId(system.getId());
				List<AccIdentityAccountDto> identityAccounts = identityAccountService.find(identityAccountFilter, null)
						.getContent();

				// Filtering only identity-roles for that system
				identityAccounts.forEach(identityAccount -> {
					identityRolesForSystem.addAll(identityRoles.stream() //
							.filter(identityRole -> identityRole.getId().equals(identityAccount.getIdentityRole())) //
							.collect(Collectors.toList()) //
					);
				});

				identityRolesToProcess = identityRolesForSystem;
			} else {
				// For ASSIGNED_ROLES_FIELD we will convert all identity-roles for that identity
				identityRolesToProcess = identityRoles;
			}

			List<AssignedRoleDto> assignedRoles = new ArrayList<>();
			identityRolesToProcess.forEach(identityRole -> {
				IdmFormInstanceDto formInstanceDto = identityRoleService.getRoleAttributeValues(identityRole);
				identityRole.getEavs().clear();
				identityRole.getEavs().add(formInstanceDto);
				// Convert identityRole to AssignedRoleDto
				assignedRoles.add(IdentityProvisioningExecutor.convertToAssignedRoleDto(identityRole));
			});

			return attributeMappingService.transformValueToResource(uid, assignedRoles, attribute, dto);
		}
		// For user-type (projection) will be attribute value IdmFormProjectionDto.
		if (attribute != null
				&& dto != null
				&& dto.getFormProjection() != null
				&& IdmIdentity_.formProjection.getName().equals(attribute.getIdmPropertyName())
				) {
			
			BaseDto projection = lookupService.lookupEmbeddedDto(dto, IdmIdentity_.formProjection);
			return attributeMappingService.transformValueToResource(uid, projection, attribute, dto);
		}
		return super.getAttributeValue(uid, dto, attribute, system, mappingContext);
	}

	public static AssignedRoleDto convertToAssignedRoleDto(IdmIdentityRoleDto identityRole) {
		if (identityRole == null) {
			return null;
		}

		AssignedRoleDto dto = new AssignedRoleDto();
		dto.setId(identityRole.getId());
		dto.setExternalId(identityRole.getExternalId());
		dto.setValidFrom(identityRole.getValidFrom());
		dto.setValidTill(identityRole.getValidTill());
		dto.setRole(DtoUtils.getEmbedded(identityRole, IdmIdentityRole_.role, IdmRoleDto.class, null));
		dto.setIdentityContract(DtoUtils.getEmbedded(identityRole, IdmIdentityRole_.identityContract,
				IdmIdentityContractDto.class, null));
		dto.setContractPosition(DtoUtils.getEmbedded(identityRole, IdmIdentityRole_.contractPosition,
				IdmContractPositionDto.class, null));
		dto.setDirectRole(
				DtoUtils.getEmbedded(identityRole, IdmIdentityRole_.directRole, IdmIdentityRoleDto.class, null));
		dto.setRoleTreeNode(DtoUtils.getEmbedded(identityRole, IdmIdentityRoleDto.PROPERTY_ROLE_TREE_NODE,
				AbstractIdmAutomaticRoleDto.class, null));
		dto.setRoleComposition(DtoUtils.getEmbedded(identityRole, IdmIdentityRole_.roleComposition,
				IdmRoleCompositionDto.class, null));

		UUID definition = dto.getRole().getIdentityRoleAttributeDefinition();
		if (definition != null) {
			// Definition for role attributes exists
			IdmFormInstanceDto formInstanceDto = identityRole.getEavs() //
					.stream() //
					.filter(formInstance -> definition.equals(formInstance.getFormDefinition().getId())) //
					.findFirst() //
					.orElse(null);

			if (formInstanceDto != null) {
				List<IdmFormValueDto> values = formInstanceDto.getValues();
				values.stream() // Search all attributes
						.map(IdmFormValueDto::getFormAttribute) //
						.distinct() //
						.forEach(attribute -> {

							List<IdmFormValueDto> formValues = values.stream() // Search all values for one attribute
									.filter(value -> attribute.equals(value.getFormAttribute())) //
									.collect(Collectors.toList()); //
							IdmFormAttributeDto formAttributeDto = DtoUtils.getEmbedded(formValues.get(0),
									IdmFormValue_.formAttribute, IdmFormAttributeDto.class);

							dto.getAttributes().put(formAttributeDto.getCode(), formValues.stream() //
									.map(IdmFormValueDto::getValue) // Value is always list
									.collect(Collectors.toList()) //
							);
						});
			}
		}

		return dto;

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
