package eu.bcvsolutions.idm.core.notification.service.impl;

import java.io.StringWriter;
import java.util.Map;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;
import eu.bcvsolutions.idm.core.notification.dto.filter.NotificationTemplateFilter;
import eu.bcvsolutions.idm.core.notification.entity.IdmMessage;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotificationTemplate;
import eu.bcvsolutions.idm.core.notification.repository.IdmNotificationTemplateRepository;
import eu.bcvsolutions.idm.core.notification.service.api.IdmNotificationTemplateService;

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

	@Override
	public IdmMessage getMessage(String code, Map<String, Object> model) {
		IdmNotificationTemplate template = this.repository.findOneByCode(code);
		return this.getMessage(template, model);
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
	public IdmMessage getMessage(IdmNotificationTemplate template, Map<String, Object> model) {
		StringWriter body = new StringWriter();
		StringWriter subject = new StringWriter();
		//
		// Same parameters as body
		VelocityContext velocityContext = getContext(model);
		// TODO: get from DataSource?
		velocityEngine.evaluate(velocityContext, body, template.getCode(), template.getBody());
		velocityEngine.evaluate(velocityContext, subject, template.getCode(), template.getSubject());
		//
		// Build IdmMessage
		IdmMessage message = new IdmMessage.Builder()
				.setHtmlMessage(body.toString())
				.setMessage(body.toString())
				.setSubject(subject.toString())
				.setLevel(template.getLevel())
				.build();
		//
		return message;
	}

	@Override
	public IdmMessage getMessage(IdmNotificationTemplate template) {
		return this.getMessage(template, null);
	}

	@Override
	public IdmMessage getMessage(String code) {
		return this.getMessage(code, null);
	}
}
