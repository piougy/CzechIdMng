package eu.bcvsolutions.idm.icf.dto;

import eu.bcvsolutions.idm.icf.api.IcfConnectorKey;

/**
 * Uniquely identifies a connector within an installation. Consists of the
 * quadruple (icfType, bundleName, bundleVersion, connectorName)
 */
public class IcfConnectorKeyDto implements IcfConnectorKey {

	public IcfConnectorKeyDto() {
		super();
	}
	
	public IcfConnectorKeyDto(String icfType, String bundleName, String bundleVersion, String connectorName) {
		super();
		this.icfType = icfType;
		this.bundleName = bundleName;
		this.bundleVersion = bundleVersion;
		this.connectorName = connectorName;
	}

	private String icfType;
	private String bundleName;
	private String bundleVersion;
	private String connectorName;
	
	@Override
	public String getIcfType() {
		return icfType;
	}

	public void setIcfType(String icfType) {
		this.icfType = icfType;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bundleName == null) ? 0 : bundleName.hashCode());
		result = prime * result + ((bundleVersion == null) ? 0 : bundleVersion.hashCode());
		result = prime * result + ((connectorName == null) ? 0 : connectorName.hashCode());
		result = prime * result + ((icfType == null) ? 0 : icfType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IcfConnectorKeyDto other = (IcfConnectorKeyDto) obj;
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
		if (icfType == null) {
			if (other.icfType != null)
				return false;
		} else if (!icfType.equals(other.icfType))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "IcfConnectorKeyDto [icfType=" + icfType + ", bundleName=" + bundleName + ", bundleVersion="
				+ bundleVersion + ", connectorName=" + connectorName + "]";
	}


}
