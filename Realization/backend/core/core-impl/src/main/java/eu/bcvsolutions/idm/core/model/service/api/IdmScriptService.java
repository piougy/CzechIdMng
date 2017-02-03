package eu.bcvsolutions.idm.core.model.service.api;

import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.api.service.ReadWriteEntityService;
import eu.bcvsolutions.idm.core.model.dto.filter.ScriptFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmScript;

/**
 * Default service for script
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Service
public interface IdmScriptService extends ReadWriteEntityService<IdmScript, ScriptFilter> {

}
