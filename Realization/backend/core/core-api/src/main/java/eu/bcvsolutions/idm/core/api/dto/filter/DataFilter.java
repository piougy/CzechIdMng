package eu.bcvsolutions.idm.core.api.dto.filter;

import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.repository.filter.FilterBuilder;

/**
 * Common filter for registrable filters - contains fiter parameters as map. 
 * Registered filters will hava all values available.
 * 
 * TODO: dto lookup + default converters
 * TODO: filter builder
 * 
 * @see FilterBuilder
 * @author Radek Tomiška
 *
 */
public class DataFilter extends QuickFilter {

	protected final MultiValueMap<String, Object> data;
	
	public DataFilter(MultiValueMap<String, Object> data) {
		Assert.notNull(data, "Filtering properties are required!");
		//
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
}
