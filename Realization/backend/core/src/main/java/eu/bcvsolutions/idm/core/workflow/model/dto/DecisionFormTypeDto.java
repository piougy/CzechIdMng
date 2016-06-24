package eu.bcvsolutions.idm.core.workflow.model.dto;

public class DecisionFormTypeDto {
	
	private boolean showWarning = false;
	private String id;
	private String level;
	private String label;
	private String tooltip;
	private String premissions;
	
	
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public boolean isShowWarning() {
		return showWarning;
	}
	public void setShowWarning(boolean showWarning) {
		this.showWarning = showWarning;
	}
	public String getLevel() {
		return level;
	}
	public void setLevel(String level) {
		this.level = level;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getTooltip() {
		return tooltip;
	}
	public void setTooltip(String tooltip) {
		this.tooltip = tooltip;
	}
	public String getPremissions() {
		return premissions;
	}
	public void setPremissions(String premissions) {
		this.premissions = premissions;
	}

	
}
