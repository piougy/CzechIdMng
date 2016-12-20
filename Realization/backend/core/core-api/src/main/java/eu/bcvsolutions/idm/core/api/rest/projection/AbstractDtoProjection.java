package eu.bcvsolutions.idm.core.api.rest.projection;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Projection for abstract entity
 * 
 * @author Radek Tomiška 
 *
 */
public interface AbstractDtoProjection extends BaseDtoProjection {

	DateTime getCreated();
	
	String getCreator();
	
	String getModifier();
	
	DateTime getModified();	
	
	/**
	 * All projections are considered as trimmed view on entity (does not contain all properties) 
	 * 
	 * @return
	 */
	@JsonProperty("_trimmed")
	@Value("#{true}") 
    boolean isTrimmed();
}
