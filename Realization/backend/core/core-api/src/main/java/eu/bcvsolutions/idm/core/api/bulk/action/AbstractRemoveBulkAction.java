package eu.bcvsolutions.idm.core.api.bulk.action;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;

/**
 * Abstract class for all remove operations
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 * @param <DTO>
 * @param <F>
 */

public abstract class AbstractRemoveBulkAction<DTO extends AbstractDto, F extends BaseFilter>
		extends AbstractBulkAction<DTO, F> {

	@Override
	protected OperationResult processDto(DTO dto) {
		this.getService().delete(dto);
		return new OperationResult.Builder(OperationState.EXECUTED).build();
	}

	@Override
	protected BasePermission[] getPermissionForEntity() {
		BasePermission[] permissions= {
				IdmBasePermission.DELETE
		};
		return permissions;
	}
}
