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
	
	String SKIP_RECALCULATE_CONTRACT_SLICE = "skip-recalculate-contract-slice";
	String FORCE_RECALCULATE_CURRENT_USING_SLICE = "force-recalculate-current-using-slice";
	String SKIP_CONTRACT_SLICE_SAVE = "skip-contract-slice-save"; // skip all slice recalculation (ContractSliceSaveRecalculateProcessor)
	String SET_DIRTY_STATE_CONTRACT_SLICE = "set-dirty-state-contract-slice"; // during recalculation set dirty state, the dirty state must fix ClearDirtyStateForContractSliceTaskExecutor 

}
