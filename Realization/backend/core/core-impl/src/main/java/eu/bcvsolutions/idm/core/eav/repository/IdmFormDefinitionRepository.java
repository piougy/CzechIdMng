package eu.bcvsolutions.idm.core.eav.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.core.api.dto.filter.QuickFilter;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormDefinition;
import eu.bcvsolutions.idm.core.eav.rest.projection.IdmFormDefinitionExcerpt;

/**
 * Form definition repository
 * 
 * @author Radek Tomiška 
 *
 */
@RepositoryRestResource( //
		collectionResourceRel = "formDefinitions", // 
		path = "form-definitions", //
		itemResourceRel = "formDefinition", //
		excerptProjection = IdmFormDefinitionExcerpt.class,
		exported = false)
public interface IdmFormDefinitionRepository extends AbstractEntityRepository<IdmFormDefinition, QuickFilter> {
	
	/**
	 * Returns all form definitions by given type
	 * 
	 * @param type
	 * @return
	 */
	List<IdmFormDefinition> findByType(@Param("type") String type);
	
	/**
	 * Returns form definition by given type and name (unique)
	 * 
	 * @param type
	 * @param name
	 * @return
	 */
	IdmFormDefinition findOneByTypeAndName(@Param("type") String type, @Param("name") String name);
	
	/**
	 * Quick search
	 */
	@Override
	@Query(value = "SELECT e FROM #{#entityName} e WHERE"
			+ " (?#{[0].text} is null or lower(e.name) like ?#{[0].text == null ? '%' : '%'.concat([0].text.toLowerCase()).concat('%')})")
	Page<IdmFormDefinition> find(QuickFilter filter, Pageable pageable);
}
