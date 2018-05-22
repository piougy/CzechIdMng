package eu.bcvsolutions.idm.core.model.repository;

import java.util.List;
import java.util.UUID;

import org.joda.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.domain.RecursionType;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;

/**
 * Identity contracts (working positions etc.)
 * 
 * @author Radek Tomiška
 *
 */
public interface IdmIdentityContractRepository extends AbstractEntityRepository<IdmIdentityContract> {
	
	List<IdmIdentityContract> findAllByIdentity(@Param("identity") IdmIdentity identity, Sort sort);
	
	List<IdmIdentityContract> findAllByIdentity_Id(@Param("identityId") UUID identityId, Sort sort);
	
	@Query(value = "select e from #{#entityName} e join e.workPosition n"
			+ " where"
			+ " (n.treeType = ?#{[0].treeType})" // more tree types
			+ " and"
			+ " ("
				+ " (n = ?#{[0]})" // takes all recursion
				+ " or"
				+ " (?#{[1].name()} = 'DOWN' and n.forestIndex.lft between ?#{[0].lft} and ?#{[0].rgt})"
				+ " or"
				+ " (?#{[1].name()} = 'UP' and ?#{[0].lft} between n.forestIndex.lft and n.forestIndex.rgt)"
			+ " )")
	List<IdmIdentityContract> findAllByWorkPosition(IdmTreeNode workPosition, RecursionType recursionType);
	
	@Query(value = "select e from #{#entityName} e"
			+ " where"
			+ " (e.state is null or e.state != eu.bcvsolutions.idm.core.api.domain.ContractState.DISABLED)"
			+ " and"
			+ " (:identityId is null or e.identity.id = :identityId)"
			+ " and"
			+ "  (:onlyExterne is null or e.externe = :onlyExterne)"
			+ " and"
			+ " ( e.validTill is null or (?#{[1] == null ? 'null' : ''} = 'null' or e.validTill >= :date ))"
			+ " and"
			+ " ( e.validFrom is null or (?#{[1] == null ? 'null' : ''} = 'null' or e.validFrom <= :date ))")
	List<IdmIdentityContract> findAllValidContracts(
			@Param("identityId") UUID identityId, 
			@Param("date") LocalDate date, 
			@Param("onlyExterne") Boolean onlyExterne);
	
	/**
	 * @deprecated use {@link #countByWorkPosition_Id(UUID)}
	 */
	@Deprecated
	Long countByWorkPosition(@Param("treeNode") IdmTreeNode treeNode);
	
	Long countByWorkPosition_Id(@Param("treeNodeId") UUID treeNodeId);
	
	/**
	 * @deprecated use {@link #countByWorkPosition_TreeType_Id(UUID)}
	 */
	@Deprecated
	Long countByWorkPosition_TreeType(@Param("treeType") IdmTreeType treeType);
	
	Long countByWorkPosition_TreeType_Id(UUID treeTypeId);
	
	/**
	 * Returns expired contracts. Its useful to find enabled contracts only.
	 * 
	 * @param expiration date to compare
	 * @param disabled find disabled contracts or not
	 * @param pageable
	 * @return
	 */
	@Query(value = "select e from #{#entityName} e" +
			" where"
	        + " (validTill is not null and validTill < :expiration)")
	Page<IdmIdentityContract> findExpiredContracts(@Param("expiration") LocalDate expiration, Pageable pageable);
	
	/**
	 * Returns persisted identity contract
	 * 
	 * @param identityContract
	 * @return
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.REPEATABLE_READ)
	@Query("select e from #{#entityName} e where e.id = :identityContractId")
	IdmIdentityContract getPersistedIdentityContract(@Param("identityContractId") UUID identityContractId);
}
