package eu.bcvsolutions.idm.vs;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.dto.SysRoleSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.vs.dto.VsSystemDto;
import eu.bcvsolutions.idm.vs.service.api.VsSystemService;

/**
 * Reuses core and acc TestHelper and adds virtual system spec. methods
 * 
 * @author Svanda
 */
@Primary
@Component("vsTestHelper")
public class DefaultTestHelper extends eu.bcvsolutions.idm.test.api.DefaultTestHelper implements TestHelper {

	@Autowired
	private VsSystemService systemService;
	@Autowired
	private SysSystemMappingService systemMappingService;
	@Autowired
	private SysRoleSystemService roleSystemService;

	@Override
	public SysSystemDto createVirtualSystem(String name) {
		VsSystemDto dto = new VsSystemDto();
		dto.setName(name);
		return this.createVirtualSystem(dto);
	}

	@Override
	public SysSystemDto createVirtualSystem(VsSystemDto config) {
		return this.systemService.create(config);
	}

	@Override
	public SysRoleSystemDto createRoleSystem(IdmRoleDto role, SysSystemDto system) {
		SysRoleSystemDto roleSystem = new SysRoleSystemDto();
		roleSystem.setRole(role.getId());
		roleSystem.setSystem(system.getId());
		// default mapping
		List<SysSystemMappingDto> mappings = systemMappingService.findBySystem(system, SystemOperationType.PROVISIONING,
				SystemEntityType.IDENTITY);
		//
		roleSystem.setSystemMapping(mappings.get(0).getId());
		return roleSystemService.save(roleSystem);
	}

}
