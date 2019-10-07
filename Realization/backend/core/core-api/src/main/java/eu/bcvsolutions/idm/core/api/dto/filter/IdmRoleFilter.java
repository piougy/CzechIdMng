package eu.bcvsolutions.idm.core.api.dto.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.domain.ExternalIdentifiable;
import eu.bcvsolutions.idm.core.api.domain.RoleType;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;

/**
 * Filter for roles
 * 
 * Codeable filter parameter can be used
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 *
 */
public class IdmRoleFilter 
		extends DataFilter 
		implements CorrelationFilter, ExternalIdentifiable {

	/**
	 * Parent role identifier - find sub roles by role composition
	 */
	public static final String PARAMETER_PARENT = IdmTreeNodeFilter.PARAMETER_PARENT;
	//
	public static final String PARAMETER_ROLE_CATALOGUE = "roleCatalogue";
	public static final String PARAMETER_GUARANTEE = "guarantee";
	public static final String PARAMETER_ENVIRONMENT = "environment"; // list - OR
	public static final String PARAMETER_BASE_CODE = "baseCode";
	public static final String PARAMETER_IDENTITY_ROLE_ATTRIBUTE_DEF = "identityRoleAttributeDefinition";
	@Deprecated // unused since the start of project
	private RoleType roleType;
	
	public IdmRoleFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public IdmRoleFilter(MultiValueMap<String, Object> data) {
		this(data, null);
	}
	
	public IdmRoleFilter(MultiValueMap<String, Object> data, ParameterConverter parameterConverter) {
		super(IdmRoleDto.class, data, parameterConverter);
	}

	@Deprecated
	public RoleType getRoleType() {
		return roleType;
	}

	@Deprecated
	public void setRoleType(RoleType roleType) {
		this.roleType = roleType;
	}
	
	@Override
	public String getProperty() {
		return (String) data.getFirst(PARAMETER_CORRELATION_PROPERTY);
	}

	@Override
	public void setProperty(String property) {
		data.set(PARAMETER_CORRELATION_PROPERTY, property);
	}

	@Override
	public String getValue() {
		return (String) data.getFirst(PARAMETER_CORRELATION_VALUE);
	}

	@Override
	public void setValue(String value) {
		data.set(PARAMETER_CORRELATION_VALUE, value);
	}

	public UUID getRoleCatalogueId() {
		return DtoUtils.toUuid(data.getFirst(PARAMETER_ROLE_CATALOGUE));
	}

	public void setRoleCatalogueId(UUID roleCatalogueId) {
		data.set(PARAMETER_ROLE_CATALOGUE, roleCatalogueId);
	}

	public UUID getAttributeFormDefinitionId() {
		return DtoUtils.toUuid(data.getFirst(PARAMETER_IDENTITY_ROLE_ATTRIBUTE_DEF));
	}

	public void setAttributeFormDefinitionId(UUID id) {
		data.set(PARAMETER_IDENTITY_ROLE_ATTRIBUTE_DEF, id);
	}
	
	@Override
	public String getExternalId() {
		return (String) data.getFirst(PROPERTY_EXTERNAL_ID);
	}
	
	@Override
	public void setExternalId(String externalId) {
		data.set(PROPERTY_EXTERNAL_ID, externalId);
	}
	
	public String getEnvironment() {
    	return (String) data.getFirst(PARAMETER_ENVIRONMENT);
	}
    
    public void setEnvironment(String environment) {
    	if (StringUtils.isEmpty(environment)) {
    		data.remove(PARAMETER_ENVIRONMENT);
    	} else {
    		data.put(PARAMETER_ENVIRONMENT, Lists.newArrayList(environment));
    	}
	}
    
    public List<String> getEnvironments() {
		return getParameterConverter().toStrings(data, PARAMETER_ENVIRONMENT);
	}
    
    public void setEnvironments(List<String> environments) {
    	if (CollectionUtils.isEmpty(environments)) {
    		data.remove(PARAMETER_ENVIRONMENT);
    	} else {
    		data.put(PARAMETER_ENVIRONMENT, new ArrayList<Object>(environments));
    	}
	}
	
	public String getBaseCode() {
		return (String) data.getFirst(PARAMETER_BASE_CODE);
	}
	
	public void setBaseCode(String baseCode) {
		data.set(PARAMETER_BASE_CODE, baseCode);
	}
	
	public UUID getGuaranteeId() {
		return getParameterConverter().toUuid(data ,PARAMETER_GUARANTEE);
	}

	public void setGuaranteeId(UUID guaranteeId) {
		data.set(PARAMETER_GUARANTEE, guaranteeId);
	}
	
	/**
	 * @since 9.4.0
	 * @return
	 */
	public UUID getParent() {
		return getParameterConverter().toUuid(data, PARAMETER_PARENT);
	}

	/**
	 * @since 9.4.0
	 * @param parent
	 */
	public void setParent(UUID parent) {
		data.set(PARAMETER_PARENT, parent);
	}
}
