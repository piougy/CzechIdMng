package eu.bcvsolutions.idm.core.api.dto.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.domain.ExternalCodeable;
import eu.bcvsolutions.idm.core.api.domain.IdentityState;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;

/**
 * Filter for identities.
 * 
 * @author Radek Tomiška
 *
 */
public class IdmIdentityFilter extends DataFilter implements CorrelationFilter, ExternalIdentifiableFilter, ExternalCodeable {
	
	/**
	 * Identity by username.
	 */
	public static final String PARAMETER_USERNAME = "username";
	/**
	 * Subordinates for given identity.
	 */
	public static final String PARAMETER_SUBORDINATES_FOR = "subordinatesFor";
	/**
	 * Subordinates by given tree structure.
	 */
	public static final String PARAMETER_SUBORDINATES_BY_TREE_TYPE = "subordinatesByTreeType";
	/**
	 * Managers with contract guarantees included (manually assigned guarantees).
	 */
	public static final String PARAMETER_INCLUDE_GUARANTEES = "includeGuarantees";
	/**
	 * Managers for given identity.
	 */
	public static final String PARAMETER_MANAGERS_FOR = "managersFor";
	/**
	 * Managers by given tree structure.
	 */
	public static final String PARAMETER_MANAGERS_BY_TREE_TYPE = "managersByTreeType";
	/**
	 * Returns managers by identity's contract working prosition.
	 */
	public static final String PARAMETER_MANAGERS_BY_CONTRACT = "managersByContract";
	/**
	 * Identity is disabled.
	 */
	public static final String PARAMETER_DISABLED = "disabled";
	/**
	 * Identity state.
	 */
	public static final String PARAMETER_STATE = "state";
	/**
	 * Automatic role (by tree, attribute).
	 */
	public static final String PARAMETER_AUTOMATIC_ROLE = "automaticRoleId";
	/**
	 * Identifiers filter in externalCode, username.
	 */
	public static final String PARAMETER_IDENTIFIERS = "identifiers";
	/**
	 * Guarantees for given role.
	 */
	public static final String PARAMETER_GUARANTEES_FOR_ROLE = "guaranteesForRole";
	/**
	 * Identities by email.
	 */
	public static final String PARAMETER_EMAIL = "email";
	/**
	 * role - multiple, OR.
	 */
	public static final String PARAMETER_ROLE = "role";
	/**
	 * Identities for tree structure (by identity contract).
	 */
	public static final String PARAMETER_TREE_NODE = "treeNodeId";
	/**
	 * Identities for tree structure recursively down (true by default).
	 */
	public static final String PARAMETER_RECURSIVELY = "recursively";
	/**
	 * Identities for tree structure (by identity contract).
	 */
	public static final String PARAMETER_TREE_TYPE = "treeTypeId";
	/**
	 * Identity first name - exact match.
	 */
	public static final String PARAMETER_FIRSTNAME = "firstName";
	/**
	 * Identity last name - exact match.
	 */
	public static final String PARAMETER_LASTNAME = "lastName";
	
	public IdmIdentityFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public IdmIdentityFilter(MultiValueMap<String, Object> data) {
		this(data, null);
	}
	
	public IdmIdentityFilter(MultiValueMap<String, Object> data, ParameterConverter parameterConverter) {
		super(IdmIdentityDto.class, data, parameterConverter);
	}
	
	public String getUsername() {
		return getParameterConverter().toString(data, PARAMETER_USERNAME);
	}

	public void setUsername(String username) {
		set(PARAMETER_USERNAME, username);
	}

	public UUID getSubordinatesFor() {
		return getParameterConverter().toUuid(data, PARAMETER_SUBORDINATES_FOR);
	}

	public void setSubordinatesFor(UUID subordinatesFor) {
		set(PARAMETER_SUBORDINATES_FOR, subordinatesFor);
	}

	public UUID getSubordinatesByTreeType() {
		return getParameterConverter().toUuid(data, PARAMETER_SUBORDINATES_BY_TREE_TYPE);
	}

	public void setSubordinatesByTreeType(UUID subordinatesByTreeType) {
		set(PARAMETER_SUBORDINATES_BY_TREE_TYPE, subordinatesByTreeType);
	}
	
	public void setManagersFor(UUID managersFor) {
		set(PARAMETER_MANAGERS_FOR, managersFor);
	}
	
	public UUID getManagersFor() {
		return getParameterConverter().toUuid(data, PARAMETER_MANAGERS_FOR);
	}
	
	public void setManagersByTreeType(UUID managersByTreeType) {
		set(PARAMETER_MANAGERS_BY_TREE_TYPE, managersByTreeType);
	}
	
	public UUID getManagersByTreeType() {
		return getParameterConverter().toUuid(data, PARAMETER_MANAGERS_BY_TREE_TYPE);
	}
	
	public UUID getManagersByContract() {
		return getParameterConverter().toUuid(data, PARAMETER_MANAGERS_BY_CONTRACT);
	}
	
	public void setManagersByContract(UUID managersByContract) {
		set(PARAMETER_MANAGERS_BY_CONTRACT, managersByContract);
	}
	
	public void setRoles(List<UUID> roles) {
		if (CollectionUtils.isEmpty(roles)) {
    		data.remove(PARAMETER_ROLE);
    	} else {
    		data.put(PARAMETER_ROLE, new ArrayList<Object>(roles));
    	}
	}
	
	public List<UUID> getRoles() {
		return getParameterConverter().toUuids(data, PARAMETER_ROLE);
	}
	
	public UUID getTreeNode() {
		return getParameterConverter().toUuid(data, PARAMETER_TREE_NODE);
	}
	
	public void setTreeNode(UUID treeNode) {
		set(PARAMETER_TREE_NODE, treeNode);
	}
	
	public UUID getTreeType() {
		return getParameterConverter().toUuid(data, PARAMETER_TREE_TYPE);
	}
	
	public void setTreeType(UUID treeType) {
		set(PARAMETER_TREE_TYPE, treeType);
	}
	
	public boolean isRecursively() {
		return getParameterConverter().toBoolean(data, PARAMETER_RECURSIVELY, true);
	}
	
	public void setRecursively(boolean recursively) {
		set(PARAMETER_RECURSIVELY, recursively);
	}
	
	public boolean isIncludeGuarantees() {
		return getParameterConverter().toBoolean(data, PARAMETER_INCLUDE_GUARANTEES, true);
	}
	
	public void setIncludeGuarantees(boolean includeGuarantees) {
		set(PARAMETER_INCLUDE_GUARANTEES, includeGuarantees);
	}

	public Boolean getDisabled() {
		return getParameterConverter().toBoolean(data, PARAMETER_DISABLED);
	}

	public void setDisabled(Boolean disabled) {
		set(PARAMETER_DISABLED, disabled);
	}

	public String getFirstName() {
		return getParameterConverter().toString(data, PARAMETER_FIRSTNAME);
	}

	public void setFirstName(String firstName) {
		set(PARAMETER_FIRSTNAME, firstName);
	}

	public String getLastName() {
		return getParameterConverter().toString(data, PARAMETER_LASTNAME);
	}

	public void setLastName(String lastName) {
		set(PARAMETER_LASTNAME, lastName);
	}
	
	public void setState(IdentityState state) {
		set(PARAMETER_STATE, state);
	}
	
	public IdentityState getState() {
		return getParameterConverter().toEnum(data, PARAMETER_STATE, IdentityState.class);
	}

	public UUID getAutomaticRoleId() {
		return getParameterConverter().toUuid(data, PARAMETER_AUTOMATIC_ROLE);
	}

	public void setAutomaticRoleId(UUID automaticRoleId) {
		set(PARAMETER_AUTOMATIC_ROLE, automaticRoleId);
	}
	
	public void setIdentifiers(List<String> identifiers) {
		if (CollectionUtils.isEmpty(identifiers)) {
    		data.remove(PARAMETER_IDENTIFIERS);
    	} else {
    		data.put(PARAMETER_IDENTIFIERS, new ArrayList<Object>(identifiers));
    	}
	}

	public List<String> getIdentifiers() {
		return getParameterConverter().toStrings(data, PARAMETER_IDENTIFIERS);
	}

	@Override
	public String getExternalCode() {
		return (String) data.getFirst(PROPERTY_EXTERNAL_CODE);
	}

	@Override
	public void setExternalCode(String externalCode) {
		set(PROPERTY_EXTERNAL_CODE, externalCode);
	}
	
	public UUID getGuaranteesForRole() {
		return getParameterConverter().toUuid(data, PARAMETER_GUARANTEES_FOR_ROLE);
	}
	
	public void setGuaranteesForRole(UUID guaranteesForRole) {
		set(PARAMETER_GUARANTEES_FOR_ROLE, guaranteesForRole);
	}
	
	/**
	 * @since 9.3.0
	 * @return
	 */
	public String getEmail() {
		return (String) data.getFirst(PARAMETER_EMAIL);
	}
	
	/**
	 * @since 9.3.0
	 * @param email
	 */
	public void setEmail(String email) {
		set(PARAMETER_EMAIL, email);
	}
}
