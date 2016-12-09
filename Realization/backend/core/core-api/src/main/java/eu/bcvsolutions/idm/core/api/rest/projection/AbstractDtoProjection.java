package eu.bcvsolutions.idm.core.api.rest.projection;

import java.util.Date;

import org.springframework.beans.factory.annotation.Value;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Projection for abstract entity
 * 
 * @author Radek Tomiška 
 *
 */
public interface AbstractDtoProjection extends BaseDtoProjection {

	Date getCreated();
	
	String getCreator();
	
	String getModifier();
	
	Date getModified();	
	
	/**
	 * All projections are considered as trimmed view on entity (does not contain all properties) 
	 * 
	 * @return
	 */
	@JsonProperty("_trimmed")
	@Value("#{true}") 
    boolean isTrimmed();
}
