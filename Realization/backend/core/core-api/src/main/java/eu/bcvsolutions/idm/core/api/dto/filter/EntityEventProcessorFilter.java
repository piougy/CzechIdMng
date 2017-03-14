package eu.bcvsolutions.idm.core.api.dto.filter;

import java.io.Serializable;

/**
 * Entity event processors filter
 * 
 * @author Radek Tomiška
 *
 */
public class EntityEventProcessorFilter implements BaseFilter {

	Class<? extends Serializable> contentClass;
	
	public Class<? extends Serializable> getContentClass() {
		return contentClass;
	}
	
	public void setContentClass(Class<? extends Serializable> contentClass) {
		this.contentClass = contentClass;
	}
}
