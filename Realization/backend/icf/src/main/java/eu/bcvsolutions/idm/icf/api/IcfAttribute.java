package eu.bcvsolutions.idm.icf.api;

import java.util.List;

import eu.bcvsolutions.idm.security.domain.GuardedString;

public interface IcfAttribute {

	/**
	 * Property name
	 * 
	 * @return
	 */
	String getName();

	/**
	 * Return single value. Attribute have to set multiValue on false.
	 * 
	 * @return
	 */
	Object getValue();

	/**
	 * Attribute values
	 * 
	 * @return
	 */
	List<Object> getValues();

	/**
	 * If is true, then have to this attribute unique for objectClass. Is
	 * <i>user-friendly identifier</i> of an object on a target resource. For
	 * instance, the name of an <code>Account</code> will most often be its
	 * loginName.
	 * 
	 * @return
	 */
	boolean isLogin();

	boolean isMultiValue();

}