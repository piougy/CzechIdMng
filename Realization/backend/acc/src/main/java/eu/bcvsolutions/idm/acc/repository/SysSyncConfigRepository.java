package eu.bcvsolutions.idm.acc.repository;

import eu.bcvsolutions.idm.acc.entity.SysSyncRoleConfig;
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
	
	/**
	 * Find role configs by member system mapping ID.
	 * 
	 * @since 11.0.1
	 */
	@Query("select e from SysSyncRoleConfig e where e.memberSystemMapping.id = :memberSystemMapping")
	List<SysSyncRoleConfig> findRoleConfigBySystemMapping(@Param("memberSystemMapping") UUID memberSystemMapping);
	
	/**
	 * Find role configs by system member-of attribute ID.
	 * 
	 * @since 11.0.1
	 */
	@Query("select e from SysSyncRoleConfig e where e.memberOfAttribute.id = :attributeMapping")
	List<SysSyncRoleConfig> findRoleConfigByMemberOfAttribute(@Param("attributeMapping") UUID attributeMapping);
	
	/**
	 * Find role configs by system member identifier attribute ID.
	 * 
	 * @since 11.0.1
	 */
	@Query("select e from SysSyncRoleConfig e where e.memberIdentifierAttribute.id = :memberIdentifierAttribute")
	List<SysSyncRoleConfig> findRoleConfigByMemberIdentifierAttribute(@Param("memberIdentifierAttribute") UUID memberIdentifierAttribute);
	
	/**
	 * Find role configs by role catalog node ID.
	 * 
	 * @since 11.0.1
	 */
	@Query("select e from SysSyncRoleConfig e where e.mainCatalogueRoleNode.id = :mainCatalogueRoleNode")
	List<SysSyncRoleConfig> findRoleConfigByMainCatalogueRoleNode(@Param("mainCatalogueRoleNode") UUID mainCatalogueRoleNode);
	
	/**
	 * Find role configs by role catalog node ID.
	 * 
	 * @since 11.0.1
	 */
	@Query("select e from SysSyncRoleConfig e where e.removeCatalogueRoleParentNode.id = :removeCatalogueRoleParentNode")
	List<SysSyncRoleConfig> findRoleConfigByRemoveCatalogueRoleParentNode(@Param("removeCatalogueRoleParentNode") UUID removeCatalogueRoleParentNode);
}
