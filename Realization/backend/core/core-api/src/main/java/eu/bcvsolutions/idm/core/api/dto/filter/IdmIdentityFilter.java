package eu.bcvsolutions.idm.core.api.dto.filter;

import java.util.List;
import java.util.UUID;

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
public class IdmIdentityFilter 
		extends DataFilter 
		implements CorrelationFilter, ExternalIdentifiableFilter, ExternalCodeable, DisableableFilter, FormableFilter {
	
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
	 * Returns managers by identity's contract working position.
	 */
	public static final String PARAMETER_MANAGERS_BY_CONTRACT = "managersByContract";
	/**
	 * Returns managers for valid now or in future contracts. Can be combined with PARAMETER_MANAGERS_FOR only.
	 * Contract state (~DISABLED) is ignored. This filter works just with contract dates.
	 * 
	 * @since 10.3.0
	 */
	public static final String PARAMETER_VALID_CONTRACT_MANAGERS = "validContractManagers";
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
	 * Guarantees for given role and guarantees type.
	 * This parameter will be use only if PARAMETER_GUARANTEES_FOR_ROLE is sets!
	 * 
	 * @since 10.3.0
	 */
	public static final String PARAMETER_GUARANTEE_TYPE = "guaranteesType";
	/**
	 * Identities by email.
	 */
	public static final String PARAMETER_EMAIL = "email";
	/**
	 * Identities by phone.
	 * 
	 * @since 10.3.0
	 */
	public static final String PARAMETER_PHONE = "phone";
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
	/**
	 * Identity projection.
	 */
	public static final String PARAMETER_FORM_PROJECTION = "formProjection";
	/**
	 * Without work position - without contract or with contract without work position is set.
	 * 
	 * @since 10.5.0
	 */
	public static final String PARAMETER_WITHOUT_WORK_POSITION = "withoutWorkPosition";
	
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
		return getParameterConverter().toString(getData(), PARAMETER_USERNAME);
	}

	public void setUsername(String username) {
		set(PARAMETER_USERNAME, username);
	}

	public UUID getSubordinatesFor() {
		return getParameterConverter().toUuid(getData(), PARAMETER_SUBORDINATES_FOR);
	}

	public void setSubordinatesFor(UUID subordinatesFor) {
		set(PARAMETER_SUBORDINATES_FOR, subordinatesFor);
	}

	public UUID getSubordinatesByTreeType() {
		return getParameterConverter().toUuid(getData(), PARAMETER_SUBORDINATES_BY_TREE_TYPE);
	}

	public void setSubordinatesByTreeType(UUID subordinatesByTreeType) {
		set(PARAMETER_SUBORDINATES_BY_TREE_TYPE, subordinatesByTreeType);
	}
	
	public void setManagersFor(UUID managersFor) {
		set(PARAMETER_MANAGERS_FOR, managersFor);
	}
	
	public UUID getManagersFor() {
		return getParameterConverter().toUuid(getData(), PARAMETER_MANAGERS_FOR);
	}
	
	public void setManagersByTreeType(UUID managersByTreeType) {
		set(PARAMETER_MANAGERS_BY_TREE_TYPE, managersByTreeType);
	}
	
	public UUID getManagersByTreeType() {
		return getParameterConverter().toUuid(getData(), PARAMETER_MANAGERS_BY_TREE_TYPE);
	}
	
	public UUID getManagersByContract() {
		return getParameterConverter().toUuid(getData(), PARAMETER_MANAGERS_BY_CONTRACT);
	}
	
	public void setManagersByContract(UUID managersByContract) {
		set(PARAMETER_MANAGERS_BY_CONTRACT, managersByContract);
	}
	
	public void setRoles(List<UUID> roles) {
    	put(PARAMETER_ROLE, roles);
	}
	
	public List<UUID> getRoles() {
		return getParameterConverter().toUuids(getData(), PARAMETER_ROLE);
	}
	
	public UUID getTreeNode() {
		return getParameterConverter().toUuid(getData(), PARAMETER_TREE_NODE);
	}
	
	public void setTreeNode(UUID treeNode) {
		set(PARAMETER_TREE_NODE, treeNode);
	}
	
	public UUID getTreeType() {
		return getParameterConverter().toUuid(getData(), PARAMETER_TREE_TYPE);
	}
	
	public void setTreeType(UUID treeType) {
		set(PARAMETER_TREE_TYPE, treeType);
	}
	
	public boolean isRecursively() {
		return getParameterConverter().toBoolean(getData(), PARAMETER_RECURSIVELY, true);
	}
	
	public void setRecursively(boolean recursively) {
		set(PARAMETER_RECURSIVELY, recursively);
	}
	
	public boolean isIncludeGuarantees() {
		return getParameterConverter().toBoolean(getData(), PARAMETER_INCLUDE_GUARANTEES, true);
	}
	
	public void setIncludeGuarantees(boolean includeGuarantees) {
		set(PARAMETER_INCLUDE_GUARANTEES, includeGuarantees);
	}

	public String getFirstName() {
		return getParameterConverter().toString(getData(), PARAMETER_FIRSTNAME);
	}

	public void setFirstName(String firstName) {
		set(PARAMETER_FIRSTNAME, firstName);
	}

	public String getLastName() {
		return getParameterConverter().toString(getData(), PARAMETER_LASTNAME);
	}

	public void setLastName(String lastName) {
		set(PARAMETER_LASTNAME, lastName);
	}
	
	public void setState(IdentityState state) {
		set(PARAMETER_STATE, state);
	}
	
	public IdentityState getState() {
		return getParameterConverter().toEnum(getData(), PARAMETER_STATE, IdentityState.class);
	}

	public UUID getAutomaticRoleId() {
		return getParameterConverter().toUuid(getData(), PARAMETER_AUTOMATIC_ROLE);
	}

	public void setAutomaticRoleId(UUID automaticRoleId) {
		set(PARAMETER_AUTOMATIC_ROLE, automaticRoleId);
	}
	
	public void setIdentifiers(List<String> identifiers) {
		put(PARAMETER_IDENTIFIERS, identifiers);
	}

	public List<String> getIdentifiers() {
		return getParameterConverter().toStrings(getData(), PARAMETER_IDENTIFIERS);
	}

	@Override
	public String getExternalCode() {
		return getParameterConverter().toString(getData(), PROPERTY_EXTERNAL_CODE);
	}

	@Override
	public void setExternalCode(String externalCode) {
		set(PROPERTY_EXTERNAL_CODE, externalCode);
	}
	
	public UUID getGuaranteesForRole() {
		return getParameterConverter().toUuid(getData(), PARAMETER_GUARANTEES_FOR_ROLE);
	}
	
	public void setGuaranteesForRole(UUID guaranteesForRole) {
		set(PARAMETER_GUARANTEES_FOR_ROLE, guaranteesForRole);
	}
	
	/**
	 * Guarantees for given role and guarantees type.
	 * This parameter will be use in filter only if the setGuaranteesForRole parameter will be set!
	 * 
	 * @return
	 * @since 10.3.0
	 */
	public String getGuaranteeType() {
		return getParameterConverter().toString(getData(), PARAMETER_GUARANTEE_TYPE);
	}

	/**
	 * Guarantees for given role and guarantees type.
	 * This parameter will be use in filter only if the setGuaranteesForRole parameter will be set!
	 * 
	 * @param type
	 * @since 10.3.0
	 */
	public void setGuaranteeType(String type) {
		set(PARAMETER_GUARANTEE_TYPE, type);
	}
	
	/**
	 * @since 9.3.0
	 * @return
	 */
	public String getEmail() {
		return getParameterConverter().toString(getData(), PARAMETER_EMAIL);
	}
	
	/**
	 * @since 9.3.0
	 * @param email
	 */
	public void setEmail(String email) {
		set(PARAMETER_EMAIL, email);
	}
	
	/**
	 * Identity phone.
	 * 
	 * @since 10.3.0
	 * @return filter value
	 */
	public String getPhone() {
		return getParameterConverter().toString(getData(), PARAMETER_PHONE);
	}
	
	/**
	 * Identity phone.
	 * 
	 * @since 10.3.0
	 * @param phone filter value
	 */
	public void setPhone(String phone) {
		set(PARAMETER_PHONE, phone);
	}
	
	/**
	 * Form projection (~identity type).
	 * 
	 * @return projection
	 * @since 10.2.0
	 */
	public UUID getFormProjection() {
		return getParameterConverter().toUuid(getData(), PARAMETER_FORM_PROJECTION);
	}
	
	/**
	 * Form projection (~identity type).
	 * 
	 * @param formProjection projection
	 * @since 10.2.0
	 */
	public void setFormProjection(UUID formProjection) {
		set(PARAMETER_FORM_PROJECTION, formProjection);
	}
	
	/**
	 * Filter managers for valid now or in future contracts.
	 * - true: valid now or in future
	 * - false: ended contracts
	 * 
	 * Contract state (~DISABLED) is ignored. This filter works just with contract dates.
	 * 
	 * @return filter value
	 * @since 10.3.0
	 */
	public Boolean getValidContractManagers() {
    	return getParameterConverter().toBoolean(getData(), PARAMETER_VALID_CONTRACT_MANAGERS);
	}
	
	/**
	 * Filter managers for valid now or in future contracts.
	 * - true: valid now or in future
	 * - false: ended contracts
	 * 
	 * Contract state (~DISABLED) is ignored. This filter works just with contract dates.
	 * 
	 * @param filter value
	 * @since 10.3.0
	 */
	public void setValidContractManagers(Boolean validContractManagers) {
		set(PARAMETER_VALID_CONTRACT_MANAGERS, validContractManagers);
	}
	
	/**
	 * Without work position - without contract or with contract without work position is set.
	 * 
	 * @return without work position 
	 * @since 10.5.0
	 */
	public Boolean getWithoutWorkPosition() {
		return getParameterConverter().toBoolean(getData(), PARAMETER_WITHOUT_WORK_POSITION);
	}
	
	/**
	 * Without work position - without contract or with contract without work position is set.
	 * 
	 * @param withoutWorkPosition without work position 
	 * @since 10.5.0
	 */
	public void setWithoutWorkPosition(Boolean withoutWorkPosition) {
		set(PARAMETER_WITHOUT_WORK_POSITION, withoutWorkPosition);
	}
}
