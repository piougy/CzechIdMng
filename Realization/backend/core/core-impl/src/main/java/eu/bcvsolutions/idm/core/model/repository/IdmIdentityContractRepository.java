package eu.bcvsolutions.idm.core.model.repository;

import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.domain.RecursionType;
import eu.bcvsolutions.idm.core.model.dto.filter.IdentityContractFilter;
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
@RepositoryRestResource(//
		collectionResourceRel = "identityContracts", //
		path = "identity-contracts", //
		itemResourceRel = "identityContract", //
		exported = false
)
public interface IdmIdentityContractRepository extends AbstractEntityRepository<IdmIdentityContract, IdentityContractFilter> {

	/*
	 * (non-Javadoc)
	 * @see eu.bcvsolutions.idm.core.api.repository.BaseEntityRepository#find(eu.bcvsolutions.idm.core.api.dto.BaseFilter, Pageable)
	 */
	@Override
	@Query(value = "select e from #{#entityName} e"
			+ " left join e.workingPosition wp" +
	        " where"
	        + " ("
	        	+ " ?#{[0].text} is null"
	        	+ " or "
	        	+ "	lower(e.position) like ?#{[0].text == null ? '%' : '%'.concat([0].text.toLowerCase()).concat('%')}"
	        	+ " or"
	        	+ " lower(wp.name) like ?#{[0].text == null ? '%' : '%'.concat([0].text.toLowerCase()).concat('%')}"
	        	+ " or"
	        	+ " lower(wp.code) like ?#{[0].text == null ? '%' : '%'.concat([0].text.toLowerCase()).concat('%')}"
        	+ " )"
	        + " and"
	        + "	("
	        	+ "?#{[0].identity} is null or e.identity = ?#{[0].identity}"
        	+ " )")
	Page<IdmIdentityContract> find(IdentityContractFilter filter, Pageable pageable);
	
	List<IdmIdentityContract> findAllByIdentity(@Param("identity") IdmIdentity identity, Sort sort);
	
	@Query(value = "select e from #{#entityName} e"
			+ " where"
			+ " (e.workingPosition = ?#{[0]})" // takes all recursion
			+ " or"
			+ " (?#{[1].name()} = 'DOWN' and e.workingPosition.forestIndex.lft between ?#{[0].lft} and ?#{[0].rgt})"
			+ " or"
			+ " (?#{[1].name()} = 'UP' and ?#{[0].lft} between e.workingPosition.forestIndex.lft and e.workingPosition.forestIndex.rgt)")
	List<IdmIdentityContract> findAllByWorkPosition(IdmTreeNode workPosition, RecursionType recursionType);
	
	Long countByWorkingPosition(@Param("treeNode") IdmTreeNode treeNode);
	
	Long countByWorkingPosition_TreeType(@Param("treeType") IdmTreeType treeType);

	/**
	 * Removes all contracts of given identity
	 * 
	 * @param identity
	 * @return
	 */
	int deleteByIdentity(@Param("identity") IdmIdentity identity);
	
	/**
	 * Clears guarantee from all contracts, where identity is guarantee (=identity disclaims guarantee).
	 * 
	 * @param identity
	 * @return
	 */
	@Modifying
	@Query("update #{#entityName} e set e.guarantee = null, e.modified = :modified where e.guarantee = :identity")
	int clearGuarantee(@Param("identity") IdmIdentity identity, @Param("modified") DateTime modified);
	
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
	        + " (validTill is not null and validTill < :expiration)"
	        + " and"
	        + " (disabled = :disabled)")
	Page<IdmIdentityContract> findExpiredContracts(@Param("expiration") LocalDate expiration, @Param("disabled") boolean disabled, Pageable pageable);
	
	/**
	 * Clears default tree type for all tree types instead given updatedEntityId
	 * 
	 * @param updatedEntityId
	 */
	@Modifying
	@Query("update #{#entityName} e set e.main = false, e.modified = :modified where e.identity = :identity and (:updatedEntityId is null or e.id != :updatedEntityId)")
	void clearMain(@Param("identity") IdmIdentity identity, @Param("updatedEntityId") UUID updatedEntityId, @Param("modified") DateTime modified);
	
	/**
	 * Return persisted identity contract
	 * 
	 * @param identityContract
	 * @return
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.REPEATABLE_READ)
	@Query("select e from #{#entityName} e where e = :identityContract")
	IdmIdentityContract getPersistedIdentityContract(@Param("identityContract") IdmIdentityContract identityContract);
}
