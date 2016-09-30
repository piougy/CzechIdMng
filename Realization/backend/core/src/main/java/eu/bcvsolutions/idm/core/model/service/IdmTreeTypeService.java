package eu.bcvsolutions.idm.core.model.service;

import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.model.dto.QuickFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;

/**
 * Operations with IdmTreeType
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Service
public interface IdmTreeTypeService extends ReadWriteEntityService<IdmTreeType, QuickFilter> {

}
