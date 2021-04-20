package eu.bcvsolutions.idm.core.bulk.action.impl.contract;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

import eu.bcvsolutions.idm.core.api.bulk.action.AbstractBulkAction;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.IdmContractGuaranteeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmContractGuaranteeFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityContractFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.IdmContractGuaranteeService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.eav.api.domain.BaseFaceType;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmProcessedTaskItemDto;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;

/**
 * Bulk operation changing contract guarantee selected identity.
 *
 * @author Ondrej Husnik
 * @author Radek Tomiška
 *
 */
public abstract class AbstractContractGuaranteeBulkAction extends AbstractBulkAction<IdmIdentityDto, IdmIdentityFilter> {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractContractGuaranteeBulkAction.class);
	
	public static final String PROPERTY_NEW_GUARANTEE = "new-guarantee";
	public static final String PROPERTY_OLD_GUARANTEE = "old-guarantee";
	
	@Autowired
	protected IdmIdentityService identityService;
	@Autowired
	protected IdmIdentityContractService identityContractService;
	@Autowired
	protected IdmContractGuaranteeService contractGuaranteeService;
	
	@Override
	public List<String> getAuthorities() {
		List<String> permissions = super.getAuthorities();
		permissions.add(CoreGroupPermission.IDENTITY_READ);
		permissions.add(CoreGroupPermission.IDENTITY_AUTOCOMPLETE);
		return permissions;
	}

	@Override
	public ReadWriteDtoService<IdmIdentityDto, IdmIdentityFilter> getService() {
		return identityService;
	}
	
	/**
	 * Creates FE configuration form attribute 
	 * @param code
	 * @param includeDisabled
	 * @return
	 */
	protected IdmFormAttributeDto getGuaranteeAttribute(String code, boolean includeDisabled, boolean isMultiple) {
		IdmFormAttributeDto identitiy = new IdmFormAttributeDto(
				code, 
				code, 
				PersistentType.UUID); 
		identitiy.setFaceType(
				includeDisabled
				? BaseFaceType.IDENTITY_ALLOW_DISABLED_SELECT 
				: BaseFaceType.IDENTITY_SELECT);
		identitiy.setMultiple(isMultiple);
		identitiy.setRequired(true);
		return identitiy;
	}
	
	 /**
	  * Gets all contracts of identity
	  * 
	  * @param identity
	  * @param permissions
	  * @return
	  */
	protected List<IdmIdentityContractDto> findContracts(UUID identity, BasePermission... permissions) {
		IdmIdentityContractFilter filter = new IdmIdentityContractFilter();
		filter.setIdentity(identity);
		return identityContractService.find(filter, null, permissions).getContent();
	}
	
	/**
	 * Get all guarantees for identity
	 * 
	 * @param identity
	 * @param permissions
	 * @return
	 */
	protected List<IdmContractGuaranteeDto> findGuarantees(UUID identity, BasePermission... permissions) {
		IdmContractGuaranteeFilter filter = new IdmContractGuaranteeFilter();
		filter.setIdentity(identity);
		return contractGuaranteeService.find(filter, null, permissions).getContent();
	}
	
	/**
	 * Creates the map of all guarantees for every contract of the identityUUID
	 * 
	 * @param identityUUID
	 * @return Key ~ contract UUID, Value ~ List of guarantee bindings
	 */
	protected Map<UUID, List<IdmContractGuaranteeDto>> getIdentityGuaranteesOrderedByContract(UUID identityId) {
		Map<UUID, List<IdmContractGuaranteeDto>> contractGuaranteeMap = new HashMap<UUID, List<IdmContractGuaranteeDto>>();
		
		// find and set all existing contracts
		List<IdmIdentityContractDto> contracts = findContracts(identityId);
		Set<UUID> contractIds = contracts.stream().map(AbstractDto::getId).collect(Collectors.toSet());
		contractIds.forEach(contractId -> contractGuaranteeMap.put(contractId, new ArrayList<>()));
		
		// assign guarantees for particular contract
		findGuarantees(identityId).stream().forEach(item -> {
			contractGuaranteeMap.get(item.getIdentityContract()).add(item);
		});
		return contractGuaranteeMap;
	}
	
	
	/**
	 * Assign the guarantee for given contract 
	 * 
	 * @param guarantee
	 * @param contract
	 * {@link @ForbiddenEntityException} if insufficient permissions
	 */
	protected IdmContractGuaranteeDto createContractGuarantee(UUID guarantee, UUID contract, BasePermission... permissions) {
		IdmContractGuaranteeDto contractGuarantee = new IdmContractGuaranteeDto(contract, guarantee);
		//
		return contractGuaranteeService.save(contractGuarantee, permissions);
	}
	
	/**
	 * Get selected guarantees UUID on frontend 
	 * 
	 * @param code
	 * @return
	 */
	protected Set<UUID> getSelectedGuaranteeUuids(String propertyName) {
		return Sets.newHashSet(getParameterConverter().toUuids(getProperties(), propertyName));
	}
	
	/**
	 * Get single value UUID from FE
	 * @param code
	 * @return
	 */
	protected UUID getSelectedGuaranteeUuid(String propertyName) {
		return getParameterConverter().toUuid(getProperties(), propertyName);
	}
	
	/**
	 * Log specified dto with error specific for missing permission.
	 * 
	 * @param dto
	 * @param guaranteeId
	 * @param contractId
	 * @param permission
	 * @param e
	 */
	protected void logContractGuaranteePermissionError(AbstractDto dto, UUID guaranteeId, UUID contractId,
			BasePermission permission, Exception e) {
		logItemProcessed(dto,
				new OperationResult.Builder(OperationState.NOT_EXECUTED)
						.setModel(new DefaultResultModel(CoreResultCode.BULK_ACTION_NOT_AUTHORIZED_CONTRACT_GUARANTEE,
								ImmutableMap.of(
										"permission", permission.toString(), 
										"guaranteeId", String.valueOf(guaranteeId), 
										"contractId", String.valueOf(contractId))))
						.build());
	}
	
	protected void logResultCodeException(AbstractDto dto, ResultCodeException e) {
		logItemProcessed(dto,
				new OperationResult.Builder(OperationState.NOT_EXECUTED)
						.setException(e)
						.build());
	}
	
	@Override
	public <DTO extends AbstractDto> IdmProcessedTaskItemDto logItemProcessed(DTO item, OperationResult opResult) {
		if (item instanceof IdmIdentityDto) {
			// we don't want to log identities, which are iterated only
			LOG.debug("Identity [{}] was processed by bulk action.", item.getId());
			return null;
		}
		return super.logItemProcessed(item, opResult);
	}
}
