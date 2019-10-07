package eu.bcvsolutions.idm.core.api.repository.filter;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
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
		Assert.notNull(entityClass, "Entity class is required for filter builder key construction.");
		Assert.hasLength(name, "Property name is required for filter builder key construction (is used for filter registration).");
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
	
	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof FilterKey)) {
			return false;
		}
		FilterKey that = (FilterKey) o;
		
		EqualsBuilder builder = new EqualsBuilder();
		
		return builder
				.append(entityClass, that.entityClass)
				.append(name, that.name)
				.isEquals();
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.append(entityClass)
				.append(name)
				 .toHashCode();
	}
}
