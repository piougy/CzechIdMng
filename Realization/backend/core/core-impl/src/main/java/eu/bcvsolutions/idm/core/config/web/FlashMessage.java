package eu.bcvsolutions.idm.core.config.web;

/**
 * Websocket message
 * 
 * @author Radek Tomiška
 *
 */
public class FlashMessage {

	private String message;

	public FlashMessage() {
	}

	public FlashMessage(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}