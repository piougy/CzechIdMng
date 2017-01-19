package eu.bcvsolutions.idm.acc.entity;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import org.hibernate.envers.Audited;
import org.hibernate.validator.constraints.NotEmpty;
import org.joda.time.LocalDateTime;

import com.sun.istack.NotNull;

import eu.bcvsolutions.idm.acc.domain.ReconciliationMissingAccountActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationLinkedActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationMissingEntityActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationUnlinkedActionType;
import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.ic.domain.IcFilterOperationType;

/**
 * <i>SysSynchronizationConfig</i> is responsible for keep informations about
 * synchronization configuration
 * 
 * @author svandav
 *
 */
@Entity
@Table(name = "sys_sync_config")
public class SysSynchronizationConfig extends AbstractEntity {

	private static final long serialVersionUID = 6852881356003914520L;

	@Audited
	@NotNull
	@Column(name = "enabled", nullable = false)
	private boolean enabled = true;

	@Audited
	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@Column(name = "name", length = DefaultFieldLengths.NAME, nullable = false, unique = true)
	private String name;

	@Audited
	@Column(name = "description")
	private String description;

	@Audited
	@Column(name = "run_on_server")
	private String runOnServer;

	@Audited
	@NotNull
	@Column(name = "reconciliation", nullable = false)
	private boolean reconciliation = false;

	@Audited
	@NotNull
	@Column(name = "custom_filter", nullable = false)
	private boolean customFilter = false;

	@Audited
	@Lob
	@Column(name = "token")
	private String token;

	@Audited
	@Column(name = "timestamp")
	private LocalDateTime timestamp;

	@Audited
	@Lob
	@Column(name = "custom_filter_script")
	private String customFilterScript;

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

	public String getRunOnServer() {
		return runOnServer;
	}

	public void setRunOnServer(String runOnServer) {
		this.runOnServer = runOnServer;
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

	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
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

}
