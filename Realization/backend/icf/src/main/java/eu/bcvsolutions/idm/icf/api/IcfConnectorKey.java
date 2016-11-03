package eu.bcvsolutions.idm.icf.api;

public interface IcfConnectorKey {

	String getIcfType();

	String getBundleName();

	String getBundleVersion();

	String getConnectorName();

	int hashCode();

}