package eu.bcvsolutions.idm.core.eav.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.eav.dto.filter.FormAttributeFilter;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormAttribute;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormDefinition;
import eu.bcvsolutions.idm.core.eav.rest.projection.IdmFormAttributeExcerpt;

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
		excerptProjection = IdmFormAttributeExcerpt.class,
		exported = false)
public interface IdmFormAttributeRepository extends AbstractEntityRepository<IdmFormAttribute, FormAttributeFilter> {
	
	/**
	 * Attribute definition name is unique in one form definition
	 * 
	 * @param formDefinition
	 * @param name
	 * @return
	 */
	IdmFormAttribute findOneByFormDefinitionAndName(@Param("formDefinition") IdmFormDefinition formDefinition, @Param("name") String name);
	
	@Override
	@Query(value = "select e from #{#entityName} e "
			+ " where"
			+ " (?#{[0].formDefinition} is null or e.formDefinition = ?#{[0].formDefinition})"
			+ " and"
			+ " (?#{[0].definitionType} is null or e.formDefinition.type = ?#{[0].definitionType})"
			+ " and"
			+ " (?#{[0].definitionName} is null or e.formDefinition.name = ?#{[0].definitionName})"
			+ " and"
			+ " (?#{[0].name} is null or lower(e.name) like ?#{[0].name == null ? '%' : '%'.concat([0].name.toLowerCase()).concat('%')})")
	Page<IdmFormAttribute> find(FormAttributeFilter filter, Pageable pageable);
	
	/**
	 * Finds one attribute from given definition by given name
	 * 
	 * @param definitionType
	 * @param definitionName
	 * @param name
	 * @return
	 */
	IdmFormAttribute findOneByFormDefinition_typeAndFormDefinition_nameAndName(String definitionType, String definitionName, String name);
	
	/**
	 * Returns all form attributes by given definition ordered by seq
	 * 
	 * @param formDefinition
	 * @return
	 */
	List<IdmFormAttribute> findByFormDefinitionOrderBySeq(@Param("formDefinition") IdmFormDefinition formDefinition);
}
