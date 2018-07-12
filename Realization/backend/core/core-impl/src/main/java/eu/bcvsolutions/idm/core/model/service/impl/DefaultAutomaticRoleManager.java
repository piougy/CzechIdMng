

package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.domain.AutomaticRoleRequestType;
import eu.bcvsolutions.idm.core.api.domain.RequestOperationType;
import eu.bcvsolutions.idm.core.api.domain.RequestState;
import eu.bcvsolutions.idm.core.api.dto.AbstractIdmAutomaticRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleAttributeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleAttributeRuleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleAttributeRuleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmAutomaticRoleAttributeRuleFilter;
import eu.bcvsolutions.idm.core.api.exception.AcceptedException;
import eu.bcvsolutions.idm.core.api.exception.CoreException;
import eu.bcvsolutions.idm.core.api.service.AutomaticRoleManager;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleAttributeRuleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleAttributeRuleService;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleAttributeService;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleTreeNodeService;

/**
 * Manager for automatic role
 * 
 * @author svandav
 *
 */
@Service("automaticRoleManager")
public class DefaultAutomaticRoleManager implements AutomaticRoleManager {

	@Autowired
	private IdmAutomaticRoleAttributeRuleRequestService ruleRequestService;
	@Autowired
	private IdmAutomaticRoleRequestService roleRequestService;
	@Autowired
	private IdmAutomaticRoleAttributeService automaticRoleAttributeService;
	@Autowired
	private IdmRoleTreeNodeService roleTreeNodeService;
	@Autowired
	private IdmAutomaticRoleAttributeRuleService ruleService;

	@Override
	public IdmAutomaticRoleAttributeDto createAutomaticRoleByAttribute(IdmAutomaticRoleAttributeDto automaticRole,
			boolean executeImmediately, IdmAutomaticRoleAttributeRuleDto... rules) {
		IdmAutomaticRoleRequestDto request = new IdmAutomaticRoleRequestDto();
		request.setOperation(RequestOperationType.ADD);
		request.setRequestType(AutomaticRoleRequestType.ATTRIBUTE);
		request.setExecuteImmediately(executeImmediately);
		request.setName(automaticRole.getName());
		request.setRole(automaticRole.getRole());
		request = roleRequestService.save(request);

		if (rules != null) {
			for (IdmAutomaticRoleAttributeRuleDto rule : rules) {

				IdmAutomaticRoleAttributeRuleRequestDto ruleRequest = new IdmAutomaticRoleAttributeRuleRequestDto();
				ruleRequest.setRequest(request.getId());
				ruleRequest.setOperation(RequestOperationType.ADD);
				ruleRequest.setAttributeName(rule.getAttributeName());
				ruleRequest.setComparison(rule.getComparison());
				ruleRequest.setType(rule.getType());
				ruleRequest.setFormAttribute(rule.getFormAttribute());
				ruleRequest.setValue(rule.getValue());
				ruleRequest.setRule(rule.getId());
				ruleRequest = ruleRequestService.save(ruleRequest);
			}

		}
		request = roleRequestService.startRequestInternal(request.getId(), true);
		if (RequestState.EXECUTED == request.getState()) {
			UUID createdAutomaticRoleId = request.getAutomaticRole();
			Assert.notNull(createdAutomaticRoleId);
			return automaticRoleAttributeService.get(request.getAutomaticRole());
		}
		if (RequestState.IN_PROGRESS == request.getState()) {
			throw new AcceptedException(request.getId().toString());
		}
		if (RequestState.EXCEPTION == request.getState()) {
			throw new CoreException(request.getResult().getCause());
		}
		return null;
	}

	@Override
	public IdmAutomaticRoleAttributeDto changeAutomaticRoleRules(IdmAutomaticRoleAttributeDto automaticRole,
			boolean executeImmediately, IdmAutomaticRoleAttributeRuleDto... newRules) {
		Assert.notNull(automaticRole);
		Assert.notNull(automaticRole.getId(), "Automatic role must exists!");

		IdmAutomaticRoleRequestDto request = new IdmAutomaticRoleRequestDto();
		request.setOperation(RequestOperationType.UPDATE);
		request.setRequestType(AutomaticRoleRequestType.ATTRIBUTE);
		request.setExecuteImmediately(executeImmediately);
		request.setAutomaticRole(automaticRole.getId());
		request.setName(automaticRole.getName());
		request.setRole(automaticRole.getRole());
		final IdmAutomaticRoleRequestDto createdRequest = roleRequestService.save(request);
		ArrayList<IdmAutomaticRoleAttributeRuleDto> rules = Lists.newArrayList(newRules);
		if (rules != null) {
			// Creates request for change or add rule
			rules.forEach(rule -> {
				IdmAutomaticRoleAttributeRuleRequestDto ruleRequest = new IdmAutomaticRoleAttributeRuleRequestDto();
				ruleRequest.setRequest(createdRequest.getId());
				ruleRequest.setOperation(rule.getId() != null ? RequestOperationType.UPDATE : RequestOperationType.ADD);
				ruleRequest.setAttributeName(rule.getAttributeName());
				ruleRequest.setComparison(rule.getComparison());
				ruleRequest.setType(rule.getType());
				ruleRequest.setFormAttribute(rule.getFormAttribute());
				ruleRequest.setValue(rule.getValue());
				ruleRequest.setRule(rule.getId());
				ruleRequest = ruleRequestService.save(ruleRequest);
			});
		}

		IdmAutomaticRoleAttributeRuleFilter ruleFilter = new IdmAutomaticRoleAttributeRuleFilter();
		ruleFilter.setAutomaticRoleAttributeId(automaticRole.getId());
		List<IdmAutomaticRoleAttributeRuleDto> currentRules = ruleService.find(ruleFilter, null).getContent();
		currentRules.stream().filter(currentRule -> {
			return rules == null || !rules.contains(currentRule);
		}).forEach(ruleToDelete -> {
			// Creates request for remove rule
			IdmAutomaticRoleAttributeRuleRequestDto ruleRequest = new IdmAutomaticRoleAttributeRuleRequestDto();
			ruleRequest.setRequest(createdRequest.getId());
			ruleRequest.setOperation(RequestOperationType.REMOVE);
			ruleRequest.setAttributeName(ruleToDelete.getAttributeName());
			ruleRequest.setComparison(ruleToDelete.getComparison());
			ruleRequest.setType(ruleToDelete.getType());
			ruleRequest.setFormAttribute(ruleToDelete.getFormAttribute());
			ruleRequest.setValue(ruleToDelete.getValue());
			ruleRequest.setRule(ruleToDelete.getId());
			ruleRequest = ruleRequestService.save(ruleRequest);
		});

		IdmAutomaticRoleRequestDto executedRequest = roleRequestService.startRequestInternal(createdRequest.getId(),
				true);
		if (RequestState.EXECUTED == executedRequest.getState()) {
			UUID createdAutomaticRoleId = executedRequest.getAutomaticRole();
			Assert.notNull(createdAutomaticRoleId);
			return automaticRoleAttributeService.get(executedRequest.getAutomaticRole());
		}
		if (RequestState.IN_PROGRESS == executedRequest.getState()) {
			throw new AcceptedException(executedRequest.getId().toString());
		}
		if (RequestState.EXCEPTION == executedRequest.getState()) {
			throw new CoreException(executedRequest.getResult().getCause());
		}
		return null;
	}
	
	@Override
	public void deleteAutomaticRole(AbstractIdmAutomaticRoleDto automaticRole, boolean executeImmediately) {
		Assert.notNull(automaticRole);
		Assert.notNull(automaticRole.getId(), "Automatic role must exists!");
		
		IdmAutomaticRoleRequestDto request = new IdmAutomaticRoleRequestDto();
		if(automaticRole instanceof IdmRoleTreeNodeDto) {
			request.setRequestType(AutomaticRoleRequestType.TREE);
			request.setTreeNode(((IdmRoleTreeNodeDto)automaticRole).getTreeNode());
		}else {
			request.setRequestType(AutomaticRoleRequestType.ATTRIBUTE);
		}
		request.setOperation(RequestOperationType.REMOVE);
		request.setExecuteImmediately(executeImmediately);
		request.setAutomaticRole(automaticRole.getId());
		request.setName(automaticRole.getName());
		request.setRole(automaticRole.getRole());
		request = roleRequestService.save(request);
		request = roleRequestService.startRequestInternal(request.getId(), true);
		if (RequestState.EXECUTED == request.getState()) {
			return;
		}
		if (RequestState.IN_PROGRESS == request.getState()) {
			throw new AcceptedException(request.getId().toString());
		}
		if (RequestState.EXCEPTION == request.getState()) {
			throw new CoreException(request.getResult().getCause());
		}
	}

	@Override
	public IdmRoleTreeNodeDto createAutomaticRoleByTree(IdmRoleTreeNodeDto automaticRole, boolean executeImmediately) {
		IdmAutomaticRoleRequestDto request = new IdmAutomaticRoleRequestDto();
		request.setOperation(RequestOperationType.ADD);
		request.setRequestType(AutomaticRoleRequestType.TREE);
		request.setExecuteImmediately(executeImmediately);
		request.setName(automaticRole.getName());
		request.setRole(automaticRole.getRole());
		request.setTreeNode(automaticRole.getTreeNode());
		request = roleRequestService.save(request);
		request = roleRequestService.startRequestInternal(request.getId(), true);
		if (RequestState.EXECUTED == request.getState()) {
			UUID createdAutomaticRoleId = request.getAutomaticRole();
			Assert.notNull(createdAutomaticRoleId);
			return roleTreeNodeService.get(request.getAutomaticRole());
		}
		if (RequestState.IN_PROGRESS == request.getState()) {
			throw new AcceptedException(request.getId().toString());
		}
		if (RequestState.EXCEPTION == request.getState()) {
			throw new CoreException(request.getResult().getCause());
		}
		return null;
	}
}

