package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.exception.CoreResultCode;
import eu.bcvsolutions.idm.core.exception.RestApplicationException;
import eu.bcvsolutions.idm.core.model.domain.ResourcesWrapper;
import eu.bcvsolutions.idm.core.model.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRoleRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleRepository;
import eu.bcvsolutions.idm.core.model.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowFilterDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowProcessInstanceDto;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowProcessInstanceService;

@Service
public class DefaultIdmIdentityService implements IdmIdentityService {
	public static String ADD_ROLE_TO_IDENTITY_WORKFLOW = "addRoleToIdentityWorkflow";

	@Autowired
	private IdmIdentityRepository identityRepository;

	@Autowired
	private IdmRoleRepository idmRoleRepository;

	@Autowired
	private IdmIdentityRoleRepository identityRoleRepository;

	@Autowired
	private WorkflowProcessInstanceService workflowProcessInstanceService;

	@Override
	public boolean addRole(IdmIdentityRole identityRole, boolean startApproveWorkflow) {
		IdmIdentity identity = identityRole.getIdentity();
		IdmRole role = identityRole.getRole();
		Date validFrom = identityRole.getValidFrom();
		Date validTill = identityRole.getValidTill();

		if (!startApproveWorkflow || !role.isApprovable()) {
			identityRoleRepository.save(identityRole);
			return true;
		}
		Map<String, Object> variables = new HashMap<>();
		variables.put("roleIdentifier", role.getId());
		variables.put("identityIdentifier", identity.getId());
		variables.put("validFrom", validFrom);
		variables.put("validTill", validTill);

		// Check on exist duplication workflow
		checkDuplicationWorkflow(identity, role, variables);

		workflowProcessInstanceService.startProcess(ADD_ROLE_TO_IDENTITY_WORKFLOW, IdmIdentity.class.getSimpleName(),
				identity.getId().toString(), variables);
		// TODO: if role is approved imediatelly, then return true (e.g. if
		// request author is in approvers)
		return false;
	}
	
	@Override
	public boolean addRoleByDto(IdmIdentityRoleDto identityRoleDto, boolean startApproveWorkflow) {
		return addRole(toEntity(identityRoleDto), startApproveWorkflow);
	}

	private IdmIdentityRole toEntity(IdmIdentityRoleDto identityRoleDto) {
		if (identityRoleDto == null) {
			return null;
		}
		IdmRole role = null;
		IdmIdentity identity = null;
		if (identityRoleDto.getRole() != null) {
			role = idmRoleRepository.findOne(identityRoleDto.getRole());
		}
		if (identityRoleDto.getIdentity() != null) {
			identity = identityRepository.findOne(identityRoleDto.getIdentity());
		}
		IdmIdentityRole identityRole = new IdmIdentityRole();
		identityRole.setId(identityRoleDto.getId());
		identityRole.setRole(role);
		identityRole.setIdentity(identity);
		identityRole.setValidFrom(identityRoleDto.getValidFrom());
		identityRole.setValidTill(identityRoleDto.getValidTill());
		return identityRole;
	}

	/**
	 * Check on exist duplication workflow
	 * 
	 * @param identity
	 * @param role
	 * @param filter
	 */
	private void checkDuplicationWorkflow(IdmIdentity identity, IdmRole role, Map<String, Object> variables) {
		WorkflowFilterDto filter = new WorkflowFilterDto();
		filter.setProcessDefinitionKey(ADD_ROLE_TO_IDENTITY_WORKFLOW);
		filter.getEqualsVariables().putAll(variables);

		ResourcesWrapper<WorkflowProcessInstanceDto> result = workflowProcessInstanceService.search(filter);
		if (result != null && result.getResources() != null && !result.getResources().isEmpty()) {
			throw new RestApplicationException(CoreResultCode.CONFLICT,
					"For role %s and identity %s approve workflow already exist!",
					ImmutableMap.of("identity", identity.getUsername(), "role", role.getName()));
		}
	}

	@Override
	public IdmIdentity getByUsername(String username) {
		return identityRepository.findOneByUsername(username);
	}

	@Override
	public IdmIdentity get(Long id) {
		IdmIdentity entity = identityRepository.findOne(id);
		entity.getRoles();
		return entity;
	}

	@Override
	public String getNiceLabel(IdmIdentity identity) {
		if (identity == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		if (identity.getTitleBefore() != null) {
			sb.append(identity.getTitleBefore()).append(" ");
		}
		if (identity.getFirstName() != null) {
			sb.append(identity.getFirstName()).append(" ");
		}
		if (identity.getLastName() != null) {
			sb.append(identity.getLastName()).append(" ");
		}
		if (identity.getTitleAfter() != null) {
			sb.append(identity.getTitleAfter()).append(" ");
		}
		return sb.toString().trim();
	}

}
