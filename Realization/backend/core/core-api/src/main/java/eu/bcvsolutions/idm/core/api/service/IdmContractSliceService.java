package eu.bcvsolutions.idm.core.api.service;

import eu.bcvsolutions.idm.core.api.dto.IdmContractSliceDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmContractSliceFilter;
import eu.bcvsolutions.idm.core.api.script.ScriptEnabled;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Service for operations with contract time slices
 * 
 * @author svandav
 *
 */
public interface IdmContractSliceService extends
		EventableDtoService<IdmContractSliceDto, IdmContractSliceFilter>,
		AuthorizableService<IdmContractSliceDto>,
		ScriptEnabled {
	

	public final String SKIP_RECALCULATE_CONTRACT_SLICE = "skip-recalculate-contract-slice";
	public final String FORCE_RECALCULATE_CURRENT_USING_SLICE = "force-recalculate-current-using-slice";

}
