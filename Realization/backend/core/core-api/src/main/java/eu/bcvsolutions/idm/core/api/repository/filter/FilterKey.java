package eu.bcvsolutions.idm.core.api.repository.filter;

import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.entity.BaseEntity;

/**
 * Plugable filter builder identifier.
 * 
 * @author Radek Tomiška
 *
 */
public class FilterKey {

	private final Class<? extends BaseEntity> entityClass;
	private final String name;
	
	public FilterKey(Class<? extends BaseEntity> entityClass, String name) {
		Assert.notNull(entityClass);
		Assert.hasLength(name);
		//
		this.entityClass = entityClass;
		this.name = name;
	}
	
	/**
	 * Property in filter - filter will be applied, when property will be set in filtering parameters.
	 * FilterBuilder could read other properties from filter, 
	 * 
	 * @return
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * Filter will be applied for given entity type.
	 * 
	 * @return
	 */
	public Class<? extends BaseEntity> getEntityClass() {
		return entityClass;
	}
	
	
}
