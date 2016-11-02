package eu.bcvsolutions.idm.icf.api;

import java.util.List;

/**
 * Defines type or category of connector object. Unlike {@link IcfObjectClass}
 * describing definitions of attributes.
 * 
 * @author svandav
 *
 */
public interface IcfObjectClassInfo {

	String getType();

	List<IcfAttributeInfo> getAttributeInfos();

	/**
	 * True if this can contain other object classes.
	 */
	boolean isContainer();

	/**
	 * Returns flag indicating whether this is a definition of auxiliary object
	 * class. Auxiliary object classes define additional characteristics of the
	 * object.
	 */
	boolean isAuxiliary();

}