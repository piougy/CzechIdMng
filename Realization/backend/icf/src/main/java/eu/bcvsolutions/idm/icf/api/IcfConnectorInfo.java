package eu.bcvsolutions.idm.icf.api;

public interface IcfConnectorInfo {

	String getConnectorDisplayName();

	String getConnectorCategory();

	IcfConnectorKey getConnectorKey();

}