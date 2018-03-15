package eu.bcvsolutions.idm.core.model.entity;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.envers.Audited;

import eu.bcvsolutions.idm.core.api.domain.AutomaticRoleRequestType;
import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.RecursionType;
import eu.bcvsolutions.idm.core.api.domain.RequestOperationType;
import eu.bcvsolutions.idm.core.api.domain.RequestState;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;

/**
 * Entity for request keeps main information about automatic roles and their
 * assignment
 * 
 * @author svandav
 * @since 8.0.0
 *
 */
@Entity
@Table(name = "idm_auto_role_request", indexes = { @Index(name = "idx_idm_auto_role_role_req", columnList = "role_id"),
		@Index(name = "idx_idm_auto_role_name_req", columnList = "name") })
public class IdmAutomaticRoleRequest extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	@Audited
	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "request_type", nullable = false)
	private AutomaticRoleRequestType requestType = AutomaticRoleRequestType.ATTRIBUTE;

	@Audited
	@NotNull
	@Column(name = "state")
	@Enumerated(EnumType.STRING)
	private RequestState state = RequestState.CONCEPT;

	@Audited
	@ManyToOne(optional = true)
	@JoinColumn(name = "auto_role_att_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey(name = "none")
	private IdmAutomaticRole automaticRole;

	@Audited
	@Size(max = DefaultFieldLengths.NAME)
	@Column(name = "name", length = DefaultFieldLengths.NAME, nullable = true)
	private String name;

	@Audited
	@ManyToOne(optional = true)
	@JoinColumn(name = "role_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey(name = "none")
	private IdmRole role;

	@Audited
	@ManyToOne(optional = true)
	@JoinColumn(name = "tree_node_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey(name = "none")
	private IdmTreeNode treeNode;

	@Audited
	@Enumerated(EnumType.STRING)
	@Column(name = "recursion_type", nullable = true)
	private RecursionType recursionType = RecursionType.NO;

	@Audited
	@Size(max = DefaultFieldLengths.NAME)
	@Column(name = "wf_process_id")
	private String wfProcessId;

	@Embedded
	private OperationResult result;

	@Audited
	@NotNull
	@Column(name = "execute_immediately")
	private boolean executeImmediately = false;

	@Audited
	@Size(max = DefaultFieldLengths.DESCRIPTION)
	@Column(name = "description")
	private String description;

	@Audited
	@Enumerated(EnumType.STRING)
	@Column(name = "operation")
	private RequestOperationType operation = RequestOperationType.UPDATE;

	public IdmRole getRole() {
		return role;
	}

	public void setRole(IdmRole role) {
		this.role = role;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getWfProcessId() {
		return wfProcessId;
	}

	public void setWfProcessId(String wfProcessId) {
		this.wfProcessId = wfProcessId;
	}

	public OperationResult getResult() {
		return result;
	}

	public void setResult(OperationResult result) {
		this.result = result;
	}

	public IdmTreeNode getTreeNode() {
		return treeNode;
	}

	public void setTreeNode(IdmTreeNode treeNode) {
		this.treeNode = treeNode;
	}

	public RecursionType getRecursionType() {
		return recursionType;
	}

	public void setRecursionType(RecursionType recursionType) {
		this.recursionType = recursionType;
	}

	public AutomaticRoleRequestType getRequestType() {
		return requestType;
	}

	public void setRequestType(AutomaticRoleRequestType requestType) {
		this.requestType = requestType;
	}

	public RequestState getState() {
		return state;
	}

	public void setState(RequestState state) {
		this.state = state;
	}

	public boolean isExecuteImmediately() {
		return executeImmediately;
	}

	public void setExecuteImmediately(boolean executeImmediately) {
		this.executeImmediately = executeImmediately;
	}

	public IdmAutomaticRole getAutomaticRole() {
		return automaticRole;
	}

	public void setAutomaticRole(IdmAutomaticRole automaticRole) {
		this.automaticRole = automaticRole;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public RequestOperationType getOperation() {
		return operation;
	}

	public void setOperation(RequestOperationType operation) {
		this.operation = operation;
	}

}