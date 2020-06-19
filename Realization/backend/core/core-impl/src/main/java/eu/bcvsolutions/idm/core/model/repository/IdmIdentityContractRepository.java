package eu.bcvsolutions.idm.core.model.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.idm.core.api.domain.RecursionType;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityContractFilter;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;

/**
 * Identity contracts (working positions etc.)
 * 
 * @author Radek Tomiška
 *
 */
public interface IdmIdentityContractRepository extends AbstractEntityRepository<IdmIdentityContract> {
	
	String FIND_BY_WORK_PROSITION_QUERY = "select e from IdmTreeNode wp, #{#entityName} e join e.workPosition n"
			+ " where"
			+ " wp.id = :workPositionId"
			+ " and"
			+ " n.treeType = wp.treeType" // more tree types
			+ " and"
			+ " ("
				+ " (n.id = wp.id)" // takes all recursion
				+ " or"
				+ " ("
					+ " ?#{[1] == null ? '' : #recursionType.name()} = 'DOWN'"
					+ " and n.forestIndex.lft between wp.forestIndex.lft and wp.forestIndex.rgt"
				+ " )"
				+ " or"
				+ " ("
					+ " ?#{[1] == null ? '' : #recursionType.name()} = 'UP'"
					+ " and wp.forestIndex.lft between n.forestIndex.lft and n.forestIndex.rgt"
				+ " )"
			+ " )";
	
	/**
	 * All contracts of given identity.
	 * 
	 * @param identity
	 * @param sort
	 * @return
	 */
	List<IdmIdentityContract> findAllByIdentity(@Param("identity") IdmIdentity identity, Sort sort);
	
	/**
	 * All contracts of given identity.
	 * 
	 * @param identityId
	 * @param sort
	 * @return
	 */
	List<IdmIdentityContract> findAllByIdentity_Id(@Param("identityId") UUID identityId, Sort sort);
	
	/**
	 * Contracts with given tree node (by workPositionId) recursively (by recursionType).
	 * 
	 * @param workPositionId
	 * @param recursionType
	 * @return
	 * @see #findByWorkPosition(UUID, RecursionType, Pageable)
	 * @deprecated @since 10.4.0 use {@link IdmIdentityContractFilter#PARAMETER_RECURSION_TYPE}
	 */
	@Query(value = FIND_BY_WORK_PROSITION_QUERY)
	List<IdmIdentityContract> findAllByWorkPosition(
			@Param("workPositionId") UUID workPositionId, 
			@Param("recursionType") RecursionType recursionType);
	
	/**
	 * Valid contracts (by given date) of given identity.
	 * 
	 * @param identityId
	 * @param date
	 * @param isExterne
	 * @return
	 */
	@Query(value = "select e from #{#entityName} e"
			+ " where"
			+ " (e.state is null or e.state != eu.bcvsolutions.idm.core.api.domain.ContractState.DISABLED)"
			+ " and"
			+ " (:identityId is null or e.identity.id = :identityId)"
			+ " and"
			+ "  (:isExterne is null or e.externe = :isExterne)"
			+ " and"
			+ " ( e.validTill is null or (?#{[1] == null ? 'null' : ''} = 'null' or e.validTill >= :date ))"
			+ " and"
			+ " ( e.validFrom is null or (?#{[1] == null ? 'null' : ''} = 'null' or e.validFrom <= :date ))")
	List<IdmIdentityContract> findAllValidContracts(
			@Param("identityId") UUID identityId, 
			@Param("date") LocalDate date, 
			@Param("isExterne") Boolean isExterne);
	
	/**
	 * Count of contracts with given tree node.
	 * 
	 * @param treeNodeId
	 * @return
	 */
	Long countByWorkPosition_Id(@Param("treeNodeId") UUID treeNodeId);
	
	/**
	 * Count of contracts with given tree type.
	 * 
	 * @param treeTypeId
	 * @return
	 */
	Long countByWorkPosition_TreeType_Id(UUID treeTypeId);

	/**
	 * Returns expired contracts for given identity.
	 *
	 * @param identityId
	 * @param expiration
	 * @param pageable
	 * @return
	 */
	@Query(value = "select e from #{#entityName} e" +
			" where"
			+ " (:identityId is null or e.identity.id = :identityId)"
			+ " and"
	        + " (validTill is not null and validTill < :expiration)")
	Page<IdmIdentityContract> findExpiredContractsByIdentity(@Param("identityId") UUID identityId, @Param("expiration") LocalDate expiration, Pageable pageable);
	
	/**
	 * Returns expired contracts. Its useful to find enabled contracts only.
	 * The method search <b>ALL</b> expired contracts.
	 *
	 * @param expiration
	 * @param pageable
	 * @return
	 */
	@Query(value = "select e from #{#entityName} e" +
			" where"
	        + " (validTill is not null and validTill < :expiration)")
	Page<IdmIdentityContract> findExpiredContracts(@Param("expiration") LocalDate expiration, Pageable pageable);
}
