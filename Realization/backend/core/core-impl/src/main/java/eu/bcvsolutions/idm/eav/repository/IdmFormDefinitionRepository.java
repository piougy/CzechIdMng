package eu.bcvsolutions.idm.eav.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.core.api.dto.EmptyFilter;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.eav.entity.IdmFormDefinition;
import eu.bcvsolutions.idm.eav.repository.projection.IdmFormDefinitionExcerpt;

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
public interface IdmFormDefinitionRepository extends AbstractEntityRepository<IdmFormDefinition, EmptyFilter> {
	
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
	@Query(value = "select e from #{#entityName} e")
	Page<IdmFormDefinition> find(EmptyFilter filter, Pageable pageable);
}
