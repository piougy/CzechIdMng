package eu.bcvsolutions.idm.core.eav.api.service;

import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.IdmDelegationDefinitionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmDelegationDto;
import eu.bcvsolutions.idm.core.api.utils.EntityUtils;
import java.util.List;
import java.util.UUID;
import org.springframework.core.Ordered;

/**
 * Delegation type
 *
 * @author Vít Švanda
 * @since 10.4.0
 */
public interface DelegationType extends Ordered{

	/**
	 * Bean name / unique identifier (spring bean name).
	 *
	 * @return
	 */
	String getId();

	/**
	 * Class of owner type for this delegation. Owner is a object for wich is
	 * delegation made (task, vs-system, ...).
	 *
	 * @return
	 */
	Class<? extends BaseDto> getOwnerType();

	/**
	 * Returns module of that delegation.
	 *
	 * @return
	 */
	default String getModule() {
		return EntityUtils.getModule(this.getClass());
	}

	/**
	 * If return true, then this delegation using identity-contract and will be
	 * showing on the FE.
	 *
	 * @return
	 */
	public boolean isSupportsDelegatorContract();

	/**
	 * Order of delegation (in select-box).
	 *
	 * @return
	 */
	@Override
	public int getOrder();

	/**
	 * Indicates if that delegation should be evaluate automatically or is executed
	 * by own way. In some cases we would to evaluate delegation with own way (for
	 * example directly in the WF process) without automatically evaluating.
	 *
	 * @return true - if we don't want evaluate a delegation automatically. false is
	 *         default.
	 */
	public boolean isCustomeDelegation();

	/**
	 * Find delegation. Method can contains custom logic for that delegation type.
	 * 
	 * @param delegatorId
	 * @param delegatorContractId
	 * @param owner
	 * @return
	 */
	public List<IdmDelegationDefinitionDto> findDelegation(UUID delegatorId, UUID delegatorContractId, BaseDto owner);

	/**
	 * Creates delegatio for given task (owner). Method can contains custom logic for that delegation type.
	 * @param owner
	 * @param definition
	 * @return
	 */
	public IdmDelegationDto delegate(BaseDto owner, IdmDelegationDefinitionDto definition);
	
}
