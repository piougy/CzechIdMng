package eu.bcvsolutions.idm.core.model.repository.projection;

import java.util.Date;

/**
 * Projection for abstract entity
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
 *
 */
public interface AbstractDtoProjection extends BaseDtoProjection {

	Date getCreated();
	
	String getCreator();
	
	String getModifier();
	
	Date getModified();	
}
