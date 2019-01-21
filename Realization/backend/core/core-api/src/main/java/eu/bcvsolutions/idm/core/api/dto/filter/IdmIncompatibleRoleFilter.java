package eu.bcvsolutions.idm.core.api.dto.filter;

import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.domain.ExternalIdentifiable;
import eu.bcvsolutions.idm.core.api.domain.RequestFilterPredicate;
import eu.bcvsolutions.idm.core.api.dto.IdmIncompatibleRoleDto;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;


/**
 * Segregation of Duties
 * 
 * @author Radek Tomiška 
 * @since 9.4.0
 */
public class IdmIncompatibleRoleFilter extends DataFilter implements ExternalIdentifiable {
	/**
	 * Superior role
	 */
	public static final String PARAMETER_SUPERIOR_ID = "superiorId";
	/**
	 * Sub role
	 */
	public static final String PARAMETER_SUB_ID = "subId";
	
	public IdmIncompatibleRoleFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public IdmIncompatibleRoleFilter(MultiValueMap<String, Object> data) {
		super(IdmIncompatibleRoleDto.class, data);
	}
	
	@RequestFilterPredicate(field = "superior")
	public UUID getSuperiorId() {
		return DtoUtils.toUuid(data.getFirst(PARAMETER_SUPERIOR_ID));
	}
	
	public void setSuperiorId(UUID superiorId) {
		data.set(PARAMETER_SUPERIOR_ID, superiorId);
	}
	
	@RequestFilterPredicate(field = "sub")
	public UUID getSubId() {
		return DtoUtils.toUuid(data.getFirst(PARAMETER_SUB_ID));
	}
	
	public void setSubId(UUID subId) {
		data.set(PARAMETER_SUB_ID, subId);
	}
	
	@Override
	public String getExternalId() {
		return (String) data.getFirst(PROPERTY_EXTERNAL_ID);
	}
	
	@Override
	public void setExternalId(String externalId) {
		data.set(PROPERTY_EXTERNAL_ID, externalId);
	}
}
