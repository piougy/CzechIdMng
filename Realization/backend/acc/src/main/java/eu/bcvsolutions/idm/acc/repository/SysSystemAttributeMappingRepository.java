package eu.bcvsolutions.idm.acc.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemAttributeMappingFilter;
import eu.bcvsolutions.idm.acc.entity.SysSystemAttributeMapping;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;

/**
 * Schema attributes handling repository
 * 
 * @author Svanda
 *
 */
public interface SysSystemAttributeMappingRepository extends AbstractEntityRepository<SysSystemAttributeMapping> {

	/**
	 * Old find method in repository
	 *
	 * @param filter
	 * @param pageable
	 * @deprecated use {@link SysSystemAttributeMappingService#find(SysSystemAttributeMappingFilter, Pageable, BasePermission...)}. New filter properties may not be implemented.
	 * @return
	 */
	@Deprecated
	@Query(value = "select e from SysSystemAttributeMapping e" +
			" where"
			+ " (?#{[0].text} is null or lower(e.name) like ?#{[0].text == null ? '%' : '%'.concat([0].text.toLowerCase()).concat('%')})"
	        + " and"
	        + " (?#{[0].systemMappingId} is null or e.systemMapping.id = ?#{[0].systemMappingId})"
	        + " and"
	        + " (?#{[0].schemaAttributeId} is null or e.schemaAttribute.id = ?#{[0].schemaAttributeId})"
	        +  "and"
	        + " (?#{[0].schemaAttributeName} is null or e.schemaAttribute.name = ?#{[0].schemaAttributeName})"
	        + " and"
	        + " (?#{[0].systemId} is null or e.systemMapping.objectClass.system.id = ?#{[0].systemId})"
	        + " and"
	        + " (?#{[0].isUid} is null or e.uid = ?#{[0].isUid})"
	        + " and"
			+ " (?#{[0].idmPropertyName} is null or e.idmPropertyName = ?#{[0].idmPropertyName})"
			+ " and"
	        + " (?#{[0].sendOnPasswordChange} is null or e.sendOnPasswordChange = ?#{[0].sendOnPasswordChange})"
	        + " and"
	        + " (?#{[0].passwordAttribute} is null or e.passwordAttribute = ?#{[0].passwordAttribute})"
	        + " and"
	        + " (?#{[0].disabledAttribute} is null or e.disabledAttribute = ?#{[0].disabledAttribute})"
			)
	Page<SysSystemAttributeMapping> find(SysSystemAttributeMappingFilter filter, Pageable pageable);

	/**
	 * Find authentication attribute
	 *
	 * @param systemId
	 * @param operationType
	 * @param entityType
	 * @deprecated use filter properties from {@link SysSystemAttributeMappingFilter}
	 * @return
	 */
	@Deprecated
	@Query("SELECT e FROM SysSystemAttributeMapping e WHERE "
			+ "e.authenticationAttribute = true "
			+ "AND "
			+ "e.systemMapping.operationType = :operationType "
			+ "AND "
			+ "e.systemMapping.objectClass.system.id = :systemId "
			+ "AND "
			+ "e.systemMapping.entityType = :entityType")
	SysSystemAttributeMapping findAuthenticationAttribute(
			@Param("systemId") UUID systemId,
			@Param("operationType") SystemOperationType operationType, 
			@Param("entityType") SystemEntityType entityType);

	/**
	 * Find uid attribute
	 *
	 * @param systemId
	 * @param operationType
	 * @param entityType
	 * @deprecated use filter properties from {@link SysSystemAttributeMappingFilter}
	 * @return
	 */
	@Deprecated
	@Query("SELECT e FROM SysSystemAttributeMapping e WHERE "
			+ "e.uid = true "
			+ "AND "
			+ "e.systemMapping.operationType = :operationType "
			+ "AND "
			+ "e.systemMapping.objectClass.system.id = :systemId "
			+ "AND "
			+ "e.systemMapping.entityType = :entityType")
	SysSystemAttributeMapping findUidAttribute(@Param("systemId") UUID systemId,
			@Param("operationType") SystemOperationType operationType, @Param("entityType")  SystemEntityType entityType);
	
	/**
	 * Single mapped attribute in given mapping by given name
	 * 
	 * @param systemMappingId
	 * @param name
	 * @deprecated use filter properties from {@link SysSystemAttributeMappingFilter}
	 * @return
	 */
	@Deprecated
	SysSystemAttributeMapping findBySystemMapping_IdAndName(@Param("systemMappingId") UUID systemMappingId, @Param("name") String name);
	
	/**
	 * All mapped attributes in given mapping
	 * 
	 * @param systemMappingId
	 * @deprecated use filter properties from {@link SysSystemAttributeMappingFilter}
	 * @return
	 */
	@Deprecated
	List<SysSystemAttributeMapping> findAllBySystemMapping_Id(@Param("systemMappingId") UUID systemMappingId);
}
