package eu.bcvsolutions.idm.icf.dto;

import java.util.ArrayList;
import java.util.List;

import eu.bcvsolutions.idm.icf.api.IcfAttribute;

public class IcfAttributeDto implements IcfAttribute {
	protected String name;
	protected List<Object> values;
	protected boolean login = false;
	protected boolean multiValue = false;

	public IcfAttributeDto() {
		super();
	}
	
	public IcfAttributeDto(String name, List<Object> values, boolean login, boolean multiValue) {
		super();
		this.name = name;
		this.values = values;
		this.login = login;
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
	
	public IcfAttributeDto(String login) {
		super();
		this.values = new ArrayList<>();
		if (login != null) {
			this.values.add(login);
		}
		this.login = true;
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

	/**
	 * If is true, then have to this attribute unique for objectClass. Is
	 * <i>user-friendly identifier</i> of an object on a target resource. For
	 * instance, the name of an <code>Account</code> will most often be its
	 * loginName.
	 * 
	 * @return
	 */
	public boolean isLogin() {
		return login;
	}

	public void setLogin(boolean login) {
		this.login = login;
	}

	public boolean isMultiValue() {
		return multiValue;
	}

	public void setMultiValue(boolean multiValue) {
		this.multiValue = multiValue;
	}

}
