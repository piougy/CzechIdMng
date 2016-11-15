package eu.bcvsolutions.idm.icf.api;

import eu.bcvsolutions.idm.security.domain.GuardedString;

/**
 * Attribute keep password value
 * @author svandav
 *
 */
public interface IcfPasswordAttribute extends IcfAttribute{

	/**
	 * Return confidential single value. Attribute have to set multiValue on
	 * false and confidential to true.
	 * 
	 * @return
	 */
	GuardedString getPasswordValue();

}