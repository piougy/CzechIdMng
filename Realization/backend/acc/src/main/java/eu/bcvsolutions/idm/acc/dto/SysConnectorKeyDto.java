package eu.bcvsolutions.idm.acc.dto;

import java.io.Serializable;

import javax.persistence.Embeddable;

import eu.bcvsolutions.idm.acc.entity.SysConnectorKey;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.ic.api.IcConnectorKey;

/**
 * DTO for  {@link SysConnectorKey}
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@Embeddable
public class SysConnectorKeyDto implements IcConnectorKey, Serializable {

	private static final long serialVersionUID = 430337513097070131L;

	private String framework;
	private String connectorName;
	private String bundleName;
	private String bundleVersion;
	
	public SysConnectorKeyDto() {
	}
	
	public SysConnectorKeyDto(IcConnectorKey key) {
		this.framework = key.getFramework();
		this.connectorName = key.getConnectorName();
		this.bundleName = key.getBundleName();
		this.bundleVersion = key.getBundleVersion();
	}

	public String getFramework() {
		return framework;
	}

	public void setFramework(String framework) {
		this.framework = framework;
	}

	public String getConnectorName() {
		return connectorName;
	}

	public void setConnectorName(String connectorName) {
		this.connectorName = connectorName;
	}

	public String getBundleName() {
		return bundleName;
	}

	public void setBundleName(String bundleName) {
		this.bundleName = bundleName;
	}

	public String getBundleVersion() {
		return bundleVersion;
	}

	public void setBundleVersion(String bundleVersion) {
		this.bundleVersion = bundleVersion;
	}

}
