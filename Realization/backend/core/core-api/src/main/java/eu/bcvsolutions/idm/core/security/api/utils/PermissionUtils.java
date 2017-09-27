package eu.bcvsolutions.idm.core.security.api.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import org.springframework.util.ObjectUtils;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;

/**
 * Utility method for manipulating <tt>BasePermission</tt> collections etc.
 * 
 * @see BasePermission
 * @author Radek Tomiška
 */
public abstract class PermissionUtils {
	
	/**
	 * Returns permission list without {@code null} permissions. 
	 * 
	 * @param permissions
	 * @return
	 */
	public static BasePermission[] trimNull(BasePermission... permissions) {
		if(ObjectUtils.isEmpty(permissions)) {
			return null;
		}
		return Lists.newArrayList(permissions)
			.stream()
			.filter(permission -> {
				return permission != null;
			})
			.toArray(BasePermission[]::new);
	}
	
	/**
	 * Returns true, when permissions have all given permission, or {@link IdmBasePermission#ADMIN} permission
	 * 
	 * @param permissions
	 * @param permission permissions to evaluate (AND)
	 * @return
	 */
	public static boolean hasPermission(Collection<String> permissions, BasePermission... permission) {
		return permissions.contains(IdmBasePermission.ADMIN.getName()) // admin - wildcard
				|| permissions.containsAll(
						Arrays
							.stream(permission)
							.map(Object::toString)
							.collect(Collectors.toList()));
	}
	
	/**
	 * Returns true, when permissions have at least one of given permission, or {@link IdmBasePermission#ADMIN} permission
	 * 
	 * @param permissions
	 * @param permission permissions to evaluate (OR)
	 * @return
	 */
	public static boolean hasAnyPermission(Collection<String> permissions, BasePermission... permission) {
		return permissions.contains(IdmBasePermission.ADMIN.getName()) // admin - wildcard
				|| Arrays
					.stream(permission)
					.map(Object::toString)
					.anyMatch(singlePermission -> {
						return permissions.contains(singlePermission);
					});
	}
	
	/**
	 * Converts set of {@link BasePermission} to set of permission names.
	 * 
	 * @param permissions
	 * @return
	 */
	public static Collection<String> toString(Collection<BasePermission> permissions) {
		if (permissions == null) {
			return Collections.<String>emptySet();
		}
		return permissions
				.stream()
				.map(Object::toString)
				.collect(Collectors.toSet());
	}
}
