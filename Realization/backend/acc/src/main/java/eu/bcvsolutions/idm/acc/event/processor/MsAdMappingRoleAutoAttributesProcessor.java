package eu.bcvsolutions.idm.acc.event.processor;

import com.google.common.collect.Sets;
import eu.bcvsolutions.idm.acc.connector.AdGroupConnectorType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.dto.SysSchemaAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.service.api.ConnectorManager;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.core.api.domain.IdmScriptCategory;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.service.IdmScriptService;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.model.entity.IdmRole_;
import eu.bcvsolutions.idm.core.script.evaluator.AbstractScriptEvaluator;
import eu.bcvsolutions.idm.ic.api.IcObjectClassInfo;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Component;

/**
 * Processor for automatic creation of role mapped attributes by common schema attributes for MS AD connector (MS Group AD+WinRM connector).
 *
 * @author Vít Švanda
 * @since 11.1.0
 */
@Component("accMsAdMappingRoleAutoAttributesProcessor")
@Description("Processor for automatic creation of role mapped attributes by common schema attributes for MS AD connector (MS Group AD+WinRM connector).")
public class MsAdMappingRoleAutoAttributesProcessor extends MsAdMappingIdentityAutoAttributesProcessor {

	private static final String PROCESSOR_NAME = "ms-ad-mapping-role-auto-attributes-processor";

	@Autowired
	private LookupService lookupService;
	@Autowired
	private IdmScriptService scriptService;
	@Autowired
	private ConnectorManager connectorManager;
	@Autowired
	private SysSystemAttributeMappingService systemAttributeMappingService;
	@Autowired
	private SysSystemMappingService systemMappingService;
	@Autowired
	private SysSchemaAttributeService schemaAttributeService;
	@Autowired
	private List<AbstractScriptEvaluator> evaluators;
	private PluginRegistry<AbstractScriptEvaluator, IdmScriptCategory> pluginExecutors;

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	SystemEntityType getSystemEntityType() {
		return SystemEntityType.ROLE;
	}

	@Override
	protected SystemOperationType getSystemOperationType() {
		return SystemOperationType.SYNCHRONIZATION;
	}

	@Override
	protected String getSchemaType() {
		return IcObjectClassInfo.GROUP;
	}

	@Override
	public EventResult<SysSystemMappingDto> process(EntityEvent<SysSystemMappingDto> event) {
		SysSystemMappingDto dto = event.getContent();
		UUID schemaId = dto.getObjectClass();
		if (schemaId == null) {
			return new DefaultEventResult<>(event, this);
		}

		List<SysSchemaAttributeDto> schemaAttributes = getSchemaAttributes(schemaId);

		// UID attribute.
		SysSchemaAttributeDto primarySchemaAttribute = getSchemaAttributeByCatalogue(schemaAttributes, this.getPrimaryKeyCatalogue());
		if (primarySchemaAttribute != null) {
			createAttributeMappingBySchemaAttribute(dto, primarySchemaAttribute, null, true);
		}
		
		// Code and name attribute.
		SysSchemaAttributeDto codeSchemaAttribute = getSchemaAttributeByCatalogue(schemaAttributes, this.getCodeCatalogue());
		if (codeSchemaAttribute != null) {
			createAttributeMappingBySchemaAttribute(dto, codeSchemaAttribute, IdmRole_.name.getName(), false);
			codeSchemaAttribute.setName(IdmRole_.code.getName());
			createAttributeMappingBySchemaAttribute(dto, codeSchemaAttribute, IdmRole_.code.getName(), false);
		}
		
		// PwdLastSet attribute (true only for create new account (force change of password))
//		schemaAttribute = getSchemaAttributeByCatalogue(schemaAttributes, Sets.newHashSet(PWD_LAST_SET_ATTRIBUTE_KEY));
//		if (schemaAttribute != null) {
//			SysSystemAttributeMappingDto attribute = createAttributeMappingByScriptToResource(dto, schemaAttribute, "return true;");
//			// Set attribute strategy as send only on create (true only for create new account (force change of password)).
//			attribute.setStrategyType(AttributeMappingStrategyType.CREATE);
//			systemAttributeMappingService.save(attribute);
//		}

		DefaultEventResult<SysSystemMappingDto> resultEvent = new DefaultEventResult<>(event, this);
		// Event will be end now. To prevent start default auto mapping processor.
		resultEvent.setSuspended(true);
		return resultEvent;
	}

	private Set<String> getPrimaryKeyCatalogue() {
		Set<String> catalogue = Sets.newLinkedHashSet();
		catalogue.add(AdGroupConnectorType.OBJECT_GUID_ATTRIBUTE);

		return catalogue;
	}
	
	private Set<String> getCodeCatalogue() {
		Set<String> catalogue = Sets.newLinkedHashSet();
		catalogue.add("name");
		return catalogue;
	}

	@Override
	public int getOrder() {
		// Run before default automatic mapping.
		return CoreEvent.DEFAULT_ORDER + 4;
	}

}
