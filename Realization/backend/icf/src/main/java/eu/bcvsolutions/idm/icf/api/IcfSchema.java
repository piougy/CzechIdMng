package eu.bcvsolutions.idm.icf.api;

import java.util.List;
import java.util.Map;

/**
 * Schema for connector
 * @author svandav
 *
 */
public interface IcfSchema {

	/**
     * Returns the set of object classes that are defined in the schema.
     */
	List<IcfObjectClassInfo> getDeclaredObjectClasses();

	/**
	 * Returns map of object classes and supported operations. Key is operation name and value is list of object class names 
	 */
	Map<String, List<String>> getSupportedObjectClassesByOperation();

}