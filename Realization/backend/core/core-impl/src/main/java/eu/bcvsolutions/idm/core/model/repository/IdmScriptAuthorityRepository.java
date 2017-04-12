package eu.bcvsolutions.idm.core.model.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.Description;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.dto.filter.ScriptAuthorityFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmScriptAuthority;
import eu.bcvsolutions.idm.core.rest.projection.IdmScriptExcerpt;

/**
 * Repository for {@link IdmScriptAuthority}, use default filtering by script id
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@RepositoryRestResource(
		collectionResourceRel = "scriptAuthorities",
		itemResourceRel = "scriptAuthority",
		collectionResourceDescription = @Description("ScriptAuthorities"),
		itemResourceDescription = @Description("Authority for scripts"),
		excerptProjection = IdmScriptExcerpt.class,
		exported = false
	)
public interface IdmScriptAuthorityRepository extends AbstractEntityRepository<IdmScriptAuthority, ScriptAuthorityFilter> {

	@Override
	@Query(value = "select e from #{#entityName} e" +
	        " where"
	        + " ("
	        	+ " ?#{[0].script.id} is null"
	        	+ " or e.script.id = ?#{[0].scriptId}" 
	        + " )")
	Page<IdmScriptAuthority> find(ScriptAuthorityFilter filter, Pageable pageable);
}
