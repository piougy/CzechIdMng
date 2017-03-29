package eu.bcvsolutions.idm.core.security.api.dto;

import java.io.Serializable;

import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.security.api.domain.GroupPermission;

/**
 * Authorizable type info - assign permission group to type.
 * 
 * @author Radek Tomiška
 *
 */
public class AuthorizableType implements BaseDto {
	
	private static final long serialVersionUID = -866021631923877118L;
	//
	private Class<? extends Identifiable> type;
	private GroupPermission group;
	
	public AuthorizableType() {
	}
	
	public AuthorizableType(Class<? extends Identifiable> type, GroupPermission group) {
		this.type = type;
		this.group = group;
	}

	@Override
	public Serializable getId() {
		return type;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void setId(Serializable id) {
		type = (Class<? extends Identifiable>) id;
	}
	
	public Class<? extends Identifiable> getType() {
		return type;
	}
	
	public void setType(Class<? extends Identifiable> type) {
		this.type = type;
	}
	
	public GroupPermission getGroup() {
		return group;
	}
	
	public void setGroup(GroupPermission group) {
		this.group = group;
	}
}
