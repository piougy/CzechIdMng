package eu.bcvsolutions.idm.core.audit.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;

/**
 * Entity logging event exception
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Entity
@Table(name = "logging_event_exception", indexes = { 
		})
public class IdmLoggingEventException implements BaseEntity {
	
	private static final long serialVersionUID = -1409152284267694057L;

	@Id
	@Column(name = "i", nullable = true)
	private Long id;
	
	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(name = "event_id", referencedColumnName = "event_id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private IdmLoggingEvent event;
	
	@Column(name = "trace_line", length = DefaultFieldLengths.NAME, nullable = false)
	private String traceLine;

	public Long getId() {
		return id;
	}

	@Override
	public void setId(Serializable id) {
		this.id = (Long) id;
	}

	public IdmLoggingEvent getEvent() {
		return event;
	}

	public void setEvent(IdmLoggingEvent event) {
		this.event = event;
	}

	public String getTraceLine() {
		return traceLine;
	}

	public void setTraceLine(String traceLine) {
		this.traceLine = traceLine;
	}
}
