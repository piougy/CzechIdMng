package eu.bcvsolutions.idm.eav.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.eav.dto.FormAttributeFilter;
import eu.bcvsolutions.idm.eav.entity.IdmFormAttribute;
import eu.bcvsolutions.idm.eav.entity.IdmFormDefinition;
import eu.bcvsolutions.idm.eav.repository.projection.IdmFormAttributeDefinitionExcerpt;

/**
 * Form attribute definition repository
 * 
 * @author Radek Tomiška 
 *
 */
@RepositoryRestResource( //
		collectionResourceRel = "formAttributes", // 
		path = "form-attributes", //
		itemResourceRel = "formAttribute", //
		excerptProjection = IdmFormAttributeDefinitionExcerpt.class,
		exported = false)
public interface IdmFormAttributeRepository extends AbstractEntityRepository<IdmFormAttribute, FormAttributeFilter> {
	
	/**
	 * Attribute definition name is unique in one form definition
	 * 
	 * @param formDefinition
	 * @param name
	 * @return
	 */
	IdmFormDefinition findOneByFormDefinitionAndName(@Param("formDefinition") IdmFormDefinition formDefinition, @Param("name") String name);
	
	@Override
	@Query(value = "select e from #{#entityName} e "
			+ " where"
			+ " (?#{[0].formDefinition} is null or e.formDefinition = ?#{[0].formDefinition})"
			+ " and"
			+ " (?#{[0].name} is null or lower(e.name) like ?#{[0].name == null ? '%' : '%'.concat([0].name.toLowerCase()).concat('%')})")
	Page<IdmFormAttribute> find(FormAttributeFilter filter, Pageable pageable);
	
	/**
	 * Returns all form attributes by given definition ordered by seq
	 * 
	 * @param formDefinition
	 * @return
	 */
	List<IdmFormAttribute> findByFormDefinitionOrderBySeq(@Param("formDefinition") IdmFormDefinition formDefinition);
	
	/**
	 * Removes all attribute definition by given fom definition
	 * 
	 * @param formDefinition
	 * @return
	 */
	int deleteByFormDefinition(@Param("formDefinition") IdmFormDefinition formDefinition);
}
