package eu.bcvsolutions.idm.core.scheduler.task.impl;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmEntityStateFilter;
import eu.bcvsolutions.idm.core.api.service.EntityStateManager;
import eu.bcvsolutions.idm.core.api.service.IdmRoleTreeNodeService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleTreeNode_;
import eu.bcvsolutions.idm.core.model.entity.IdmRole_;

/**
 * Recount all automatic role by tree structure for all contracts and all other positions.
 * 
 * Can be executed repetitively to assign role to unprocessed identities, after process was stopped or interrupted (e.g. by server restart). 
 * 
 * @author Radek Tomiška                                                     
 * @since 10.4.0 
 */
@Component(ProcessAllAutomaticRoleByTreeTaskExecutor.TASK_NAME)
public class ProcessAllAutomaticRoleByTreeTaskExecutor extends ProcessAutomaticRoleByTreeTaskExecutor {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ProcessAllAutomaticRoleByTreeTaskExecutor.class);
	public static final String TASK_NAME = "core-process-all-automatic-role-tree-long-running-task";
	//
	@Autowired private IdmRoleTreeNodeService roleTreeNodeService;
	@Autowired private EntityStateManager entityStateManager;
	//
	private List<UUID> automaticRoles = null;
	private ZonedDateTime startTime;
	
	@Override
	public String getName() {
		return TASK_NAME;
	}
	
	@Override
	public boolean requireNewTransaction() {
		return true;
	}
	
	@Override
	public boolean continueOnException() {
		return true;
	}
	
	@Override
	public boolean isRecoverable() {
		return true;
	}
	
	@Override
	public void init(Map<String, Object> properties) {
		startTime = ZonedDateTime.now();
		automaticRoles = roleTreeNodeService
				.findIds(
						PageRequest.of(
								0, 
								Integer.MAX_VALUE, 
								Sort.by(String.format("%s.%s", IdmRoleTreeNode_.role.getName(), IdmRole_.code.getName()))
						)
				)
				.getContent();
		setAutomaticRoles(automaticRoles);
		//
		LOG.info("Start recount all automatic roles [{}] by tree structure in [{}].", automaticRoles.size(), startTime);
		//
		super.init(properties);
	}
	
	@Override
	protected List<UUID> getAutomaticRoles(Map<String, Object> properties) {
		return automaticRoles;
	}
	
	@Override
	protected Boolean end(Boolean result, Exception ex) {
		Boolean ended = super.end(result, ex);
		//
		if (BooleanUtils.isTrue(ended)) {
			// clear all flags for automatic roles (for contract and position) created before task started (all automatic roles are recounted now)
			IdmEntityStateFilter filter = new IdmEntityStateFilter();
			filter.setCreatedTill(startTime);
			filter.setStates(Lists.newArrayList(OperationState.BLOCKED));
			filter.setResultCode(CoreResultCode.AUTOMATIC_ROLE_SKIPPED.getCode());
			// all potential owners with this flag
			entityStateManager.findStates(filter, null).forEach(state -> {
				entityStateManager.deleteState(state);
			});
		}
		//
		return ended;
	}
	
	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties =  super.getProperties();
		// all => not configurable
		properties.remove(AbstractAutomaticRoleTaskExecutor.PARAMETER_ROLE_TREE_NODE);
		return properties;
	}
	
	@Override
	public List<String> getPropertyNames() {
		List<String> propertyNames = super.getPropertyNames();
		// all => not configurable
		propertyNames.remove(AbstractAutomaticRoleTaskExecutor.PARAMETER_ROLE_TREE_NODE);
		return propertyNames;
	}
	
	@Override
	public List<IdmFormAttributeDto> getFormAttributes() {
		// all => not configurable
		return new ArrayList<>();
	}
}
