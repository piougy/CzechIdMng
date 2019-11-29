package eu.bcvsolutions.idm.tool.domain;

import java.util.Arrays;
import java.util.List;

import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.GroupPermission;
import eu.bcvsolutions.idm.tool.ToolModuleDescriptor;

/**
 * Aggregate base permission. Name can't contain character '_' - its used for joining to authority name.
 *
 * @author BCV solutions s.r.o.
 *
 */
public enum ToolGroupPermission implements GroupPermission {

	/*
	 * Define your group permission there and example permission you can remove
	 */
	EXAMPLETOOL(
			IdmBasePermission.ADMIN);

	public static final String EXAMPLE_TOOL_ADMIN = "EXAMPLETOOL" + BasePermission.SEPARATOR + "ADMIN";

	private final List<BasePermission> permissions;

	private ToolGroupPermission(BasePermission... permissions) {
		this.permissions = Arrays.asList(permissions);
	}
	
	@Override
	public List<BasePermission> getPermissions() {
		return permissions;
	}

	@Override
	public String getName() {
		return name();
	}

	@Override
	public String getModule() {
		return ToolModuleDescriptor.MODULE_ID;
	}
}
