package eu.bcvsolutions.idm.core.api.bulk.operation.dto;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.hateoas.core.Relation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;

/**
 * Basic DTO for bulk operation. The DTO contains information about bulk
 * operation and parameters.
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@Relation(collectionRelation = "bulkOperations")
public class IdmBulkOperationDto implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name;
	private String module;
	private String entityClass;
	private String filterClass;
	@JsonIgnore
	private transient BaseFilter filter;
	@JsonProperty(access = Access.WRITE_ONLY)
	private Map<String, Object> properties;
	@JsonProperty(access = Access.READ_ONLY)
	private UUID longRunningTaskId;
	private Set<String> identifiers;
	private Set<String> removeIdentifiers;

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

	public BaseFilter getFilter() {
		return filter;
	}

	public void setFilter(BaseFilter filter) {
		this.filter = filter;
	}

	public Set<String> getIdentifiers() {
		return identifiers;
	}

	public void setIdentifiers(Set<String> identifiers) {
		this.identifiers = identifiers;
	}

	public Set<String> getRemoveIdentifiers() {
		return removeIdentifiers;
	}

	public void setRemoveIdentifiers(Set<String> removeIdentifiers) {
		this.removeIdentifiers = removeIdentifiers;
	}
}
