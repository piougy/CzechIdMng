package eu.bcvsolutions.idm.core.workflow.model.dto;

import java.util.HashMap;
import java.util.Map;

public class FormDataDto {

	private String id;
	private String name;
	private String value;
	private String type;
	private boolean readable;
	private boolean writable;
	private boolean required;
	private String tooltip;
	private String placeholder;
	private Map<String, String> additionalInformations;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean isReadable() {
		return readable;
	}

	public void setReadable(boolean readable) {
		this.readable = readable;
	}

	public boolean isWritable() {
		return writable;
	}

	public void setWritable(boolean writable) {
		this.writable = writable;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public String getTooltip() {
		return tooltip;
	}

	public void setTooltip(String tooltip) {
		this.tooltip = tooltip;
	}

	public String getPlaceholder() {
		return placeholder;
	}

	public void setPlaceholder(String placeholder) {
		this.placeholder = placeholder;
	}

	public Map<String, String> getAdditionalInformations() {
		if(additionalInformations == null){
			additionalInformations = new HashMap<>();
		}
		return additionalInformations;
	}

	public void setAdditionalInformations(Map<String, String> additionalInformations) {
		this.additionalInformations = additionalInformations;
	}

}
