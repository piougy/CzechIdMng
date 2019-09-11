package eu.bcvsolutions.idm.core.api.dto;

import org.springframework.hateoas.core.Relation;

/**
 * Filter dto
 *
 * @author artem
 */
@Relation(collectionRelation = "filterBuilders")
public class FilterBuilderDto extends AbstractComponentDto {

    //private String name; Declared in super.
    //private String module; Declared in super.
    //private String description; Declared in super.
    private String text;
    private Boolean disabled;
    private String entityType;
    private Class entityClass;
    private String filterBuilderClass;


    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Boolean getDisabled() {
        return disabled;
    }

    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public void setEntityClass(Class entityClass) {
        this.entityClass = entityClass;
    }

    public Class getEntityClass() {
        return entityClass;
    }

    public void setFilterBuilderClass(String filterBuilderClass) {
        this.filterBuilderClass = filterBuilderClass;
    }

    public String getFilterBuilderClass() {
        return filterBuilderClass;
    }
}
