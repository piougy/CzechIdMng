package eu.bcvsolutions.idm.core.bulk.action.impl.role;

import java.util.Objects;

import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleCompositionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleComposition_;
import eu.bcvsolutions.idm.core.model.event.processor.role.DuplicateRoleCompositionProcessor;

/**
 * Project specific processor for duplicate role composition.
 * 
 * @author Radek Tomiška
 * @since 9.5.0
 */
@Component(CustomDuplicateRoleCompositionProcessor.PROCESSOR_NAME)
@Description("Duplicate role - composition and recursion.")
public class CustomDuplicateRoleCompositionProcessor extends DuplicateRoleCompositionProcessor {
	
	public static final String PROCESSOR_NAME = "custom-duplicate-role-composition-processor";
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	/**
	 * Returns true, when role should be cloned recursively
	 * - it's not cloned, if application sub role doesn't exist on the target environment before.
	 * 
	 * @param event processed event
	 * @param originalSubRole original sub role
	 * @param targetSubRole duplicate sub role. {@code null} if target role has to be created. 
	 * @return
	 */
	@Override
	public boolean duplicateRecursively(EntityEvent<IdmRoleDto> event, IdmRoleDto originalSubRole, IdmRoleDto targetSubRole) {
		 return (targetSubRole != null && targetSubRole.getId() != null) || originalSubRole.getChildrenCount() > 0;
	}
	
	/**
	 * Returns true, when role composition should be included in the target role
	 * - it's not included, when sub role doesn't have the same environment
	 * 
	 * @param event processed event
	 * @param composition source composition
	 * @return
	 */
	@Override
	public boolean includeComposition(EntityEvent<IdmRoleDto> event, IdmRoleCompositionDto composition) {
		 IdmRoleDto subRole = DtoUtils.getEmbedded(composition, IdmRoleComposition_.sub);
		 //
		 return Objects.equals(event.getOriginalSource().getEnvironment(), subRole.getEnvironment());
	}
	
	@Override
	public boolean isDefaultDisabled() {
		// just for the test usage: has to be enable in concrete test
		// remove it in production usage and disable core processor by default (by application.properties)
		return true;
	}
}
