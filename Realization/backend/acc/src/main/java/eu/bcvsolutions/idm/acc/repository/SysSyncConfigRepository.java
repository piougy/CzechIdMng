package eu.bcvsolutions.idm.acc.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.idm.acc.dto.filter.SysSyncConfigFilter;
import eu.bcvsolutions.idm.acc.entity.SysSyncConfig;
import eu.bcvsolutions.idm.acc.entity.SysSystemAttributeMapping;
import eu.bcvsolutions.idm.acc.entity.SysSystemMapping;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;

/**
 * Synchronization config repository 
 * 
 * TODO: refactor methods countByTokenAttribute, countByFilterAttribute, countBySystemMapping attributes to UUID
 * 
 * @author Svanda
 *
 */
public interface SysSyncConfigRepository extends AbstractEntityRepository<SysSyncConfig> {

	@Query(value = "select e from SysSyncConfig e"+ 
			" where" +
	        " (?#{[0].systemId} is null or e.systemMapping.objectClass.system.id = ?#{[0].systemId})"+
			" and"+
			 " (?#{[0].name} is null or e.name = ?#{[0].name})"
			)
	Page<SysSyncConfig> find(SysSyncConfigFilter filter, Pageable pageable);
	
	@Query("select count(e) from SysSyncLog e where e.synchronizationConfig = :config and e.running = TRUE")
	int runningCount(@Param("config") SysSyncConfig config);
	
	Long countByCorrelationAttribute_Id(@Param("correlationAttribute") UUID correlationAttribute);	
	
	Long countByTokenAttribute(@Param("tokenAttribute") SysSystemAttributeMapping entity);	
	
	Long countByFilterAttribute(@Param("filterAttribute") SysSystemAttributeMapping entity);	
	
	Long countBySystemMapping(@Param("systemMapping") SysSystemMapping entity);	
}
