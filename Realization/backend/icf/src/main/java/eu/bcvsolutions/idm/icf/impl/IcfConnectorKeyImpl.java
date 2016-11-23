package eu.bcvsolutions.idm.icf.impl;

import eu.bcvsolutions.idm.icf.api.IcfConnectorKey;

/**
 * Uniquely identifies a connector within an installation. Consists of the
 * quadruple (icfType, bundleName, bundleVersion, connectorName)
 */
public class IcfConnectorKeyImpl implements IcfConnectorKey {

	private String framework;
	private String bundleName;
	private String bundleVersion;
	private String connectorName;

	public IcfConnectorKeyImpl() {
		super();
	}

	public IcfConnectorKeyImpl(String framework, String bundleName, String bundleVersion, String connectorName) {
		super();
		this.framework = framework;
		this.bundleName = bundleName;
		this.bundleVersion = bundleVersion;
		this.connectorName = connectorName;
	}

	@Override
	public String getFramework() {
		return framework;
	}

	public void setFramework(String framework) {
		this.framework = framework;
	}

	@Override
	public String getBundleName() {
		return bundleName;
	}

	public void setBundleName(String bundleName) {
		this.bundleName = bundleName;
	}

	@Override
	public String getBundleVersion() {
		return bundleVersion;
	}

	public void setBundleVersion(String bundleVersion) {
		this.bundleVersion = bundleVersion;
	}

	@Override
	public String getConnectorName() {
		return connectorName;
	}

	public void setConnectorName(String connectorName) {
		this.connectorName = connectorName;
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bundleName == null) ? 0 : bundleName.hashCode());
		result = prime * result + ((bundleVersion == null) ? 0 : bundleVersion.hashCode());
		result = prime * result + ((connectorName == null) ? 0 : connectorName.hashCode());
		result = prime * result + ((framework == null) ? 0 : framework.hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IcfConnectorKeyImpl other = (IcfConnectorKeyImpl) obj;
		if (bundleName == null) {
			if (other.bundleName != null)
				return false;
		} else if (!bundleName.equals(other.bundleName))
			return false;
		if (bundleVersion == null) {
			if (other.bundleVersion != null)
				return false;
		} else if (!bundleVersion.equals(other.bundleVersion))
			return false;
		if (connectorName == null) {
			if (other.connectorName != null)
				return false;
		} else if (!connectorName.equals(other.connectorName))
			return false;
		if (framework == null) {
			if (other.framework != null)
				return false;
		} else if (!framework.equals(other.framework))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "IcfConnectorKeyImpl [framework=" + framework + ", bundleName=" + bundleName + ", bundleVersion="
				+ bundleVersion + ", connectorName=" + connectorName + "]";
	}

}
