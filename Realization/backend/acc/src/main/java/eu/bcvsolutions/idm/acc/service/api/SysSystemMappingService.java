package eu.bcvsolutions.idm.acc.service.api;

import java.util.List;
import java.util.UUID;

import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaObjectClassDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemMappingFilter;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.service.CloneableService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;

/**
 * System entity handling service
 * 
 * @author svandav
 *
 */
public interface SysSystemMappingService extends ReadWriteDtoService<SysSystemMappingDto, SysSystemMappingFilter>, CloneableService<SysSystemMappingDto> {

	/**
	 * Find system mapping by given attributes
	 * 
	 * @param system
	 * @param operation
	 * @param entityType
	 * @return
	 */
	List<SysSystemMappingDto> findBySystem(SysSystemDto system, SystemOperationType operation, SystemEntityType entityType);
	
	/**
	 * Find system mapping by given attributes
	 * 
	 * @param systemId
	 * @param operation
	 * @param entityType
	 * @return
	 */
	List<SysSystemMappingDto> findBySystemId(UUID systemId, SystemOperationType operation, SystemEntityType entityType);
	
	/**
	 * Find system mapping by given attributes
	 * 
	 * @param objectClass
	 * @param operation
	 * @param entityType
	 * @return
	 */
	List<SysSystemMappingDto> findByObjectClass(SysSchemaObjectClassDto objectClass, SystemOperationType operation, SystemEntityType entityType);

	/**
	 * Is enabled protection of account against delete
	 * @param account
	 * @return
	 */
	boolean isEnabledProtection(AccAccountDto account);

	/**
	 * Interval of protection against account delete
	 * @param account
	 * @return
	 */
	Integer getProtectionInterval(AccAccountDto account);

	boolean canBeAccountCreated(AbstractDto dto, String script, SysSystemDto system);
}
