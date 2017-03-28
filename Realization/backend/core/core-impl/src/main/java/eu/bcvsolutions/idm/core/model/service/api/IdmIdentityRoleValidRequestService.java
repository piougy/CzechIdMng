package eu.bcvsolutions.idm.core.model.service.api;

import java.util.List;

import org.joda.time.DateTime;

import eu.bcvsolutions.idm.core.api.dto.filter.EmptyFilter;
import eu.bcvsolutions.idm.core.api.service.ReadWriteEntityService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRoleValidRequest;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;

/**
 * Service for create and read identity role valid requests.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public interface IdmIdentityRoleValidRequestService extends ReadWriteEntityService<IdmIdentityRoleValidRequest, EmptyFilter> {
	
	IdmIdentityRoleValidRequest createByIdentityRole(IdmIdentityRole identityRole);
	
	/**
	 * Method find all {@link IdmIdentityRoleValidRequest} that can be process from now = role is valid form today.
	 * @return
	 */
	List<IdmIdentityRoleValidRequest> findAllValid();
	
	/**
	 * Method find all {@link IdmIdentityRoleValidRequest} that can be process from {@value from} given in parameter.
	 * @param from
	 * @return
	 */
	List<IdmIdentityRoleValidRequest> findAllValidFrom(DateTime from);
	
	/**
	 * Find all {@link IdmIdentityRoleValidRequest} for role
	 * @param role
	 * @return
	 */
	List<IdmIdentityRoleValidRequest> findAllValidRequestForRole(IdmRole role);
	
	/**
	 * Find all {@link IdmIdentityRoleValidRequest} for identity
	 * @param identity
	 * @return
	 */
	List<IdmIdentityRoleValidRequest> findAllValidRequestForIdentity(IdmIdentity identity);
	
	/**
	 * Find all {@link IdmIdentityRoleValidRequest} for identityRole
	 * @param identityRole
	 * @return
	 */
	List<IdmIdentityRoleValidRequest> findAllValidRequestForIdentityRole(IdmIdentityRole identityRole);
	
	/**
	 * Find all {@link IdmIdentityRoleValidRequest} for identityContract
	 * @param identityContract
	 * @return
	 */
	List<IdmIdentityRoleValidRequest> findAllValidRequestForIdentityContract(IdmIdentityContract identityContract);
	
	/**
	 * Remove all entities {@link IdmIdentityRoleValidRequest} check for null and empty list.
	 * @param entities
	 */
	void deleteAll(List<IdmIdentityRoleValidRequest> entities);
}
