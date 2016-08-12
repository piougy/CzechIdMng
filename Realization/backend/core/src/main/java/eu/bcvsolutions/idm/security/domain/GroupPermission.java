package eu.bcvsolutions.idm.security.domain;

import java.util.List;

public interface GroupPermission extends BasePermission {

	List<BasePermission> getPermissions();
}
