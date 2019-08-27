package eu.bcvsolutions.idm.core.api.domain;

/**
 * Interface for entity with own log
 *
 * @author svandav
 */
public interface Loggable {
	

	String LOG_SEPARATOR = "-------------------------";

	String addToLog(String text);

}