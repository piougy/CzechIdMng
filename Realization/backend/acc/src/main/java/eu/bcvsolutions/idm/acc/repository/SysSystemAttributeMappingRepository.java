package eu.bcvsolutions.idm.acc.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.dto.filter.SystemAttributeMappingFilter;
import eu.bcvsolutions.idm.acc.entity.SysSystemAttributeMapping;
import eu.bcvsolutions.idm.acc.rest.projection.SysSystemAttributeMappingExcerpt;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;

/**
 * Schema attributes handling repository
 * 
 * @author Svanda
 *
 */
@RepositoryRestResource(//
		collectionResourceRel = "systemAttributeMappings", //
		path = "system-attribute-mappings", //
		itemResourceRel = "systemAttributeMapping", //
		excerptProjection = SysSystemAttributeMappingExcerpt.class,//
		exported = false // we are using repository metadata, but we want expose
							// rest endpoint manually
)
public interface SysSystemAttributeMappingRepository extends AbstractEntityRepository<SysSystemAttributeMapping, SystemAttributeMappingFilter> {
	
	/*
	 * (non-Javadoc)
	 * @see eu.bcvsolutions.idm.core.api.repository.BaseEntityRepository#find(eu.bcvsolutions.idm.core.api.dto.BaseFilter, Pageable)
	 */
	@Override
	@Query(value = "select e from SysSystemAttributeMapping e" +
			" where"
			+ " (?#{[0].text} is null or lower(e.name) like ?#{[0].text == null ? '%' : '%'.concat([0].text.toLowerCase()).concat('%')})"
	        + " and"
	        + " (?#{[0].systemMappingId} is null or e.systemMapping.id = ?#{[0].systemMappingId})"
	        + " and"
	        + " (?#{[0].schemaAttributeId} is null or e.schemaAttribute.id = ?#{[0].schemaAttributeId})"
	        + " and"
	        + " (?#{[0].systemId} is null or e.systemMapping.objectClass.system.id = ?#{[0].systemId})"
	        + " and"
	        + " (?#{[0].isUid} is null or e.uid = ?#{[0].isUid})"
	        + " and"
			+ " (?#{[0].idmPropertyName} is null or lower(e.idmPropertyName) like ?#{[0].idmPropertyName == null ? '%' : '%'.concat([0].idmPropertyName.toLowerCase()).concat('%')})"
			)
	Page<SysSystemAttributeMapping> find(SystemAttributeMappingFilter filter, Pageable pageable);
	
	@Query("SELECT e FROM SysSystemAttributeMapping e WHERE "
			+ "e.authenticationAttribute = true "
			+ "AND "
			+ "e.systemMapping.operationType = :operationType "
			+ "AND "
			+ "e.systemMapping.objectClass.system.id = :systemId ")
	SysSystemAttributeMapping findAuthenticationAttribute(@Param("systemId") UUID systemId, @Param("operationType") SystemOperationType operationType);
	
	@Query("SELECT e FROM SysSystemAttributeMapping e WHERE "
			+ "e.uid = true "
			+ "AND "
			+ "e.systemMapping.operationType = :operationType "
			+ "AND "
			+ "e.systemMapping.objectClass.system.id = :systemId ")
	SysSystemAttributeMapping findUidAttribute(@Param("systemId") UUID systemId, @Param("operationType") SystemOperationType operationType);
}
