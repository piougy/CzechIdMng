package eu.bcvsolutions.idm.core.event;

import java.io.Serializable;
import java.util.UUID;

import eu.bcvsolutions.idm.core.api.entity.BaseEntity;

/**
 * Test event content
 * 
 * @author Radek Tomiška
 *
 */
public class ConditionalContent implements BaseEntity {

	private static final long serialVersionUID = 1L;
	private final boolean condition;
	
	public ConditionalContent(boolean condition) {
		this.condition = condition;
	}
	
	@Override
	public Serializable getId() {
		return UUID.randomUUID();
	}

	@Override
	public void setId(Serializable id) {
		// nothing
	}
	
	public boolean isCondition() {
		return condition;
	}	
}