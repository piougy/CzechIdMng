package eu.bcvsolutions.idm.acc;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.core.api.domain.AbstractModuleDescriptor;
import eu.bcvsolutions.idm.security.api.domain.GroupPermission;

/**
 * Account management module descriptor
 * 
 * TODO: module dependencies
 * 
 * @author Radek Tomiška
 *
 */
@Component
public class AccModuleDescriptor extends AbstractModuleDescriptor {

	public static final String MODULE_ID = "acc";
	
	@Override
	public String getId() {
		return MODULE_ID;
	}
	
	@Override
	public List<GroupPermission> getPermissions() {
		return Arrays.asList(AccGroupPermission.values());
	}
}
