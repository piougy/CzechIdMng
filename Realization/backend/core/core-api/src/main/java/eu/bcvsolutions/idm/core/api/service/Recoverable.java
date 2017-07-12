package eu.bcvsolutions.idm.core.api.service;

import eu.bcvsolutions.idm.core.api.dto.AbstractDto;

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

public interface Recoverable<DTO extends AbstractDto> {

	public static final String ENCODING_HANDLER = "com.sun.xml.bind.characterEscapeHandler";
	public static final String BACKUP_FOLDER_CONFIG = "idm.sec.core.backups.default.folder.path";
	public static final String EXPORT_FILE_SUFIX = ".xml";
	
	/**
	 * Method load system entities from resources by all classpath defined by
	 * application property, save all new entity into database. Entities that
	 * already exist will not be overwritten.
	 * 
	 */
	void init();

	/**
	 * Backup DTO to directory given in application properties.
	 * 
	 * @param dto
	 * @param folder
	 */
	void backup(DTO dto);

	/**
	 * Redeploy DTO. Redeployed will be only DTOs, that has pattern in resource.
	 * Before save newly loaded DO will be backup the old DTO into backup
	 * directory.
	 * 
	 * @param dto
	 * @return
	 */
	DTO redeploy(DTO dto);
}
