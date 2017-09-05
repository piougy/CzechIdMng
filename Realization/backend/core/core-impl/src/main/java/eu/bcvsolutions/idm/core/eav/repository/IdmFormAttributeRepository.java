package eu.bcvsolutions.idm.core.eav.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.eav.api.dto.filter.IdmFormAttributeFilter;
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
public interface IdmFormAttributeRepository extends AbstractEntityRepository<IdmFormAttribute, IdmFormAttributeFilter> {
	
	/**
	 * Attribute definition name is unique in one form definition
	 * 
	 * @param formDefinition
	 * @param name
	 * @return
	 */
	IdmFormAttribute findOneByFormDefinitionAndCode(@Param("formDefinition") IdmFormDefinition formDefinition, @Param("code") String code);
	
	@Override
	@Query(value = "select e from #{#entityName} e "
			+ " where"
			+ " (?#{[0].formDefinitionId} is null or e.formDefinition.id = ?#{[0].formDefinitionId})"
			+ " and"
			+ " (?#{[0].definitionType} is null or e.formDefinition.type = ?#{[0].definitionType})"
			+ " and"
			+ " (?#{[0].definitionName} is null or e.formDefinition.name = ?#{[0].definitionName})"
			+ " and"
			+ " (?#{[0].code} is null or lower(e.code) like ?#{[0].code == null ? '%' : '%'.concat([0].code.toLowerCase()).concat('%')})")
	Page<IdmFormAttribute> find(IdmFormAttributeFilter filter, Pageable pageable);
	
	/**
	 * Finds one attribute from given definition by given name
	 * 
	 * @param definitionType
	 * @param definitionName
	 * @param name
	 * @return
	 */
	IdmFormAttribute findOneByFormDefinition_typeAndFormDefinition_codeAndCode(String definitionType, String definitionCode, String code);
	
	/**
	 * Returns all form attributes by given definition ordered by seq
	 * 
	 * @param formDefinition
	 * @return
	 */
	List<IdmFormAttribute> findByFormDefinitionOrderBySeq(@Param("formDefinition") IdmFormDefinition formDefinition);
	
	/**
	 * Returns all form attributes by given definition ordered by seq
	 * 
	 * @param formDefinition
	 * @return
	 */
	List<IdmFormAttribute> findByFormDefinition_IdOrderBySeq(@Param("formDefinitionId") UUID formDefinitionId);
}
