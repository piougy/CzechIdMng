package eu.bcvsolutions.idm.core.api.dto;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.entity.BaseEntity;

/**
 * Filter builder dto.
 *
 * @author Radek Tomiška
 * @author artem
 * @since 9.7.7
 */
@Relation(collectionRelation = "filterBuilders")
public class FilterBuilderDto extends AbstractComponentDto {

	private static final long serialVersionUID = 1L;
	//
	private Class<? extends BaseEntity> entityClass;
    private Class<?> filterBuilderClass; // filter builder or service class

    public void setEntityClass(Class<? extends BaseEntity> entityClass) {
        this.entityClass = entityClass;
    }

    public Class<? extends BaseEntity> getEntityClass() {
        return entityClass;
    }

    public void setFilterBuilderClass(Class<?> filterBuilderClass) {
        this.filterBuilderClass = filterBuilderClass;
    }

    public Class<?> getFilterBuilderClass() {
        return filterBuilderClass;
    }
}
