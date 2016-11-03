package eu.bcvsolutions.idm.icf.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.bcvsolutions.idm.icf.api.IcfAttribute;

public class IcfAttributeDto implements IcfAttribute {
	protected String name;
	protected List<Object> values;
	protected boolean multiValue = false;

	public IcfAttributeDto() {
		super();
	}
	
	public IcfAttributeDto(String name, List<Object> values, boolean multiValue) {
		super();
		this.name = name;
		this.values = values;
		this.multiValue = multiValue;
	}

	public IcfAttributeDto(String name, List<Object> values) {
		super();
		this.name = name;
		this.values = values;
		this.multiValue = true;
	}

	public IcfAttributeDto(String name, Object value) {
		super();
		this.name = name;
		this.values = new ArrayList<>();
		if (value != null) {
			this.values.add(value);
		}
		this.multiValue = false;
	}
	

	/**
	 * Property name
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Return single value. Attribute have to set multiValue on false.
	 * 
	 * @return
	 */
	@JsonIgnore
	public Object getValue() {
		if (this.multiValue || (this.values != null && this.values.size() > 1)) {
			throw new IllegalArgumentException("Must be a single value.");
		}
		if (this.values == null || this.values.isEmpty()) {
			return null;
		}
		return this.values.get(0);

	}
	
	/**
	 * Attribute values
	 * 
	 * @return
	 */
	public List<Object> getValues() {

		return values;
	}

	public void setValues(List<Object> values) {
		this.values = values;
	}

	public boolean isMultiValue() {
		return multiValue;
	}

	public void setMultiValue(boolean multiValue) {
		this.multiValue = multiValue;
	}

	@Override
	public String toString() {
		return "IcfAttributeDto [name=" + name + ", values=" + values + "]";
	}

}
