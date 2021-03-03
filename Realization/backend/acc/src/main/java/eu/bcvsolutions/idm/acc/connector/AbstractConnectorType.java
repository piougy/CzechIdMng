package eu.bcvsolutions.idm.acc.connector;

import com.google.common.collect.ImmutableMap;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.dto.AbstractSysSyncConfigDto;
import eu.bcvsolutions.idm.acc.dto.ConnectorTypeDto;
import eu.bcvsolutions.idm.acc.dto.SysRoleSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaObjectClassDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysRoleSystemFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSchemaObjectClassFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSyncConfigFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemMappingFilter;
import eu.bcvsolutions.idm.acc.event.SystemMappingEvent;
import eu.bcvsolutions.idm.acc.service.api.ConnectorType;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaObjectClassService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncConfigService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeTypeDto;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmTreeTypeService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * Connector type extends standard IC connector for more metadata (image, wizard, ...).
 *
 * @author Vít Švanda
 * @since 10.7.0
 */
public abstract class AbstractConnectorType implements
		ConnectorType,
		BeanNameAware {

	private String beanName; // spring bean name - used as id

	@Autowired
	private IdmRoleService roleService;
	@Autowired
	private SysSystemService systemService;
	@Autowired
	private SysRoleSystemService roleSystemService;
	@Autowired
	private SysSystemMappingService systemMappingService;
	@Autowired
	private FormService formService;
	@Autowired
	private SysSchemaObjectClassService schemaService;
	@Autowired
	private SysSyncConfigService syncConfigService;
	@Autowired
	private IdmTreeTypeService treeTypeService;

	@Override
	public void setBeanName(String name) {
		this.beanName = name;
	}

	@Override
	public String getId() {
		return beanName;
	}

	@Override
	public Map<String, String> getMetadata() {
		return new HashMap<>();
	}

	@Override
	@Transactional
	public ConnectorTypeDto execute(ConnectorTypeDto connectorType) {
		Assert.notNull(connectorType.getWizardStepName(), "Wizard step name have to be filled for execute a connector type.");
		if (STEP_FINISH.equals(connectorType.getWizardStepName())) {
			executeStepFinish(connectorType);
		} else if (STEP_MAPPING.equals(connectorType.getWizardStepName())) {
			executeMappingStep(connectorType);
		}
		return connectorType;
	}

	@Override
	public ConnectorTypeDto load(ConnectorTypeDto connectorType) {
		if (!connectorType.isReopened()) {
			// Load default value for new system.
			connectorType.setMetadata(this.getMetadata());
			return connectorType;
		}
		// Load the system.
		SysSystemDto systemDto = (SysSystemDto) connectorType.getEmbedded().get(SYSTEM_DTO_KEY);
		Assert.notNull(systemDto, "System must exists!");
		connectorType.getEmbedded().put(SYSTEM_DTO_KEY, systemDto);
		// Set remote server ID to the connector type.
		connectorType.setRemoteServer(systemDto.getRemoteServer());

		// Load the mapping.
		SysSystemMappingFilter mappingFilter = new SysSystemMappingFilter();
		mappingFilter.setSystemId(systemDto.getId());
		List<SysSystemMappingDto> mappingDtos = systemMappingService.find(mappingFilter, null)
				.getContent()
				.stream()
				.sorted(Comparator.comparing(SysSystemMappingDto::getCreated))
				.collect(Collectors.toList());
		// Show alert if more mappings exists.
		if (mappingDtos.size() > 1) {
			connectorType.getMetadata().put(ALERT_MORE_MAPPINGS, Boolean.TRUE.toString());
		}
		SysSystemMappingDto mappingDto = mappingDtos.stream().findFirst().orElse(null);
		connectorType.getEmbedded().put(MAPPING_DTO_KEY, mappingDto);

		// Load the sync.
		SysSyncConfigFilter syncFilter = new SysSyncConfigFilter();
		syncFilter.setSystemId(systemDto.getId());
		if (mappingDto != null) {
			syncFilter.setSystemMappingId(mappingDto.getId());
		}
		AbstractSysSyncConfigDto syncDto = syncConfigService.find(syncFilter, null)
				.getContent()
				.stream()
				.min(Comparator.comparing(AbstractDto::getCreated))
				.orElse(null);
		connectorType.getEmbedded().put(SYNC_DTO_KEY, syncDto);

		return connectorType;
	}

	/**
	 * Execute last step of default wizard. Creates role with that system.
	 */
	private void executeStepFinish(ConnectorTypeDto connectorType) {
		// Validations:
		String createRoleWithSystem = connectorType.getMetadata().get(CREATES_ROLE_WITH_SYSTEM);
		if (!(Boolean.parseBoolean(createRoleWithSystem))) {
			return;
		}
		createRoleSystem(connectorType);
	}

	/**
	 * Creates role with that system.
	 */
	protected IdmRoleDto createRoleSystem(ConnectorTypeDto connectorType) {
		String newRoleWithSystemCode = connectorType.getMetadata().get(NEW_ROLE_WITH_SYSTEM_CODE);
		Assert.isTrue(Strings.isNotBlank(newRoleWithSystemCode), "Code of the role cannot be null!");

		String systemId = connectorType.getMetadata().get(SYSTEM_DTO_KEY);
		Assert.notNull(systemId, "System ID cannot be null!");
		SysSystemDto systemDto = systemService.get(UUID.fromString(systemId), IdmBasePermission.READ);
		Assert.notNull(systemDto, "System cannot be null!");

		String mappingId = connectorType.getMetadata().get(MAPPING_ID);
		Assert.notNull(mappingId, "Mapping ID cannot be null!");
		SysSystemMappingDto mappingDto = systemMappingService.get(UUID.fromString(mappingId), IdmBasePermission.READ);
		Assert.notNull(mappingDto, "Mapping cannot be null!");

		IdmRoleDto newRole = roleService.getByCode(newRoleWithSystemCode);
		if (newRole == null) {
			newRole = new IdmRoleDto();
			newRole.setCode(newRoleWithSystemCode);
			newRole.setName(newRoleWithSystemCode);
			newRole.setPriority(0);
			newRole = roleService.save(newRole);
		}

		SysRoleSystemFilter systemFilter = new SysRoleSystemFilter();
		systemFilter.setRoleId(newRole.getId());
		systemFilter.setSystemId(systemDto.getId());

		List<SysRoleSystemDto> systemRoles = roleSystemService.find(systemFilter, null).getContent();

		if (systemRoles.isEmpty()) {
			SysRoleSystemDto systemRole = new SysRoleSystemDto();
			systemRole.setRole(newRole.getId());
			systemRole.setSystem(systemDto.getId());
			systemRole.setSystemMapping(mappingDto.getId());
			systemRole = roleSystemService.save(systemRole);

			connectorType.getMetadata().put(ROLE_SYSTEM_ID, systemRole.getId().toString());
		}
		return newRole;
	}

	/**
	 * Execute simple mapping step.
	 *
	 * @param connectorTypeDto
	 */
	private void executeMappingStep(ConnectorTypeDto connectorTypeDto) {

		String schemaId = connectorTypeDto.getMetadata().get(SCHEMA_ID);
		SysSchemaObjectClassDto schemaDto = null;
		if (schemaId != null) {
			schemaDto = schemaService.get(UUID.fromString(schemaId), IdmBasePermission.READ);
		}else {
			String systemId = connectorTypeDto.getMetadata().get(SYSTEM_DTO_KEY);
			SysSchemaObjectClassFilter filter = new SysSchemaObjectClassFilter();
			Assert.isTrue(Strings.isNotBlank(systemId), "System ID cannot be empty!");
			filter.setSystemId(UUID.fromString(systemId));
			List<SysSchemaObjectClassDto> schemas = schemaService.find(filter, null, IdmBasePermission.READ)
					.getContent()
					.stream()
					.sorted(Comparator.comparing(SysSchemaObjectClassDto::getCreated))
					.collect(Collectors.toList());
			if (!schemas.isEmpty()){
				schemaDto = schemas.get(0);
			}
		}
		Assert.notNull(schemaDto, "System schema must exists!");

		String entityType = connectorTypeDto.getMetadata().get(ENTITY_TYPE);
		SystemEntityType systemEntityType = SystemEntityType.valueOf(entityType);
		Assert.notNull(systemEntityType, "Entity type cannot be null!");

		// For tree type have to be filled tree type ID too.
		IdmTreeTypeDto treeTypeDto = null;
		if (SystemEntityType.TREE == systemEntityType) {
			String treeTypeId = connectorTypeDto.getMetadata().get(TREE_TYPE_ID);
			Assert.notNull(treeTypeId, "Tree type ID cannot be null for TREE entity type!");
			treeTypeDto = treeTypeService.get(UUID.fromString(treeTypeId));
			Assert.notNull(treeTypeDto, "Tree type DTO cannot be null for TREE entity type!");
		}

		String operationType = connectorTypeDto.getMetadata().get(OPERATION_TYPE);
		SystemOperationType systemOperationType = SystemOperationType.valueOf(operationType);
		Assert.notNull(systemOperationType, "Operation type cannot be null!");

		// Load existing mapping or create new one.
		String mappingId = connectorTypeDto.getMetadata().get(MAPPING_ID);
		SysSystemMappingDto mappingDto = new SysSystemMappingDto();
		mappingDto.setName("Mapping");
		boolean isNew = true;
		if (mappingId != null) {
			SysSystemMappingDto mappingExisted = systemMappingService.get(mappingId, IdmBasePermission.READ);
			if (mappingExisted != null) {
				isNew = false;
				mappingDto = mappingExisted;
			}
		}
		// For tree type have to be filled tree type ID too.
		if (SystemEntityType.TREE == systemEntityType) {
			mappingDto.setTreeType(treeTypeDto.getId());
		}
		mappingDto.setEntityType(systemEntityType);
		mappingDto.setOperationType(systemOperationType);
		mappingDto.setObjectClass(schemaDto.getId());
		// Save mapping. Event must be publish with property for enable automatic mapping.
		mappingDto = systemMappingService.publish(
				new SystemMappingEvent(
						isNew ? SystemMappingEvent.SystemMappingEventType.CREATE : SystemMappingEvent.SystemMappingEventType.UPDATE,
						mappingDto,
						ImmutableMap.of(SysSystemMappingService.ENABLE_AUTOMATIC_CREATION_OF_MAPPING, Boolean.TRUE)),
				isNew ? IdmBasePermission.CREATE : IdmBasePermission.UPDATE)
				.getContent();

		connectorTypeDto.getEmbedded().put(MAPPING_DTO_KEY, mappingDto);
	}

	protected void setValueToConnectorInstance(String attributeCode, Serializable value, SysSystemDto systemDto, IdmFormDefinitionDto connectorFormDef) {
		IdmFormAttributeDto attribute = connectorFormDef.getMappedAttributeByCode(attributeCode);
		List<Serializable> values = new ArrayList<>();
		values.add(value);
		formService.saveValues(systemDto, attribute, values);
	}
	
	protected void setValueToConnectorInstance(String attributeCode, List<Serializable> values, SysSystemDto systemDto, IdmFormDefinitionDto connectorFormDef) {
		IdmFormAttributeDto attribute = connectorFormDef.getMappedAttributeByCode(attributeCode);;
		formService.saveValues(systemDto, attribute, values);
	}

	protected String getValueFromConnectorInstance(String attributeCode, SysSystemDto systemDto, IdmFormDefinitionDto connectorFormDef) {
		IdmFormAttributeDto attribute = connectorFormDef.getMappedAttributeByCode(attributeCode);
		if (attribute != null) {
			List<IdmFormValueDto> values = formService.getValues(systemDto, attribute, IdmBasePermission.READ);
			if (values != null && values.size() == 1) {
				return values.get(0).getValue().toString();
			}
		}
		return null;
	}
	
	protected String getConfidentialValueFromConnectorInstance(String attributeCode, SysSystemDto systemDto, IdmFormDefinitionDto connectorFormDef) {
		IdmFormAttributeDto attribute = connectorFormDef.getMappedAttributeByCode(attributeCode);
		if (attribute == null) {
			return null;
		}
		List<IdmFormValueDto> values = formService.getValues(systemDto, attribute, IdmBasePermission.READ);
		if (values != null && values.size() == 1) {
			return formService.getConfidentialPersistentValue(values.get(0)).toString();
		}
		return null;
	}

	/**
	 * Find unique system name. If name already exist, then will iterate postfix.
	 */
	protected String findUniqueSystemName(String name, int i) {
		SysSystemFilter filter = new SysSystemFilter();
		if (i > 1) {
			filter.setText(MessageFormat.format("{0} {1}", name, i));
		} else {
			filter.setText(name);
		}
		if (!systemService.find(filter, null).hasContent()) {
			return filter.getText();
		}
		return findUniqueSystemName(name, i + 1);

	}

	@Override
	public boolean supports() {
		return true;
	}

	@Override
	public boolean supportsSystem(SysSystemDto systemDto) {
		if (systemDto.getConnectorKey() == null) {
			return false;
		}
		String connectorName = systemDto.getConnectorKey().getConnectorName();
		return this.getConnectorName().equals(connectorName);
	}

	protected SysSystemService getSystemService() {
		return systemService;
	}

	protected FormService getFormService() {
		return formService;
	}
}
