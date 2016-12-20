package eu.bcvsolutions.idm.icf.impl;

import java.util.ArrayList;

import eu.bcvsolutions.idm.icf.api.IcfLoginAttribute;

/**
 * Is <i>user-friendly identifier</i> of an object on a target resource. For
 * instance, the name of an <code>Account</code> will most often be its
 * loginName.
 * 
 * @author svandav
 *
 */
public class IcfLoginAttributeImpl extends IcfAttributeImpl implements IcfLoginAttribute {

	private final static String NAME = "__UID__";
	protected boolean login = false;

	public IcfLoginAttributeImpl(String login) {
		super();
		this.name = NAME;
		this.login = true;
		this.values = new ArrayList<>();
		if (login != null) {
			this.values.add(login);
		}
		multiValue = false;
	}

	public IcfLoginAttributeImpl(String name, String login) {
		super();
		this.name = name;
		this.login = true;
		this.values = new ArrayList<>();
		if (login != null) {
			this.values.add(login);
		}
		multiValue = false;
	}

	/**
	 * If is true, then have to this attribute unique for objectClass. Is
	 * <i>user-friendly identifier</i> of an object on a target resource. For
	 * instance, the name of an <code>Account</code> will most often be its
	 * loginName.
	 * 
	 * @return
	 */
	@Override
	public boolean isLogin() {
		return login;
	}

	public void setLogin(boolean login) {
		this.login = login;
	}

	@Override
	public String getLoginValue() {
		if (this.multiValue || (this.values != null && this.values.size() > 1)) {
			throw new IllegalArgumentException("Must be a single value.");
		}
		if (this.values == null || this.values.isEmpty()) {
			return null;
		}
		if (!(this.values.get(0) instanceof String)) {
			throw new IllegalArgumentException("Must be a String value.");
		}
		return (String) this.values.get(0);

	}

}
