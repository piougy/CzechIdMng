package eu.bcvsolutions.idm.core.model.repository.eav;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.core.eav.repository.AbstractFormValueRepository;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.entity.eav.IdmTreeNodeFormValue;

/**
 * Extended attributes for tree node
 * 
 * @author Radek Tomiška
 *
 */
@RepositoryRestResource(
		itemResourceRel = "formValue",
		collectionResourceRel = "formValues",
		exported = false
		)
public interface IdmTreeNodeFormValueRepository extends AbstractFormValueRepository<IdmTreeNode, IdmTreeNodeFormValue> {
	
}
