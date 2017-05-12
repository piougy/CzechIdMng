package eu.bcvsolutions.idm.core.api.dto.filter;

import java.util.UUID;

import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.repository.filter.FilterBuilder;

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
	 * Dto identifier
	 */
	public static final String PARAMETER_ID = "id";
	
	/**
	 * Dto class
	 */
	private final Class<? extends BaseDto> dtoClass;
	@JsonIgnore
	protected final MultiValueMap<String, Object> data;
	
	public DataFilter(Class<? extends BaseDto> dtoClass, MultiValueMap<String, Object> data) {
		Assert.notNull(data, "Filtering properties are required!");
		//
		this.dtoClass = dtoClass;
		this.data = data;
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
		return (UUID) data.getFirst(PARAMETER_ID);
	}
	
	@Override
	public void setId(UUID id) {
		data.set(PARAMETER_ID, id);
	}
}
