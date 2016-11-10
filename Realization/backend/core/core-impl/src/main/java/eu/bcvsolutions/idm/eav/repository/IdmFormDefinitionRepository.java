package eu.bcvsolutions.idm.eav.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.core.api.dto.EmptyFilter;
import eu.bcvsolutions.idm.core.api.repository.BaseRepository;
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
public interface IdmFormDefinitionRepository extends BaseRepository<IdmFormDefinition, EmptyFilter> {
	
	IdmFormDefinition findOneByName(@Param("name") String name);
	
	@Override
	@Query(value = "select e from #{#entityName} e")
	Page<IdmFormDefinition> find(EmptyFilter filter, Pageable pageable);
}
