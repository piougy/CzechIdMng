package eu.bcvsolutions.idm.core.api.bulk.action.dto;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.hateoas.core.Relation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;

/**
 * Basic DTO for bulk actions. The DTO contains information about bulk
 * actions, filter and parameters.
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@Relation(collectionRelation = "bulkOperations")
public class IdmBulkActionDto implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name;
	@JsonProperty(access = Access.READ_ONLY)
	private String module;
	@JsonProperty(access = Access.READ_ONLY)
	private String entityClass;
	@JsonProperty(access = Access.READ_ONLY)
	private String filterClass;
	@JsonIgnore
	private transient BaseFilter transformedFilter;
	@JsonProperty(access = Access.WRITE_ONLY)
	private Map<String, Object> properties;
	@JsonProperty(access = Access.READ_ONLY)
	private Map<String, BasePermission[]> permissions;
	@JsonProperty(access = Access.WRITE_ONLY)
	private Map<String, Object> filter;
	@JsonProperty(access = Access.READ_ONLY)
	private UUID longRunningTaskId;
	@JsonProperty(access = Access.WRITE_ONLY)
	private Set<UUID> identifiers;
	@JsonProperty(access = Access.WRITE_ONLY)
	private Set<UUID> removeIdentifiers;
	@JsonProperty(access = Access.READ_ONLY)
	private List<IdmFormAttributeDto> formAttributes;

	public UUID getLongRunningTaskId() {
		return longRunningTaskId;
	}

	public void setLongRunningTaskId(UUID longRunningTaskId) {
		this.longRunningTaskId = longRunningTaskId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	public Map<String, Object> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}

	public String getEntityClass() {
		return entityClass;
	}

	public void setEntityClass(String entityClass) {
		this.entityClass = entityClass;
	}

	public String getFilterClass() {
		return filterClass;
	}

	public void setFilterClass(String filterClass) {
		this.filterClass = filterClass;
	}

	public BaseFilter getTransformedFilter() {
		return transformedFilter;
	}

	public void setTransformedFilter(BaseFilter transformedFilter) {
		this.transformedFilter = transformedFilter;
	}

	public Map<String, Object> getFilter() {
		return filter;
	}

	public void setFilter(Map<String, Object> filter) {
		this.filter = filter;
	}

	public Set<UUID> getIdentifiers() {
		return identifiers;
	}

	public void setIdentifiers(Set<UUID> identifiers) {
		this.identifiers = identifiers;
	}

	public Set<UUID> getRemoveIdentifiers() {
		return removeIdentifiers;
	}

	public void setRemoveIdentifiers(Set<UUID> removeIdentifiers) {
		this.removeIdentifiers = removeIdentifiers;
	}

	public List<IdmFormAttributeDto> getFormAttributes() {
		return formAttributes;
	}

	public void setFormAttributes(List<IdmFormAttributeDto> formAttributes) {
		this.formAttributes = formAttributes;
	}
	
	public Map<String, BasePermission[]> getPermissions() {
		return permissions;
	}

	public void setPermissions(Map<String, BasePermission[]> permissions) {
		this.permissions = permissions;
	}
}
