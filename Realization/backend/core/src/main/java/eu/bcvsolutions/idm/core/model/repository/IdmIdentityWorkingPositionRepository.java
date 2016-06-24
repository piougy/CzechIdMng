package eu.bcvsolutions.idm.core.model.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.security.access.prepost.PreAuthorize;

import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityWorkingPosition;

@PreAuthorize("hasAuthority('superAdminRole')")
@RepositoryRestResource(//
		collectionResourceRel = "workingPositions", //
		path = "workingPositions", //
		itemResourceRel = "workingPosition" //
	)
public interface IdmIdentityWorkingPositionRepository extends BaseRepository<IdmIdentityWorkingPosition> {
	
	Page<IdmIdentityWorkingPosition> findByIdentity(@Param("identity") IdmIdentity identity, Pageable pageable);
}
