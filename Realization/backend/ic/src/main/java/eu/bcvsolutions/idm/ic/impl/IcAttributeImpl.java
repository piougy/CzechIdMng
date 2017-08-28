package eu.bcvsolutions.idm.ic.impl;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.bcvsolutions.idm.ic.api.IcAttribute;

/**
 * Basic implementation of IC attribute
 * 
 * @author svandav
 *
 */
public class IcAttributeImpl implements IcAttribute {

	private static final long serialVersionUID = 1L;
	protected String name;
	protected List<Object> values;
	protected boolean multiValue = false;

	public IcAttributeImpl() {
		super();
	}

	public IcAttributeImpl(String name, List<Object> values, boolean multiValue) {
		super();
		this.name = name;
		this.values = values;
		this.multiValue = multiValue;
	}

	public IcAttributeImpl(String name, List<Object> values) {
		super();
		this.name = name;
		this.values = values;
		if (values instanceof List) {
			this.multiValue = true;
		}
	}

	public IcAttributeImpl(String name, Object value) {
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
			throw new IllegalArgumentException("Attribute [" + name + "] must be a single value.");
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
		return "IcAttributeImpl [name=" + name + ", values=" + values + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (multiValue ? 1231 : 1237);
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((values == null) ? 0 : values.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		IcAttributeImpl other = (IcAttributeImpl) obj;
		if (multiValue != other.multiValue) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (values == null) {
			if (other.values != null) {
				return false;
			}
		} else if (!values.equals(other.values)) {
			return false;
		}
		return true;
	}

}
