package eu.bcvsolutions.idm.core.api.dto.filter;

import eu.bcvsolutions.idm.core.api.entity.BaseEntity;

/**
 * Entity event processors filter
 * 
 * @author Radek Tomiška
 *
 */
public class EntityEventProcessorFilter implements BaseFilter {

	Class<? extends BaseEntity> entityClass;
	
	public void setEntityClass(Class<? extends BaseEntity> entityClass) {
		this.entityClass = entityClass;
	}
	
	/**
	 * processor supports entity class
	 * 
	 * @return
	 */
	public Class<? extends BaseEntity> getEntityClass() {
		return entityClass;
	}
}
