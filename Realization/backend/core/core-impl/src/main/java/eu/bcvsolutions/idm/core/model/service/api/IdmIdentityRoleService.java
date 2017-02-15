package eu.bcvsolutions.idm.core.model.service.api;

import java.util.List;

import eu.bcvsolutions.idm.core.api.service.ReadWriteEntityService;
import eu.bcvsolutions.idm.core.model.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.model.dto.filter.IdentityRoleFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;

/**
 * Operations with identity roles
 * 
 * @author svanda
 * @author Radek Tomiška
 *
 */
public interface IdmIdentityRoleService extends ReadWriteEntityService<IdmIdentityRole, IdentityRoleFilter> {
	
	/**
	 * Returns all identity's roles
	 * 
	 * @param identity
	 * @return
	 */
	List<IdmIdentityRole> getRoles(IdmIdentity identity);
	
	/**
	 * Returns all roles related to given {@link IdmIdentityContract}
	 * 
	 * @param identityContract
	 * @return
	 */
	List<IdmIdentityRole> getRoles(IdmIdentityContract identityContract);
	
	/**
	 * Returns identity roles by their ids (uuid in string).
	 * 
	 * Used from wf.
	 * 
	 * @param ids
	 * @return
	 */
	List<IdmIdentityRole> getByIds(List<String> ids);

	/**
	 * Used from wf.
	 * 
	 * @param id
	 * @param dto
	 * @return
	 */
	IdmIdentityRole updateByDto(String id, IdmIdentityRoleDto dto);

	/**
	 * Used from wf.
	 * 
	 * @param dto
	 * @return
	 */
	IdmIdentityRole addByDto(IdmIdentityRoleDto dto);
}
