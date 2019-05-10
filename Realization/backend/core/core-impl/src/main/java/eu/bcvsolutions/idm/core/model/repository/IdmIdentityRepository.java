package eu.bcvsolutions.idm.core.model.repository;

import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;

/**
 * Repository for identities
 * 
 * @author Radek Tomiška 
 *
 */
public interface IdmIdentityRepository extends AbstractEntityRepository<IdmIdentity> {

	IdmIdentity findOneByUsername(@Param("username") String username);
}
