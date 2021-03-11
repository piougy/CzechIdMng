package eu.bcvsolutions.idm.core.bulk.action.impl.contract;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.CoreModuleDescriptor;
import eu.bcvsolutions.idm.core.api.bulk.action.AbstractBulkAction;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.exception.ForbiddenEntityException;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmTreeNodeService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.eav.api.domain.BaseFaceType;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmProcessedTaskItemDto;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;

/**
 * Bulk operation changing contract validity and tree node (organization structure position) 
 * for all contracts of the identity.
 *
 * @author Ondrej Husnik
 *
 */
@Enabled(CoreModuleDescriptor.MODULE_ID)
@Component(IdentityChangeContractTreeNodeAndValidityBulkAction.NAME)
@Description("Change tree node and contract validity in bulk action.")
public class IdentityChangeContractTreeNodeAndValidityBulkAction extends AbstractBulkAction<IdmIdentityDto, IdmIdentityFilter> {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IdentityChangeContractTreeNodeAndValidityBulkAction.class);

	public static final String NAME = "identity-change-contract-tree-node-and-validity-bulk-action";
	public static final String PARAMETER_TREE_NODE_NAME = "tree-node";
	public static final String PARAMETER_VALID_FROM_NAME = "valid-from";
	public static final String PARAMETER_VALID_TILL_NAME = "valid-till";
		
	
	@Autowired
	private IdmIdentityService identityService;
	@Autowired
	private IdmTreeNodeService treeNodeService;
	@Autowired
	private IdmIdentityContractService contractService;
		
	
	@Override
	public List<IdmFormAttributeDto> getFormAttributes() {
		List<IdmFormAttributeDto> formAttributes = super.getFormAttributes();
		formAttributes.add(getTreeNodeFormAttr(PARAMETER_TREE_NODE_NAME));
		formAttributes.add(getDatePickFormAttr(PARAMETER_VALID_FROM_NAME));
		formAttributes.add(getDatePickFormAttr(PARAMETER_VALID_TILL_NAME));
		return formAttributes;
	}
	
	@Override
	public List<String> getAuthorities() {
		List<String> permissions = super.getAuthorities();
		permissions.add(CoreGroupPermission.IDENTITYCONTRACT_UPDATE);
		permissions.add(CoreGroupPermission.TREENODE_AUTOCOMPLETE);
		return permissions;
	}

	@Override
	public String getName() {
		return IdentityChangeContractTreeNodeAndValidityBulkAction.NAME;
	}
	
	@Override
	public int getOrder() {
		return DEFAULT_ORDER + 510;
	}
	
	@Override
	public ReadWriteDtoService<IdmIdentityDto, IdmIdentityFilter> getService() {
		return identityService;
	}

	@Override
	protected OperationResult processDto(IdmIdentityDto identity) {
		List<IdmIdentityContractDto> contracts = contractService.findAllByIdentity(identity.getId());
		
		IdmTreeNodeDto treeNode = getSelectedTreeNode();
		LocalDate tillDate = getSelectedDate(PARAMETER_VALID_TILL_NAME);
		LocalDate fromDate = getSelectedDate(PARAMETER_VALID_FROM_NAME);
			
		
		if (treeNode == null && tillDate == null && fromDate == null) {
			return new OperationResult.Builder(OperationState.EXECUTED).build();
		}
		
		for (IdmIdentityContractDto contract : contracts) {
			if (treeNode != null) {
				contract.setWorkPosition(treeNode.getId());
			}
			if (fromDate != null) {
				contract.setValidFrom(fromDate);
			}
			if (tillDate != null) {
				contract.setValidTill(tillDate);
			}
			
			try {
				contractService.save(contract, IdmBasePermission.UPDATE);
				logItemProcessed(contract, new OperationResult.Builder(OperationState.EXECUTED).build()); 
			} catch (ForbiddenEntityException ex) {
				LOG.warn("Insufficient permissions for changing contract [{}]",contract.getId(), ex);
				logItemProcessed(contract,
						new OperationResult.Builder(OperationState.NOT_EXECUTED).setModel(
								new DefaultResultModel(CoreResultCode.BULK_ACTION_NOT_AUTHORIZED_MODIFY_CONTRACT,
										ImmutableMap.of("contractId", contract.getId())))
								.build());
			}
		}
		return new OperationResult.Builder(OperationState.EXECUTED).build();
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
	
	private IdmTreeNodeDto getSelectedTreeNode() {
		UUID treeNodeId = getParameterConverter().toUuid(getProperties(), PARAMETER_TREE_NODE_NAME);
		if (treeNodeId == null) {
			return null;
		}
		return treeNodeService.get(treeNodeId); 
	}
	
	private LocalDate getSelectedDate(String code) {
		return getParameterConverter().toLocalDate(getProperties(), code);
	}
	
	private IdmFormAttributeDto getTreeNodeFormAttr(String code) {
		IdmFormAttributeDto treeNode = new IdmFormAttributeDto(code, code, PersistentType.UUID);
		treeNode.setFaceType(BaseFaceType.TREE_NODE_SELECT);
		return treeNode;
	}
	
	private IdmFormAttributeDto getDatePickFormAttr(String code) {
		return new IdmFormAttributeDto(code, code, PersistentType.DATE);
	}
}
