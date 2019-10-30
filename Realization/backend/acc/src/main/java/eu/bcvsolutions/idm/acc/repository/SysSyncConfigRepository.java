package eu.bcvsolutions.idm.acc.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.idm.acc.entity.SysSyncConfig;
import eu.bcvsolutions.idm.acc.entity.SysSyncContractConfig;
import eu.bcvsolutions.idm.acc.entity.SysSyncIdentityConfig;
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
	
	@Query("select count(e) from SysSyncLog e where e.synchronizationConfig = :config and e.running = TRUE")
	int runningCount(@Param("config") SysSyncConfig config);
	
	Long countByCorrelationAttribute_Id(@Param("correlationAttribute") UUID correlationAttribute);	
	
	Long countByTokenAttribute(@Param("tokenAttribute") SysSystemAttributeMapping entity);	
	
	Long countByFilterAttribute(@Param("filterAttribute") SysSystemAttributeMapping entity);	
	
	Long countBySystemMapping(@Param("systemMapping") SysSystemMapping entity);	
	
	/**
	 * Select sync configs by default leader.
	 * 
	 * @param defaultLeaderId
	 * @return
	 * @since 10.0.0
	 */
	@Query("select e from SysSyncContractConfig e where e.defaultLeader.id = :defaultLeader")
	List<SysSyncContractConfig> findByDefaultLeader(@Param("defaultLeader") UUID defaultLeaderId);
	
	/**
	 * Find configs by default tree type.
	 * 
	 * @param defaultTreeTypeId
	 * @return
	 * @since 10.0.0
	 */
	@Query("select e from SysSyncContractConfig e where e.defaultTreeType.id = :defaultTreeType")
	List<SysSyncContractConfig> findByDefaultTreeType(@Param("defaultTreeType") UUID defaultTreeTypeId);
	
	/**
	 * Find configs by default tree node.
	 * 
	 * @param defaultTreeNodeId
	 * @return
	 * @since 10.0.0
	 */
	@Query("select e from SysSyncContractConfig e where e.defaultTreeNode.id = :defaultTreeNode")
	List<SysSyncContractConfig> findByDefaultTreeNode(@Param("defaultTreeNode") UUID defaultTreeNodeId);
	
	/**
	 * Find configs by default role.
	 * 
	 * @param defaultRole
	 * @return
	 * @since 10.0.0
	 */
	@Query("select e from SysSyncIdentityConfig e where e.defaultRole.id = :defaultRole")
	List<SysSyncIdentityConfig> findByDefaultRole(@Param("defaultRole") UUID defaultRole);
}
