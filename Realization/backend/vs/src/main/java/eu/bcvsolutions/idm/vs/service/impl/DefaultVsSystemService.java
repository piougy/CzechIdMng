package eu.bcvsolutions.idm.vs.service.impl;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.acc.domain.AttributeMappingStrategyType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.dto.SysConnectorKeyDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaObjectClassDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSchemaAttributeFilter;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormAttributeService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.ic.api.IcAttributeInfo;
import eu.bcvsolutions.idm.ic.api.IcConnector;
import eu.bcvsolutions.idm.ic.api.IcConnectorInfo;
import eu.bcvsolutions.idm.ic.api.IcObjectClassInfo;
import eu.bcvsolutions.idm.ic.api.annotation.IcConnectorClass;
import eu.bcvsolutions.idm.ic.czechidm.domain.CzechIdMIcConvertUtil;
import eu.bcvsolutions.idm.vs.connector.api.VsVirtualConnector;
import eu.bcvsolutions.idm.vs.connector.basic.BasicVirtualConfiguration;
import eu.bcvsolutions.idm.vs.connector.basic.BasicVirtualConnector;
import eu.bcvsolutions.idm.vs.dto.VsSystemDto;
import eu.bcvsolutions.idm.vs.entity.VsAccount;
import eu.bcvsolutions.idm.vs.service.api.VsSystemService;

/**
 * Service for virtual system
 * 
 * @author Svanda
 *
 */
@Service
public class DefaultVsSystemService implements VsSystemService {

	private static final Logger LOG = LoggerFactory.getLogger(DefaultVsSystemService.class);

	private final SysSystemService systemService;
	private final FormService formService;
	private final IdmFormAttributeService formAttributeService;
	private final SysSystemMappingService systemMappingService;
	private final SysSystemAttributeMappingService systemAttributeMappingService;
	private final SysSchemaAttributeService schemaAttributeService;

	@Autowired
	public DefaultVsSystemService(SysSystemService systemService, FormService formService,
			SysSystemMappingService systemMappingService,
			SysSystemAttributeMappingService systemAttributeMappingService,
			SysSchemaAttributeService schemaAttributeService, IdmFormAttributeService formAttributeService) {
		Assert.notNull(systemService);
		Assert.notNull(formService);
		Assert.notNull(systemMappingService);
		Assert.notNull(systemAttributeMappingService);
		Assert.notNull(schemaAttributeService);
		Assert.notNull(formAttributeService);
		//
		this.systemService = systemService;
		this.formService = formService;
		this.systemMappingService = systemMappingService;
		this.systemAttributeMappingService = systemAttributeMappingService;
		this.schemaAttributeService = schemaAttributeService;
		this.formAttributeService = formAttributeService;
	}

	@Override
	public SysSystemDto create(VsSystemDto vsSystem) {
		Assert.notNull(vsSystem, "Vs system dto cannot be null (for create new virtual system)");
		Assert.notNull(vsSystem.getName(), "Vs system name cannot be null (for create new virtual system)");
		LOG.info("Create new virtual system with name [{}].", vsSystem.getName());

		SysSystemDto system = new SysSystemDto();

		// Find connector for VS
		Class<? extends VsVirtualConnector> defaultVirtualConnector = BasicVirtualConnector.class;
		IcConnectorClass connectorAnnotation = defaultVirtualConnector.getAnnotation(IcConnectorClass.class);
		IcConnectorInfo info = CzechIdMIcConvertUtil.convertConnectorClass(connectorAnnotation,
				(Class<? extends IcConnector>) defaultVirtualConnector);

		// Set connector key for VS
		system.setConnectorKey(new SysConnectorKeyDto(info.getConnectorKey()));
		system.setName(vsSystem.getName());
		// Create system
		system = this.systemService.save(system, IdmBasePermission.CREATE);

		// Find and update attributes for implementers
		IdmFormDefinitionDto connectorFormDef = this.systemService
				.getConnectorFormDefinition(system.getConnectorInstance());
		IdmFormAttributeDto implementersFormAttr = connectorFormDef.getMappedAttributeByCode(IMPLEMENTERS_PROPERTY);
		formService.saveValues(system, implementersFormAttr, new ArrayList<>(vsSystem.getImplementers()));
		IdmFormAttributeDto implementerRolesFormAttr = connectorFormDef
				.getMappedAttributeByCode(IMPLEMENTER_ROLES_PROPERTY);
		formService.saveValues(system, implementerRolesFormAttr, new ArrayList<>(vsSystem.getImplementerRoles()));
		IdmFormAttributeDto attributesFormAttr = connectorFormDef.getMappedAttributeByCode(ATTRIBUTES_PROPERTY);
		if (!vsSystem.getAttributes().isEmpty()) {
			formService.saveValues(system, attributesFormAttr, new ArrayList<>(vsSystem.getAttributes()));
		} else {
			List<Serializable> defaultAttributes = Lists
					.newArrayList((Serializable[]) BasicVirtualConfiguration.DEFAULT_ATTRIBUTES);
			defaultAttributes.add(RIGHTS_ATTRIBUTE);
			formService.saveValues(system, attributesFormAttr, defaultAttributes);
		}
		
		this.systemService.checkSystem(system);

		// Search attribute definition for rights and set him to multivalue
		String virtualSystemKey = MessageFormat.format("{0}:systemId={1}", system.getConnectorKey().getFullName(),
				system.getId().toString());
		String type = VsAccount.class.getName();
		IdmFormDefinitionDto definition = this.formService.getDefinition(type, virtualSystemKey);
		IdmFormAttributeDto rightsFormAttr = formAttributeService.findAttribute(type, definition.getCode(),
				RIGHTS_ATTRIBUTE);
		if(rightsFormAttr != null){
			rightsFormAttr.setMultiple(true);
			formService.saveAttribute(rightsFormAttr);
		}

		// Generate schema
		List<SysSchemaObjectClassDto> schemas = this.systemService.generateSchema(system);
		SysSchemaObjectClassDto schemaAccount = schemas.stream()
				.filter(schema -> IcObjectClassInfo.ACCOUNT.equals(schema.getObjectClassName())).findFirst()
				.orElse(null);
		Assert.notNull(schemaAccount, "We cannot found schema for ACCOUNT!");

		// Create mapping by default attributes
		this.createDefaultMapping(system, schemaAccount, vsSystem);

		return this.systemService.get(system.getId());
	}

	/**
	 * Create default mapping for virtual system by given default attributes
	 * 
	 * @param system
	 * @param schema
	 * @param vsSystem
	 */
	private void createDefaultMapping(SysSystemDto system, SysSchemaObjectClassDto schema, VsSystemDto vsSystem) {
		SysSystemMappingDto systemMapping = new SysSystemMappingDto();
		systemMapping.setName("Default provisioning");
		systemMapping.setEntityType(SystemEntityType.IDENTITY);
		systemMapping.setOperationType(SystemOperationType.PROVISIONING);
		systemMapping.setObjectClass(schema.getId());
		systemMapping = systemMappingService.save(systemMapping);

		SysSchemaAttributeFilter schemaAttributeFilter = new SysSchemaAttributeFilter();
		schemaAttributeFilter.setSystemId(system.getId());
		List<SysSchemaAttributeDto> schemaAttributes = schemaAttributeService.find(schemaAttributeFilter, null)
				.getContent();
		ArrayList<String> defaultAttributes = Lists.newArrayList(BasicVirtualConfiguration.DEFAULT_ATTRIBUTES);
		List<String> attributes = vsSystem.getAttributes().isEmpty() ? defaultAttributes : vsSystem.getAttributes();
		for (SysSchemaAttributeDto schemaAttr : schemaAttributes) {
			if (IcAttributeInfo.NAME.equals(schemaAttr.getName())) {
				SysSystemAttributeMappingDto attributeMapping = new SysSystemAttributeMappingDto();
				attributeMapping.setUid(true);
				attributeMapping.setEntityAttribute(true);
				attributeMapping.setIdmPropertyName(IdmIdentity_.username.getName());
				attributeMapping.setName(schemaAttr.getName());
				attributeMapping.setSchemaAttribute(schemaAttr.getId());
				attributeMapping.setSystemMapping(systemMapping.getId());
				systemAttributeMappingService.save(attributeMapping);
			} else if (IcAttributeInfo.ENABLE.equals(schemaAttr.getName())) {
				SysSystemAttributeMappingDto attributeMapping = new SysSystemAttributeMappingDto();
				attributeMapping.setUid(false);
				attributeMapping.setEntityAttribute(true);
				attributeMapping.setIdmPropertyName(IdmIdentity_.disabled.getName());
				attributeMapping.setTransformToResourceScript("return !attributeValue;");
				attributeMapping.setName(schemaAttr.getName());
				attributeMapping.setSchemaAttribute(schemaAttr.getId());
				attributeMapping.setSystemMapping(systemMapping.getId());
				systemAttributeMappingService.save(attributeMapping);
			} else if (RIGHTS_ATTRIBUTE.equals(schemaAttr.getName())) {
				SysSystemAttributeMappingDto attributeMapping = new SysSystemAttributeMappingDto();
				attributeMapping.setUid(false);
				attributeMapping.setEntityAttribute(false);
				attributeMapping.setStrategyType(AttributeMappingStrategyType.MERGE);
				attributeMapping.setExtendedAttribute(false);
				attributeMapping.setName("'Rights' - multivalued merge attribute. ");
				attributeMapping.setSchemaAttribute(schemaAttr.getId());
				attributeMapping.setSystemMapping(systemMapping.getId());
				systemAttributeMappingService.save(attributeMapping);
			} else if (attributes.contains(schemaAttr.getName()) && defaultAttributes.contains(schemaAttr.getName())) {
				SysSystemAttributeMappingDto attributeMapping = new SysSystemAttributeMappingDto();
				attributeMapping.setUid(false);
				attributeMapping.setEntityAttribute(true);
				attributeMapping.setIdmPropertyName(schemaAttr.getName());
				attributeMapping.setSchemaAttribute(schemaAttr.getId());
				attributeMapping.setName(schemaAttr.getName());
				attributeMapping.setSystemMapping(systemMapping.getId());
				systemAttributeMappingService.save(attributeMapping);
			}
		}
	}
}
