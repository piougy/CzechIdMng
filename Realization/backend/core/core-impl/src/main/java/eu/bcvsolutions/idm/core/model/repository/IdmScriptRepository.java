package eu.bcvsolutions.idm.core.model.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.Description;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.dto.filter.ScriptFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmScript;
import eu.bcvsolutions.idm.core.rest.projection.IdmScriptExcerpt;

/**
 * Repository for scripts.
 * @see {@link IdmScript}
 * @see {@link ScriptFilter}
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@RepositoryRestResource(
		collectionResourceRel = "scripts",
		itemResourceRel = "scripts",
		collectionResourceDescription = @Description("Scripts"),
		itemResourceDescription = @Description("Scripts"),
		excerptProjection = IdmScriptExcerpt.class,
		exported = false
	)
public interface IdmScriptRepository extends AbstractEntityRepository<IdmScript, ScriptFilter> {

	@Override
	@Query(value = "select e from IdmScript e" +
	        " where"
	        + " ("
		        + " ?#{[0].text} is null"
		        + " or lower(e.name) like ?#{[0].text == null ? '%' : '%'.concat([0].text.toLowerCase()).concat('%')}"
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
	        + " )")
	Page<IdmScript> find(ScriptFilter filter, Pageable pageable);
}
