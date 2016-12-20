package eu.bcvsolutions.idm.icf.api;

import java.util.Date;

/**
 * Attribute defines if is object enabled or disabled on resource
 * 
 * @author svandav
 *
 */
public interface IcfEnabledAttribute extends IcfAttribute {

	/**
	 *  True indicates the object is enabled; otherwise false.
	 * @return
	 */
	Boolean getEnabled();

	/**
	 * Date of enabled
	 * @return
	 */
	Date getEnabledDate();

	/**
	 * Date of disabled
	 * @return
	 */
	Date getDisabledDate();

}