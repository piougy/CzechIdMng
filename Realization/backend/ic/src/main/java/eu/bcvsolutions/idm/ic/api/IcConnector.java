package eu.bcvsolutions.idm.ic.api;

public interface IcConnector {

	public void init(IcConnectorConfiguration configuration);

	public IcConnectorConfiguration getDefaultConfiguration();

}
