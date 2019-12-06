package eu.bcvsolutions.idm.core.eav.repository;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.eav.api.entity.FormableEntity;
import eu.bcvsolutions.idm.core.eav.entity.AbstractFormValue;

/**
 * Abstract form attribute values repository
 * 
 * @author Radek Tomiška
 *
 * @param <E> Form attribute value type
 * @param <O> Values owner type
 */
@NoRepositoryBean
public interface AbstractFormValueRepository<O extends FormableEntity, E extends AbstractFormValue<O>> extends AbstractEntityRepository<E> {
	
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
	Page<O> findOwnersByDateValue(@Param("formAttributeId") UUID attributeId, @Param("persistentValue") ZonedDateTime persistentValue, Pageable pageable);
	
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
