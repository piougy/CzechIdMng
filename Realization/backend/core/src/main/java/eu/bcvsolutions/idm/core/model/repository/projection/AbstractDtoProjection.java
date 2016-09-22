package eu.bcvsolutions.idm.core.model.repository.projection;

import java.util.Date;

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
}
