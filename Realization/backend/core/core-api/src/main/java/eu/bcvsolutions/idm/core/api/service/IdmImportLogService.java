package eu.bcvsolutions.idm.core.api.service;

import eu.bcvsolutions.idm.core.api.dto.IdmImportLogDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmImportLogFilter;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * CRUD for import logs
 * 
 * @author Vít Švanda
 *
 */
public interface IdmImportLogService
		extends ReadWriteDtoService<IdmImportLogDto, IdmImportLogFilter>, AuthorizableService<IdmImportLogDto> {

	/**
	 * Standard save, but (for new log) first check if doesn't exists log for same batch and DTO
	 * ID. If exists, then ID of existed log will be used as ID for this log (prevent
	 * creation of duplicated logs).
	 * 
	 * @param dto
	 * @param permission
	 * @return
	 */
	IdmImportLogDto saveDistinct(IdmImportLogDto dto, BasePermission... permission);

}
