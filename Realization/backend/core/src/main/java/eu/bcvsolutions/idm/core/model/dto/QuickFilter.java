package eu.bcvsolutions.idm.core.model.dto;

public class QuickFilter implements BaseFilter {
	
	private String text;
	
	public void setText(String text) {
		this.text = text;
	}
	
	public String getText() {
		return text;
	}
}
