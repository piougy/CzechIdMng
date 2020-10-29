package eu.bcvsolutions.idm.core.api.service;

import java.io.File;
import java.util.List;

import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.exception.ForbiddenEntityException;
import eu.bcvsolutions.idm.core.ecm.api.dto.IdmAttachmentDto;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;

/**
 * Interface for services. Services that implement this interface allow backup
 * and restore of entities. Backup and recovery is performed using XML.
 * Recovering is from a files in the classpath. Backup is located in the folder
 * defined in the configuration file.
 * 
 * Now suported operations:
 * - default initialization
 * - backup DTO
 * - redeploy DTO
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
public interface Recoverable<DTO extends BaseDto> {

	String ENCODING_HANDLER = "com.sun.xml.bind.characterEscapeHandler";
	String BACKUP_FOLDER_CONFIG = "idm.sec.core.backups.default.folder.path";
	String EXPORT_FILE_SUFIX = ".xml";
	
	/**
	 * Method load system entities from resources by all classpath defined by
	 * application property, save all new entity into database. Entities that
	 * already exist will not be overwritten.
	 * 
	 */
	void init();
	
	/**
	 * Return folder for backups. If isn't folder defined in configuration
	 * properties use default folder from system property java.io.tmpdir.
	 * 
	 * @return
	 * @since 10.6.0
	 */
	String getBackupFolder();

	/**
	 * Backup DTO to directory given in application properties.
	 * 
	 * @param dto
	 * @param folder
	 * @retur backup file
	 */
	File backup(DTO dto);

	/**
	 * Redeploy DTO. Redeployed will be only DTOs, that has pattern in resource.
	 * Before save newly loaded DTO will be backup the old DTO into backup
	 * directory.
	 * 
	 * @param dto
	 * @param permission permissions to evaluate (AND)
	 * @return
	 * @throws ForbiddenEntityException if authorization policies doesn't met
	 */
	DTO redeploy(DTO dto, BasePermission... permission);
	
	/**
	 * Deploy from attachment. Deployed (created or updated) will be DTOs in attachment (.zip, .xml files are supported).
	 * Before save newly loaded DTO will be backup the old DTO into backup
	 * directory.
	 * 
	 * @param persisted attachment
	 * @param permission permissions to evaluate (AND)
	 * @return list of redeployed DTOs
	 * @throws ForbiddenEntityException if authorization policies doesn't met
	 * @since 10.6.0
	 */
	List<DTO> deploy(IdmAttachmentDto attachment, BasePermission... permission);
}
