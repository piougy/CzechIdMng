package eu.bcvsolutions.idm.core.notification.service.impl;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;
import eu.bcvsolutions.idm.core.notification.dto.filter.NotificationTemplateFilter;
import eu.bcvsolutions.idm.core.notification.entity.IdmMessage;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotificationConfiguration;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotificationTemplate;
import eu.bcvsolutions.idm.core.notification.repository.IdmNotificationConfigurationRepository;
import eu.bcvsolutions.idm.core.notification.repository.IdmNotificationTemplateRepository;
import eu.bcvsolutions.idm.core.notification.service.api.IdmNotificationTemplateService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;

/**
 * Default implementation interface {@link IdmNotificationTemplateService} basic
 * method for template engine - apache velocity. Initialization apache velocity
 * is in constructor.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@Service
public class DefaultIdmNotificationTemplateService
		extends AbstractReadWriteEntityService<IdmNotificationTemplate, NotificationTemplateFilter>
		implements IdmNotificationTemplateService {

	private static final String TEMPLATE_FOLDER = "idm.pub.core.notification.template.folder";

	private static final String TEMPLATE_FILE_SUFIX = "idm.pub.core.notification.template.fileSuffix";

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory
			.getLogger(DefaultIdmNotificationTemplateService.class);

	// private static final String ENCODING = "UTF-8";

	private final IdmNotificationTemplateRepository repository;

	private final VelocityEngine velocityEngine;

	private final ApplicationContext applicationContext;

	private final ConfigurationService configurationService;

	private final IdmNotificationConfigurationRepository notificationConfigurationRepository;

	@Autowired
	public DefaultIdmNotificationTemplateService(IdmNotificationTemplateRepository repository,
			ConfigurationService configurationService, ApplicationContext applicationContext,
			IdmNotificationConfigurationRepository notificationConfigurationRepository) {
		super(repository);
		//
		Assert.notNull(repository);
		Assert.notNull(configurationService);
		Assert.notNull(applicationContext);
		Assert.notNull(notificationConfigurationRepository);
		//
		this.repository = repository;
		//
		VelocityEngine velocityEngine = new VelocityEngine();
		// Initialization apache velocity
		velocityEngine.setProperty(VelocityEngine.RUNTIME_LOG_LOGSYSTEM_CLASS, LOG);
		velocityEngine.init();
		this.velocityEngine = velocityEngine;
		this.configurationService = configurationService;
		this.applicationContext = applicationContext;
		this.notificationConfigurationRepository = notificationConfigurationRepository;
	}

	@Override
	@Transactional
	public IdmNotificationTemplate save(IdmNotificationTemplate entity) {
		return super.save(entity);
	}

	@Override
	@Transactional
	public void delete(IdmNotificationTemplate entity) {
		if (entity.isUnmodifiable()) {
			throw new ResultCodeException(CoreResultCode.NOTIFICATION_SYSTEM_TEMPLATE_DELETE_FAILED,
					ImmutableMap.of("template", entity.getName()));
		}
		super.delete(entity);
	}

	@Override
	public IdmNotificationTemplate getByName(String name) {
		return repository.findOneByName(name);
	}

	@Override
	public IdmNotificationTemplate getTemplateByCode(String code) {
		return this.repository.findOneByCode(code);
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
	public IdmMessage buildMessage(IdmMessage message, boolean showGuardedString) {
		StringWriter bodyHtml = new StringWriter();
		StringWriter bodyText = new StringWriter();
		StringWriter subject = new StringWriter();
		IdmNotificationTemplate template = message.getTemplate();
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
		String html = template != null ? template.getBodyHtml() : message.getHtmlMessage();
		String text = template != null ? template.getBodyText() : message.getTextMessage();
		String subjectString = template != null ? template.getSubject() : message.getSubject();
		// Same parameters for all (html, txt, subject)
		VelocityContext velocityContext = getContext(parameters);
		// TODO: get from DataSource?
		velocityEngine.evaluate(velocityContext, bodyHtml, template.getCode(), html);
		velocityEngine.evaluate(velocityContext, bodyText, template.getCode(), text);
		velocityEngine.evaluate(velocityContext, subject, template.getCode(), subjectString);
		//
		IdmMessage newMessage = null;
		// if is set model from message build with them
		if (message.getModel() != null) {
			newMessage = new IdmMessage.Builder().setHtmlMessage(bodyHtml.toString())
					.setTextMessage(bodyText.toString()).setSubject(message.getModel().getStatusEnum())
					.setLevel(message.getLevel()) // level get from old message
					.setTemplate(template).setParameters(model).setModel(message.getModel()).build();
		} else {
			// Build IdmMessage
			newMessage = new IdmMessage.Builder().setHtmlMessage(bodyHtml.toString())
					.setTextMessage(bodyText.toString()).setSubject(subject.toString()).setLevel(message.getLevel()) // level
																														// get
																														// from
																														// old
																														// message
					.setTemplate(template).setParameters(model).build();
		}
		//
		return newMessage;
	}

	@Override
	public IdmMessage buildMessage(IdmMessage message) {
		if (message.getTemplate() == null) {
			return message;
		}
		return this.buildMessage(message, false);
	}

	@Override
	@Transactional
	public void initSystemTemplates() {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			LOG.info(
					"[DefaultIdmNotificationTemplateService] initialization system template from path: {}, with file sufix: {} ",
					TEMPLATE_FOLDER, TEMPLATE_FILE_SUFIX);
			//
			// found all resources on all classpath by properties from
			// configuration file
			Resource[] resources = applicationContext.getResources(configurationService.getValue(TEMPLATE_FOLDER)
					+ configurationService.getValue(TEMPLATE_FILE_SUFIX));
			//
			List<IdmNotificationTemplate> entities = new ArrayList<>();
			// iterate trough all found resources
			for (Resource resource : resources) {
				Document doc = builder.parse(resource.getInputStream());
				doc.getDocumentElement().normalize();
				NodeList templates = (NodeList) doc.getElementsByTagName("template");
				if (templates != null) {
					//
					// iterate trough templates, now can one file contains only
					// one templates, but in future...
					for (int i = 0; i < templates.getLength(); i++) {
						Node template = templates.item(i);
						if (template.getNodeType() == Node.ELEMENT_NODE) {
							IdmNotificationTemplate newTemplate = new IdmNotificationTemplate();
							//
							Element temp = (Element) template;
							//
							// try to found template by code, if not found save
							// it
							String code = temp.getElementsByTagName("code").item(0).getTextContent();
							LOG.info("[DefaultIdmNotificationTemplateService] Load template with code {} ", code);
							IdmNotificationTemplate oldTemplate = this.getTemplateByCode(code);
							if (oldTemplate == null) {
								LOG.info("[DefaultIdmNotificationTemplateService] Create template with code {} ", code);
								newTemplate.setName(temp.getElementsByTagName("name").item(0).getTextContent());
								newTemplate.setCode(code);
								newTemplate.setSubject(temp.getElementsByTagName("subject").item(0).getTextContent());
								newTemplate.setBodyHtml(temp.getElementsByTagName("bodyHtml").item(0).getTextContent());
								newTemplate.setBodyText(temp.getElementsByTagName("bodyText").item(0).getTextContent());
								newTemplate
										.setParameter(temp.getElementsByTagName("parameter").item(0).getTextContent());
								newTemplate.setUnmodifiable(Boolean
										.valueOf(temp.getElementsByTagName("systemTemplate").item(0).getTextContent()));
								newTemplate.setModule(temp.getElementsByTagName("moduleId").item(0).getTextContent());
								//
								entities.add(newTemplate);
							}
						}
					}
				}
			}
			//
			this.saveAll(entities);
		} catch (ParserConfigurationException | IOException | SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public List<IdmNotificationTemplate> findAllSystemTemplates() {
		NotificationTemplateFilter filter = new NotificationTemplateFilter();
		filter.setUnmodifiable(Boolean.TRUE);
		return this.find(filter, null).getContent();
	}

	@Override
	public IdmNotificationTemplate resolveTemplate(String topic, NotificationLevel level) {
		IdmNotificationConfiguration configuration = notificationConfigurationRepository
				.findNotificationByTopicLevel(topic, level);

		// if configurations is empty try to wildcard with null level
		if (configuration == null) {
			configuration = notificationConfigurationRepository.findNotificationByTopicLevel(topic, null);
		}
		if (configuration == null) {
			return null;
		}
		return configuration.getTemplate();
	}
}
