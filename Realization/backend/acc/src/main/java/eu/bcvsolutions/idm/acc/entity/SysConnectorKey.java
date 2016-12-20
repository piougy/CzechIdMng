package eu.bcvsolutions.idm.acc.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.Size;

import org.hibernate.envers.Audited;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.icf.api.IcfConnectorKey;

/**
 * Connector key persisted with target system
 * 
 * @author Radek Tomiška
 *
 */
@Audited
@Embeddable
public class SysConnectorKey implements IcfConnectorKey, Serializable {

	private static final long serialVersionUID = 1L;

	@Size(max = DefaultFieldLengths.NAME)
	@Column(name = "connector_framework", length = DefaultFieldLengths.NAME)
	private String framework;
	
	@Size(max = DefaultFieldLengths.NAME)
	@Column(name = "connector_name", length = DefaultFieldLengths.NAME)
	private String connectorName;
	
	@Size(max = DefaultFieldLengths.NAME)
	@Column(name = "connector_bundle_name", length = DefaultFieldLengths.NAME)
	private String bundleName;
	
	@Size(max = 30)
	@Column(name = "connector_bundle_version", length = 30)
	private String bundleVersion;
	
	public SysConnectorKey() {
	}
	
	public SysConnectorKey(IcfConnectorKey key) {
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
