package eu.bcvsolutions.idm.core.security.api.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.util.ObjectUtils;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdentityBasePermission;
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
							.map(BasePermission::getName)
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
					.map(BasePermission::getName)
					.anyMatch(singlePermission -> {
						return permissions.contains(singlePermission);
					});
	}

	/**
	 * Method resolve given list of permission constants (eq. 'IDENTITY_READ').
	 * From the list will be parsed (by separator from BasePermission) permission. For IDENTITY_READ
	 * will be result READ.
	 * BasePermission enum will be get from these enums: {@link IdmBasePermission} or {@link IdentityBasePermission}.
	 * If constant isn't found method throws error.
	 *
	 * BEWARE: if given list contains constant from different group result set will be united. 
	 * For input list IDENTITY_READ, ROLE_UPDATE, ROLE_READ will be result: READ, UPDATE!
	 *
	 * @param permissions
	 * @return BasePermission list 
	 */
	public static Collection<BasePermission> toPermissions(Collection<String> authorities) {
		if (CollectionUtils.isEmpty(authorities)) {
			return Collections.<BasePermission>emptySet();
		}
		Set<String> resolvedPermissions = new HashSet<>();
		Set<BasePermission> result = new HashSet<>();
		for (String authority : authorities) {
			if (authority.contains(BasePermission.SEPARATOR)) {
				String[] split = authority.split(BasePermission.SEPARATOR);
				// permission is on last place
				authority = split[split.length - 1];
			}
			if (resolvedPermissions.contains(authority)) {
				continue;
			}
			final String rawPermission = authority;
			// Base permission may be child from IdmBasePermission
			BasePermission permission = EnumUtils.getEnum(IdmBasePermission.class, rawPermission);
			// but can be registered dynamically in custom module => new BasePermission is created
			if (permission == null) {
				permission = (BasePermission) () -> rawPermission;
			}
			//
			result.add(permission);
			resolvedPermissions.add(permission.getName());
		}
		return result;
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
				.map(BasePermission::getName)
				.collect(Collectors.toSet());
	}
}
