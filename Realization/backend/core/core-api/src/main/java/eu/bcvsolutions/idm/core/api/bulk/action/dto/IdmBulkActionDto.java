package eu.bcvsolutions.idm.core.api.bulk.action.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.core.Ordered;
import org.springframework.hateoas.core.Relation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import eu.bcvsolutions.idm.core.api.dto.AbstractComponentDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Basic DTO for bulk actions. The DTO contains information about bulk
 * actions, filter and parameters.
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 *
 */
@JsonInclude(Include.NON_NULL)
@Relation(collectionRelation = "bulkOperations")
public class IdmBulkActionDto extends AbstractComponentDto implements Ordered {

	private static final long serialVersionUID = 1L;

	@JsonProperty(access = Access.READ_ONLY)
	private String entityClass;
	@JsonProperty(access = Access.READ_ONLY)
	private String dtoClass;
	@JsonProperty(access = Access.READ_ONLY)
	private String filterClass;
	@JsonIgnore
	private transient BaseFilter transformedFilter;
	private Map<String, Object> properties;
	@JsonProperty(access = Access.READ_ONLY)
	private List<String> authorities;
	private Map<String, Object> filter;
	@JsonProperty(access = Access.READ_ONLY)
	private UUID longRunningTaskId;
	private Set<UUID> identifiers;
	private Set<UUID> removeIdentifiers;
	@JsonProperty(access = Access.READ_ONLY)
	private List<IdmFormAttributeDto> formAttributes;
	private boolean showWithoutSelection;
	private boolean showWithSelection;
	@ApiModelProperty(notes = "Action level - decorator only (label with color).")
	private NotificationLevel level;
	@ApiModelProperty(notes = "Action order - order in selectbox on FE.")
	private int order;
	@ApiModelProperty(notes = "Action deletes records. This action will be sorted on the end section on FE.")
	private boolean deleteAction;
	@ApiModelProperty(notes = "Action FE icon. Icon from locale will be used by default.")
	private String icon;
	@ApiModelProperty(notes = "Action will be included in quick buttons on FE. Action can be shown as quick button, when icon is defined (in locale or by icon property).")
	private boolean quickButton = false;

	public UUID getLongRunningTaskId() {
		return longRunningTaskId;
	}

	public void setLongRunningTaskId(UUID longRunningTaskId) {
		this.longRunningTaskId = longRunningTaskId;
	}

	public Map<String, Object> getProperties() {
		if (properties == null) {
			properties = new HashMap<>();
		}
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
		if (identifiers == null) {
			identifiers = new HashSet<>();
		}
		return identifiers;
	}

	public void setIdentifiers(Set<UUID> identifiers) {
		this.identifiers = identifiers;
	}

	public Set<UUID> getRemoveIdentifiers() {
		if (removeIdentifiers == null) {
			removeIdentifiers = new HashSet<>();
		}
		return removeIdentifiers;
	}

	public void setRemoveIdentifiers(Set<UUID> removeIdentifiers) {
		this.removeIdentifiers = removeIdentifiers;
	}

	public List<IdmFormAttributeDto> getFormAttributes() {
		if (formAttributes == null) {
			formAttributes = new ArrayList<>();
		}
		return formAttributes;
	}

	public void setFormAttributes(List<IdmFormAttributeDto> formAttributes) {
		this.formAttributes = formAttributes;
	}
	
	public List<String> getAuthorities() {
		if (authorities == null) {
			authorities = new ArrayList<>();
		}
		return authorities;
	}

	public void setAuthorities(List<String> authorities) {
		this.authorities = authorities;
	}

	public boolean isShowWithoutSelection() {
		return showWithoutSelection;
	}

	public void setShowWithoutSelection(boolean showWithoutSelection) {
		this.showWithoutSelection = showWithoutSelection;
	}

	public boolean isShowWithSelection() {
		return showWithSelection;
	}

	public void setShowWithSelection(boolean showWithSelection) {
		this.showWithSelection = showWithSelection;
	}
	
	public void setLevel(NotificationLevel level) {
		this.level = level;
	}
	
	public NotificationLevel getLevel() {
		return level;
	}

	public String getDtoClass() {
		return dtoClass;
	}

	public void setDtoClass(String dtoClass) {
		this.dtoClass = dtoClass;
	}
	
	/**
	 * Action order.
	 * 
	 * @param order action order
	 * @since 10.6.0
	 */
	public void setOrder(int order) {
		this.order = order;
	}
	
	/**
	 * Action order.
	 * 
	 * @return order action order
	 * @since 10.6.0
	 */
	@Override
	public int getOrder() {
		return order;
	}
	
	/**
	 * Action deletes records. This action will be sorted on the end section on FE.
	 * 
	 * @return false by default. true - action deletes records
	 * @since 10.6.0
	 */
	public boolean isDeleteAction() {
		return deleteAction;
	}
	
	/**
	 * Action deletes records. This action will be sorted on the end section on FE.
	 * 
	 * @param deleteAction true - action deletes records
	 * @since 10.6.0
	 */
	public void setDeleteAction(boolean deleteAction) {
		this.deleteAction = deleteAction;
	}

	/**
	 * Action FE icon. Icon from locale will be used by default.
	 * 
	 * @return configured icon (e.g. fa:plus)
	 * @since 10.6.0
	 */
	public String getIcon() {
		return icon;
	}

	/**
	 * Action FE icon. Icon from locale will be used by default.
	 * 
	 * @param icon configured icon (e.g. fa:plus)
	 * @since 10.6.0
	 */
	public void setIcon(String icon) {
		this.icon = icon;
	}

	/**
	 * Action will be included in quick buttons on FE. 
	 * Action can be shown as quick button, when icon is defined (in locale or by icon property).
	 * 
	 * @return true - action button will be shown, if icon is defined.
	 * @since 10.6.0
	 */
	public boolean isQuickButton() {
		return quickButton;
	}

	/**
	 * Action will be included in quick buttons on FE. 
	 * Action can be shown as quick button, when icon is defined (in locale or by icon property).
	 * 
	 * @param quickAccess true - action button will be shown, if icon is defined.
	 * @since 10.6.0
	 */
	public void setQuickButton(boolean quickButton) {
		this.quickButton = quickButton;
	}
}
