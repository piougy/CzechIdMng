package eu.bcvsolutions.idm.core.model.repository.eav;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.core.eav.repository.AbstractFormValueRepository;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.eav.IdmIdentityContractFormValue;

/**
 * Extended attributes for identity contracts
 * 
 * @author Radek Tomiška
 *
 */
@RepositoryRestResource(
		itemResourceRel = "formValue",
		collectionResourceRel = "formValues",
		exported = false
		)
public interface IdmIdentityContractFormValueRepository extends AbstractFormValueRepository<IdmIdentityContract, IdmIdentityContractFormValue> {
	
}
