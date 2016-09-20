package eu.bcvsolutions.idm.core.model.repository;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityWorkingPosition;

@RepositoryRestResource(//
		collectionResourceRel = "workingPositions", //
		path = "workingPositions", //
		itemResourceRel = "workingPosition", //
		exported = false
)
public interface IdmIdentityWorkingPositionRepository extends BaseRepository<IdmIdentityWorkingPosition> {

	List<IdmIdentityWorkingPosition> findAllByIdentity(@Param("identity") IdmIdentity identity, Sort sort);
}
