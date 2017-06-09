package eu.bcvsolutions.idm.core.security.api.dto;

import java.io.Serializable;

import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.security.api.domain.GroupPermission;

/**
 * Authorizable group to type mapping - assign permission group to type.
 * Unique - group could have only once type. TODO: this could be extended - remove equals method etc.
 * 
 * @author Radek Tomiška
 */
public class AuthorizableType implements BaseDto {
	
	private static final long serialVersionUID = -866021631923877118L;
	//
	private GroupPermission group;
	private Class<? extends Identifiable> type;
	
	public AuthorizableType() {
	}
	
	public AuthorizableType(GroupPermission group, Class<? extends Identifiable> type) {
		Assert.notNull(group);
		//
		this.group = group;
		this.type = type;
	}

	@Override
	public Serializable getId() {
		return group.getName();
	}

	@Override
	public void setId(Serializable id) {
		Assert.notNull(group);
		//
		group = (GroupPermission) id;
	}
	
	/**
	 * Secured domain type
	 * 
	 * @return
	 */
	public Class<? extends Identifiable> getType() {
		return type;
	}
	
	public void setType(Class<? extends Identifiable> type) {
		this.type = type;
	}
	
	/**
	 * Group module
	 * 
	 * @return
	 */
	public String getModule() {
		if (group == null) {
			return null;
		}
		return group.getModule();
	}
	
	/**
	 * Assigned group
	 * 
	 * @return
	 */
	public GroupPermission getGroup() {
		return group;
	}
	
	public void setGroup(GroupPermission group) {
		Assert.notNull(group);
		//
		this.group = group;
	}
	
	/**
	 * True - supports authorization evaluators. Added mainly for back compatibility issues.
	 * 
	 * @return
	 */
	public boolean isAuthorizable() {
		return type == null;
	}
	
	@Override
	public String toString() {
		return getClass().getCanonicalName() + "[ id=" + getId() + " ]";
	}

	@Override
	public int hashCode() {
		int hash = 0;
		hash += (getId() != null ? getId().hashCode() : 0);
		return hash;
	}

	@Override
	public boolean equals(Object object) {
		if (object == null || !object.getClass().equals(getClass())) {
			return false;
		}

		AuthorizableType other = (AuthorizableType) object;
		return !((this.getId() == null && other.getId() != null)
				|| (this.getId() != null && !this.getId().equals(other.getId()))
				|| (this.getId() == null && other.getId() == null && this != other));
	}
}
