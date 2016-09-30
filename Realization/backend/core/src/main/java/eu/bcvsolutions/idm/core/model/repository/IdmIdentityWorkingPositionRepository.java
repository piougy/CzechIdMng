package eu.bcvsolutions.idm.core.model.repository;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityWorkingPosition;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;

@RepositoryRestResource(//
		collectionResourceRel = "workingPositions", //
		path = "workingPositions", //
		itemResourceRel = "workingPosition", //
		exported = false
)
public interface IdmIdentityWorkingPositionRepository extends BaseRepository<IdmIdentityWorkingPosition> {

	List<IdmIdentityWorkingPosition> findAllByIdentity(@Param("identity") IdmIdentity identity, Sort sort);
	
	@Query(value = "select e from IdmIdentityWorkingPosition e" +
	        " where" +
	        " (:treeType is null or e.treeNode.treeType = :treeType)")
	List<IdmIdentityWorkingPosition> findAllByTreeType(@Param("treeType") IdmTreeType treeType);
	
	@Query(value = "select e from IdmIdentityWorkingPosition e" +
	        " where" +
	        " (:treeNode is null or e.treeNode = :treeNode)")
	List<IdmIdentityWorkingPosition> findAllByTreeNode(@Param("treeNode") IdmTreeNode treeNode);
}
