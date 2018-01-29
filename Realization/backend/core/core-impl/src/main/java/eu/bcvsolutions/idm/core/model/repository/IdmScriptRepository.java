package eu.bcvsolutions.idm.core.model.repository;

import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.idm.core.api.dto.filter.IdmScriptFilter;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.entity.IdmScript;

/**
 * Repository for scripts.
 * @see {@link IdmScript}
 * @see {@link IdmScriptFilter}
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public interface IdmScriptRepository extends AbstractEntityRepository<IdmScript> {

	IdmScript findOneByName(@Param("name") String name);
	
	IdmScript findOneByCode(@Param("code") String code);
}
