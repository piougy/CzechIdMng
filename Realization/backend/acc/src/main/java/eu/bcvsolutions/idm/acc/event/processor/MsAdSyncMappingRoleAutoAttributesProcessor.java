package eu.bcvsolutions.idm.acc.event.processor;

import com.google.common.collect.Sets;
import eu.bcvsolutions.idm.acc.connector.AdGroupConnectorType;
import eu.bcvsolutions.idm.acc.connector.AdUserConnectorType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.dto.SysSchemaAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSchemaAttributeFilter;
import eu.bcvsolutions.idm.acc.service.api.ConnectorManager;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.acc.service.impl.RoleSynchronizationExecutor;
import eu.bcvsolutions.idm.core.api.domain.IdmScriptCategory;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.service.IdmScriptService;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.model.entity.IdmRole_;
import eu.bcvsolutions.idm.core.script.evaluator.AbstractScriptEvaluator;
import eu.bcvsolutions.idm.ic.api.IcAttributeInfo;
import eu.bcvsolutions.idm.ic.api.IcObjectClassInfo;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Component;

/**
 * Processor for automatic creation of role mapped attributes (for sync) by common schema attributes for MS AD connector (MS Group AD+WinRM connector).
 *
 * @author Vít Švanda
 * @since 11.1.0
 */
@Component("accMsAdSyncMappingRoleAutoAttributesProcessor")
@Description("Processor for automatic creation of role mapped attributes (for sync) by common schema attributes for MS AD connector (MS Group AD+WinRM connector).")
public class MsAdSyncMappingRoleAutoAttributesProcessor extends MsAdMappingIdentityAutoAttributesProcessor {

	private static final String PROCESSOR_NAME = "ms-ad-sync-mapping-role-auto-attributes-processor";
	private static final String RESOLVE_ROLE_CATALOG_BY_DN_SCRIPT = "resolveRoleCatalogueByDn";
	private static final String RESOLVE_ROLE_CATALOG_UNDER_MAIN_SCRIPT = "resolveRoleCatalogueUnderMainCatalogue";
	public static final String MEMBER_ATTR_CODE = "member";

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
			codeSchemaAttribute.setName("Role name");
			createAttributeMappingBySchemaAttribute(dto, codeSchemaAttribute, IdmRole_.name.getName(), false);
			codeSchemaAttribute.setName("Role code");
			createAttributeMappingBySchemaAttribute(dto, codeSchemaAttribute, IdmRole_.baseCode.getName(), false);
		}

		// Attribute for resolve role catalogue.
		SysSchemaAttributeFilter schemaAttributeFilter = new SysSchemaAttributeFilter();
		schemaAttributeFilter.setObjectClassId(schemaId);
		schemaAttributeFilter.setName(AdUserConnectorType.DN_ATTR_CODE);
		SysSchemaAttributeDto dnAttribute = schemaAttributeService.find(schemaAttributeFilter, null)
				.stream()
				.findFirst()
				.orElse(null);
		if (dnAttribute != null) {
			dnAttribute.setName("Role catalog");
			SysSystemAttributeMappingDto attributeCatalogWithScript = 
					createAttributeWithScript(dto, dnAttribute, RESOLVE_ROLE_CATALOG_UNDER_MAIN_SCRIPT, IdmScriptCategory.TRANSFORM_FROM, false);
			if (attributeCatalogWithScript != null) {
				attributeCatalogWithScript.setEntityAttribute(true);
				attributeCatalogWithScript.setIdmPropertyName(RoleSynchronizationExecutor.ROLE_CATALOGUE_FIELD);
				systemAttributeMappingService.save(attributeCatalogWithScript);
			}
		}
		
		// Attribute for resolve membership. Returns DN of role by default.
		if (dnAttribute != null) {
			dnAttribute.setName("Membership (DN)");
			createAttributeMappingBySchemaAttribute(dto,
					dnAttribute,
					RoleSynchronizationExecutor.ROLE_MEMBERSHIP_ID_FIELD,
					false);
		}
		
		// Attribute for resolve forwardAcm. Returns true by default.
		if (dnAttribute != null) {
			dnAttribute.setName("Forward ACM");
			SysSystemAttributeMappingDto forwardAcmAttribute = createAttributeMappingBySchemaAttribute(dto,
					dnAttribute,
					RoleSynchronizationExecutor.ROLE_FORWARD_ACM_FIELD,
					false);
			forwardAcmAttribute.setTransformFromResourceScript("return true;");
			systemAttributeMappingService.save(forwardAcmAttribute);
		}
		
		// Attribute for resolve "Skip value if contract excluded". Returns true by default.
		if (dnAttribute != null) {
			dnAttribute.setName("Skip value if contract excluded");
			SysSystemAttributeMappingDto skipValueIfExcludedAttribute = createAttributeMappingBySchemaAttribute(dto,
					dnAttribute,
					RoleSynchronizationExecutor.ROLE_SKIP_VALUE_IF_EXCLUDED_FIELD,
					false);
			skipValueIfExcludedAttribute.setTransformFromResourceScript("return true;");
			systemAttributeMappingService.save(skipValueIfExcludedAttribute);
		}

		// Attribute returns List of members (user's DNs).
		schemaAttributeFilter.setName(MEMBER_ATTR_CODE);
		SysSchemaAttributeDto memberAttribute = schemaAttributeService.find(schemaAttributeFilter, null)
				.stream()
				.findFirst()
				.orElse(null);
		if (memberAttribute != null) {
			createAttributeMappingBySchemaAttribute(dto, memberAttribute, RoleSynchronizationExecutor.ROLE_MEMBERS_FIELD, false);
		}

		DefaultEventResult<SysSystemMappingDto> resultEvent = new DefaultEventResult<>(event, this);
		// Event will be end now. To prevent start default auto mapping processor.
		resultEvent.setSuspended(true);
		return resultEvent;
	}

	private Set<String> getPrimaryKeyCatalogue() {
		Set<String> catalogue = Sets.newLinkedHashSet();
		catalogue.add(IcAttributeInfo.UID);
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
