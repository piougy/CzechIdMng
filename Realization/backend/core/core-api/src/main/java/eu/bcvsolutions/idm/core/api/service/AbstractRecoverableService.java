package eu.bcvsolutions.idm.core.api.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.DecimalFormat;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.GenericTypeResolver;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.domain.Codeable;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.RecoverableDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.jaxb.JaxbCharacterEscapeEncoder;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.api.utils.SpinalCase;
import eu.bcvsolutions.idm.core.api.utils.ZipUtils;
import eu.bcvsolutions.idm.core.ecm.api.dto.IdmAttachmentDto;
import eu.bcvsolutions.idm.core.ecm.api.service.AttachmentManager;
import eu.bcvsolutions.idm.core.notification.api.service.IdmNotificationTemplateService;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.core.security.api.utils.PermissionUtils;

/**
 * Common recoverable service.
 * Eventable service is extended.
 * 
 * TODO: move all methods from Recoverable interface from notification and script service.
 * 
 * @param <T> xml type
 * @param <DTO> dto type
 * @param <E> entity type
 * @param <F> filter type
 * @author Radek Tomiška
 * @since 10.6.0
 * @see IdmNotificationTemplateService
 * @see IdmScriptService
 */
public abstract class AbstractRecoverableService<T extends Codeable, DTO extends RecoverableDto, E extends BaseEntity, F extends BaseFilter>
		extends AbstractEventableDtoService<DTO, E, F> 
		implements Recoverable<DTO>, CodeableService<DTO> {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractRecoverableService.class);
	//
	@Autowired private AttachmentManager attachmentManager;
	@Autowired private SecurityService securityService;
	@Autowired private ConfigurationService configurationService;
	//
	private final JAXBContext jaxbContext;
	
	public AbstractRecoverableService(AbstractEntityRepository<E> repository, EntityEventManager entityEventManager) {
		super(repository, entityEventManager);
		//
		// init jaxbContext
		try {
			Class<?>[] genericTypes = GenericTypeResolver.resolveTypeArguments(getClass(), AbstractRecoverableService.class);
			jaxbContext = JAXBContext.newInstance(genericTypes[0]);
		} catch (JAXBException e) {
			// TODO throw error, or just log and continue?
			throw new ResultCodeException(CoreResultCode.XML_JAXB_INIT_ERROR, e);
		}
	}
	
	/**
	 * Return folder for backups. Folder will be created in backup directory.
	 * 
	 * @return folder name 
	 */
	abstract protected String getBackupFolderName();
	
	/**
	 * Transform type to dto, if second parameter is null it will be created new dto.
	 * 
	 * @param type codeable type from xml file
	 * @param dto [optional]
	 * @return dto filled with type values
	 */
	abstract protected DTO toDto(T type, DTO dto);
	
	/**
	 * Transform dto to xml type.
	 * 
	 * @param dto filled with type values
	 * @return codeable type to xml file
	 */
	abstract protected T toType(DTO dto);
	
	/**
	 * Create instance of JaxbMarshaller and set required properties to him.
	 * 
	 * @return
	 */
	protected Marshaller initJaxbMarshaller() {
		Marshaller jaxbMarshaller = null;
		try {
			jaxbMarshaller = jaxbContext.createMarshaller();
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			jaxbMarshaller.setProperty(Marshaller.JAXB_ENCODING, StandardCharsets.UTF_8.name());
			jaxbMarshaller.setProperty(ENCODING_HANDLER, new JaxbCharacterEscapeEncoder());
		} catch (JAXBException e) {
			throw new ResultCodeException(CoreResultCode.XML_JAXB_INIT_ERROR, e);
		}
		return jaxbMarshaller;
	}
	
	/**
	 * Read codeable type from xml file.
	 * 
	 * @param sourceName resource location / name
	 * @param source input
	 * @return resolved type from xml
	 */
	@SuppressWarnings("unchecked")
	protected T readType(String sourceName, InputStream source) {
		Unmarshaller jaxbUnmarshaller = null;
		//
		try {
			jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		} catch (JAXBException e) {
			throw new ResultCodeException(CoreResultCode.XML_JAXB_INIT_ERROR, e);
		}
		try {
			return (T) jaxbUnmarshaller.unmarshal(source);
		} catch (JAXBException ex) {
			LOG.error("Xml validation failed, file name [{}].", sourceName, ex);
			return null;
		}
	}
	
	@Override
	public File backup(DTO dto) {
		Marshaller jaxbMarshaller = initJaxbMarshaller();
		//
		String directory = getBackupFolder();
		File backupFolder = new File(directory);
		if (!backupFolder.exists()) {
			boolean success = backupFolder.mkdirs();
			// if make dir after check if exist, throw error.
			if (!success) {
				LOG.error("Backup for resource [{}] failed, backup folder path: [{}] can't be created.",
						dto.getCode(),
						backupFolder.getAbsolutePath());
				throw new ResultCodeException(CoreResultCode.BACKUP_FAIL,
						ImmutableMap.of("code", dto.getCode()));
			}
		}
		//
		T type = toType(dto);
		File file = new File(getBackupFileName(directory, dto));
		try {
			jaxbMarshaller.marshal(type, file);
			//
			return file;
		} catch (JAXBException e) {
			LOG.error("Backup for template: {} failed",
					dto.getCode());
			throw new ResultCodeException(CoreResultCode.BACKUP_FAIL,
					ImmutableMap.of("code", dto.getCode()), e);
		}
	}
	
	@Override
	@Transactional
	public List<DTO> deploy(IdmAttachmentDto attachment, BasePermission... permission) {
		Assert.notNull(attachment, "Attachment is required.");
		UUID attachmentId = attachment.getId();
		Assert.notNull(attachmentId, "Persisted attachment is required.");
		InputStream attachmentData = attachmentManager.getAttachmentData(attachmentId);
		Assert.notNull(attachmentData, "Attachment data is required.");
		//
		String attachmentName = attachment.getName();
		if (attachment.getMimetype().equals(MediaType.APPLICATION_XML_VALUE)
				|| attachment.getMimetype().equals(MediaType.TEXT_XML_VALUE)) {
			LOG.debug("Single resource [{}] will extracted and deployed.", attachmentName);
			return Lists.newArrayList(deploy(attachmentName, attachmentData));
		} 
		//
		LOG.debug("Archive [{}] will extracted and deployed.", attachmentName);
		Map<String, DTO> deploedResources = new HashMap<>();
		File zipFile = attachmentManager.createTempFile();
		Path zipFolder = attachmentManager.createTempDirectory(null);
		try {
			// copy archive
			Files.copy(attachmentData, zipFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			// and extract
			ZipUtils.extract(zipFile, zipFolder.toString());
			//
			File[] listFiles = zipFolder.toFile().listFiles();
			LOG.debug("Found [{}] resources on location [{}]", listFiles == null ? 0 : listFiles.length, attachmentName);
			//
			if (ArrayUtils.isEmpty(listFiles)) {
				return Lists.newArrayList();
			}
			for (File resource : listFiles) {
				try {
					DTO deployedResource = deploy(resource.getName(), new FileInputStream(resource), permission);
					String resourceCode = deployedResource.getCode();
					// log error, if resource with the same code was found twice in one resource
					if (deploedResources.containsKey(resourceCode)) {
						LOG.error("More templates with code [{}] found on the same location [{}].", resourceCode, attachmentName);
					}
					// last one wins
					deploedResources.put(resourceCode, deployedResource);
				} catch (IOException ex) {
					LOG.error("Failed get input stream from, file name [{}].", resource.getName(), ex);
				}					
			}
			//
			LOG.info("Redeployed [{}] resources from location [{}]", deploedResources.size(), attachmentName);
			return Lists.newArrayList(deploedResources.values());
		} catch (Exception ex) {
			throw new ResultCodeException(CoreResultCode.DEPLOY_ERROR, ImmutableMap.of("path", attachmentName), ex);
		} finally {
			FileUtils.deleteQuietly(zipFile);
			FileUtils.deleteQuietly(zipFolder.toFile());
		}
	}
	
	/**
	 * Method replace all attribute from dto with type attributes, old dto will
	 * be backup to system folder.
	 * 
	 * @param resource current resource
	 * @param type xml type 
	 * @return saved dto
	 */
	protected DTO backupAndDeploy(DTO resource, T type, BasePermission... permission) {
		if (resource == null) {
			LOG.info("New resource with code [{}] will be created.", type.getCode());
			//
			return save(toDto(type, null), PermissionUtils.isEmpty(permission) ? null : IdmBasePermission.CREATE);
		}
		//
		LOG.info("Resource with code [{}] will be updated.", type.getCode());
		// backup
		backup(resource);
		// deploy
		return save(toDto(type, resource), permission);
	}
	
	/**
	 * Return folder for backups. If isn't folder defined in configuration
	 * properties use default folder from system property java.io.tmpdir.
	 * 
	 * @return
	 * @since 10.6.0
	 */
	@Override
	public String getBackupFolder() {
		String backupPath = configurationService.getValue(BACKUP_FOLDER_CONFIG);
		if (backupPath == null) {
			backupPath = attachmentManager.getStoragePath();
			LOG.info("Backup files are saved under attachment storage [{}] path. "
					+ "Configure different backup path by configuration property [{}] if needed",
					backupPath,
					BACKUP_FOLDER_CONFIG);
		}
		// append script default backup folder
		backupPath = String.format("%s/%s", backupPath, getBackupFolderName());
		// add date folder
		ZonedDateTime date = ZonedDateTime.now();
		DecimalFormat decimalFormat = new DecimalFormat("00");
		return backupPath + date.getYear() + decimalFormat.format(date.getMonthValue())
				+ decimalFormat.format(date.getDayOfMonth()) + "/";
	}
	
	/**
	 * Method return path for file. That will be save into backup directory.
	 * 
	 * @param directory
	 * @param resource
	 * @return
	 */
	protected String getBackupFileName(String directory, RecoverableDto resource) {
		return String.format(
				"%s%s_%s_%s%s", 
				directory, 
				attachmentManager.getValidFileName(resource.getCode()),
				SpinalCase.format(securityService.getCurrentUsername()),
				System.currentTimeMillis(),
				EXPORT_FILE_SUFIX);
	}
	
	/**
	 * Deploy template from xml file.
	 * Update existing template or creates new one.
	 * 
	 * @param sourceName
	 * @param xmlInputStream
	 * @return
	 */
	private DTO deploy(String sourceName, InputStream xmlInputStream, BasePermission... permission) {
		LOG.debug("Source [{}] will be deployed.", sourceName);
		//
		T xmlType = readType(sourceName, xmlInputStream);
		if (xmlType == null) {
			throw new ResultCodeException(CoreResultCode.DEPLOY_ERROR,
					ImmutableMap.of("code", sourceName));
		}
		// find dto by code
		String code = xmlType.getCode();
		DTO resource = getByCode(code);
		//
		return backupAndDeploy(resource, xmlType, permission);
	}
}
