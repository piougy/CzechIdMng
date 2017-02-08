package eu.bcvsolutions.idm.ic.api;

/**
 * Interface for ic facade. Identifies connector by {@link IcConnectorKey} and
 * {@link IcConnectorServer}.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public interface IcConnectorInstance {
	
	IcConnectorKey getConnectorKey();
	
	IcConnectorServer getConnectorServer();
	
	boolean getRemote();
}
