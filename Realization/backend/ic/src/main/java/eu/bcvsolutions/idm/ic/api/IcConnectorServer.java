package eu.bcvsolutions.idm.ic.api;

import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;

/**
 * Interface for work with remote connector server
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
public interface IcConnectorServer {
	
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
	GuardedString getPassword();
	
	/**
	 * Method is for setting password from confidential storage
	 * 
	 * @param password
	 */
	void setPassword(GuardedString password);
	
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
