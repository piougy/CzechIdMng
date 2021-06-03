package eu.bcvsolutions.idm.acc.event.processor;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.plugin.core.OrderAwarePluginRegistry;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Component;

import com.google.common.collect.Sets;

import eu.bcvsolutions.idm.acc.connector.AdUserConnectorType;
import eu.bcvsolutions.idm.acc.domain.AttributeMappingStrategyType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.dto.SysSchemaAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaObjectClassDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSchemaAttributeFilter;
import eu.bcvsolutions.idm.acc.entity.SysSchemaObjectClass_;
import eu.bcvsolutions.idm.acc.entity.SysSystemMapping_;
import eu.bcvsolutions.idm.acc.event.SystemMappingEvent.SystemMappingEventType;
import eu.bcvsolutions.idm.acc.service.api.ConnectorManager;
import eu.bcvsolutions.idm.acc.service.api.ConnectorType;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.core.api.domain.IdmScriptCategory;
import eu.bcvsolutions.idm.core.api.dto.IdmScriptDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmScriptFilter;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.service.IdmScriptService;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.script.evaluator.AbstractScriptEvaluator;
import eu.bcvsolutions.idm.ic.api.IcAttributeInfo;

/**
 * Processor for automatic creation of identity mapped attributes by common schema attributes for MS AD connector (MS AD+WinRM connector).
 *
 * @author Vít Švanda
 * @since 10.8.0
 */
@Component("accMsAdMappingIdentityAutoAttributesProcessor")
@Description("Processor for automatic creation of identity mapped attributes by common schema attributes for MS AD connector (MS AD+WinRM connector).")
public class MsAdMappingIdentityAutoAttributesProcessor extends AbstractSystemMappingAutoAttributesProcessor {

	private static final String PROCESSOR_NAME = "ms-ad-mapping-auto-attributes-processor";
	private static final String FULL_NAME_SCRIPT = "getFullName";
	private static final String DEFAULT_DN_SCRIPT = "getDefaultDN";
	private static final String USER_PRINCIPAL_NAME_SCRIPT = "getUserPrincipalName";
	private static final String ENABLE_SCRIPT = "getEnabled";
	private static final String PWD_LAST_SET_ATTRIBUTE_KEY = "pwdLastSet";

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

	@Autowired
	public MsAdMappingIdentityAutoAttributesProcessor() {
		super(SystemMappingEventType.CREATE);
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}


	@Override
	SystemEntityType getSystemEntityType() {
		return SystemEntityType.IDENTITY;
	}

	@Override
	public boolean conditional(EntityEvent<SysSystemMappingDto> event) {
		boolean conditional = super.conditional(event);
		if (!conditional) {
			return false;
		}

		SysSystemMappingDto systemMappingDto = event.getContent();
		// Attributes will be generated only for __ACCOUNT__ schema.
		SysSchemaObjectClassDto objectClassDto = lookupService.lookupEmbeddedDto(systemMappingDto, SysSystemMapping_.objectClass);
		if (objectClassDto != null && objectClassDto.getSystem() != null) {
			SysSystemDto systemDto = lookupService.lookupEmbeddedDto(objectClassDto, SysSchemaObjectClass_.system);
			ConnectorType connectorType = connectorManager.findConnectorTypeBySystem(systemDto);
			if (connectorType != null) {
				// Only for AD user and AD+WinRM user wizards.
				return connectorType instanceof AdUserConnectorType;
			}
		}
		return false;
	}

	@Override
	public EventResult<SysSystemMappingDto> process(EntityEvent<SysSystemMappingDto> event) {
		SysSystemMappingDto dto = event.getContent();
		UUID schemaId = dto.getObjectClass();
		if (schemaId == null) {
			return new DefaultEventResult<>(event, this);
		}

		List<SysSchemaAttributeDto> schemaAttributes = getSchemaAttributes(schemaId);

		// UID attribute
		SysSchemaAttributeDto primarySchemaAttribute = getSchemaAttributeByCatalogue(schemaAttributes, this.getPrimaryKeyCatalogue());
		if (primarySchemaAttribute != null) {
			createAttributeMappingBySchemaAttribute(dto, primarySchemaAttribute, IdmIdentity_.username.getName(), true);
		}

		// First name attribute
		SysSchemaAttributeDto schemaAttribute = getSchemaAttributeByCatalogue(schemaAttributes, this.getFirstNameCatalogue());
		if (schemaAttribute != null) {
			createAttributeMappingBySchemaAttribute(dto, schemaAttribute, IdmIdentity_.firstName.getName(), false);
		}

		// Last name attribute
		schemaAttribute = getSchemaAttributeByCatalogue(schemaAttributes, this.getLastNameCatalogue());
		if (schemaAttribute != null) {
			createAttributeMappingBySchemaAttribute(dto, schemaAttribute, IdmIdentity_.lastName.getName(), false);
		}

		// Display name attribute (script "getFullName")
		schemaAttribute = getSchemaAttributeByCatalogue(schemaAttributes, this.getDisplayName());
		if (schemaAttribute != null) {
			// Find "getFullName" script.
			createAttributeWithScript(dto, schemaAttribute, FULL_NAME_SCRIPT, IdmScriptCategory.DEFAULT);
		}

		// Enable attribute (script "getOppositeBoolean")
		SysSchemaAttributeFilter schemaAttributeFilter = new SysSchemaAttributeFilter();
		schemaAttributeFilter.setObjectClassId(schemaId);
		schemaAttributeFilter.setName(IcAttributeInfo.ENABLE);
		SysSchemaAttributeDto enableAttribute = schemaAttributeService.find(schemaAttributeFilter, null)
				.stream()
				.findFirst()
				.orElse(null);
		if (enableAttribute != null) {
			SysSystemAttributeMappingDto attributeEnableWithScript = createAttributeWithScript(dto, enableAttribute, ENABLE_SCRIPT, IdmScriptCategory.TRANSFORM_TO);
			if (attributeEnableWithScript != null) {
				attributeEnableWithScript.setEntityAttribute(true);
				attributeEnableWithScript.setIdmPropertyName(IdmIdentity_.disabled.getName());
				systemAttributeMappingService.save(attributeEnableWithScript);
			}
		}

		// Password attribute
		schemaAttributeFilter.setName(IcAttributeInfo.PASSWORD);
		SysSchemaAttributeDto passwordSchemaAttribute = schemaAttributeService.find(schemaAttributeFilter, null)
				.stream()
				.findFirst()
				.orElse(null);
		if (passwordSchemaAttribute != null) {
			SysSystemAttributeMappingDto passwordAttribute = createAttributeMappingBySchemaAttribute(dto, passwordSchemaAttribute, null, false);
			if (passwordAttribute != null) {
				passwordAttribute.setPasswordAttribute(true);
				systemAttributeMappingService.save(passwordAttribute);
			}
		}

		// Ldap groups (Merge)
		schemaAttributeFilter.setName(AdUserConnectorType.LDAP_GROUPS_ATTRIBUTE);
		SysSchemaAttributeDto ldapGroupsSchemaAttribute = schemaAttributeService.find(schemaAttributeFilter, null)
				.stream()
				.findFirst()
				.orElse(null);

		if (ldapGroupsSchemaAttribute != null) {
			SysSystemAttributeMappingDto ldapGroupsAttribute = createAttributeMappingBySchemaAttribute(dto, ldapGroupsSchemaAttribute, null, false);
			if (ldapGroupsAttribute != null) {
				ldapGroupsAttribute.setStrategyType(AttributeMappingStrategyType.MERGE);
				systemAttributeMappingService.save(ldapGroupsAttribute);
			}
		}

		// DN attribute ("__NAME__"). Use the getDefaultDN script.
		SysSchemaAttributeDto dnAttribute = getSchemaAttributeByCatalogue(schemaAttributes, this.getDNCode());
		if (dnAttribute != null) {
			SysSystemAttributeMappingDto attributeDnWithScript = createAttributeWithScript(dto, dnAttribute, DEFAULT_DN_SCRIPT, IdmScriptCategory.TRANSFORM_TO);
			if (attributeDnWithScript != null) {
				attributeDnWithScript.setEntityAttribute(true);
				attributeDnWithScript.setIdmPropertyName(IdmIdentity_.username.getName());
				systemAttributeMappingService.save(attributeDnWithScript);

				// Script "getDefaultDN" uses a connector object from provisioning context.
				// Add connectorObject to the context.
				if (!dto.isAddContextConnectorObject()) {
					dto.setAddContextConnectorObject(true);
					systemMappingService.save(dto);
				}
			}
		}

		// UserPrincipalName attribute. Use the getUserPrincipalName script.
		SysSchemaAttributeDto principleNameAttribute = getSchemaAttributeByCatalogue(schemaAttributes, this.getUserPrincipalName());
		if (principleNameAttribute != null) {
			SysSystemAttributeMappingDto attributeWithScript = createAttributeWithScript(dto, principleNameAttribute, USER_PRINCIPAL_NAME_SCRIPT, IdmScriptCategory.TRANSFORM_TO);
			if (attributeWithScript != null) {
				// By default disabled.
				attributeWithScript.setDisabledAttribute(true);
				systemAttributeMappingService.save(attributeWithScript);
			}
		}

		// Email attribute.
		schemaAttribute = getSchemaAttributeByCatalogue(schemaAttributes, this.getEmailCatalogue());
		if (schemaAttribute != null) {
			SysSystemAttributeMappingDto attribute = createAttributeMappingBySchemaAttribute(dto, schemaAttribute, IdmIdentity_.email.getName(), false);
			// Set attribute as disabled.
			attribute.setDisabledAttribute(true);
			systemAttributeMappingService.save(attribute);
		}

		// Department attribute.
		// TODO: department - Name of a org ("c" = "CZ", "co" = "Česká republika")
		// Manager attribute.
		// TODO: manager - DN of the manager according to the primary contract.

		// Employee ID attribute.
		schemaAttribute = getSchemaAttributeByCatalogue(schemaAttributes, this.getPersonalNumberCode());
		if (schemaAttribute != null) {
			SysSystemAttributeMappingDto attribute = createAttributeMappingBySchemaAttribute(dto, schemaAttribute, IdmIdentity_.externalCode.getName(), false);
			// Set attribute as send only if value exists in the IDM.
			attribute.setSendOnlyIfNotNull(true);
			systemAttributeMappingService.save(attribute);
		}

		// Phone attribute
		schemaAttribute = getSchemaAttributeByCatalogue(schemaAttributes, this.getPhoneCatalogue());
		if (schemaAttribute != null) {
			SysSystemAttributeMappingDto attribute = createAttributeMappingBySchemaAttribute(dto, schemaAttribute, IdmIdentity_.phone.getName(), false);
			// Set attribute as send only if value exists in the IDM.
			attribute.setSendOnlyIfNotNull(true);
			systemAttributeMappingService.save(attribute);
		}

		// PwdLastSet attribute (true only for create new account (force change of password))
		schemaAttribute = getSchemaAttributeByCatalogue(schemaAttributes, Sets.newHashSet(PWD_LAST_SET_ATTRIBUTE_KEY));
		if (schemaAttribute != null) {
			SysSystemAttributeMappingDto attribute = createAttributeMappingByScriptToResource(dto, schemaAttribute, "return true;");
			// Set attribute strategy as send only on create (true only for create new account (force change of password)).
			attribute.setStrategyType(AttributeMappingStrategyType.CREATE);
			systemAttributeMappingService.save(attribute);
		}

		DefaultEventResult<SysSystemMappingDto> resultEvent = new DefaultEventResult<>(event, this);
		// Event will be end now. To prevent start default auto mapping processor.
		resultEvent.setSuspended(true);
		return resultEvent;
	}

	protected SysSystemAttributeMappingDto createAttributeWithScript(
			SysSystemMappingDto dto,
			SysSchemaAttributeDto schemaAttribute,
			String scriptCode,
			IdmScriptCategory category) {
		return createAttributeWithScript(dto, schemaAttribute, scriptCode, category, true);
	}

	protected SysSystemAttributeMappingDto createAttributeWithScript(
			SysSystemMappingDto dto,
			SysSchemaAttributeDto schemaAttribute,
			String scriptCode,
			IdmScriptCategory category,
			boolean toResource) {
		IdmScriptFilter scriptFilter = new IdmScriptFilter();
		scriptFilter.setCode(scriptCode);
		scriptFilter.setCategory(category);

		IdmScriptDto scriptDto = scriptService.find(scriptFilter, null).getContent()
				.stream()
				.findFirst()
				.orElse(null);
		if (scriptDto != null) {
			String script = this.getPluginExecutors().getPluginFor(toResource ? IdmScriptCategory.TRANSFORM_TO : IdmScriptCategory.TRANSFORM_FROM)
					.generateTemplate(scriptDto);
			if (Strings.isNotBlank(script)) {
				return createAttributeMappingByScriptToResource(dto, schemaAttribute, script, toResource);
			}
		}
		return null;
	}

	/**
	 * Code catalogue for primary key (UID). Order in the catalogue is use in search.
	 */
	private Set<String> getPrimaryKeyCatalogue() {
		Set<String> catalogue = Sets.newLinkedHashSet();
		catalogue.add(AdUserConnectorType.SAM_ACCOUNT_NAME_ATTRIBUTE);

		return catalogue;
	}

	/**
	 * Code catalogue for first name. Order in the catalogue is use in search.
	 */
	private Set<String> getFirstNameCatalogue() {
		Set<String> catalogue = Sets.newLinkedHashSet();
		catalogue.add("givenname");
		catalogue.add("given_name");

		return catalogue;
	}

	/**
	 * Code catalogue for last name. Order in the catalogue is use in search.
	 */
	private Set<String> getLastNameCatalogue() {
		Set<String> catalogue = Sets.newLinkedHashSet();
		catalogue.add("sn");
		catalogue.add("surname");

		return catalogue;
	}

	/**
	 * Code for display name
	 */
	private Set<String> getDisplayName() {
		Set<String> catalogue = Sets.newLinkedHashSet();
		catalogue.add("displayName");

		return catalogue;
	}

	/**
	 * Code for __NAME__.
	 */
	private Set<String> getDNCode() {
		Set<String> catalogue = Sets.newLinkedHashSet();
		catalogue.add("__NAME__");

		return catalogue;
	}

	/**
	 * Code catalogue for email. Order in the catalogue is use in search.
	 */
	private Set<String> getEmailCatalogue() {
		Set<String> catalogue = Sets.newLinkedHashSet();
		catalogue.add("mail");
		catalogue.add("email");
		catalogue.add("e-mail");
		catalogue.add("e_mail");

		return catalogue;
	}

	/**
	 * Code catalogue for userPrincipalName . Order in the catalogue is use in search.
	 */
	private Set<String> getUserPrincipalName() {
		Set<String> catalogue = Sets.newLinkedHashSet();
		catalogue.add("userPrincipalName");

		return catalogue;
	}

	/**
	 * Code catalogue for personal number . Order in the catalogue is use in search.
	 */
	private Set<String> getPersonalNumberCode() {
		Set<String> catalogue = Sets.newLinkedHashSet();
		catalogue.add("employeeID");

		return catalogue;
	}


	/**
	 * Code catalogue for phone. Order in the catalogue is use in search.
	 */
	private Set<String> getPhoneCatalogue() {
		Set<String> catalogue = Sets.newLinkedHashSet();
		catalogue.add("phone");
		catalogue.add("telephone");
		catalogue.add("mobile");
		catalogue.add("mobilephone");
		catalogue.add("mobile_phone");

		return catalogue;
	}

	private PluginRegistry<AbstractScriptEvaluator, IdmScriptCategory> getPluginExecutors() {
		if (this.pluginExecutors == null) {
			this.pluginExecutors = OrderAwarePluginRegistry.create(evaluators);
		}
		return this.pluginExecutors;
	}

	@Override
	public int getOrder() {
		// Run before default automatic mapping.
		return CoreEvent.DEFAULT_ORDER + 5;
	}

}
