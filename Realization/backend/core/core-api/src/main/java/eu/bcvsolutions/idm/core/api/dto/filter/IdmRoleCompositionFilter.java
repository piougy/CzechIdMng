package eu.bcvsolutions.idm.core.api.dto.filter;

import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.domain.ExternalIdentifiable;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleCompositionDto;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;


/**
 * Filter for {@link IdmRoleComposition}
 * 
 * TODO: common composition filter?
 * 
 * @author Radek Tomiška
 * @since 9.0.0
 */
public class IdmRoleCompositionFilter extends DataFilter implements ExternalIdentifiable {
	/**
	 * Superior role
	 */
	public static final String PARAMETER_SUPERIOR_ID = "superiorId";
	/**
	 * Sub role
	 */
	public static final String PARAMETER_SUB_ID = "subId";
	
	public IdmRoleCompositionFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public IdmRoleCompositionFilter(MultiValueMap<String, Object> data) {
		super(IdmRoleCompositionDto.class, data);
	}
	
	public UUID getSuperiorId() {
		return DtoUtils.toUuid(data.getFirst(PARAMETER_SUPERIOR_ID));
	}
	
	public void setSuperiorId(UUID superiorId) {
		data.set(PARAMETER_SUPERIOR_ID, superiorId);
	}
	
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
