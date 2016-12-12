package eu.bcvsolutions.idm.core.api.dto.filter;

/**
 * Quick filter - "fulltext" search
 * 
 * @author Radek Tomiška
 *
 */
public class QuickFilter implements BaseFilter {
	
	private String text;
	
	public void setText(String text) {
		this.text = text;
	}
	
	public String getText() {
		return text;
	}
}
