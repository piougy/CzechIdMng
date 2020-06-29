package eu.bcvsolutions.idm.vs.scheduler.task.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.quartz.DisallowConcurrentExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.dto.SysRoleSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.scheduler.api.service.AbstractSchedulableTaskExecutor;
import eu.bcvsolutions.idm.vs.dto.VsSystemDto;
import eu.bcvsolutions.idm.vs.exception.VsResultCode;
import eu.bcvsolutions.idm.vs.service.api.VsSystemService;


/**
 * Generator of virtual systems (for optimization testing only). Generates given number of systems, roles and
 * identities for performance test purpose. Generated entities are evenly
 * distributed. Generated roles are evenly assigned to created users. And
 * generate roles are evenly assigned to systems.

 * @author Ondrej Husnik
 *
 */
@DisallowConcurrentExecution
@Component(VsSystemGeneratorTaskExecutor.TASK_NAME)
public class VsSystemGeneratorTaskExecutor extends AbstractSchedulableTaskExecutor<Boolean> {
	
	@Autowired
	private VsSystemService vsSystemService;
	@Autowired
	private IdmRoleService roleService;
	@Autowired
	private SysRoleSystemService roleSystemService;
	@Autowired
	private IdmIdentityRoleService identityRoleService;
	@Autowired
	private IdmIdentityService identityService;
	@Autowired
	private IdmIdentityContractService identityContractService;
	@Autowired
	private SysSystemMappingService systemMappingService;
	
	public static final String TASK_NAME = "vs-system-generator-long-running-task";
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(VsSystemGeneratorTaskExecutor.class);
	
	public static final String SYSTEM_COUNT = "system-count";
	public static final String ITEM_PREFIX_KEY = "item-prefix";
	public static final String IDNENTITY_COUNT = "identity-count";
	public static final String ROLE_COUNT = "role-count";
	public static final String DEFAULT_PREFIX = "generatedVirtualSystem";
	private static final long DEFAULT_COUNT = 1L;
	
	private String description;
	private String itemPrefix;
	private int userCount;
	private int roleCount;
	private int systemCount;
	
	@Override
	public String getName() {
		return TASK_NAME;
	}
	
	@Override
	public void init(Map<String, Object> properties) {
		checkSuperAdminPermission();
		super.init(properties);
		// init system count
		systemCount = getParameterConverter().toLong(properties, SYSTEM_COUNT).intValue();
		// init itemPrefix
		itemPrefix = getParameterConverter().toString(properties, ITEM_PREFIX_KEY);
		// init count of identitiess
		userCount = getParameterConverter().toLong(properties, IDNENTITY_COUNT).intValue();
		// init role count  
		roleCount = getParameterConverter().toLong(properties, ROLE_COUNT).intValue();
		Assert.isTrue(systemCount >= 1, "At least 1 system needs to be used.");
		Assert.isTrue(userCount >= 1, "At least 1 identity needs to be used.");
		Assert.isTrue(roleCount >= 1, "At least 1 role needs to be used.");
		count = (long) userCount + roleCount + Math.max(roleCount, userCount) + Math.max(roleCount, systemCount);
		counter = 0L;
	}

	@Override
	public Boolean process() {
		generateSystems();
		return Boolean.TRUE;
	}
	
	@Override
	public List<String> getPropertyNames() {
		List<String> parameters = super.getPropertyNames();
		parameters.add(ITEM_PREFIX_KEY);
		parameters.add(SYSTEM_COUNT);
		parameters.add(IDNENTITY_COUNT);
		parameters.add(ROLE_COUNT);
		return parameters;
	}
	
	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = super.getProperties();
		properties.put(ITEM_PREFIX_KEY, itemPrefix);
		properties.put(SYSTEM_COUNT, systemCount);
		properties.put(IDNENTITY_COUNT, userCount);
		properties.put(ROLE_COUNT, roleCount);
		return properties;
	}
	
	@Override
	public List<IdmFormAttributeDto> getFormAttributes() {
		List<IdmFormAttributeDto> attributes = Lists.newArrayList();
		// name prefix
		IdmFormAttributeDto nameAttr = new IdmFormAttributeDto(ITEM_PREFIX_KEY, ITEM_PREFIX_KEY,
				PersistentType.SHORTTEXT);
		nameAttr.setRequired(true);
		nameAttr.setDefaultValue(DEFAULT_PREFIX);
		attributes.add(nameAttr);
		// number of generated systems
		IdmFormAttributeDto countAttr = new IdmFormAttributeDto(SYSTEM_COUNT, SYSTEM_COUNT, PersistentType.INT);
		countAttr.setDefaultValue(String.valueOf(DEFAULT_COUNT));
		countAttr.setRequired(true);
		attributes.add(countAttr);
		// user count
		IdmFormAttributeDto userCountAttr = new IdmFormAttributeDto(IDNENTITY_COUNT, IDNENTITY_COUNT,
				PersistentType.INT);
		userCountAttr.setRequired(true);
		userCountAttr.setDefaultValue(String.valueOf(DEFAULT_COUNT));
		attributes.add(userCountAttr);
		// role count
		IdmFormAttributeDto roleNumAttr = new IdmFormAttributeDto(ROLE_COUNT, ROLE_COUNT, PersistentType.INT);
		roleNumAttr.setRequired(true);
		roleNumAttr.setDefaultValue(String.valueOf(DEFAULT_COUNT));
		attributes.add(roleNumAttr);
		return attributes;
	}
    
	@Override
	public String getDescription() {
		if (description != null) {
			return description;
		}
		return super.getDescription();
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	@Override
	public boolean isRecoverable() {
		return true;
	}
    
	/**
	 * Creates universal name composed of defined prefix and UUID.randomUUID()
	 * string
	 * 
	 * @return
	 */
	private String createPrefixedName() {
		return String.format("%s-%s", itemPrefix, UUID.randomUUID());
	}
    
	/**
	 * Creates requested number of roles
	 * 
	 * @param roleCount
	 * @return
	 */
	private List<IdmRoleDto> createRole(int roleCount) {
		List<IdmRoleDto> dtos = new ArrayList<>(roleCount);
		for (int i = 0; i < roleCount; i++) {
			IdmRoleDto role = new IdmRoleDto();
			role.setCode(createPrefixedName());
			role = roleService.save(role);
			dtos.add(role);
			increaseCounter();
			if (!updateState()) {
				break;
			}
		}
		return dtos;
	}
	
	/**
	 * Creates requested number of identities
	 * 
	 * @param identityCount
	 * @return
	 */
	private List<IdmIdentityDto> createIdentity(int identityCount) {
		List<IdmIdentityDto> dtos = new ArrayList<>(identityCount);
		for (int i = 0; i < identityCount; i++) {
			IdmIdentityDto dto = new IdmIdentityDto();
			dto.setUsername(createPrefixedName());
			dto.setFirstName(createPrefixedName());
			dto.setLastName(createPrefixedName());
			dto.setEmail("test@vstest.eu");
			dto.setPhone("123456789");
			dto.setTitleBefore("titleBefore");
			dto.setTitleAfter("titleAfter");
			dto = identityService.save(dto);
			dtos.add(dto);
			increaseCounter();
			if (!updateState()) {
				break;
			}
		}
		return dtos;
	}
	
	/**
	 * Links roles with identities
	 * 
	 * @param roles
	 * @param identities
	 */
	private void createIdentityRole(List<IdmRoleDto> roles, List<IdmIdentityDto> identities) {
		IdmIdentityDto identity;
		IdmRoleDto role;
		boolean moreRoles = false;
		int moduloCnt = roles.size();
		int totalCnt = identities.size();

		if (roles.size() > identities.size()) {
			moreRoles = true;
			moduloCnt = identities.size();
			totalCnt = roles.size();
		}
		for (int i = 0; i < totalCnt; ++i) {
			if (moreRoles) {
				role = roles.get(i);
				identity = identities.get(i % moduloCnt);
			} else {
				role = roles.get(i % moduloCnt);
				identity = identities.get(i);
			}
			IdmIdentityContractDto contract = identityContractService.prepareMainContract(identity.getId());
			contract = identityContractService.save(contract);
			IdmIdentityRoleDto identityRole = new IdmIdentityRoleDto();
			identityRole.setIdentityContract(contract.getId());
			identityRole.setRole(role.getId());
			identityRoleService.save(identityRole);
			increaseCounter();
			if (!updateState()) {
				break;
			}
		}
	}
	

	/**
	 * Creates virtual system and adds a role to it
	 * 
	 * @param assignedRoleName
	 * @return
	 */
	private SysSystemDto createSystem(String assignedRoleName) {
		VsSystemDto vsDto = new VsSystemDto();
		vsDto.setName(createPrefixedName());
		vsDto.setRoleName(assignedRoleName);
		vsDto.setCreateDefaultRole(true);
		return vsSystemService.create(vsDto);
	}

	/**
	 * Main method aggregating all steps necessary to create systems
	 * 
	 * @return
	 */
	private void generateSystems() {
		List<IdmRoleDto> roles = createRole(roleCount);
		List<IdmIdentityDto> users = createIdentity(userCount);
		List<SysSystemDto> systems = new ArrayList<>(systemCount);
		createIdentityRole(roles, users);

		int moduloCnt = roles.size();
		int totalCnt = systemCount;
		boolean moreSystems = true;
		if (roles.size() > systemCount) {
			moreSystems = false;
			moduloCnt = systemCount;
			totalCnt = roles.size();
		}

		int roleIndex;
		int systemIndex;
		for (int i = 0; i < totalCnt; ++i) {
			roleIndex = moreSystems ? i % moduloCnt : i;
			systemIndex = moreSystems ? i : i % moduloCnt;

			if (systemIndex >= systems.size()) {
				systems.add(createSystem(roles.get(roleIndex).getName()));
			} else {
				SysSystemDto system = systems.get(systemIndex);
				IdmRoleDto role = roles.get(roleIndex);
				assignRoleToSystem(system, role);
			}
			increaseCounter();
			if (!updateState()) {
				break;
			}
		}
	}
	
	/**
	 * Method assigns a role to an existing system
	 * @param system
	 * @param role
	 */
	void assignRoleToSystem (SysSystemDto system, IdmRoleDto role) {
		List<SysSystemMappingDto> mappings = systemMappingService.findBySystem(system, SystemOperationType.PROVISIONING, SystemEntityType.IDENTITY);
		SysRoleSystemDto roleSystem = new SysRoleSystemDto();
		roleSystem.setSystem(system.getId());
		roleSystem.setSystemMapping(mappings.get(0).getId());
		roleSystem.setRole(role.getId());
		roleSystemService.save(roleSystem);
	}

	/**
	 * Checks if user has super admin permission, otherwise throws
	 */
	private void checkSuperAdminPermission() {
		if (!securityService.isAdmin()) {
			ResultCodeException e = new ResultCodeException(VsResultCode.VS_SYSTEM_GENERATOR_INSUFFICIENT_PERMISSION);
			LOG.warn("%s", e.getMessage());
			throw e;
		}
	}
}
