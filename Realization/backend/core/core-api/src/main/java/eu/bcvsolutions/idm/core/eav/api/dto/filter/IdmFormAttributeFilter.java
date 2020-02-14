package eu.bcvsolutions.idm.core.eav.api.dto.filter;

import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;

/**
 * Form attribute definition filter.
 * 
 * TODO: Data filter + rest test.
 * 
 * @author Radek Tomiška
 *
 */
public class IdmFormAttributeFilter extends DataFilter {

	public static final String PARAMETER_FORM_DEFINITION_CODE = "definitionCode";
	//
	private String code;
	private UUID definitionId;
	private String definitionType;
	private String definitionName;
	private String definitionCode;
	private Boolean confidential;
	
	public IdmFormAttributeFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public IdmFormAttributeFilter(MultiValueMap<String, Object> data) {
		super(IdmFormAttributeDto.class, data);
	}

	public void setCode(String code) {
		this.code = code;
	}
	
	public String getCode() {
		return code;
	}

	public String getDefinitionType() {
		return definitionType;
	}

	public void setDefinitionType(String definitionType) {
		this.definitionType = definitionType;
	}

	public String getDefinitionName() {
		return definitionName;
	}

	public void setDefinitionName(String definitionName) {
		this.definitionName = definitionName;
	}
	
	public void setDefinitionId(UUID definitionId) {
		this.definitionId = definitionId;
	}
	
	public UUID getDefinitionId() {
		return definitionId;
	}
	
	public void setDefinitionCode(String definitionCode) {
		this.definitionCode = definitionCode;
	}
	
	public String getDefinitionCode() {
		return definitionCode;
	}
	
	/**
	 * @since 10.1.0
	 */
	public void setConfidential(Boolean confidential) {
		this.confidential = confidential;
	}
	
	/**
	 * @since 10.1.0
	 */
	public Boolean getConfidential() {
		return confidential;
	}
}
