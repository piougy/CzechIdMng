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
public class TestContent implements BaseEntity {

	private static final long serialVersionUID = 1L;
	private String text;
	private Integer suspend;
	private Integer close;

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
	
	public void setSuspend(Integer suspend) {
		this.suspend = suspend;
	}
	
	public Integer getSuspend() {
		return suspend;
	}
	
	public Integer getClose() {
		return close;
	}
	
	public void setClose(Integer close) {
		this.close = close;
	}
}