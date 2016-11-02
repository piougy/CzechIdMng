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

	boolean isContainer();

	boolean isAuxiliary();

}