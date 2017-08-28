package eu.bcvsolutions.idm.vs.connector.basic;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.UUID;

import eu.bcvsolutions.idm.ic.api.IcConnectorConfigurationClass;
import eu.bcvsolutions.idm.ic.api.annotation.IcConfigurationClassProperty;
import eu.bcvsolutions.idm.ic.exception.IcException;

/**
 * Configuration for virtual system
 * 
 * @author svandav
 *
 */
public class BasicVirtualConfiguration implements IcConnectorConfigurationClass {

	private static final long serialVersionUID = 1L;

	private String[] attributes = { "firstName", "lastName", "email" };
	private String[] implementers;
	private String[] implementerRoles;
	private boolean resetPasswordSupported = false;
	private boolean disableSupported = true;
	private boolean requiredConfirmation = true;
	private String[] reservedNames = { "uid", "__NAME__", "enable", "__ENABLE__", "__PASSWORD__" };

	@IcConfigurationClassProperty(order = 5, displayName = "Required confirmation by the implementer", helpMessage = "All requests will be solved immediately. None notification will be sent to implementers.")
	public boolean isRequiredConfirmation() {
		return requiredConfirmation;
	}

	public void setRequiredConfirmation(boolean requiredConfirmation) {
		this.requiredConfirmation = requiredConfirmation;
	}

	@IcConfigurationClassProperty(order = 10, required = true, displayName = "Attributes", helpMessage = "Properties for create EAV model.")
	public String[] getAttributes() {
		return attributes;
	}

	public void setAttributes(String[] attributes) {
		this.attributes = attributes;
	}

	@IcConfigurationClassProperty(order = 20, displayName = "Implementers", helpMessage = "For this implementers will be created realization task. Every implementer must be dentity in CzechIdM. Value are UUIDs of identities (multivalue).")
	public String[] getImplementers() {
		return implementers;
	}

	public void setImplementers(String[] implementers) {
		this.implementers = implementers;
	}

	@IcConfigurationClassProperty(order = 30, displayName = "Roles of implementers", helpMessage = "All identity with this roles will be implementers. Every role must be role in CzechIdM. Value are UUIDs of roles (multivalue).")
	public String[] getImplementerRoles() {
		return implementerRoles;
	}

	public void setImplementerRoles(String[] realizatorRoles) {
		this.implementerRoles = realizatorRoles;
	}

	@IcConfigurationClassProperty(order = 40, displayName = "Supports password reset", helpMessage = "Not implemented yet!")
	public boolean isResetPasswordSupported() {
		return resetPasswordSupported;
	}

	public void setResetPasswordSupported(boolean changePasswordSupported) {
		this.resetPasswordSupported = changePasswordSupported;
	}

	@IcConfigurationClassProperty(order = 50, displayName = "Supports account disable/enable", helpMessage = "Not implemented yet!")
	public boolean isDisableSupported() {
		return disableSupported;
	}

	public void setDisableSupported(boolean disableSupported) {
		this.disableSupported = disableSupported;
	}

	@Override
	public void validate() {
		if (this.getAttributes() == null || this.getAttributes().length == 0) {
			throw new IcException("BasicVirtualConfiguration validation problem: Attributes cannost be null or empty!");
		}

		// Validation on reserved attribute names
		for (String name : this.reservedNames) {
			boolean reservedAttributeFound = Arrays.asList(this.getAttributes()).stream()
					.filter(attribute -> attribute.toLowerCase().equals(name.toLowerCase())).findFirst().isPresent();

			if (reservedAttributeFound) {
				throw new IcException(MessageFormat.format(
						"BasicVirtualConfiguration validation problem: Attributes contains [{0}] attribute. This attribute name is reserved!",
						name));
			}
		}

		for (String implementer : this.implementers) {
			try {
				UUID.fromString(implementer);
			} catch (IllegalArgumentException ex) {
				throw new IcException(MessageFormat.format(
						"BasicVirtualConfiguration validation problem: Implementers must contains only String representation of UUID values. Implementer [{0}] cannot be cast to UUID!",
						implementer));
			}
		}
	}

}
