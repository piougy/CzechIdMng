package eu.bcvsolutions.idm.core.eav.repository;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.eav.api.entity.FormableEntity;
import eu.bcvsolutions.idm.core.eav.api.service.FormValueService;
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
//TODO without this annotation it wont startup when I use this repository in IdmServiceConfigration, it fail on multiple methods that it cannot create context
@NoRepositoryBean
public interface AbstractFormValueRepository<O extends FormableEntity, E extends AbstractFormValue<O>> extends AbstractEntityRepository<E> {
	
	/**
	 * Returns all form values by given owner (from all definitions)
	 * 
	 * @param owner
	 * @return
	 * @deprecated owner needs to be persisted - use {@link #findByOwner_Id(Serializable)}
	 * @deprecated @since 8.2.0 use {@link FormValueService#find(eu.bcvsolutions.idm.core.eav.api.dto.filter.IdmFormValueFilter, Pageable))}
	 */
	@Deprecated
	List<E> findByOwner(@Param(FormValueService.PROPERTY_OWNER) O owner);
	
	/**
	 * Returns all form values by given owner (from all definitions)
	 * 
	 * @param ownerId
	 * @return
	 * @deprecated @since 8.2.0 use {@link FormValueService#find(eu.bcvsolutions.idm.core.eav.api.dto.filter.IdmFormValueFilter, Pageable))}
	 */
	@Deprecated
	List<E> findByOwner_Id(Serializable ownerId);
	
	/**
	 * Returns form values by given owner and definition ordered by seq
	 * 
	 * @param owner
	 * @param formDefiniton
	 * @return
	 * @deprecated owner needs to be persisted - use {@link #findByOwner_IdAndFormAttribute_FormDefinition_IdOrderBySeqAsc(Serializable, UUID)}
	 * @deprecated @since 8.2.0 use {@link FormValueService#find(eu.bcvsolutions.idm.core.eav.api.dto.filter.IdmFormValueFilter, Pageable))}
	 */
	@Deprecated
	List<E> findByOwnerAndFormAttribute_FormDefinitionOrderBySeqAsc(@Param(FormValueService.PROPERTY_OWNER) O owner, @Param("formDefinition") IdmFormDefinition formDefiniton);
	
	/**
	 * Returns form values by given owner and definition ordered by seq
	 * 
	 * @param owner
	 * @param formDefinitonId
	 * @return
	 * @deprecated owner needs to be persisted - use {@link #findByOwner_IdAndFormAttribute_FormDefinition_IdOrderBySeqAsc(Serializable, UUID)}
	 * @deprecated @since 8.2.0 use {@link FormValueService#find(eu.bcvsolutions.idm.core.eav.api.dto.filter.IdmFormValueFilter, Pageable))}
	 */
	@Deprecated
	List<E> findByOwnerAndFormAttribute_FormDefinition_IdOrderBySeqAsc(@Param(FormValueService.PROPERTY_OWNER) O owner, @Param("formDefinitionId") UUID formDefinitonId);
	
	/**
	 * Returns form values by given owner and definition ordered by seq
	 * 
	 * @param ownerId
	 * @param formDefinitonId
	 * @return
	 * @deprecated @since 8.2.0 use {@link FormValueService#find(eu.bcvsolutions.idm.core.eav.api.dto.filter.IdmFormValueFilter, Pageable))}
	 */
	@Deprecated
	List<E> findByOwner_IdAndFormAttribute_FormDefinition_IdOrderBySeqAsc(Serializable ownerId, UUID formDefinitonId);
	
	/**
	 * Returns form values by given owner and attribute ordered by seq
	 * 
	 * @param owner
	 * @param attribute
	 * @return
	 * @deprecated owner needs to be persisted - use {@link #findByOwner_IdAndFormAttribute_IdOrderBySeqAsc(Serializable, UUID)}
	 * @deprecated @since 8.2.0 use {@link FormValueService#find(eu.bcvsolutions.idm.core.eav.api.dto.filter.IdmFormValueFilter, Pageable))}
	 */
	@Deprecated
	List<E> findByOwner_IdAndFormAttributeOrderBySeqAsc(@Param(FormValueService.PROPERTY_OWNER) O owner, @Param("attribute") IdmFormAttribute attribute);
	
	/**
	 * Returns form values by given owner and attribute ordered by seq
	 * 
	 * @param ownerId
	 * @param attributeId
	 * @return
	 * @deprecated @since 8.2.0 use {@link FormValueService#find(eu.bcvsolutions.idm.core.eav.api.dto.filter.IdmFormValueFilter, Pageable))}
	 */
	@Deprecated
	List<E> findByOwner_IdAndFormAttribute_IdOrderBySeqAsc(Serializable ownerId, UUID attributeId);
	
	/**
	 * Finds owners by given attribute and value. Use {@link #findOwnersByShortTextValue(UUID, String, Pageable)} instead - it's indexed.
	 * 
	 * @see #findOwnersByShortTextValue(UUID, String, Pageable)
	 * @param attribute
	 * @param persistentValue
	 * @return
	 * @deprecated use {@link #findOwnersByShortTextValue(UUID, String, Pageable)}
	 */
	@Deprecated
	@Query(value = "select distinct e.owner from #{#entityName} e " 
			+ " where"
			+ " (e.formAttribute.id = :formAttributeId)"
			+ "	and"
			+ " (e.stringValue = :persistentValue)")
	Page<O> findOwnersByStringValue(@Param("formAttributeId") UUID attributeId, @Param("persistentValue") String persistentValue, Pageable pageable);
	
	/**
	 * Finds owners by given attribute and value
	 * 
	 * @param attribute
	 * @param persistentValue
	 * @return
	 * @since 7.8.3
	 */
	@Query(value = "select distinct e.owner from #{#entityName} e " 
			+ " where"
			+ " (e.formAttribute.id = :formAttributeId)"
			+ "	and"
			+ " (e.shortTextValue = :persistentValue)")
	Page<O> findOwnersByShortTextValue(@Param("formAttributeId") UUID attributeId, @Param("persistentValue") String persistentValue, Pageable pageable);
	
	/**
	 * Finds owners by given attribute and value
	 * 
	 * @param attribute
	 * @param persistentValue
	 * @return
	 */
	@Query(value = "select distinct e.owner from #{#entityName} e " 
			+ " where"
			+ " (e.formAttribute.id = :formAttributeId)"
			+ "	and"
			+ " (e.longValue = :persistentValue)")
	Page<O> findOwnersByLongValue(@Param("formAttributeId") UUID attributeId, @Param("persistentValue") Long persistentValue, Pageable pageable);
	
	/**
	 * Finds owners by given attribute and value
	 * 
	 * @param attribute
	 * @param persistentValue
	 * @return
	 */
	@Query(value = "select distinct e.owner from #{#entityName} e " 
			+ " where"
			+ " (e.formAttribute.id = :formAttributeId)"
			+ "	and"
			+ " (e.booleanValue = :persistentValue)")
	Page<O> findOwnersByBooleanValue(@Param("formAttributeId") UUID attributeId, @Param("persistentValue") Boolean persistentValue, Pageable pageable);
	
	/**
	 * Finds owners by given attribute and value
	 * 
	 * @param attribute
	 * @param persistentValue
	 * @return
	 */
	@Query(value = "select distinct e.owner from #{#entityName} e " 
			+ " where"
			+ " (e.formAttribute.id = :formAttributeId)"
			+ "	and"
			+ " (e.dateValue = :persistentValue)")
	Page<O> findOwnersByDateValue(@Param("formAttributeId") UUID attributeId, @Param("persistentValue") DateTime persistentValue, Pageable pageable);
	
	/**
	 * Finds owners by given attribute and value
	 * 
	 * @param attribute
	 * @param persistentValue
	 * @return
	 */
	@Query(value = "select distinct e.owner from #{#entityName} e " 
			+ " where"
			+ " (e.formAttribute.id = :formAttributeId)"
			+ "	and"
			+ " (e.doubleValue = :persistentValue)")
	Page<O> findOwnersByDoubleValue(@Param("formAttributeId") UUID attributeId, @Param("persistentValue") BigDecimal persistentValue, Pageable pageable);
	
	/**
	 * Finds owners by given attribute and value
	 * 
	 * @param attribute
	 * @param persistentValue
	 * @return
	 */
	@Query(value = "select distinct e.owner from #{#entityName} e " 
			+ " where"
			+ " (e.formAttribute.id = :formAttributeId)"
			+ "	and"
			+ " (e.byteValue = :persistentValue)")
	Page<O> findOwnersByByteArrayValue(@Param("formAttributeId") UUID attributeId, @Param("persistentValue") byte[] persistentValue, Pageable pageable);
	
	/**
	 * Finds owners by given attribute and value
	 * 
	 * @param attribute
	 * @param persistentValue
	 * @return
	 */
	@Query(value = "select distinct e.owner from #{#entityName} e " 
			+ " where"
			+ " (e.formAttribute.id = :formAttributeId)"
			+ "	and"
			+ " (e.uuidValue = :persistentValue)")
	Page<O> findOwnersByUuidValue(@Param("formAttributeId") UUID attributeId, @Param("persistentValue") UUID persistentValue, Pageable pageable);
}
