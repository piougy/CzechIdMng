package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.runtime.ProcessInstance;
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
	public static String ADD_ROLE_TO_IDENTITY_WORKFLOW = "changeIdentityRoles";

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
		identityRoleRepository.save(identityRole);
		return true;
	}
	
	@Override
	public boolean addRoleByDto(IdmIdentityRoleDto identityRoleDto, boolean startApproveWorkflow) {
		return addRole(toEntity(identityRoleDto), startApproveWorkflow);
	}
	
	@Override
	public ProcessInstance changePermissions(IdmIdentity identity){
		Map<String, Object> variables = new HashMap<>();
		variables.put(WorkflowProcessInstanceService.APPLICANT_IDENTIFIER, identity.getId());
		//check duplication
		//checkDuplicationWorkflow(identity, variables);
		return workflowProcessInstanceService.startProcess(ADD_ROLE_TO_IDENTITY_WORKFLOW, IdmIdentity.class.getSimpleName(), identity.getUsername(), identity.getId(), variables);	
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
	private void checkDuplicationWorkflow(IdmIdentity identity, Map<String, Object> variables) {
		WorkflowFilterDto filter = new WorkflowFilterDto();
		filter.setProcessDefinitionKey(ADD_ROLE_TO_IDENTITY_WORKFLOW);
		filter.getEqualsVariables().putAll(variables);

		ResourcesWrapper<WorkflowProcessInstanceDto> result = workflowProcessInstanceService.search(filter);
		if (result != null && result.getResources() != null && !result.getResources().isEmpty()) {
			throw new RestApplicationException(CoreResultCode.CONFLICT,
					"For identity %s change permission workflow already exist!",
					ImmutableMap.of("identity", identity.getUsername()));
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
	
	/**
	 * Find all identities usernames by assigned role
	 * @param roleId
	 * @return String with all found usernames separate with comma 
	 */
	public String findAllByRole(Long roleId){
		List<IdmIdentity> identities =  identityRepository.findAllByRole(roleId);
		if(identities == null){
			return null;
		}
		StringBuilder sb = new StringBuilder();
		for(IdmIdentity i : identities){
			sb.append(i.getUsername());
			sb.append(",");
		}
		return sb.toString();
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
