package eu.bcvsolutions.idm.ic.api;

import java.util.Date;

/**
 * Attribute defines if is object enabled or disabled on resource
 * 
 * @author svandav
 *
 */
public interface IcEnabledAttribute extends IcAttribute {

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