package eu.bcvsolutions.idm.icf.api;

public interface IcfLoginAttribute {

	/**
	 * If is true, then have to this attribute unique for objectClass. Is
	 * <i>user-friendly identifier</i> of an object on a target resource. For
	 * instance, the name of an <code>Account</code> will most often be its
	 * loginName.
	 * 
	 * @return
	 */
	boolean isLogin();

	String getLoginValue();

}