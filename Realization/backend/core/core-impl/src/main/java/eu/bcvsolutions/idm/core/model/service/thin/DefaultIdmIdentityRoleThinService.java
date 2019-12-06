package eu.bcvsolutions.idm.core.model.service.thin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.ObjectUtils;

import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.dto.thin.IdmIdentityRoleThinDto;
import eu.bcvsolutions.idm.core.api.service.AbstractReadDtoService;
import eu.bcvsolutions.idm.core.api.service.thin.IdmIdentityRoleThinService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.entity.thin.IdmIdentityRoleThin;
import eu.bcvsolutions.idm.core.model.repository.thin.IdmIdentityRoleThinRepository;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;
import eu.bcvsolutions.idm.core.security.api.utils.PermissionUtils;

/**
 * Operations with identity roles - thin variant:
 * - supports get method only.
 * 
 * @author Radek Tomiška
 * @since 9.7.0
 *
 */
public class DefaultIdmIdentityRoleThinService 
		extends AbstractReadDtoService<IdmIdentityRoleThinDto, IdmIdentityRoleThin, IdmIdentityRoleFilter>
		implements IdmIdentityRoleThinService {

	@Autowired
	public DefaultIdmIdentityRoleThinService(IdmIdentityRoleThinRepository repository) {
		super(repository);
	}
	
	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(CoreGroupPermission.IDENTITYROLE, IdmIdentityRole.class);
	}
	
	@Override
	protected Specification<IdmIdentityRoleThin> toCriteria(
			IdmIdentityRoleFilter filter, 
			boolean applyFetchMode,
			BasePermission... permission) {
		throw new UnsupportedOperationException("Find methods using criteria are not supported for thin entity. Use get method only.");
	}
	
	@Override
	protected IdmIdentityRoleThin checkAccess(IdmIdentityRoleThin entity, BasePermission... permission) {
		if (!ObjectUtils.isEmpty(PermissionUtils.trimNull(permission))) {
			throw new UnsupportedOperationException("Check acces on thin entity is not supported.");
		}
		return entity;
	}
}
