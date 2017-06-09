package eu.bcvsolutions.idm.ic.api;

import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;

/**
 * Attribute keep password value
 * @author svandav
 *
 */
public interface IcPasswordAttribute extends IcAttribute{

	/**
	 * Return confidential single value. Attribute have to set multiValue on
	 * false and confidential to true.
	 * 
	 * @return
	 */
	GuardedString getPasswordValue();

}