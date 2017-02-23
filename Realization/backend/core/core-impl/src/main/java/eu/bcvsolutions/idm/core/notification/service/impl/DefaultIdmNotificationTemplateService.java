package eu.bcvsolutions.idm.core.notification.service.impl;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;
import eu.bcvsolutions.idm.core.notification.dto.filter.NotificationTemplateFilter;
import eu.bcvsolutions.idm.core.notification.entity.IdmMessage;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotificationTemplate;
import eu.bcvsolutions.idm.core.notification.repository.IdmNotificationTemplateRepository;
import eu.bcvsolutions.idm.core.notification.service.api.IdmNotificationTemplateService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;

/**
 * Default implementation interface {@link IdmNotificationTemplateService} basic method
 * for template engine - apache velocity. Initialization apache velocity is in constructor.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@Service
public class DefaultIdmNotificationTemplateService extends AbstractReadWriteEntityService<IdmNotificationTemplate, NotificationTemplateFilter> implements IdmNotificationTemplateService {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultIdmNotificationTemplateService.class);
	
	// private static final String ENCODING = "UTF-8";
	
	private final IdmNotificationTemplateRepository repository;
	
	private final VelocityEngine velocityEngine;
	
	@Autowired
	public DefaultIdmNotificationTemplateService(IdmNotificationTemplateRepository repository) {
		super(repository);
		//
		Assert.notNull(repository);
		//
		this.repository = repository;
		//
		VelocityEngine velocityEngine = new VelocityEngine();
		// Initialization apache velocity
		velocityEngine.setProperty(VelocityEngine.RUNTIME_LOG_LOGSYSTEM_CLASS, LOG);
		velocityEngine.init();
		this.velocityEngine = velocityEngine;
	}
	
	@Override
	@Transactional
	public IdmNotificationTemplate save(IdmNotificationTemplate entity) {
		return super.save(entity);
	}
	
	@Override
	@Transactional
	public void delete(IdmNotificationTemplate entity) {
		if (entity.isSystemTemplate()) {
			throw new ResultCodeException(CoreResultCode.NOTIFICATION_SYSTEM_TEMPLATE_DELETE_FAILED, ImmutableMap.of("template", entity.getName()));
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
	public IdmMessage getMessage(IdmMessage message, boolean showGuardedString) {
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
		// iterate trough parameters and find GuardedStrings, there may be templates, but no parameters
		if (model != null) {
			for (String key : model.keySet()) {
				if (model.get(key) instanceof GuardedString && showGuardedString) {
					parameters.put(key, ((GuardedString) model.get(key)).asString());
				} else if (model.get(key) instanceof GuardedString) {
					parameters.put(key, ((GuardedString) model.get(key)).toString());
				} else {
					parameters.put(key, model.get(key));
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
		// Build IdmMessage
		IdmMessage newMessage = new IdmMessage.Builder()
				.setHtmlMessage(bodyHtml.toString())
				.setTextMessage(bodyText.toString())
				.setSubject(subject.toString())
				.setLevel(message.getLevel()) // level get from old message
				.setTemplate(template)
				.setParameters(model)
				// build without use template, but template and parameter is persisted
				.buildWithoutUseTemplate()
				.build();
		//
		return newMessage;
	}


	@Override
	public IdmMessage getMessage(IdmMessage message) {
		if (message.getTemplate() == null) {
			return message;
		}
		return this.getMessage(message, false);
	}
}
