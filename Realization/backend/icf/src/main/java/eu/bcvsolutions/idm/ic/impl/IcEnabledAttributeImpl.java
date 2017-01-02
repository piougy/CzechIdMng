package eu.bcvsolutions.idm.ic.impl;

import java.util.Date;

import eu.bcvsolutions.idm.ic.api.IcEnabledAttribute;

public class IcEnabledAttributeImpl extends IcAttributeImpl implements IcEnabledAttribute {

	private Boolean enabled;
	private Date enabledDate;
	private Date disabledDate;

	public IcEnabledAttributeImpl(boolean enable, String name) {
		super(name, enable);
		this.enabled = enable;
	}

	public IcEnabledAttributeImpl(Boolean enabled, Date enabledDate, Date disabledDate) {
		super();
		this.enabled = enabled;
		this.enabledDate = enabledDate != null ? (Date) enabledDate.clone() : null;
		this.disabledDate = disabledDate != null ? (Date) disabledDate.clone() : null;
	}

	@Override
	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enable) {
		this.enabled = enable;
	}

	@Override
	public Date getEnabledDate() {
		return enabledDate != null ? (Date) enabledDate.clone() : null;
	}

	public void setEnabledDate(Date enabledDate) {
		this.enabledDate = enabledDate != null ? (Date) enabledDate.clone() : null;
	}

	@Override
	public Date getDisabledDate() {
		return  disabledDate != null ? (Date) disabledDate.clone() : null;
	}

	public void setDisabledDate(Date disabledDate) {
		this.disabledDate = disabledDate != null ? (Date) disabledDate.clone() : null;
	}

}
