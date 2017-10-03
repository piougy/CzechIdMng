package eu.bcvsolutions.idm.vs;

import eu.bcvsolutions.idm.acc.dto.SysRoleSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.vs.dto.VsSystemDto;

/**
 * Reuses core and acc TestHelper and adds vs spec. methods
 * 
 * @author Svanda
 *
 */
public interface TestHelper extends eu.bcvsolutions.idm.test.api.TestHelper {
	

	/**
	 * Create virtual system by default configuration. System will included schema, mapped attributes.
	 * @param config
	 * @return
	 */
	SysSystemDto createVirtualSystem(VsSystemDto config);

	/**
	 * Create virtual system by default configuration. System will included schema, mapped attributes.
	 * @param name
	 * @return
	 */
	SysSystemDto createVirtualSystem(String name);

	/**
	 * Assing system to given role with default mapping (provisioning, identity)
	 * 
	 * @see #getDefaultMapping(SysSystem)
	 * @param role
	 * @param system
	 * @return
	 */
	SysRoleSystemDto createRoleSystem(IdmRoleDto role, SysSystemDto system);
	
}
