package eu.bcvsolutions.idm.vs.repository;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.core.eav.repository.AbstractFormValueRepository;
import eu.bcvsolutions.idm.vs.entity.VsAccount;
import eu.bcvsolutions.idm.vs.entity.VsAccountFormValue;

/**
 * Extended attributes for account on virtual system
 * 
 * @author Svanda
 *
 */
@RepositoryRestResource(
		itemResourceRel = "formValue",
		collectionResourceRel = "formValues",
		exported = false
		)
public interface VsAccountFormValueRepository extends AbstractFormValueRepository<VsAccount, VsAccountFormValue> {
	
}
