package eu.bcvsolutions.idm.ic.api;

import java.util.List;

/**
 * Defines type or category of connector object. Unlike {@link IcObjectClass}
 * describing definitions of attributes.
 * 
 * @author svandav
 *
 */
public interface IcObjectClassInfo {
	public static final String ACCOUNT = "__ACCOUNT__";
	public static final String GROUP = "__GROUP__";

	String getType();

	List<IcAttributeInfo> getAttributeInfos();

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
