package eu.bcvsolutions.idm.core.eav.processor;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.CoreModuleDescriptor;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleFilter;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.service.IdmConfigurationService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.processor.pojo.EavValue;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmMessageDto;
import eu.bcvsolutions.idm.core.notification.api.service.NotificationManager;

/**
 * Processor listening for change in particular eav and then notify holder of current role
 *
 * @author Marek Klement
 * @since 10.2.0
 */
@Component(FormInstanceMonitoredFieldsProcessor.PROCESSOR_NAME)
@Description("Monitor fields extended attribute")
public class FormInstanceMonitoredFieldsProcessor extends CoreEventProcessor<IdmFormInstanceDto> {

	public static final String TOPIC = "EavMonitoredFieldsChanged";
	public static final String CONFIGURATION_PREFIX = IdmConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX + "core" +
			".event.notification.";
	public static final String ROLES_SUFFIX = ".roles";
	public static final String NAME_SUFFIX = ".attributesNames";
	static final String PROCESSOR_NAME = "core-form-instance-monitored-fields";
	private static final org.slf4j.Logger LOG =
			org.slf4j.LoggerFactory.getLogger(FormInstanceMonitoredFieldsProcessor.class);

	@Autowired
	private IdmRoleService roleService;
	@Autowired
	private IdmIdentityService identityService;
	@Autowired
	private NotificationManager notificationManager;
	@Autowired
	private LookupService lookupService;

	@Override
	public EventResult<IdmFormInstanceDto> process(EntityEvent<IdmFormInstanceDto> event) {
		IdmFormInstanceDto content = event.getContent();
		// get eav type
		String cutType = cutDto(content.getOwnerType().getSimpleName());
		String path = createPath(cutType);
		// get eav name
		List<String> eavs = getConfigurationService().getValues(path + NAME_SUFFIX);
		if (eavs == null || eavs.isEmpty()) {
			LOG.debug("None eav attributes to notify - set it in configuration!");
			return new DefaultEventResult<>(event, this);
		}
		// get eav values
		List<EavValue> valuesToNotify = getValuesToNotify(event, eavs);
		if (valuesToNotify.isEmpty()) {
			return new DefaultEventResult<>(event, this);
		}
		List<IdmRoleDto> roles = getRolesToNotify(getConfigurationService().getValues(path + ROLES_SUFFIX));
		List<IdmIdentityDto> recipients = new LinkedList<>();
		if (roles == null || roles.isEmpty()) {
			LOG.debug("None roles found in configuration.");
		} else {
			for (IdmRoleDto role : roles) {
				recipients.addAll(identityService.findAllByRole(role.getId()));
			}
		}
		if (CollectionUtils.isEmpty(recipients)) {
			LOG.debug("None recipients found in configuration.");
		}
		BaseDto baseDto = lookupService.lookupDto(content.getOwnerType(), content.getOwnerId());
		// now I should have roles, now I need to get current value
		String topic = cutType + TOPIC;
		IdmMessageDto message = new IdmMessageDto.Builder(NotificationLevel.WARNING)
				.addParameter("entity", baseDto)
				.addParameter("eavValues", valuesToNotify)
				.build();
		notificationManager.send(String.format(CoreModuleDescriptor.MODULE_ID +
						":%s", topic), message,
				recipients);
		return new DefaultEventResult<>(event, this);
	}

	private String createPath(String type) {
		return CONFIGURATION_PREFIX + type;
	}

	private String cutDto(String type){
		if (Objects.isNull(type)) {
			throw new IllegalArgumentException("OwnerType cannot be null!");
		}
		if (type.length() >= 3 && type.substring(type.length() - 3).equals("Dto")) {
			return type.substring(0, type.length() - 4);
		}
		return type;
	}

	@Override
	public int getOrder() {
		return 20000;
	}

	/**
	 * Main function to decide whether to send notification or not
	 *
	 * @param event
	 * @param eavs
	 * @return
	 */
	private List<EavValue> getValuesToNotify(EntityEvent<IdmFormInstanceDto> event, List<String> eavs) {
		// add needed values to new list
		final List<IdmFormValueDto> values = event.getContent().getValues();
		return values.stream()
				.flatMap(value -> iterateEavs(eavs, event, value))
				.collect(Collectors.toList());
	}

	private Stream<EavValue> iterateEavs(List<String> eavs,
										 EntityEvent<IdmFormInstanceDto> event, IdmFormValueDto value) {
		IdmFormAttributeDto formAttribute = event.getContent().getMappedAttribute(value.getFormAttribute());
		return eavs.stream()
				.filter(eav -> formAttribute.getCode().equals(eav) && event.getOriginalSource() != null && !event.getOriginalSource().getValues().isEmpty())
				.flatMap(eav -> addValues(formAttribute.getName(), event, value));
	}

	private Stream<EavValue> addValues(String name,
									   EntityEvent<IdmFormInstanceDto> event, IdmFormValueDto value) {
		List<IdmFormValueDto> pastValues = event.getOriginalSource().getValues();
		return pastValues.stream()
				.filter(pastValue -> pastValue.getFormAttribute().equals(value.getFormAttribute()) && !pastValue.isEquals(value))
				.map(pastValue -> new EavValue(name, pastValue, value));
	}

	/**
	 * Get all roles to be notified
	 *
	 * @param valuesRoles
	 * @return
	 */
	private List<IdmRoleDto> getRolesToNotify(List<String> valuesRoles) {
		if (valuesRoles == null || valuesRoles.isEmpty()) {
			return null;
		}
		List<IdmRoleDto> roles = new LinkedList<>();
		IdmRoleFilter filter = new IdmRoleFilter();
		for (String valueRole : valuesRoles) {
			filter.setBaseCode(valueRole);
			List<IdmRoleDto> result = roleService.find(filter, null).getContent();
			if (result.size() != 1) {
				LOG.debug("None role with name " + valueRole + " found in configuration.");
			}
			roles.add(result.get(0));
		}
		return roles;
	}
}
