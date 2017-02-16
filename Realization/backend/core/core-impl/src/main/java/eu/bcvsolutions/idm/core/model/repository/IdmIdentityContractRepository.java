package eu.bcvsolutions.idm.core.model.repository;

import java.util.List;

import org.joda.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
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
	@Query("update #{#entityName} e set e.guarantee = null where e.guarantee = :identity")
	int clearGuarantee(@Param("identity") IdmIdentity identity);
	
	/**
	 * Returns expired contracts
	 * 
	 * @param expiration date to compare
	 * @param pageable
	 * @return
	 */
	@Query(value = "select e from #{#entityName} e" +
			" where"
	        + " (validTill is not null and validTill < :expiration)")
	Page<IdmIdentityContract> findExpiredContracts(@Param("expiration") LocalDate expiration, Pageable pageable);
	

}
