package eu.bcvsolutions.idm.core.model.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.entity.IdmContractSlice;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;

/**
 * Contract time slices
 * 
 * @author svandav
 *
 */
public interface IdmContractSliceRepository extends AbstractEntityRepository<IdmContractSlice> {
	
	List<IdmContractSlice> findAllByIdentity(@Param("identity") IdmIdentity identity, Sort sort);
	
	List<IdmContractSlice> findAllByIdentity_Id(@Param("identityId") UUID identityId, Sort sort);
	
}
