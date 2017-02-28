package eu.bcvsolutions.idm.ic.api;

/**
 * Interface for ic facade. Identifies connector by {@link IcConnectorKey} and
 * {@link IcConnectorServer}.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public interface IcConnectorInstance {
	
	/**
	 * Default server delimitier
	 */
	public static char SERVER_NAME_DELIMITER = ':';
	
	/**
	 * Return connector key. Connector key define connector version, framework and etc.
	 * 
	 * @return
	 */
	IcConnectorKey getConnectorKey();
	
	/**
	 * Return connector server. Connector server define server with connectors.
	 * 
	 * @return
	 */
	IcConnectorServer getConnectorServer();
	
	/**
	 * This flag defined if connector is on remote server or is local.
	 * 
	 * @return
	 */
	boolean isRemote();
}
