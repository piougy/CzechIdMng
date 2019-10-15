package eu.bcvsolutions.idm.acc.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
	
	@Query("select count(e) from SysSyncLog e where e.synchronizationConfig = :config and e.running = TRUE")
	int runningCount(@Param("config") SysSyncConfig config);
	
	Long countByCorrelationAttribute_Id(@Param("correlationAttribute") UUID correlationAttribute);	
	
	Long countByTokenAttribute(@Param("tokenAttribute") SysSystemAttributeMapping entity);	
	
	Long countByFilterAttribute(@Param("filterAttribute") SysSystemAttributeMapping entity);	
	
	Long countBySystemMapping(@Param("systemMapping") SysSystemMapping entity);	
	
	/**
	 * Clears default leader
	 * 
	 * @param defaultLeaderId
	 * @return
	 * @deprecated @since 10.0.0 rewrite to fin and delete - skips audit
	 */
	@Deprecated
	@Modifying
	@Query("update SysSyncContractConfig e set e.defaultLeader = null where e.defaultLeader.id = :defaultLeader")
	int clearDefaultLeader(@Param("defaultLeader") UUID defaultLeaderId);
	
	/**
	 * Clears default tree type
	 * 
	 * @param Tree type id
	 * @return
	 * @deprecated @since 10.0.0 rewrite to fin and delete - skips audit
	 */
	@Deprecated
	@Modifying
	@Query("update SysSyncContractConfig e set e.defaultTreeType = null where e.defaultTreeType.id = :defaultTreeType")
	int clearDefaultTreeType(@Param("defaultTreeType") UUID defaultTreeTypeId);
	
	/**
	 * Clears default tree node
	 * 
	 * @param Tree node id
	 * @return
	 * @deprecated @since 10.0.0 rewrite to fin and delete - skips audit
	 */
	@Deprecated
	@Modifying
	@Query("update SysSyncContractConfig e set e.defaultTreeNode = null where e.defaultTreeNode.id = :defaultTreeNode")
	int clearDefaultTreeNode(@Param("defaultTreeNode") UUID defaultTreeNodeId);
	
	/**
	 * Clears default role
	 * 
	 * @param Role id
	 * @return
	 * @deprecated @since 10.0.0 rewrite to fin and delete - skips audit
	 */
	@Deprecated
	@Modifying
	@Query("update SysSyncIdentityConfig e set e.defaultRole = null where e.defaultRole.id = :defaultRole")
	int clearDefaultRole(@Param("defaultRole") UUID defaultRole);
}
