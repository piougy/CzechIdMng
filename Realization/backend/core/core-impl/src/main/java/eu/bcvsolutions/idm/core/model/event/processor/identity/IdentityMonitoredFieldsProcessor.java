package eu.bcvsolutions.idm.core.model.event.processor.identity;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.base.Strings;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.IdentityProcessor;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.utils.EntityUtils;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent.IdentityEventType;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmMessageDto;
import eu.bcvsolutions.idm.core.notification.api.service.IdmNotificationTemplateService;
import eu.bcvsolutions.idm.core.notification.api.service.NotificationManager;

/**
 * Check if defined fields on identity was changed. If yes, then send
 * notification.
 * 
 * @author Svanda
 *
 */
@Component
@Description("Check if defined fields on identity was changed. If yes, then send notification. (Extended attributes is not supported now)")
public class IdentityMonitoredFieldsProcessor
		extends CoreEventProcessor<IdmIdentityDto> 
		implements IdentityProcessor {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IdentityMonitoredFieldsProcessor.class);

	public static final String PROCESSOR_NAME = "identity-monitored-fields-processor";
	/**
	 * Monitored fields on change
	 */
	public static final String PROPERTY_MONITORED_FIELDS = "monitoredFields";
	/**
	 * Name of role. All identity with this role are recipient notification
	 */
	public static final String PROPERTY_RECIPIENTS_ROLE = "recipientsRole";
	public static final String TOPIC = "identityMonitoredFieldsChanged";
	public static final String EMAIL_TEMPLATE = "identityMonitoredFieldsChanged";

	private final IdmIdentityService service;
	private final NotificationManager notificationManager;
	private final ConfigurationService configurationService;

	@Autowired
	public IdentityMonitoredFieldsProcessor(IdmIdentityService service, 
		NotificationManager notificationManager,
		IdmNotificationTemplateService templateService,
		ConfigurationService configurationService) {
		super(IdentityEventType.UPDATE);
		//
		Assert.notNull(service);
		Assert.notNull(notificationManager);
		Assert.notNull(configurationService);
		//
		this.service = service;
		this.notificationManager = notificationManager;
		this.configurationService = configurationService;
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmIdentityDto> process(EntityEvent<IdmIdentityDto> event) {
		List<String> fields = getCommaSeparatedValues((String) this.getConfigurationMap().get(PROPERTY_MONITORED_FIELDS));
		String recipientsRole = (String) this.getConfigurationMap().get(PROPERTY_RECIPIENTS_ROLE);
		
		if(CollectionUtils.isEmpty(fields)){
			LOG.debug("None monitored fields found in configuration.");
			return new DefaultEventResult<>(event, this);
		}
		
		List<IdmIdentityDto> recipients = service.findAllByRoleName(recipientsRole);
		if(CollectionUtils.isEmpty(recipients)){
			LOG.debug("None recievers found in configuration.");
			return new DefaultEventResult<>(event, this);
		}
		IdmIdentityDto newIdentity = event.getContent();
		IdmIdentityDto identity = event.getOriginalSource();
		List<ChangedField> changedFields = new ArrayList<>();
		
		// Check monitored fields on some changes
		fields.forEach(field -> {
			try {
				Object value = EntityUtils.getEntityValue(identity, field);
				Object newValue = EntityUtils.getEntityValue(newIdentity, field);
				if(value == null && newValue == null){
					return;
				}
				if(value != null && !value.equals(newValue)){
					changedFields.add(new ChangedField(field, value.toString(), newValue == null ? null : newValue.toString()));
					return;
				}
				if(newValue != null && !newValue.equals(value)){
					changedFields.add(new ChangedField(field, value == null ? null : value.toString(), newValue.toString()));
					return;
				}
				
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
					| IntrospectionException e) {
				throw new ResultCodeException(CoreResultCode.BAD_REQUEST, e);
			}
		});

		if(!changedFields.isEmpty()){
			IdmMessageDto message = new IdmMessageDto.Builder(NotificationLevel.WARNING)
		    .addParameter("fullName", service.getNiceLabel(identity))
			.addParameter("identity", identity)
			.addParameter("changedFields", changedFields)
			.addParameter("url", configurationService.getFrontendUrl(String.format("identity/%s/profile", identity.getId())))
			.build();
			notificationManager.send(String.format("core:%s", TOPIC), message, recipients);
			
		}

		return new DefaultEventResult<>(event, this);
	}

	// TODO: Move to configuration service
	private List<String> getCommaSeparatedValues(String values) {
		if (Strings.isNullOrEmpty(values)) {
			return null;
		}
		return Arrays.asList(values.split("\\s*,\\s*"));
	}
	
    @Override
    public int getOrder() { // We want send notification on end
    	return Integer.MAX_VALUE - 100;
    }
    
    @Override
	public List<String> getPropertyNames() {
		List<String> properties =  super.getPropertyNames();
		properties.add(PROPERTY_MONITORED_FIELDS);
		properties.add(PROPERTY_RECIPIENTS_ROLE);
		return properties;		
	}

    /**
     * Pojo bean keep information about changed fields
     * 
     * @author svandav
     *
     */
	public static class ChangedField {
		
		private String name;
		private String oldValue;
		private String newValue;

		public ChangedField(String name, String oldValue, String newValue) {
			super();
			this.name = name;
			this.oldValue = oldValue == null ? "" : oldValue;
			this.newValue = newValue == null ? "" : newValue;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getOldValue() {
			return oldValue;
		}

		public void setOldValue(String oldValue) {
			this.oldValue = oldValue;
		}

		public String getNewValue() {
			return newValue;
		}

		public void setNewValue(String newValue) {
			this.newValue = newValue;
		}

	}
}