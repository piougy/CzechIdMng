package eu.bcvsolutions.idm.core.security.api.domain;

import java.util.List;

/**
 * Group permission could contain {@link BasePermission}. 
 * 
 * @author Radek Tomiška
 */
public interface GroupPermission extends BasePermission {

	List<BasePermission> getPermissions();
}
