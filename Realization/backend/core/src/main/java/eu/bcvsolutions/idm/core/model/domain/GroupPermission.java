package eu.bcvsolutions.idm.core.model.domain;

import java.util.List;

public interface GroupPermission extends BasePermission {

	List<BasePermission> getPermissions();
}
