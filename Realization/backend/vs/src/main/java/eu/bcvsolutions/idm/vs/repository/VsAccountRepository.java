package eu.bcvsolutions.idm.vs.repository;

import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.vs.entity.VsAccount;

/**
 * Repository for virtual system accounts
 * 
 * @author Svanda
 *
 */
public interface VsAccountRepository extends AbstractEntityRepository<VsAccount> {

	VsAccount findOneByUid(@Param("uid") String uid);

}
