package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.activiti.engine.runtime.ProcessInstance;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.exception.CoreResultCode;
import eu.bcvsolutions.idm.core.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.model.domain.IdmGroupPermission;
import eu.bcvsolutions.idm.core.model.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.model.dto.QuickFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityWorkingPosition;
import eu.bcvsolutions.idm.core.model.repository.BaseRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityWorkingPositionRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleRepository;
import eu.bcvsolutions.idm.core.model.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowProcessInstanceService;
import eu.bcvsolutions.idm.security.service.SecurityService;

@Service
public class DefaultIdmIdentityService extends AbstractReadWriteEntityService<IdmIdentity, QuickFilter> implements IdmIdentityService {

	public static final String ADD_ROLE_TO_IDENTITY_WORKFLOW = "changeIdentityRoles";

	@Autowired
	private IdmIdentityRepository identityRepository;

	@Autowired
	private WorkflowProcessInstanceService workflowProcessInstanceService;
	
	@Autowired
	private IdmIdentityWorkingPositionRepository workingPositionRepository;
	
	@Autowired
	private IdmRoleRepository roleRepository;

	@Autowired
	private SecurityService securityService;
	
	@Override
	protected BaseRepository<IdmIdentity> getRepository() {
		return identityRepository;
	}

	/**
	 * Start workflow for change permissions
	 */
	@Override
	public ProcessInstance changePermissions(IdmIdentity identity) {
		return workflowProcessInstanceService.startProcess(ADD_ROLE_TO_IDENTITY_WORKFLOW,
				IdmIdentity.class.getSimpleName(), identity.getUsername(), identity.getId(), null);
	}

	@Override
	public IdmIdentity getByUsername(String username) {
		return identityRepository.findOneByUsername(username);
	}

	@Override
	public IdmIdentity get(Long id) {
		IdmIdentity entity = super.get(id);
		// TODO: where is this necesarry? Find and remove ...
		entity.getRoles();
		return entity;
	}
	
	@Override
	public Page<IdmIdentity> find(QuickFilter filter, Pageable pageable) {
		if (filter == null) {
			return find(pageable);
		}
		return identityRepository.findQuick(filter.getText(), pageable);
	}	

	/**
	 * Find all identities usernames by assigned role
	 * 
	 * @param roleId
	 * @return String with all found usernames separate with comma
	 */
	public String findAllByRole(Long roleId) {
		List<IdmIdentity> identities = this.finAllByRole(roleId);
				
		StringBuilder sb = new StringBuilder();
		for (IdmIdentity i : identities) {
			sb.append(i.getUsername());
			sb.append(",");
		}
		return sb.toString();
	}
	
	/**
	 * Find all identities by assigned role
	 * @param roleId
	 * @return List of IdmIdentity with assigned role
	 */
	public List<IdmIdentity> finAllByRole(Long roleId) {
		List<IdmIdentity> identities = identityRepository.findAllByRole(roleId);
		if (identities == null) {
			return null;
		}
		return identities;
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

	/**
	 * Method find all managers by user positions and return managers username,
	 * separate by commas
	 * 
	 * @param id
	 * @return String - usernames separate by commas
	 */
	public String findAllManagersByUserPositionsString(Long id) {
		List<String> list = this.findAllManagersByUserPositions(id).stream().map(IdmIdentity::getUsername)
				.collect(Collectors.toList());
		return StringUtils.join(list, ',');
	}

	/**
	 * Method find all managers by user positions and return managers identity
	 * @param id
	 * @return List of IdmIdentities 
	 */
	public List<IdmIdentity> findAllManagersByUserPositions(Long id) {
		List<IdmIdentity> result = new ArrayList<>();
		
		IdmIdentity user = this.get(id);
		Page<IdmIdentityWorkingPosition> positions = workingPositionRepository.findByIdentity(user, null);
		
		for	(IdmIdentityWorkingPosition position : positions) {
			result.add(position.getManager());
		}
		
		if (result.isEmpty()) {
			return this.finAllByRole(this.getAdminRoleId());
		}
		
		return result;
	}

	/**
	 * Changes given identity's password
	 * 
	 * TODO: propagate password change to other systems
	 * 
	 * @param identity
	 * @param passwordChangeDto
	 */
	@Override
	public void passwordChange(IdmIdentity identity, PasswordChangeDto passwordChangeDto) {
		if (!securityService.hasAnyAuthority(IdmGroupPermission.SYSTEM_ADMIN) && !StringUtils
				.equals(new String(identity.getPassword()), new String(passwordChangeDto.getOldPassword()))) {
			throw new ResultCodeException(CoreResultCode.PASSWORD_CHANGE_CURRENT_FAILED_IDM);
		}
		identity.setPassword(passwordChangeDto.getNewPassword());
		identityRepository.save(identity);
	}
	
	private Long getAdminRoleId() {
		return this.roleRepository.findOneByName(IdmRoleRepository.ADMIN_ROLE).getId();
	}
}
