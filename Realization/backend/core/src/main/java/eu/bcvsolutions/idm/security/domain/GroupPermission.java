package eu.bcvsolutions.idm.security.domain;

import java.util.List;

/**
 * Group permision could contain {@link BasePermission}. 
 * 
 * @author Radek Tomiška
 */
public interface GroupPermission extends BasePermission {

	List<BasePermission> getPermissions();
}
