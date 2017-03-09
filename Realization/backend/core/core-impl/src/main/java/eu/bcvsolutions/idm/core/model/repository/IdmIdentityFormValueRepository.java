package eu.bcvsolutions.idm.core.model.repository;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.core.eav.repository.AbstractFormValueRepository;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityFormValue;

/**
 * Extended attributes for identity
 * 
 * @author Radek Tomiška
 *
 */
@RepositoryRestResource(
		itemResourceRel = "formValue",
		collectionResourceRel = "formValues",
		exported = false
		)
public interface IdmIdentityFormValueRepository extends AbstractFormValueRepository<IdmIdentity, IdmIdentityFormValue> {
	
}
