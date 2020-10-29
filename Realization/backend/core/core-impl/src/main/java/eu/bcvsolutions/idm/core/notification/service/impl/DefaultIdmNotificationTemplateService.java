package eu.bcvsolutions.idm.core.notification.service.impl;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.tools.generic.DateTool;
import org.apache.velocity.tools.generic.DisplayTool;
import org.dom4j.CDATA;
import org.dom4j.DocumentHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.AbstractRecoverableService;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmMessageDto;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationLogDto;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationRecipientDto;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationTemplateDto;
import eu.bcvsolutions.idm.core.notification.api.dto.NotificationConfigurationDto;
import eu.bcvsolutions.idm.core.notification.api.dto.filter.IdmNotificationTemplateFilter;
import eu.bcvsolutions.idm.core.notification.api.service.IdmNotificationConfigurationService;
import eu.bcvsolutions.idm.core.notification.api.service.IdmNotificationTemplateService;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotificationConfiguration_;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotificationTemplate;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotificationTemplate_;
import eu.bcvsolutions.idm.core.notification.jaxb.IdmNotificationTemplateType;
import eu.bcvsolutions.idm.core.notification.repository.IdmNotificationTemplateRepository;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;

/**
 * Default implementation interface {@link IdmNotificationTemplateService} basic
 * method for template engine - apache velocity. Initialization apache velocity
 * is in constructor.
 * 
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 * @author Ondrej Husnik
 *
 */
@Service("notificationTemplateService")
public class DefaultIdmNotificationTemplateService 
		extends AbstractRecoverableService<IdmNotificationTemplateType, IdmNotificationTemplateDto, 
			IdmNotificationTemplate, IdmNotificationTemplateFilter>
		implements IdmNotificationTemplateService {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultIdmNotificationTemplateService.class);
	//
	// TODO: script + common template configuration
	private static final String TEMPLATE_FILE_SUFIX = "idm.sec.core.notification.template.fileSuffix";
	private static final String TEMPLATE_DEFAULT_BACKUP_FOLDER = "templates/";
	private static final String DEFAULT_TEMPLATE_FILE_SUFIX = "**/**.xml";
	//
	private final IdmNotificationTemplateRepository repository;
	private final VelocityEngine velocityEngine;
	private IdmNotificationConfigurationService notificationConfigurationService;
	//
	@Autowired private ApplicationContext applicationContext;
	@Autowired private ConfigurationService configurationService;
	
	@Autowired
	public DefaultIdmNotificationTemplateService(IdmNotificationTemplateRepository repository, EntityEventManager entityEventManager) {
		super(repository, entityEventManager);
		//
		this.repository = repository;
		//
		VelocityEngine velocityEngine = new VelocityEngine();
		// Initialization apache velocity
		velocityEngine.setProperty(VelocityEngine.RUNTIME_LOG_LOGSYSTEM_CLASS, LOG);
		velocityEngine.setProperty(RuntimeConstants.VM_PERM_ALLOW_INLINE_REPLACE_GLOBAL, Boolean.TRUE);
		velocityEngine.init();
		this.velocityEngine = velocityEngine;
	}
	
	@Override
	@Transactional
	public void init() {
		for (IdmNotificationTemplateType templateType : findTemplates().values()) {
			IdmNotificationTemplateDto template = this.getByCode(templateType.getCode());
			// if template exist don't save it again => init only
			if (template != null) {
				LOG.info("Load template with code [{}], template is already initialized, skipping.", templateType.getCode());
				continue;
			}
			//
			LOG.info("Load template with code [{}], template will be initialized.", templateType.getCode());
			// save
			save(toDto(templateType, null));
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public IdmNotificationTemplateDto getByCode(String code) {
		return toDto(repository.findOneByCode(code));
	}

	@Override
	@Transactional
	public void delete(IdmNotificationTemplateDto entity, BasePermission... permission) {
		if (entity.isUnmodifiable()) {
			throw new ResultCodeException(CoreResultCode.NOTIFICATION_SYSTEM_TEMPLATE_DELETE_FAILED,
					ImmutableMap.of("template", entity.getName()));
		}
		super.delete(entity);
	}
	
	@Override
	protected List<Predicate> toPredicates(Root<IdmNotificationTemplate> root, CriteriaQuery<?> query, CriteriaBuilder builder, IdmNotificationTemplateFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		// quick - "fulltext"
		if (StringUtils.isNotEmpty(filter.getText())) {
			predicates.add(builder.or(
					builder.like(builder.lower(root.get(IdmNotificationTemplate_.code)), "%" + filter.getText().toLowerCase() + "%"),
					builder.like(builder.lower(root.get(IdmNotificationTemplate_.subject)), "%" + filter.getText().toLowerCase() + "%"),
					builder.like(builder.lower(root.get(IdmNotificationTemplate_.name)), "%" + filter.getText().toLowerCase() + "%")			
					));
		}
		// unmodifiable / system flag
		Boolean unmodifiable = filter.getUnmodifiable();
		if (unmodifiable != null) {
			predicates.add(builder.equal(root.get(IdmNotificationTemplate_.unmodifiable), unmodifiable));
		}
		return predicates;
	}

	/**
	 * Return {@link VelocityContext} for create templates
	 * 
	 * @param model
	 * @return
	 */
	private VelocityContext getContext(Map<String, Object> model) {
		return new VelocityContext(model);
	}

	@Override
	public IdmMessageDto buildMessage(IdmMessageDto message, boolean showGuardedString) {
		StringWriter bodyHtml = new StringWriter();
		StringWriter bodyText = new StringWriter();
		StringWriter subject = new StringWriter();
		IdmNotificationTemplateDto template = message.getTemplate() == null ? null : get(message.getTemplate().getId());
		//
		if (template == null) {
			return message;
		}
		// get parameters from messages
		Map<String, Object> model = message.getParameters();
		//
		// create copy of parameters
		Map<String, Object> parameters = new HashMap<>();
		// iterate trough parameters and find GuardedStrings, there may be
		// templates, but no parameters
		if (model != null) {
			for (Entry<String, Object> entry : model.entrySet()) {
				if (entry.getValue() instanceof GuardedString && showGuardedString) {
					parameters.put(entry.getKey(), ((GuardedString) entry.getValue()).asString());
				} else if (entry.getValue() instanceof GuardedString) {
					parameters.put(entry.getKey(), ((GuardedString) entry.getValue()).toString());
				} else {
					parameters.put(entry.getKey(), entry.getValue());
				}
			}
		}
		// prepare html, text, subject
		String html = template.getBodyHtml();
		String text = template.getBodyText();
		String subjectString = template.getSubject();
		// Same parameters for all (html, txt, subject)
		VelocityContext velocityContext = getContext(parameters);
		// include some tools from Apache velocity -
		// http://velocity.apache.org/tools/devel/generic.html#tools
		velocityContext.put("display", new DisplayTool());
		velocityContext.put("date", new DateTool());
		//
		// html and text may not exists, evaluate only if exists
		if (html != null) {
			velocityEngine.evaluate(velocityContext, bodyHtml, template.getCode(), html);
		}
		//
		if (text != null) {
			velocityEngine.evaluate(velocityContext, bodyText, template.getCode(), text);
		}
		// subject must exists
		velocityEngine.evaluate(velocityContext, subject, template.getCode(), subjectString);
		//
		IdmMessageDto newMessage;
		// if is set model from message build with them
		if (message.getModel() != null) {
			newMessage = new IdmMessageDto
					.Builder()
					.setHtmlMessage(bodyHtml.toString())
					.setTextMessage(bodyText.toString())
					.setSubject(StringUtils.isNotEmpty(subject.toString()) ? subject.toString() : message.getModel().getStatusEnum())
					.setLevel(message.getLevel()) // level get from old message
					.setTemplate(template)
					.setParameters(model)
					.setModel(message.getModel()).build();
		} else {
			// Build IdmMessage
			newMessage = new IdmMessageDto
					.Builder()
					.setHtmlMessage(bodyHtml.toString())
					.setTextMessage(bodyText.toString())
					.setSubject(subject.toString())
					.setLevel(message.getLevel()) // level
					.setTemplate(template)
					.setParameters(model)
					.build();
		}
		//
		return newMessage;
	}

	@Override
	public IdmMessageDto buildMessage(IdmMessageDto message) {
		if (message.getTemplate() == null) {
			return message;
		}
		return this.buildMessage(message, false);
	}

	@Override
	public List<IdmNotificationTemplateDto> findAllSystemTemplates() {
		IdmNotificationTemplateFilter filter = new IdmNotificationTemplateFilter();
		filter.setUnmodifiable(Boolean.TRUE);
		return this.find(filter, null).getContent().stream().collect(Collectors.toList());
	}

	@Override
	public IdmNotificationTemplateDto resolveTemplate(String topic, NotificationLevel level, String notificationType) {
		// find all configuration by topic and level
		NotificationConfigurationDto configuration = getNotificationConfigurationService()
				.getConfigurationByTopicLevelNotificationType(topic, level, notificationType);
		// if configurations is empty, found a wild card configuration
		if (configuration == null) {
			configuration = getNotificationConfigurationService()
					.getConfigurationByTopicAndNotificationTypeAndLevelIsNull(topic, notificationType);
		}
		return DtoUtils.getEmbedded(configuration, IdmNotificationConfiguration_.template, IdmNotificationTemplateDto.class);
	}
	
	@Override
	protected String getBackupFolderName() {
		return TEMPLATE_DEFAULT_BACKUP_FOLDER;
	}

	@Override
	@Transactional
	public IdmNotificationTemplateDto redeploy(IdmNotificationTemplateDto dto, BasePermission... permission) {
		IdmNotificationTemplateType foundType = findTemplates().get(dto.getCode());
		//
		if (foundType == null) {
			throw new ResultCodeException(CoreResultCode.NOTIFICATION_TEMPLATE_XML_FILE_NOT_FOUND,
					ImmutableMap.of("code", dto.getCode()));
		}
		//
		return backupAndDeploy(dto, foundType, permission);
	}

	@Override
	public List<IdmNotificationLogDto> prepareNotifications(String topic, IdmMessageDto message) {
		Assert.notNull(message, "Message is required.");
		List<IdmNotificationLogDto> notifications = new ArrayList<>();
		//
		// find all configuration by topic and level
		List<NotificationConfigurationDto> configurations = getNotificationConfigurationService().getConfigurations(topic, message.getLevel());
		// if configurations is empty, found a wild card configuration
		if (configurations.isEmpty()) {
			configurations = getNotificationConfigurationService().getWildcardConfigurations(topic);
		}
		//
		// if configurations still empty and exists final message send only his message, this message will be sent without type
		if (configurations.isEmpty()) {
			// this state is possible send message to topic that hasn't set any configurations
			IdmNotificationLogDto notification = new IdmNotificationLogDto();
			notification.setTopic(topic);
			notification.setMessage(this.buildMessage(message, false));
			notifications.add(notification);
			return notifications;
		}
		//
		// 1. Priority - Own message in IdmMessage has biggest priority than otherwise settings
		// 2. Priority - Template from IdmMessage has second biggest priority
		// 3. Priority - Get message from configuration by topic
		//
		// html, text and subject is not empty use them
		for (NotificationConfigurationDto configuration : configurations) {
			if (configuration.isDisabled()) {
				LOG.debug("Configuration [{}] for topic [{}], level [{}], type [{}] is disabled. "
						+ "Notification will not be sent by this configuration.", 
						configuration.getId(), topic, message.getLevel(), configuration.getNotificationType());
				continue;
			}
			//			
			// sending notification to original recipients 
			if (!configuration.isRedirect()) {
				notifications.add(createFinalMessage(message, configuration));
			}
			//
			// sending notification to recipients defined in configuration
			List<IdmNotificationRecipientDto> recipients = getNotificationConfigurationService().getRecipients(configuration);
			if (!recipients.isEmpty()) {
				IdmNotificationLogDto notification = createFinalMessage(message, configuration);
				notification.setRecipients(recipients);
				notifications.add(notification);
			} else if (configuration.isRedirect()) {
				// redirect and no recipient is configured => exception
				// just for sure - validation, when configuration is saved, should solve it before => we don't want to fail some operation just because notification is not sent.
				throw new ResultCodeException(CoreResultCode.NOTIFICATION_CONFIGURATION_RECIPIENT_NOT_FOUND, ImmutableMap.of("topic", configuration.getTopic()));
			}
		}
		//
		return notifications;
	}
	
	private IdmNotificationLogDto createFinalMessage(IdmMessageDto message, NotificationConfigurationDto configuration) {
		IdmMessageDto finalMessage = null;
		if (message.getTemplate() != null) {
			// exist template in message
			finalMessage = this.buildMessage(message, false);
		} else if (configuration.getTemplate() != null) {	
			finalMessage = new IdmMessageDto(message);
			finalMessage.setTemplate(DtoUtils.getEmbedded(configuration, IdmNotificationConfiguration_.template, IdmNotificationTemplateDto.class));
			finalMessage = this.buildMessage(finalMessage, false);
		} else {
			finalMessage = message;
		}
		if (!StringUtils.isEmpty(message.getSubject())) {
			finalMessage.setSubject(message.getSubject());
		}
		if (!StringUtils.isEmpty(message.getTextMessage())) {
			finalMessage.setTextMessage(message.getTextMessage());
		}
		if (!StringUtils.isEmpty(message.getHtmlMessage())) {
			finalMessage.setHtmlMessage(message.getHtmlMessage());
		}
		//
		// send message for every found configuration
		IdmNotificationLogDto notification = new IdmNotificationLogDto();
		notification.setTopic(configuration.getTopic());
		notification.setType(configuration.getNotificationType());
		notification.setMessage(finalMessage);
		//
		return notification;
	}

	@Override
	protected IdmNotificationTemplateDto toDto(IdmNotificationTemplateType type, IdmNotificationTemplateDto template) {
		if (template == null) {
			template = new IdmNotificationTemplateDto();
		}
		//
		if (type == null) {
			return template;
		}
		// transform type to DTO
		template.setCode(type.getCode());
		template.setName(type.getName());
		template.setBodyHtml(type.getBodyHtml());
		template.setBodyText(type.getBodyText());
		template.setModule(type.getModuleId());
		template.setSubject(type.getSubject());
		template.setUnmodifiable(type.isSystemTemplate());
		template.setParameter(type.getParameter());
		template.setSender(type.getSender());
		return template;
	}

	/**
	 * Return simple string as CDATA for XML.
	 * 
	 * @param input
	 * @return
	 */
	public static String getStringAsCdata(String input) {
		CDATA cdata = DocumentHelper.createCDATA(input);
		return cdata.asXML();
	}

	/**
	 * Transform dto to type.
	 * 
	 * @param dto
	 * @return
	 */
	@Override
	protected IdmNotificationTemplateType toType(IdmNotificationTemplateDto dto) {
		IdmNotificationTemplateType type = new IdmNotificationTemplateType();
		if (dto == null) {
			return type;
		}
		// transform DTO to type
		type.setCode(dto.getCode());
		type.setName(dto.getName());
		type.setBodyHtml(dto.getBodyHtml());
		type.setBodyText(dto.getBodyText());
		type.setModuleId(dto.getModule());
		type.setSubject(dto.getSubject());
		type.setSystemTemplate(dto.isUnmodifiable());
		type.setParameter(dto.getParameter());
		type.setSender(dto.getSender());
		return type;
	}
	
	/**
	 * Return list of {@link IdmNotificationTemplateType} from resources.
	 * {@link IdmNotificationTemplateType} are found by configured locations and by priority - last one wins.
	 * So default location should be configured first, then external, etc. 
	 * 
	 * @return <code, script>
	 */
	private Map<String, IdmNotificationTemplateType> findTemplates() {
		// last script with the same is used
		// => last location has the highest priority
		Map<String, IdmNotificationTemplateType> templates = new HashMap<>();
		//
		for (String location : configurationService.getValues(TEMPLATE_FOLDER)) {
			location = location + configurationService.getValue(TEMPLATE_FILE_SUFIX, DEFAULT_TEMPLATE_FILE_SUFIX);
			Map<String, IdmNotificationTemplateType> locationTemplates = new HashMap<>();
			try {
				Resource[] resources = applicationContext.getResources(location);
				LOG.debug("Found [{}] resources on location [{}]", resources == null ? 0 : resources.length, location);
				//
				if (ArrayUtils.isEmpty(resources)) {
					continue;
				}
				for (Resource resource : resources) {
					try {
						IdmNotificationTemplateType templateType = readType(resource.getFilename(), resource.getInputStream());
						//
						// log error, if script with the same code was found twice in one resource
						if (locationTemplates.containsKey(templateType.getCode())) {
							LOG.error("More templates with code [{}] found on the same location [{}].",
									templateType.getCode(),
									location);
						}
						// last one wins
						locationTemplates.put(templateType.getCode(), templateType);
					} catch (IOException ex) {
						LOG.error("Failed get input stream from, file name [{}].", resource.getFilename(), ex);
					}					
				}
				templates.putAll(locationTemplates);
			} catch (IOException ex) {
				throw new ResultCodeException(CoreResultCode.DEPLOY_ERROR, ImmutableMap.of("path", location), ex);
			}
		}
		return templates;
	}
	
	/**
	 * Lazy init ... template is injected into senders ... senders are injected into configuration => cyclic dependency
	 * TODO: redesign notification subsystem => configuration => template => senders at bottom
	 * 
	 * @return
	 */
	private IdmNotificationConfigurationService getNotificationConfigurationService() {
		if (notificationConfigurationService == null) {
			notificationConfigurationService = applicationContext.getBean(IdmNotificationConfigurationService.class);
		}
		return notificationConfigurationService;
	}
}
