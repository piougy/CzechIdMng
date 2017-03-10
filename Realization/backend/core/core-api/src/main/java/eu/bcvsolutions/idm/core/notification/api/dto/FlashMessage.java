package eu.bcvsolutions.idm.core.notification.api.dto;

import java.io.Serializable;
import java.util.UUID;

import org.joda.time.DateTime;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.ResultModel;

/**
 * Websocket message
 * 
 * @author Radek Tomiška
 *
 */
public class FlashMessage implements BaseDto {

	private UUID id;
	private String title;
	private String message;
	private DateTime date;
	private String key;
	private String level; // TODO: enumeration
	private String position; // TODO: enumeration
	private boolean hidden;	
	private ResultModel model;

	@Override
	public UUID getId() {
		return id;
	}

	@Override
	public void setId(Serializable id) {
		if (id != null) {
			Assert.isInstanceOf(UUID.class, id, "FlashMessage supports only UUID identifier.");
		}
		this.id = (UUID) id;
	}

	public DateTime getDate() {
		return date;
	}

	public void setDate(DateTime date) {
		this.date = date;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getTitle() {
		return title;
	}
	
	public ResultModel getModel() {
		return model;
	}
	
	public void setModel(ResultModel model) {
		this.model = model;
	}
}