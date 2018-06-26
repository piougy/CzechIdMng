package eu.bcvsolutions.idm.core.api.dto.filter;

import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.repository.filter.FilterBuilder;
import eu.bcvsolutions.idm.core.api.utils.EntityUtils;

/**
 * Common filter for registrable filters - contains filter parameters as map. 
 * Registered filter builders will have all values available.
 * 
 * TODO: dto lookup + default converters
 * 
 * @see FilterBuilder
 * @author Radek Tomiška
 *
 */
public class DataFilter extends QuickFilter {

	/**
	 * Dto uuid identifier
	 */
	public static final String PARAMETER_ID = BaseEntity.PROPERTY_ID;
	
	/**
	 * Codeable identifier - uuid or code
	 */
	public static final String PARAMETER_CODEABLE_IDENTIFIER = "codeable";
	
	/**
	 * "Quick" search parameter
	 */
	public static final String PARAMETER_TEXT = "text";
	
	/**
	 * Dto class
	 */
	private final Class<? extends BaseDto> dtoClass;
	@JsonIgnore
	protected final MultiValueMap<String, Object> data;
	
	public DataFilter(Class<? extends BaseDto> dtoClass) {
		this(dtoClass, null);
	}
	
	public DataFilter(Class<? extends BaseDto> dtoClass, MultiValueMap<String, Object> data) {
		this.dtoClass = dtoClass;
		this.data = data != null ? data : new LinkedMultiValueMap<>();
	}
	
	/**
	 * Return unmodifiable filtering porerties
	 * 
	 * @return
	 */
	public MultiValueMap<String, Object> getData() {
		return new LinkedMultiValueMap<>(data);
	}
	
	/**
	 * Returns target dto type for this filter
	 * 
	 * @return
	 */
	public Class<? extends BaseDto> getDtoClass() {
		return dtoClass;
	}
	
	/**
	 * Entity identifier
	 * 
	 * @return
	 */
	@Override
	public UUID getId() {
		return EntityUtils.toUuid(data.getFirst(PARAMETER_ID));
	}
	
	@Override
	public void setId(UUID id) {
		data.set(PARAMETER_ID, id);
	}
	
	@Override
	public String getText() {
		return (String) data.getFirst(PARAMETER_TEXT);
	}
	
	@Override
	public void setText(String text) {
		data.set(PARAMETER_TEXT, text);
	}
	
	public String getCodeableIdentifier() {
		return (String) data.getFirst(PARAMETER_CODEABLE_IDENTIFIER);
	}
	
	public void setCodeableIdentifier(String text) {
		data.set(PARAMETER_CODEABLE_IDENTIFIER, text);
	}
}
