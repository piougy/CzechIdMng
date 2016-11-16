package eu.bcvsolutions.idm.icf.impl;

import java.util.Date;

import eu.bcvsolutions.idm.icf.api.IcfEnabledAttribute;

public class IcfEnabledAttributeImpl extends IcfAttributeImpl implements IcfEnabledAttribute {

	private Boolean enabled;
	private Date enabledDate;
	private Date disabledDate;

	public IcfEnabledAttributeImpl(boolean enable, String name) {
		super(name, enable);
		this.enabled = enable;
	}

	public IcfEnabledAttributeImpl(Boolean enabled, Date enabledDate, Date disabledDate) {
		super();
		this.enabled = enabled;
		this.enabledDate = enabledDate;
		this.disabledDate = disabledDate;
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
		return enabledDate;
	}

	public void setEnabledDate(Date enabledDate) {
		this.enabledDate = enabledDate;
	}

	@Override
	public Date getDisabledDate() {
		return disabledDate;
	}

	public void setDisabledDate(Date disabledDate) {
		this.disabledDate = disabledDate;
	}

}
