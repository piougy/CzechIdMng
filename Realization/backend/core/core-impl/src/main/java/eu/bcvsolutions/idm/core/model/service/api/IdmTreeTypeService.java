package eu.bcvsolutions.idm.core.model.service.api;

import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.api.dto.filter.QuickFilter;
import eu.bcvsolutions.idm.core.api.service.ReadWriteEntityService;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;

/**
 * Operations with IdmTreeType
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Service
public interface IdmTreeTypeService extends ReadWriteEntityService<IdmTreeType, QuickFilter> {

	/**
	 * Returns tree type by code 
	 * 
	 * @param code
	 * @return
	 */
	IdmTreeType getByCode(String code);
	
	/**
	 * Returns default tree type or {@code null}, if no default tree type is defined
	 * 
	 * @return
	 */
	IdmTreeType getDefaultTreeType();
}
