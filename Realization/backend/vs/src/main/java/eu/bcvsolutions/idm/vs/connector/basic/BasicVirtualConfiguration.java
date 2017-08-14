package eu.bcvsolutions.idm.vs.connector.basic;

import eu.bcvsolutions.idm.ic.api.IcConnectorConfigurationClass;
import eu.bcvsolutions.idm.ic.api.annotation.IcConfigurationClassProperty;

/**
 * Configuration for virtual system
 * 
 * @author svandav
 *
 */
public class BasicVirtualConfiguration implements IcConnectorConfigurationClass {

	String attributes = "firstName, lastName";
	String implementers;
	String implementerRoles;
	boolean resetPasswordSupported = false;
	boolean disableSupported = true;
	boolean onlyNotification = false;

	@IcConfigurationClassProperty(order = 10, required = true, displayName = "Attributes", helpMessage = "Properties for create EAV model. Values must be split by comma.")
	public String getAttributes() {
		return attributes;
	}

	public void setAttributes(String attributes) {
		this.attributes = attributes;
	}

	@IcConfigurationClassProperty(order = 20, displayName = "Implementers", helpMessage = "For this implementers will be created realization tasks. Every implementer must be dentity in CzechIdM. Value are UUIDs of identities split by comma.")
	public String getImplementers() {
		return implementers;
	}

	public void setImplementers(String realizators) {
		this.implementers = realizators;
	}

	@IcConfigurationClassProperty(order = 30, displayName = "Roles of implementers", helpMessage = "All identity with this roles will be implementers. Every role must be role in CzechIdM. Value are UUIDs of roles split by comma.")
	public String getImplementerRoles() {
		return implementerRoles;
	}

	public void setImplementerRoles(String realizatorRoles) {
		this.implementerRoles = realizatorRoles;
	}

	@IcConfigurationClassProperty(order = 40, displayName = "Password reset supported", helpMessage = "Not implemented yet!")
	public boolean isResetPasswordSupported() {
		return resetPasswordSupported;
	}

	public void setResetPasswordSupported(boolean changePasswordSupported) {
		this.resetPasswordSupported = changePasswordSupported;
	}

	@IcConfigurationClassProperty(order = 50, displayName = "Account disable supported", helpMessage = "Not implemented yet!")
	public boolean isDisableSupported() {
		return disableSupported;
	}

	public void setDisableSupported(boolean disableSupported) {
		this.disableSupported = disableSupported;
	}

	@IcConfigurationClassProperty(order = 60, displayName = "Only notification", helpMessage = "None implementers task will be crated. Only message will be send. - Not implemented yet!")
	public boolean isOnlyNotification() {
		return onlyNotification;
	}

	public void setOnlyNotification(boolean onlyNotification) {
		this.onlyNotification = onlyNotification;
	}

	@Override
	public void validate() {
		// TODO Validation
	}

}
