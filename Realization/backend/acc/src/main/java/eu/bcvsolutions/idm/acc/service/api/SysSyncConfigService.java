package eu.bcvsolutions.idm.acc.service.api;

import eu.bcvsolutions.idm.acc.dto.AbstractSysSyncConfigDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSyncConfigFilter;
import eu.bcvsolutions.idm.core.api.script.ScriptEnabled;
import eu.bcvsolutions.idm.core.api.service.CloneableService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import java.util.List;
import java.util.UUID;

/**
 * Synchronization config service
 * 
 * @author Vít Švanda
 *
 */
public interface SysSyncConfigService extends ReadWriteDtoService<AbstractSysSyncConfigDto, SysSyncConfigFilter>, CloneableService<AbstractSysSyncConfigDto>, ScriptEnabled {

	/**
	 * Method check if synchronization with given config running.
	 * 
	 * @param config
	 * @return
	 */
	boolean isRunning(AbstractSysSyncConfigDto config);
	
	/**
	 * Return count of {@link AbstractSysSyncConfigDto} for {@link SysSystemMappingDto}
	 * 
	 * @param mappingDto
	 * @return
	 */
	Long countBySystemMapping(SysSystemMappingDto mappingDto);

	/**
	 * Find role sync configurations by member system mapping ID.
	 */
	List<AbstractSysSyncConfigDto> findRoleConfigBySystemMapping(UUID mappingId);

	/**
	 * Find role configs by system member-of attribute ID.
	 */
	List<AbstractSysSyncConfigDto> findRoleConfigByMemberOfAttribute(UUID mappingId);

	/**
	 * Find role configs by system member identifier attribute ID.
	 */
	List<AbstractSysSyncConfigDto> findRoleConfigByMemberIdentifierAttribute(UUID mappingId);

	/**
	 * Find role configs by role catalog node ID.
	 */
	List<AbstractSysSyncConfigDto> findRoleConfigByMainCatalogueRoleNode(UUID catalogId);

	/**
	 * Find role configs by role catalog node ID.
	 */
	List<AbstractSysSyncConfigDto> findRoleConfigByRemoveCatalogueRoleParentNode(UUID catalogId);
}
