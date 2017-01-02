package eu.bcvsolutions.idm.ic.api;

import java.util.List;
import java.util.Map;

/**
 * Schema for connector
 * @author svandav
 *
 */
public interface IcSchema {

	/**
     * Returns the set of object classes that are defined in the schema.
     */
	List<IcObjectClassInfo> getDeclaredObjectClasses();

	/**
	 * Returns map of object classes and supported operations. Key is operation name and value is list of object class names 
	 */
	Map<String, List<String>> getSupportedObjectClassesByOperation();

}