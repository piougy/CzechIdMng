package eu.bcvsolutions.idm.core.model.repository.eav;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.core.eav.repository.AbstractFormValueRepository;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.eav.IdmRoleFormValue;

/**
 * Extended attributes for role
 * 
 * @author Radek Tomiška
 *
 */
@RepositoryRestResource(
		itemResourceRel = "formValue",
		collectionResourceRel = "formValues",
		exported = false
		)
public interface IdmRoleFormValueRepository extends AbstractFormValueRepository<IdmRole, IdmRoleFormValue> {
	
}
