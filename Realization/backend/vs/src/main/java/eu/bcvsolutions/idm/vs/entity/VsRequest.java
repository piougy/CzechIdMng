package eu.bcvsolutions.idm.vs.entity;

import java.util.Objects;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.envers.Audited;
import org.hibernate.validator.constraints.NotEmpty;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.ic.api.IcConnectorConfiguration;
import eu.bcvsolutions.idm.ic.api.IcConnectorObject;
import eu.bcvsolutions.idm.vs.domain.VsOperationType;
import eu.bcvsolutions.idm.vs.domain.VsRequestState;

/**
 * Single request on virtual system.
 * 
 * @author Svanda
 *
 */
@Entity
@Table(name = "vs_request")
public class VsRequest extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	/**
	 * UID - Unique identification of account
	 */
	@Audited
	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@Column(name = "uid", length = DefaultFieldLengths.NAME, nullable = false)
	private String uid;

	/**
	 * Account is for CzechIdM system
	 */
	@Audited
	@Column(name = "system_id", nullable = false)
	private UUID systemId;

	/**
	 * Account is for specific connector version
	 */
	@Audited
	@NotEmpty
	@Column(name = "connector_key", nullable = false)
	private String connectorKey;

	@Audited
	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "operation_type", nullable = false)
	private VsOperationType operationType;

//	@ManyToOne
//	@JoinColumn(name = "duplicate_to_request_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
//	@SuppressWarnings("deprecation") // jpa FK constraint does not work in
//										// hibernate 4
//	@org.hibernate.annotations.ForeignKey(name = "none")
	
	// Limitation: We can use only one mapping on same entity type. When we
	// using two relations on same entity (duplicant and previous for example),
	// then we have exception with unsaved entity in second relation!
	@Column(name = "duplicate_to_request_id")
	private UUID duplicateToRequest;
	
	@ManyToOne
	@JoinColumn(name = "previous_request_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in
										// hibernate 4
	@org.hibernate.annotations.ForeignKey(name = "none")
	private VsRequest previousRequest;
	
	@Audited
	@Column(name = "connector_conf")
	private IcConnectorConfiguration configuration;

	@Audited
	@Column(name = "connector_object")
	private IcConnectorObject connectorObject;

	@Audited
	@NotNull
	@Column(name = "state")
	@Enumerated(EnumType.STRING)
	private VsRequestState state = VsRequestState.CONCEPT;

	@Audited
	@NotNull
	@Column(name = "execute_immediately")
	private boolean executeImmediately = false;

	public VsOperationType getOperationType() {
		return operationType;
	}

	public void setOperationType(VsOperationType operationType) {
		this.operationType = operationType;
	}

	public UUID getSystemId() {
		return systemId;
	}

	public void setSystemId(UUID systemId) {
		this.systemId = systemId;
	}

	public IcConnectorConfiguration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(IcConnectorConfiguration configuration) {
		this.configuration = configuration;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}


	public IcConnectorObject getConnectorObject() {
		return connectorObject;
	}

	public void setConnectorObject(IcConnectorObject connectorObject) {
		this.connectorObject = connectorObject;
	}

	public VsRequestState getState() {
		return state;
	}

	public void setState(VsRequestState state) {
		this.state = state;
	}

	public boolean isExecuteImmediately() {
		return executeImmediately;
	}

	public void setExecuteImmediately(boolean executeImmediately) {
		this.executeImmediately = executeImmediately;
	}

	public UUID getDuplicateToRequest() {
		return duplicateToRequest;
	}

	public void setDuplicateToRequest(UUID duplicateToRequest) {
		this.duplicateToRequest = duplicateToRequest;
	}

	public VsRequest getPreviousRequest() {
		return previousRequest;
	}

	public void setPreviousRequest(VsRequest previousRequest) {
		this.previousRequest = previousRequest;
	}

	public String getConnectorKey() {
		return connectorKey;
	}

	public void setConnectorKey(String connectorKey) {
		this.connectorKey = connectorKey;
	}

}