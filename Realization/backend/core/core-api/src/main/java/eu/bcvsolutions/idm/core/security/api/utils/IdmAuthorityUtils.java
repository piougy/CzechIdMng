package eu.bcvsolutions.idm.core.security.api.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.security.api.domain.DefaultGrantedAuthority;
import eu.bcvsolutions.idm.core.security.api.domain.GroupPermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmGroupPermission;

/**
 * Utility method for manipulating <tt>GrantedAuthority</tt> collections etc.
 * <p>
 * Mainly intended for internal use.
 * 
 * @author Radek Tomiška
 *
 */
public abstract class IdmAuthorityUtils {
	
	/**
	 * Returns super administrator authority.
	 * 
	 * @return
	 */
	public static GrantedAuthority getAdminAuthority() {
		return new DefaultGrantedAuthority(IdmGroupPermission.APP, IdmBasePermission.ADMIN);
	}
	
	
	/**
	 * Returns all authorities from given groupPermissions
	 * 
	 * @param groupPermissions
	 * @return
	 */
	public static List<GrantedAuthority> toAuthorities(List<GroupPermission> groupPermissions) {
		Set<GrantedAuthority> authorities = new HashSet<>();
		groupPermissions.forEach(groupPermission -> {
			authorities.addAll(toAuthorities(groupPermission));				
		});
		return new ArrayList<>(authorities);
	}
	
	/**
	 * Returns all authorities from given groupPermissions
	 * 
	 * @param groupPermissions
	 * @return
	 */
	public static List<GrantedAuthority> toAuthorities(GroupPermission... groupPermissions) {
		Assert.notNull(groupPermissions);
		//
		Set<GrantedAuthority> authorities = new HashSet<>();
		for (GroupPermission groupPermission : groupPermissions) {
			groupPermission.getPermissions().forEach(basePermission -> {
				authorities.add(new DefaultGrantedAuthority(groupPermission, basePermission));
			});					
		}
		return new ArrayList<>(authorities);
	}
	
	/**
	 * Transforms given authorities (string representation) to {@link GrantedAuthority}'s list.
	 * 
	 * @param authorities
	 * @return
	 */
	public static List<GrantedAuthority> toAuthorities(Collection<String> authorities) {
		if (authorities == null || authorities.isEmpty()) {
			return AuthorityUtils.NO_AUTHORITIES;
		}
		//
		return AuthorityUtils.createAuthorityList(authorities.toArray(new String[authorities.size()]));
	}
	
}
