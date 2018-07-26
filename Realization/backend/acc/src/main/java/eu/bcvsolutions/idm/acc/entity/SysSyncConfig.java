package eu.bcvsolutions.idm.acc.entity;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.validator.constraints.NotEmpty;

import eu.bcvsolutions.idm.acc.domain.ReconciliationMissingAccountActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationLinkedActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationMissingEntityActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationUnlinkedActionType;
import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.ic.domain.IcFilterOperationType;

/**
 * <i>SysSyncConfig</i> is responsible for keep informations about
 * synchronization configuration.
 *
 * @author svandav
 *
 */
@Entity
@Table(name = "sys_sync_config", indexes = {
		@Index(name = "ux_sys_s_config_name", columnList = "name,system_mapping_id", unique = true),
		@Index(name = "idx_sys_s_config_mapping", columnList = "system_mapping_id"),
		@Index(name = "idx_sys_s_config_correl", columnList = "correlation_attribute_id"),
		@Index(name = "idx_sys_s_config_token", columnList = "token_attribute_id"),
		@Index(name = "idx_sys_s_config_filter", columnList = "filter_attribute_id")
		})
@Inheritance(strategy = InheritanceType.JOINED)
public class SysSyncConfig extends AbstractEntity {

	private static final long serialVersionUID = 6852881356003914520L;

	@Audited
	@NotNull
	@Column(name = "enabled", nullable = false)
	private boolean enabled = true;

	@Audited
	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@Column(name = "name", length = DefaultFieldLengths.NAME, nullable = false)
	private String name;

	@Audited
	@Size(max = DefaultFieldLengths.DESCRIPTION)
	@Column(name = "description", length = DefaultFieldLengths.DESCRIPTION)
	private String description;

	@Audited
	@NotNull
	@Column(name = "reconciliation", nullable = false)
	private boolean reconciliation = false;

	@Audited
	@NotNull
	@Column(name = "custom_filter", nullable = false)
	private boolean customFilter = false;

	@NotAudited // token isn't audited any more
	@Type(type = "org.hibernate.type.StringClobType") // TODO: test on oracle/ mysql
	@Column(name = "token")
	private String token;

	@Audited
	@Type(type = "org.hibernate.type.StringClobType") // TODO: test on oracle/ mysql
	@Column(name = "custom_filter_script")
	private String customFilterScript;
	
	@Audited
	@Type(type = "org.hibernate.type.StringClobType") // TODO: test on oracle/ mysql
	@Column(name = "roots_filter_script")
	private String rootsFilterScript;

	@Audited
	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(name = "correlation_attribute_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in
										// hibernate 4
	@org.hibernate.annotations.ForeignKey(name = "none")
	private SysSystemAttributeMapping correlationAttribute;
	
	@Audited
	@ManyToOne(optional = true)
	@JoinColumn(name = "token_attribute_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in
										// hibernate 4
	@org.hibernate.annotations.ForeignKey(name = "none")
	private SysSystemAttributeMapping tokenAttribute;
	
	@Audited
	@ManyToOne(optional = true)
	@JoinColumn(name = "filter_attribute_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in
										// hibernate 4
	@org.hibernate.annotations.ForeignKey(name = "none")
	private SysSystemAttributeMapping filterAttribute;
	
	@Audited
	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "filter_operation", nullable = false)
	private IcFilterOperationType filterOperation = IcFilterOperationType.GREATER_THAN;
	
	@Audited
	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(name = "system_mapping_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in
										// hibernate 4
	@org.hibernate.annotations.ForeignKey(name = "none")
	private SysSystemMapping systemMapping;

	@Audited
	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "linked_action", nullable = false)
	private SynchronizationLinkedActionType linkedAction = SynchronizationLinkedActionType.UPDATE_ENTITY;

	@Audited
	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "unlinked_action", nullable = false)
	private SynchronizationUnlinkedActionType unlinkedAction = SynchronizationUnlinkedActionType.LINK_AND_UPDATE_ACCOUNT;

	@Audited
	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "missing_entity_action", nullable = false)
	private SynchronizationMissingEntityActionType missingEntityAction = SynchronizationMissingEntityActionType.CREATE_ENTITY;

	@Audited
	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "missing_account_action", nullable = false)
	private ReconciliationMissingAccountActionType missingAccountAction = ReconciliationMissingAccountActionType.IGNORE;
	
	@Audited
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@Column(name = "linked_action_wf", length = DefaultFieldLengths.NAME)
	private String linkedActionWfKey;
	
	@Audited
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@Column(name = "unlinked_action_wf", length = DefaultFieldLengths.NAME)
	private String unlinkedActionWfKey;
	
	@Audited
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@Column(name = "missing_entity_action_wf", length = DefaultFieldLengths.NAME)
	private String missingEntityActionWfKey;
	
	@Audited
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@Column(name = "missing_account_action_wf", length = DefaultFieldLengths.NAME)
	private String missingAccountActionWfKey;


	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isReconciliation() {
		return reconciliation;
	}

	public void setReconciliation(boolean reconciliation) {
		this.reconciliation = reconciliation;
	}

	public boolean isCustomFilter() {
		return customFilter;
	}

	public void setCustomFilter(boolean customFilter) {
		this.customFilter = customFilter;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public SysSystemAttributeMapping getCorrelationAttribute() {
		return correlationAttribute;
	}

	public void setCorrelationAttribute(SysSystemAttributeMapping correlationAttribute) {
		this.correlationAttribute = correlationAttribute;
	}

	public String getCustomFilterScript() {
		return customFilterScript;
	}

	public void setCustomFilterScript(String customFilterScript) {
		this.customFilterScript = customFilterScript;
	}

	public SysSystemMapping getSystemMapping() {
		return systemMapping;
	}

	public void setSystemMapping(SysSystemMapping systemMapping) {
		this.systemMapping = systemMapping;
	}

	public SynchronizationLinkedActionType getLinkedAction() {
		return linkedAction;
	}

	public void setLinkedAction(SynchronizationLinkedActionType linkedAction) {
		this.linkedAction = linkedAction;
	}

	public SynchronizationUnlinkedActionType getUnlinkedAction() {
		return unlinkedAction;
	}

	public void setUnlinkedAction(SynchronizationUnlinkedActionType unlinkedAction) {
		this.unlinkedAction = unlinkedAction;
	}

	public SynchronizationMissingEntityActionType getMissingEntityAction() {
		return missingEntityAction;
	}

	public void setMissingEntityAction(SynchronizationMissingEntityActionType missingEntityAction) {
		this.missingEntityAction = missingEntityAction;
	}

	public ReconciliationMissingAccountActionType getMissingAccountAction() {
		return missingAccountAction;
	}

	public void setMissingAccountAction(ReconciliationMissingAccountActionType missingAccountAction) {
		this.missingAccountAction = missingAccountAction;
	}

	public SysSystemAttributeMapping getTokenAttribute() {
		return tokenAttribute;
	}

	public void setTokenAttribute(SysSystemAttributeMapping tokenAttribute) {
		this.tokenAttribute = tokenAttribute;
	}

	public SysSystemAttributeMapping getFilterAttribute() {
		return filterAttribute;
	}

	public void setFilterAttribute(SysSystemAttributeMapping filterAttribute) {
		this.filterAttribute = filterAttribute;
	}

	public IcFilterOperationType getFilterOperation() {
		return filterOperation;
	}

	public void setFilterOperation(IcFilterOperationType filterOperation) {
		this.filterOperation = filterOperation;
	}

	public String getLinkedActionWfKey() {
		return linkedActionWfKey;
	}

	public void setLinkedActionWfKey(String linkedActionWfKey) {
		this.linkedActionWfKey = linkedActionWfKey;
	}

	public String getUnlinkedActionWfKey() {
		return unlinkedActionWfKey;
	}

	public void setUnlinkedActionWfKey(String unlinkedActionWfKey) {
		this.unlinkedActionWfKey = unlinkedActionWfKey;
	}

	public String getMissingEntityActionWfKey() {
		return missingEntityActionWfKey;
	}

	public void setMissingEntityActionWfKey(String missingEntityActionWfKey) {
		this.missingEntityActionWfKey = missingEntityActionWfKey;
	}

	public String getMissingAccountActionWfKey() {
		return missingAccountActionWfKey;
	}

	public void setMissingAccountActionWfKey(String missingAccountActionWfKey) {
		this.missingAccountActionWfKey = missingAccountActionWfKey;
	}

	public String getRootsFilterScript() {
		return rootsFilterScript;
	}

	public void setRootsFilterScript(String rootsFilterScript) {
		this.rootsFilterScript = rootsFilterScript;
	}

}
