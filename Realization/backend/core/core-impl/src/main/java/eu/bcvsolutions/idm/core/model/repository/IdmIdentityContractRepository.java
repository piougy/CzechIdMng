package eu.bcvsolutions.idm.core.model.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.core.api.dto.EmptyFilter;
import eu.bcvsolutions.idm.core.api.repository.BaseRepository;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;

@RepositoryRestResource(//
		collectionResourceRel = "identityContracts", //
		path = "identityContracts", //
		itemResourceRel = "identityContract", //
		exported = false
)
public interface IdmIdentityContractRepository extends BaseRepository<IdmIdentityContract, EmptyFilter> {

	@Override
	@Query(value = "select e from IdmIdentityContract e")
	Page<IdmIdentityContract> find(EmptyFilter filter, Pageable pageable);
	
	List<IdmIdentityContract> findAllByIdentity(@Param("identity") IdmIdentity identity, Sort sort);
	
	@Query(value = "select e from IdmIdentityContract e" +
	        " where" +
	        " (:treeType is null or e.workingPosition.treeType = :treeType)")
	List<IdmIdentityContract> findAllByTreeType(@Param("treeType") IdmTreeType treeType);
	
	@Query(value = "select e from IdmIdentityContract e" +
	        " where" +
	        " (:treeNode is null or e.workingPosition = :treeNode)")
	List<IdmIdentityContract> findAllByTreeNode(@Param("treeNode") IdmTreeNode treeNode);
}
