package eu.bcvsolutions.idm.core.eav.repository;

import java.math.BigDecimal;
import java.util.List;

import org.joda.time.DateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.eav.api.entity.FormableEntity;
import eu.bcvsolutions.idm.core.eav.dto.filter.FormValueFilter;
import eu.bcvsolutions.idm.core.eav.entity.AbstractFormValue;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormAttribute;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormDefinition;

/**
 * Abstract form attribute values repository
 * 
 * @author Radek Tomiška
 *
 * @param <E> Form attribute value type
 * @param <O> Values owner type
 */
@NoRepositoryBean
public interface AbstractFormValueRepository<O extends FormableEntity, E extends AbstractFormValue<O>> extends AbstractEntityRepository<E, FormValueFilter<O>> {
	
	/**
	 * Quick search 
	 */
	@Override
	@Query(value = "select e from #{#entityName} e " + " where"
			+ " (?#{[0].owner} is null or e.owner = ?#{[0].owner})"
			+ " and"
			+ " (?#{[0].formDefinition} is null or e.formAttribute.formDefinition = ?#{[0].formDefinition})"
			+ " and"
			+ " (?#{[0].formAttribute} is null or e.formAttribute = ?#{[0].formAttribute})")
	Page<E> find(FormValueFilter<O> filter, Pageable pageable);
	
	/**
	 * Returns all form values by given owner (from all definitions)
	 * 
	 * @param owner
	 * @return
	 */
	List<E> findByOwner(@Param("owner") O owner);
	
	/**
	 * Returns form values by given owner and definition ordered by seq
	 * 
	 * @param owner
	 * @param formDefiniton
	 * @return
	 */
	List<E> findByOwnerAndFormAttribute_FormDefinitionOrderBySeqAsc(@Param("owner") O owner, @Param("formDefinition") IdmFormDefinition formDefiniton);
	
	/**
	 * Returns form values by given owner and attribute ordered by seq
	 * 
	 * @param owner
	 * @param attribute
	 * @return
	 */
	List<E> findByOwnerAndFormAttributeOrderBySeqAsc(@Param("owner") O owner, @Param("attribute") IdmFormAttribute attribute);
	
	/**
	 * Finds owners by given attribute and value
	 * 
	 * @param attribute
	 * @param persistentValue
	 * @return
	 */
	@Query(value = "select distinct e.owner from #{#entityName} e " 
			+ " where"
			+ " (e.formAttribute = :formAttribute)"
			+ "	and"
			+ " (e.stringValue = :persistentValue)")
	Page<O> findOwnersByStringValue(@Param("formAttribute") IdmFormAttribute attribute, @Param("persistentValue") String persistentValue, Pageable pageable);
	
	/**
	 * Finds owners by given attribute and value
	 * 
	 * @param attribute
	 * @param persistentValue
	 * @return
	 */
	@Query(value = "select distinct e.owner from #{#entityName} e " 
			+ " where"
			+ " (e.formAttribute = :formAttribute)"
			+ "	and"
			+ " (e.longValue = :persistentValue)")
	Page<O> findOwnersByLongValue(@Param("formAttribute") IdmFormAttribute attribute, @Param("persistentValue") Long persistentValue, Pageable pageable);
	
	/**
	 * Finds owners by given attribute and value
	 * 
	 * @param attribute
	 * @param persistentValue
	 * @return
	 */
	@Query(value = "select distinct e.owner from #{#entityName} e " 
			+ " where"
			+ " (e.formAttribute = :formAttribute)"
			+ "	and"
			+ " (e.booleanValue = :persistentValue)")
	Page<O> findOwnersByBooleanValue(@Param("formAttribute") IdmFormAttribute attribute, @Param("persistentValue") Boolean persistentValue, Pageable pageable);
	
	/**
	 * Finds owners by given attribute and value
	 * 
	 * @param attribute
	 * @param persistentValue
	 * @return
	 */
	@Query(value = "select distinct e.owner from #{#entityName} e " 
			+ " where"
			+ " (e.formAttribute = :formAttribute)"
			+ "	and"
			+ " (e.dateValue = :persistentValue)")
	Page<O> findOwnersByDateValue(@Param("formAttribute") IdmFormAttribute attribute, @Param("persistentValue") DateTime persistentValue, Pageable pageable);
	
	/**
	 * Finds owners by given attribute and value
	 * 
	 * @param attribute
	 * @param persistentValue
	 * @return
	 */
	@Query(value = "select distinct e.owner from #{#entityName} e " 
			+ " where"
			+ " (e.formAttribute = :formAttribute)"
			+ "	and"
			+ " (e.doubleValue = :persistentValue)")
	Page<O> findOwnersByDoubleValue(@Param("formAttribute") IdmFormAttribute attribute, @Param("persistentValue") BigDecimal persistentValue, Pageable pageable);
	
	/**
	 * Finds owners by given attribute and value
	 * 
	 * @param attribute
	 * @param persistentValue
	 * @return
	 */
	@Query(value = "select distinct e.owner from #{#entityName} e " 
			+ " where"
			+ " (e.formAttribute = :formAttribute)"
			+ "	and"
			+ " (e.byteValue = :persistentValue)")
	Page<O> findOwnersByByteArrayValue(@Param("formAttribute") IdmFormAttribute attribute, @Param("persistentValue") byte[] persistentValue, Pageable pageable);
}
