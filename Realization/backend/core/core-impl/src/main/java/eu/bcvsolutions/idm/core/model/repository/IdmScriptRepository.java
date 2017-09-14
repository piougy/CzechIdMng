package eu.bcvsolutions.idm.core.model.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.idm.core.api.dto.filter.ScriptFilter;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.entity.IdmScript;

/**
 * Repository for scripts.
 * @see {@link IdmScript}
 * @see {@link ScriptFilter}
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public interface IdmScriptRepository extends AbstractEntityRepository<IdmScript> {

	@Query(value = "select e from IdmScript e" +
	        " where"
	        + " ("
		        + " ?#{[0].text} is null"
		        + " or (lower(e.code) like ?#{[0].text == null ? '%' : '%'.concat([0].text.toLowerCase()).concat('%')})"
		        + " or (lower(e.name) like ?#{[0].text == null ? '%' : '%'.concat([0].text.toLowerCase()).concat('%')})"
		        + " or (lower(e.description) like ?#{[0].text == null ? '%' : '%'.concat([0].text.toLowerCase()).concat('%')})"
	        + " ) "
	        + "AND "
	        + " ("
	        	+ " ?#{[0].description} is null"
	        	+ " or lower(e.description) like ?#{[0].description == null ? '%' : '%'.concat([0].description.toLowerCase()).concat('%')}"
	        + " ) "
	        + "AND "
	        + " ("
	        	+ " ?#{[0].category} is null"
	        	+ " or e.category = ?#{[0].category}" 
	        + " ) "
	        + "AND "
	        + "( "
	        	+ " ?#{[0].code} is null"
	        	+ " or e.code = ?#{[0].code}"
	        + ") ")
	Page<IdmScript> find(ScriptFilter filter, Pageable pageable);
	
	IdmScript findOneByName(@Param("name") String name);
	
	IdmScript findOneByCode(@Param("code") String code);
}
