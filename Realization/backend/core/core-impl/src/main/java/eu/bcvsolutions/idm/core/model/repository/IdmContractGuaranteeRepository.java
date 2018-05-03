package eu.bcvsolutions.idm.core.model.repository;

import java.util.UUID;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.api.repository.ExternalIdentifiableRepository;
import eu.bcvsolutions.idm.core.model.entity.IdmContractGuarantee;

/**
 * Identity contract's guarantee
 * 
 * @author Radek Tomiška
 *
 */
public interface IdmContractGuaranteeRepository extends AbstractEntityRepository<IdmContractGuarantee>, ExternalIdentifiableRepository<IdmContractGuarantee, UUID> {

}
