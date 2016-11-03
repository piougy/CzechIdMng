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


	boolean isMultiValue();

}