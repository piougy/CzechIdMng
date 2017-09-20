package eu.bcvsolutions.idm.core.model.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import eu.bcvsolutions.idm.core.api.dto.filter.IdmScriptAuthorityFilter;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.entity.IdmScriptAuthority;

/**
 * Repository for {@link IdmScriptAuthority}, use default filtering by script id
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
public interface IdmScriptAuthorityRepository extends AbstractEntityRepository<IdmScriptAuthority> {

	@Query(value = "select e from #{#entityName} e" +
	        " where"
	        + " ("
	        	+ " ?#{[0].scriptId} is null"
	        	+ " or e.script.id = ?#{[0].scriptId}" 
	        + " )")
	Page<IdmScriptAuthority> find(IdmScriptAuthorityFilter filter, Pageable pageable);
}
