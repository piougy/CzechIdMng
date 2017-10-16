package eu.bcvsolutions.idm.core.scheduler.entity;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;

/**
 * Task depends on another task identified by quartz task end.
 * 
 * @author Radek Tomiška
 *
 */
@Entity
@Table(name = "idm_dependent_task_trigger", indexes = {
		@Index(name = "idx_idm_dependent_t_init", columnList = "initiator_task_id"),
		@Index(name = "idx_idm_dependent_t_dep", columnList = "dependent_task_id")})
public class IdmDependentTaskTrigger extends AbstractEntity {
	
	private static final long serialVersionUID = 1L;

	@NotNull
	@Column(name = "initiator_task_id", length = DefaultFieldLengths.NAME)
	private String initiatorTaskId; // quartz job name - default group is supported now
	
	@NotNull
	@Column(name = "dependent_task_id", length = DefaultFieldLengths.NAME)
	private String dependentTaskId; // quartz job name - default group is supported now

	public IdmDependentTaskTrigger() {
	}
	
	public IdmDependentTaskTrigger(UUID id) {
		super(id);
	}
	
	public IdmDependentTaskTrigger(String initiatorTaskId, String dependentTaskId) {
		this.initiatorTaskId = initiatorTaskId;
		this.dependentTaskId = dependentTaskId;
	}
	
	
	public String getInitiatorTaskId() {
		return initiatorTaskId;
	}

	public void setInitiatorTaskId(String initiatorTaskId) {
		this.initiatorTaskId = initiatorTaskId;
	}

	public String getDependentTaskId() {
		return dependentTaskId;
	}

	public void setDependentTaskId(String dependentTaskId) {
		this.dependentTaskId = dependentTaskId;
	}
}
