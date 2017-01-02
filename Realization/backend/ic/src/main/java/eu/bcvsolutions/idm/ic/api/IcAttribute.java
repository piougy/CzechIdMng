package eu.bcvsolutions.idm.ic.api;

import java.util.List;

/**
 * Basic interface for all IC attributes
 * @author svandav
 *
 */
public interface IcAttribute {

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


	boolean isMultiValue();

}