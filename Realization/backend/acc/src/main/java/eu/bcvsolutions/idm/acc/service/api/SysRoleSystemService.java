package eu.bcvsolutions.idm.acc.service.api;

import java.util.List;
import java.util.UUID;

import eu.bcvsolutions.idm.acc.dto.SysRoleSystemDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysRoleSystemFilter;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.script.ScriptEnabled;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;

/**
 * Role could assign identity account on target system.
 * 
 * @author Radek Tomiška
 *
 */
public interface SysRoleSystemService extends ReadWriteDtoService<SysRoleSystemDto, SysRoleSystemFilter>, ScriptEnabled {

	/**
	 * Returns concepts with role mapped given system
	 * 
	 * @param concepts
	 * @param systemId
	 * @return
	 */
	List<IdmConceptRoleRequestDto> getConceptsForSystem(List<IdmConceptRoleRequestDto> concepts, UUID systemId);

}
