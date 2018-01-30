package eu.bcvsolutions.idm.core.api.dto.filter;

import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.dto.AbstractIdmAutomaticRoleDto;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;

/**
 * Filter for automatic roles
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @since 7.7.0
 *
 */

public class IdmAutomaticRoleFilter extends DataFilter {

	private UUID roleId;
	private String name;

	public IdmAutomaticRoleFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public IdmAutomaticRoleFilter(Class<? extends BaseDto> dtoClass, MultiValueMap<String, Object> data) {
		super(dtoClass, data);
	}
	
	public IdmAutomaticRoleFilter(MultiValueMap<String, Object> data) {
		super(AbstractIdmAutomaticRoleDto.class, data);
	}

	public UUID getRoleId() {
		return roleId;
	}

	public void setRoleId(UUID roleId) {
		this.roleId = roleId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}