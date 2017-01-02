package eu.bcvsolutions.idm.ic.api;

/**
 * Is <i>user-friendly identifier</i> of an object on a target resource. For
 * instance, the name of an <code>Account</code> will most often be its
 * loginName.
 * 
 * @author svandav
 *
 */
public interface IcLoginAttribute extends IcAttribute{

	/**
	 * If is true, then have to this attribute unique for objectClass. Is
	 * <i>user-friendly identifier</i> of an object on a target resource. For
	 * instance, the name of an <code>Account</code> will most often be its
	 * loginName.
	 * 
	 * @return
	 */
	boolean isLogin();

	/**
	 * Value of login in specific connector object
	 * @return
	 */
	String getLoginValue();

}