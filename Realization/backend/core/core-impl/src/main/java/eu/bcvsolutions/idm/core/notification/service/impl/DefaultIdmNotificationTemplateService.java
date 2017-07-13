package eu.bcvsolutions.idm.core.notification.service.impl;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.tools.generic.DateTool;
import org.apache.velocity.tools.generic.DisplayTool;
import org.dom4j.CDATA;
import org.dom4j.DocumentHelper;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.jaxb.JaxbCharacterEscapeEncoder;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmMessageDto;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationTemplateDto;
import eu.bcvsolutions.idm.core.notification.dto.filter.NotificationTemplateFilter;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotificationConfiguration;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotificationTemplate;
import eu.bcvsolutions.idm.core.notification.jaxb.IdmNotificationTemplateType;
import eu.bcvsolutions.idm.core.notification.repository.IdmNotificationConfigurationRepository;
import eu.bcvsolutions.idm.core.notification.repository.IdmNotificationTemplateRepository;
import eu.bcvsolutions.idm.core.notification.service.api.IdmNotificationTemplateService;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;

/**
 * Default implementation interface {@link IdmNotificationTemplateService} basic
 * method for template engine - apache velocity. Initialization apache velocity
 * is in constructor.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@Service("notificationTemplateService")
public class DefaultIdmNotificationTemplateService extends
		AbstractReadWriteDtoService<IdmNotificationTemplateDto, IdmNotificationTemplate, NotificationTemplateFilter>
		implements IdmNotificationTemplateService {

	private static final String TEMPLATE_FOLDER = "idm.pub.core.notification.template.folder";
	private static final String TEMPLATE_FILE_SUFIX = "idm.pub.core.notification.template.fileSuffix";
	private static final String TEMPLATE_DEFAULT_BACKUP_FOLDER = "templates/";

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory
			.getLogger(DefaultIdmNotificationTemplateService.class);

	// private static final String ENCODING = "UTF-8";

	private final IdmNotificationTemplateRepository repository;

	private final VelocityEngine velocityEngine;

	private final ApplicationContext applicationContext;

	private final ConfigurationService configurationService;

	private final IdmNotificationConfigurationRepository notificationConfigurationRepository;

	private final SecurityService securityService;

	private JAXBContext jaxbContext = null;

	@Autowired
	public DefaultIdmNotificationTemplateService(IdmNotificationTemplateRepository repository,
			ConfigurationService configurationService, ApplicationContext applicationContext,
			IdmNotificationConfigurationRepository notificationConfigurationRepository,
			SecurityService securityService) {
		super(repository);
		//
		Assert.notNull(repository);
		Assert.notNull(configurationService);
		Assert.notNull(applicationContext);
		Assert.notNull(notificationConfigurationRepository);
		Assert.notNull(securityService);
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
		this.securityService = securityService;
		//
		// init jaxbContext
		try {
			jaxbContext = JAXBContext.newInstance(IdmNotificationTemplateType.class);
		} catch (JAXBException e) {
			// TODO throw error, or just log and continue?
			throw new ResultCodeException(CoreResultCode.XML_JAXB_INIT_ERROR, e);
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public IdmNotificationTemplateDto getByCode(String code) {
		return toDto(repository.findOneByCode(code));
	}

	@Override
	@Transactional
	public IdmNotificationTemplateDto save(IdmNotificationTemplateDto entity, BasePermission... permission) {
		return super.save(entity);
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
	public IdmNotificationTemplateDto getTemplateByCode(String code) {
		final IdmNotificationTemplate entity = this.repository.findOneByCode(code);
		return toDto(entity);
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
		velocityEngine.evaluate(velocityContext, bodyHtml, template.getCode(), html);
		velocityEngine.evaluate(velocityContext, bodyText, template.getCode(), text);
		velocityEngine.evaluate(velocityContext, subject, template.getCode(), subjectString);
		//
		IdmMessageDto newMessage;
		// if is set model from message build with them
		if (message.getModel() != null) {
			newMessage = new IdmMessageDto.Builder().setHtmlMessage(bodyHtml.toString())
					.setTextMessage(bodyText.toString()).setSubject(message.getModel().getStatusEnum())
					.setLevel(message.getLevel()) // level get from old message
					.setTemplate(template).setParameters(model).setModel(message.getModel()).build();
		} else {
			// Build IdmMessage
			newMessage = new IdmMessageDto.Builder().setHtmlMessage(bodyHtml.toString())
					.setTextMessage(bodyText.toString()).setSubject(subject.toString()).setLevel(message.getLevel()) // level
					.setTemplate(template).setParameters(model).build();
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
	@Transactional
	public void init() {
		//
		Resource[] resources = getNotificationTemplateResource();
		Unmarshaller jaxbUnmarshaller = null;
		//
		try {
			//
			jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		} catch (JAXBException e) {
			throw new ResultCodeException(CoreResultCode.XML_JAXB_INIT_ERROR, e);
		}
		//
		List<IdmNotificationTemplateDto> entities = new ArrayList<>();
		for (Resource resource : resources) {
			try {
				IdmNotificationTemplateType templateType = (IdmNotificationTemplateType) jaxbUnmarshaller
						.unmarshal(resource.getInputStream());
				//
				// if template exist dont save again
				IdmNotificationTemplateDto template = this.getTemplateByCode(templateType.getCode());
				LOG.info(
						"[DefaultIdmNotificationTemplateService] Load template with code {}, Exists template in system: {}",
						templateType.getCode(), template != null);
				//
				if (template == null) {
					template = typeToDto(templateType, null);
					entities.add(template);
				}
			} catch (JAXBException e1) {
				LOG.error(
						"[DefaultIdmNotificationTemplateService] Template validation failed, file name: {}, error message: {}",
						resource.getFilename(), e1.getLocalizedMessage());
			} catch (IOException e) {
				LOG.error(
						"[DefaultIdmNotificationTemplateService] Failed get input stream from, file name: {}, error message: {}",
						resource.getFilename(), e.getLocalizedMessage());
			}
		}
		this.saveAll(entities);
	}

	@Override
	public List<IdmNotificationTemplateDto> findAllSystemTemplates() {
		NotificationTemplateFilter filter = new NotificationTemplateFilter();
		filter.setUnmodifiable(Boolean.TRUE);
		return this.find(filter, null).getContent().stream().collect(Collectors.toList());
	}

	@Override
	public IdmNotificationTemplateDto resolveTemplate(String topic, NotificationLevel level) {
		IdmNotificationConfiguration configuration = notificationConfigurationRepository
				.findNotificationByTopicLevel(topic, level);

		// if configurations is empty try to wildcard with null level
		if (configuration == null) {
			configuration = notificationConfigurationRepository.findNotificationByTopicLevel(topic, null);
		}
		if (configuration == null) {
			return null;
		}
		final IdmNotificationTemplate entity = configuration.getTemplate();
		return toDto(entity);
	}

	@Override
	public void backup(IdmNotificationTemplateDto dto) {
		String directory = getDirectoryForBackup();
		//
		Marshaller jaxbMarshaller = initJaxbMarshaller();
		//
		File backupFolder = new File(directory);
		if (!backupFolder.exists()) {
			backupFolder.mkdirs();
		}
		//
		IdmNotificationTemplateType type = dtoToType(dto);
		//
		File file = new File(getBackupFileName(directory, dto));
		try {
			jaxbMarshaller.marshal(type, file);
		} catch (JAXBException e) {
			LOG.error("Backup for template: {} failed, error message: {}",
					dto.getCode(), e.getLocalizedMessage());
			throw new ResultCodeException(CoreResultCode.BACKUP_FAIL,
					ImmutableMap.of("code", dto.getCode(), "error", e.getLocalizedMessage()), e);
		}
	}

	@Override
	public IdmNotificationTemplateDto redeploy(IdmNotificationTemplateDto dto) {
		Unmarshaller jaxbUnmarshaller = null;
		//
		try {
			jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		} catch (JAXBException e) {
			throw new ResultCodeException(CoreResultCode.XML_JAXB_INIT_ERROR, e);
		}
		//
		Resource[] resources = getNotificationTemplateResource();
		List<IdmNotificationTemplateType> types = new ArrayList<>();
		for (Resource resource : resources) {
			try {
				IdmNotificationTemplateType templateType = (IdmNotificationTemplateType) jaxbUnmarshaller
						.unmarshal(resource.getInputStream());
				//
				types.add(templateType);
			} catch (JAXBException e1) {
				LOG.error(
						"[DefaultIdmNotificationTemplateService] Template validation failed, file name: {}, error message: {}",
						resource.getFilename(), e1.getLocalizedMessage());
			} catch (IOException e) {
				LOG.error(
						"[DefaultIdmNotificationTemplateService] Failed get input stream from, file name: {}, error message: {}",
						resource.getFilename(), e.getLocalizedMessage());
			}
		}
		//
		List<IdmNotificationTemplateType> foundType = types.stream()
				.filter(type -> type.getCode().equals(dto.getCode())).collect(Collectors.toList());
		//
		if (foundType.isEmpty()) {
			throw new ResultCodeException(CoreResultCode.NOTIFICATION_TEMPLATE_XML_FILE_NOT_FOUND,
					ImmutableMap.of("code", dto.getCode()));
		} else if (foundType.size() > 1) {
			// more than one code found throw error
			throw new ResultCodeException(CoreResultCode.NOTIFICATION_TEMPLATE_MORE_CODE_FOUND,
					ImmutableMap.of("code", dto.getCode()));
		}
		//
		return deployNewAndBackupOld(dto, foundType.get(0));
	}

	/**
	 * Create instance of JaxbMarshaller and set required properties to him.
	 * 
	 * @return
	 */
	private Marshaller initJaxbMarshaller() {
		Marshaller jaxbMarshaller = null;
		try {
			jaxbMarshaller = jaxbContext.createMarshaller();
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			jaxbMarshaller.setProperty(Marshaller.JAXB_ENCODING, StandardCharsets.UTF_8.name());
			jaxbMarshaller.setProperty(ENCODING_HANDLER, new JaxbCharacterEscapeEncoder());
		} catch (JAXBException e) {
			throw new ResultCodeException(CoreResultCode.XML_JAXB_INIT_ERROR, e);
		}
		return jaxbMarshaller;
	}

	/**
	 * Transform type to dto, if second parameter is null it will be created new
	 * dto.
	 * 
	 * @param type
	 * @param template
	 * @return
	 */
	private IdmNotificationTemplateDto typeToDto(IdmNotificationTemplateType type,
			IdmNotificationTemplateDto template) {
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
	private IdmNotificationTemplateType dtoToType(IdmNotificationTemplateDto dto) {
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
		return type;
	}

	/**
	 * Return folder for backups. If isn't folder defined in configuration
	 * properties use default folder from system property java.io.tmpdir.
	 * 
	 * @return
	 */
	private String getDirectoryForBackup() {
		String backupPath = configurationService.getValue(BACKUP_FOLDER_CONFIG);
		if (backupPath == null) {
			// if backup path null throw error, backup folder must be set
			throw new ResultCodeException(CoreResultCode.BACKUP_FOLDER_NOT_FOUND,
					ImmutableMap.of("property", BACKUP_FOLDER_CONFIG));
		}
		// apend template default backup folder
		backupPath = backupPath + "/" + TEMPLATE_DEFAULT_BACKUP_FOLDER;
		// add date folder
		DateTime date = new DateTime();
		DecimalFormat decimalFormat = new DecimalFormat("00");
		String completePath = backupPath + date.getYear() + decimalFormat.format(date.getMonthOfYear())
				+ decimalFormat.format(date.getDayOfMonth()) + "/";
		return completePath;
	}

	/**
	 * Method return path for file. That will be save into backup directory.
	 * 
	 * @param directory
	 * @param template
	 * @return
	 */
	private String getBackupFileName(String directory, IdmNotificationTemplateDto template) {
		return directory + template.getCode() + "_" + securityService.getCurrentUsername() + "_"
				+ System.currentTimeMillis() + EXPORT_FILE_SUFIX;
	}

	/**
	 * Return array of {@link Resource} with all resource with notification
	 * templates.
	 * 
	 * @return
	 */
	private Resource[] getNotificationTemplateResource() {
		Resource[] resources = null;
		try {
			resources = applicationContext.getResources(configurationService.getValue(TEMPLATE_FOLDER)
					+ configurationService.getValue(TEMPLATE_FILE_SUFIX));
		} catch (IOException e) {
			throw new ResultCodeException(CoreResultCode.DEPLOY_ERROR,
					ImmutableMap.of("path", configurationService.getValue(TEMPLATE_FOLDER)
							+ configurationService.getValue(TEMPLATE_FILE_SUFIX)), e);
		}
		return resources;
	}

	/**
	 * Method replace all attribute from dto with type attributes, old dto will
	 * be backup to system folder.
	 * 
	 * @param oldTemplate
	 * @param newTemplate
	 * @return
	 */
	private IdmNotificationTemplateDto deployNewAndBackupOld(IdmNotificationTemplateDto oldTemplate,
			IdmNotificationTemplateType newTemplate) {
		// backup
		this.backup(oldTemplate);

		// transform new
		oldTemplate = typeToDto(newTemplate, oldTemplate);
		return this.save(oldTemplate);
	}	
}
