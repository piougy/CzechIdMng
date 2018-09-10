package eu.bcvsolutions.idm.core.api.service;

import java.util.List;
import java.util.UUID;

import eu.bcvsolutions.idm.core.api.domain.RecursionType;
import eu.bcvsolutions.idm.core.api.dto.IdmContractPositionDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmContractPositionFilter;
import eu.bcvsolutions.idm.core.api.script.ScriptEnabled;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Identity's contract other position
 * 
 * @author Radek Tomiška
 * @since 9.1.0
 */
public interface IdmContractPositionService extends
		EventableDtoService<IdmContractPositionDto, IdmContractPositionFilter>,
		AuthorizableService<IdmContractPositionDto>,
		ScriptEnabled {

	/**
	 * Returns all contract positions, where fits work position with given work position by recursionType.
	 * 
	 * @param workPositionId
	 * @param recursion
	 * @return
	 */
	List<IdmContractPositionDto> findAllByWorkPosition(UUID workPositionId, RecursionType recursion);
}
