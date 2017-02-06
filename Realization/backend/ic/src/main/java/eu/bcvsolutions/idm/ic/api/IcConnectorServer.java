package eu.bcvsolutions.idm.ic.api;

/**
 * Interface for work with remote connector server
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
public interface IcConnectorServer {
	
	/**
	 * Return name of remote connector server
	 * 
	 * @return
	 */
	String getName();
	
	/**
	 * Return host of remote connector server
	 * 
	 * @return
	 */
	String getHost();
	
	/**
	 * Return port for remote connector server
	 * @return
	 */
	int getPort();
	
	/**
	 * Return password for remote connector server
	 * @return
	 */
	String getPassword();
	
	/**
	 * Return flag if remote server use SSL
	 * @return
	 */
	boolean isUseSsl();
	
	/**
	 * Return timeout for remote connector server
	 * @return
	 */
	int getTimeout();
}
