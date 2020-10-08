package eu.bcvsolutions.idm.acc.event.processor;

import com.google.common.collect.Sets;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.dto.SysSchemaAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSchemaAttributeFilter;
import eu.bcvsolutions.idm.acc.event.SystemMappingEvent.SystemMappingEventType;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

/**
 * Processor for automatic creation of identity mapped attributes by common schema attributes.
 *
 * @author Vít Švanda
 */
@Component("accSystemMappingIdentityAutoAttributesProcessor")
@Description("Processor for automatic creation of identity mapped attributes by common schema attributes.")
public class SystemMappingIdentityAutoAttributesProcessor extends AbstractSystemMappingAutoAttributesProcessor {

	private static final String PROCESSOR_NAME = "system-mapping-auto-attributes-processor";
	@Autowired
	private SysSchemaAttributeService schemaAttributeService;

	@Autowired
	public SystemMappingIdentityAutoAttributesProcessor() {
		super(SystemMappingEventType.CREATE);
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

		// Email attribute
		schemaAttribute = getSchemaAttributeByCatalogue(schemaAttributes, this.getEmailCatalogue());
		if (schemaAttribute != null) {
			createAttributeMappingBySchemaAttribute(dto, schemaAttribute, IdmIdentity_.email.getName(), false);
		}

		// Title before attribute
		schemaAttribute = getSchemaAttributeByCatalogue(schemaAttributes, this.getTitleBeforeCatalogue());
		if (schemaAttribute != null) {
			createAttributeMappingBySchemaAttribute(dto, schemaAttribute, IdmIdentity_.titleBefore.getName(), false);
		}

		// Title after attribute
		schemaAttribute = getSchemaAttributeByCatalogue(schemaAttributes, this.getTitleAfterCatalogue());
		if (schemaAttribute != null) {
			createAttributeMappingBySchemaAttribute(dto, schemaAttribute, IdmIdentity_.titleAfter.getName(), false);
		}

		// Phone attribute
		schemaAttribute = getSchemaAttributeByCatalogue(schemaAttributes, this.getPhoneCatalogue());
		if (schemaAttribute != null) {
			createAttributeMappingBySchemaAttribute(dto, schemaAttribute, IdmIdentity_.phone.getName(), false);
		}

		return new DefaultEventResult<>(event, this);
	}

	@Override
	public boolean conditional(EntityEvent<SysSystemMappingDto> event) {
		if (SystemEntityType.IDENTITY != event.getContent().getEntityType()) {
			return false;
		}
		if (event.getBooleanProperty(SysSystemMappingService.ENABLE_AUTOMATIC_CREATION_OF_MAPPING)) {
			return super.conditional(event);
		}
		return false;
	}

	/**
	 * Code catalogue for primary key (UID). Order in the catalogue is use in search.
	 */
	private Set<String> getPrimaryKeyCatalogue() {
		Set<String> catalogue = Sets.newLinkedHashSet();
		catalogue.add("__NAME__");
		catalogue.add("__UID__");
		catalogue.add("username");
		catalogue.add("login");

		return catalogue;
	}

	/**
	 * Code catalogue for first name. Order in the catalogue is use in search.
	 */
	private Set<String> getFirstNameCatalogue() {
		Set<String> catalogue = Sets.newLinkedHashSet();
		catalogue.add("firstname");
		catalogue.add("first_name");
		catalogue.add("givenname");
		catalogue.add("given_name");

		return catalogue;
	}

	/**
	 * Code catalogue for last name. Order in the catalogue is use in search.
	 */
	private Set<String> getLastNameCatalogue() {
		Set<String> catalogue = Sets.newLinkedHashSet();
		catalogue.add("lastname");
		catalogue.add("last_name");
		catalogue.add("surname");
		catalogue.add("familyname");
		catalogue.add("family_name");
		catalogue.add("cognomen");

		return catalogue;
	}

	/**
	 * Code catalogue for email. Order in the catalogue is use in search.
	 */
	private Set<String> getEmailCatalogue() {
		Set<String> catalogue = Sets.newLinkedHashSet();
		catalogue.add("email");
		catalogue.add("e-mail");
		catalogue.add("e_mail");

		return catalogue;
	}

	/**
	 * Code catalogue for title before. Order in the catalogue is use in search.
	 */
	private Set<String> getTitleBeforeCatalogue() {
		Set<String> catalogue = Sets.newLinkedHashSet();
		catalogue.add("titlebefore");
		catalogue.add("title_before");
		catalogue.add("title");
		catalogue.add("degree");

		return catalogue;
	}

	/**
	 * Code catalogue for title after. Order in the catalogue is use in search.
	 */
	private Set<String> getTitleAfterCatalogue() {
		Set<String> catalogue = Sets.newLinkedHashSet();
		catalogue.add("titleafter");
		catalogue.add("title_after");

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

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public int getOrder() {
		return CoreEvent.DEFAULT_ORDER + 10;
	}

}
