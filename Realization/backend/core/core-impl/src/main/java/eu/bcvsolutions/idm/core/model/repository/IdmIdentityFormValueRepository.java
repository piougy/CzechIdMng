package eu.bcvsolutions.idm.core.model.repository;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityFormValue;
import eu.bcvsolutions.idm.eav.repository.AbstractFormValueRepository;

/**
 * Extended attributes for identity
 * 
 * @author Radek Tomiška
 *
 */
@RepositoryRestResource(exported = false)
public interface IdmIdentityFormValueRepository extends AbstractFormValueRepository<IdmIdentity, IdmIdentityFormValue> {
	
}
