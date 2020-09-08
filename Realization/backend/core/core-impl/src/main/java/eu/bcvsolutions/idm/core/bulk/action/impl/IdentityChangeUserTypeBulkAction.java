package eu.bcvsolutions.idm.core.bulk.action.impl;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;


import eu.bcvsolutions.idm.core.CoreModuleDescriptor;
import eu.bcvsolutions.idm.core.api.bulk.action.AbstractBulkAction;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.eav.api.domain.BaseFaceType;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;

/**
 * Bulk operation changing identity user type
 *
 * @author Ondrej Husnik
 *
 */
@Enabled(CoreModuleDescriptor.MODULE_ID)
@Component(IdentityChangeUserTypeBulkAction.NAME)
@Description(IdentityChangeUserTypeBulkAction.DESCRIPTION)
public class IdentityChangeUserTypeBulkAction extends AbstractBulkAction<IdmIdentityDto, IdmIdentityFilter> {

	public static final String NAME = "core-identity-change-user-type-bulk-action";
	public static final String DESCRIPTION = "Change user type of the idetity bulk action."; // just for test purposes
	public static final String PROPERTY_USER_TYPE = "user-type";

	@Autowired
	private IdmIdentityService identityService;

	@Override
	public List<IdmFormAttributeDto> getFormAttributes() {
		List<IdmFormAttributeDto> formAttributes = super.getFormAttributes();
		formAttributes.add(getUserTypeFormDef());
		return formAttributes;
	}

	@Override
	public String getName() {
		return IdentityChangeUserTypeBulkAction.NAME;
	}

	@Override
	protected OperationResult processDto(IdmIdentityDto identity) {
		UUID newProjectionId = getUserType();
		UUID oldProjectionId = identity.getFormProjection();

		if (!Objects.equals(newProjectionId, oldProjectionId)) {
			identity.setFormProjection(newProjectionId);
			identity = identityService.save(identity);
		}
		return new OperationResult.Builder(OperationState.EXECUTED).build();
	}
	
	@Override
	public List<String> getAuthorities() {
		List<String> permissions = super.getAuthorities();
		permissions.add(CoreGroupPermission.IDENTITY_CHANGEPROJECTION);
		return permissions;
	}
	
	/**
	 * Gets projection user type parameter
	 * 
	 * @return
	 */
	private UUID getUserType() {
		Object userTypeObj = this.getProperties().get(PROPERTY_USER_TYPE);
		return DtoUtils.toUuid(userTypeObj);
	}
	
	/**
	 * Prepares form definition of the bulk action parameter
	 * 
	 * @return
	 */
	private IdmFormAttributeDto getUserTypeFormDef() {
		IdmFormAttributeDto type = new IdmFormAttributeDto(
				PROPERTY_USER_TYPE, 
				PROPERTY_USER_TYPE, 
				PersistentType.UUID);
		type.setFaceType(BaseFaceType.FORM_PROJECTION_SELECT);
		type.setRequired(true);
		return type;
	}

	@Override
	public int getOrder() {
		return super.getOrder() + 450;
	}

	@Override
	public ReadWriteDtoService<IdmIdentityDto, IdmIdentityFilter> getService() {
		return identityService;
	}
}
