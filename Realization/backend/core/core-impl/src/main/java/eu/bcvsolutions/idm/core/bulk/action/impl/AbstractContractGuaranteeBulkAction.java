package eu.bcvsolutions.idm.core.bulk.action.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.ImmutableMap;

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
import eu.bcvsolutions.idm.core.api.service.IdmContractGuaranteeService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.utils.EntityUtils;
import eu.bcvsolutions.idm.core.eav.api.domain.BaseFaceType;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;

/**
 * Bulk operation changing contract guarantee selected identity
 *
 * @author Ondrej Husnik
 *
 */
public abstract class AbstractContractGuaranteeBulkAction extends AbstractBulkAction<IdmIdentityDto, IdmIdentityFilter> {
	
	public static final String NEW_GUARANTEE = "new-guarantee";
	public static final String OLD_GUARANTEE = "old-guarantee";

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractContractGuaranteeBulkAction.class);
	
	@Autowired
	protected IdmIdentityService identityService;
	@Autowired
	protected IdmIdentityContractService identityContractService;
	@Autowired
	protected IdmContractGuaranteeService contractGuaranteeService;

	@Override
	public List<IdmFormAttributeDto> getFormAttributes() {
		List<IdmFormAttributeDto> formAttributes = super.getFormAttributes();
		
		return formAttributes;
	}

	
	@Override
	public List<String> getAuthorities() {
		List<String> permissions = super.getAuthorities();
		permissions.add(CoreGroupPermission.IDENTITY_READ);
		permissions.add(CoreGroupPermission.IDENTITY_COUNT);
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
		identitiy.setFaceType(includeDisabled? BaseFaceType.IDENTITY_ALLOW_DISABLED_SELECT 
				:BaseFaceType.IDENTITY_SELECT);
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
	protected List<IdmIdentityContractDto> getContractsForIdentity(UUID identity, BasePermission... permissions) {
		IdmIdentityContractFilter contractFilt = new IdmIdentityContractFilter();
		contractFilt.setIdentity(identity);
		return identityContractService.find(contractFilt, null, permissions).getContent();
	}
	
	/**
	 * Get all guarantees for identity
	 * 
	 * @param identityUUID
	 * @param permissions
	 * @return
	 */
	protected List<IdmContractGuaranteeDto> findGuaranteesForIdentity(UUID identityUUID, BasePermission... permissions) {
		IdmContractGuaranteeFilter currentGuarFilt = new IdmContractGuaranteeFilter();
		currentGuarFilt.setIdentityId(identityUUID);
		return contractGuaranteeService.find(currentGuarFilt, null, permissions).getContent();
	}
	
	/**
	 * Creates the map of all guarantees for every contract of the identityUUID
	 * 
	 * @param identityUUID
	 * @return Key ~ contract UUID, Value ~ List of guarantee bindings
	 */
	protected Map<UUID, List<IdmContractGuaranteeDto>> getIdentityGuaranteesOrderedByContract(UUID identityUUID) {
		Map<UUID, List<IdmContractGuaranteeDto>> contractGuaranteeMap = new HashMap<UUID, List<IdmContractGuaranteeDto>>();
		
		// find and set all existing contracts for identity which can be updated
		List<IdmIdentityContractDto> contracts = getContractsForIdentity(identityUUID);
		Set<UUID> contractsUUID = contracts.stream().map(AbstractDto::getId).collect(Collectors.toSet());
		contractsUUID.forEach(uuid -> contractGuaranteeMap.put(uuid, new ArrayList<IdmContractGuaranteeDto>()));
		
		// assign guarantees for particular contract
		List<IdmContractGuaranteeDto> currentGuarDtos = findGuaranteesForIdentity(identityUUID);
		currentGuarDtos.stream().forEach(item -> {
			List<IdmContractGuaranteeDto> dtos = contractGuaranteeMap.get(item.getIdentityContract());
			if(dtos != null) { // fill only allowed contracts
				dtos.add(item);
			}
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
		IdmContractGuaranteeDto contractGuarantee = new IdmContractGuaranteeDto();
		contractGuarantee.setGuarantee(guarantee);
		contractGuarantee.setIdentityContract(contract);
		return contractGuaranteeService.save(contractGuarantee, permissions);
	}
	
	/**
	 * Get selected guarantees DTO on frontend 
	 * 
	 * @param code
	 * @return
	 */
	protected Set<IdmIdentityDto> getSelectedGuaranteeDtos(String code) {
		Object guaranteeObj = this.getProperties().get(code);
		if (guaranteeObj == null || !(guaranteeObj instanceof Collection)	) {
			return Collections.emptySet();
		}
		Set<IdmIdentityDto> guarantees = new HashSet<IdmIdentityDto>();
		((Collection<?>) guaranteeObj).forEach(guarantee -> {
			UUID uuid = EntityUtils.toUuid(guarantee);
			IdmIdentityDto identDto = identityService.get(uuid, IdmBasePermission.READ);
			if (identDto == null) {
				LOG.warn("Identity with id [{}] not found. The identDto will be skipped.", uuid);
				return;
			}
			guarantees.add(identDto);
		});
		return guarantees;
	}
	
	/**
	 * Get selected guarantees UUID on frontend 
	 * 
	 * @param code
	 * @return
	 */
	protected Set<UUID> getSelectedGuaranteeUuids(String code) {
		Object guaranteeObj = this.getProperties().get(code);
		if (guaranteeObj == null || !(guaranteeObj instanceof Collection)) {
			return Collections.emptySet();
		}
		Set<UUID> guarantees = ((Collection<?>) guaranteeObj)
				.stream()
				.map(item -> EntityUtils.toUuid(item))
				.filter(item -> { // remove nonexistent
					IdmIdentityFilter filter = new IdmIdentityFilter();
					filter.setId(item);
					boolean result = identityService.count(filter, IdmBasePermission.COUNT)==0;
					if (result) {
						LOG.warn("Identity with id [{}] not found. The UUID will be skipped.", item);
					}
					return !result;})
				.collect(Collectors.toSet());
		return guarantees;
	}
	
	/**
	 * Get single value UUID from FE
	 * @param code
	 * @return
	 */
	protected UUID getSelectedGuaranteeSingleUuid(String code) {
		return getParameterConverter().toUuid(getProperties(), code);
	}
	
	/**
	 * Log specified dto with error specific for missing permission 
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
						.setModel(new DefaultResultModel(CoreResultCode.BULK_ACTION_NOT_ATHORIZED_CONTRACT_GUARANTEE,
								ImmutableMap.of("permission", permission.toString(), "guaranteeId",
										String.valueOf(guaranteeId), "contractId", String.valueOf(contractId))))
						.build());
	}
}
