package eu.bcvsolutions.idm.core.api.dto.filter;

import java.util.UUID;

/**
 * Quick filter - "fulltext" search
 * 
 * @author Radek Tomiška
 */
public class QuickFilter implements BaseFilter {
	
	private UUID id;
	private String text;
	
	/**
	 * Entity identifier
	 * 
	 * @return
	 */
	public UUID getId() {
		return id;
	}
	
	public void setId(UUID id) {
		this.id = id;
	}
	
	/**
	 * Quick text
	 * 
	 * @param text
	 */
	public void setText(String text) {
		this.text = text;
	}
	
	public String getText() {
		return text;
	}
}
