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
public class TestContentTwo implements BaseEntity {

	private static final long serialVersionUID = 1L;
	private String text;

	@Override
	public Serializable getId() {
		return UUID.randomUUID();
	}

	@Override
	public void setId(Serializable id) {
		// nothing
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	public String getText() {
		return text;
	}
}