package eu.bcvsolutions.idm.icf.dto;

import eu.bcvsolutions.idm.icf.api.IcfConfigurationProperty;

/**
 * Elementary configuration property for connector
 * @author svandav
 *
 */
public class IcfConfigurationPropertyDto implements IcfConfigurationProperty {

	
	
	public IcfConfigurationPropertyDto() {
		super();
	}

	public IcfConfigurationPropertyDto(String name, Object value) {
		super();
		this.name = name;
		this.value = value;
		if(value != null){
			this.type = value.getClass().getName();
		}
	}

	/**
     * The unique name of the configuration property.
     */
    private String name;

    /**
     * The help message from the message catalog.
     */
    private String helpMessage;

    /**
     * The display name for this configuration property.
     */
    private String displayName;

    /**
     * Name of the group for this configuration property.
     */
    private String group;

    /**
     * The value from the property. This value should be the default value.
     */
    private Object value;

    /**
     * The type of the property.
     */
    private String type;

    /**
     * Confidential property whose value should be encrypted by the
     * application when persisted?
     */
    private boolean confidential;

    private boolean required;

	/**
	 * @return the name
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the helpMessage
	 */
	@Override
	public String getHelpMessage() {
		return helpMessage;
	}

	/**
	 * @param helpMessage the helpMessage to set
	 */
	public void setHelpMessage(String helpMessage) {
		this.helpMessage = helpMessage;
	}

	/**
	 * @return the displayName
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * @param displayName the displayName to set
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	/**
	 * @return the group
	 */
	@Override
	public String getGroup() {
		return group;
	}

	/**
	 * @param group the group to set
	 */
	public void setGroup(String group) {
		this.group = group;
	}

	/**
	 * @return the value
	 */
	@Override
	public Object getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(Object value) {
		this.value = value;
	}

	/**
	 * @return the type
	 */
	@Override
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the confidential
	 */
	@Override
	public boolean isConfidential() {
		return confidential;
	}

	/**
	 * @param confidential the confidential to set
	 */
	public void setConfidential(boolean confidential) {
		this.confidential = confidential;
	}

	/**
	 * @return the required
	 */
	@Override
	public boolean isRequired() {
		return required;
	}

	/**
	 * @param required the required to set
	 */
	public void setRequired(boolean required) {
		this.required = required;
	}

	

//    /**
//     * Set of operations for which this property must be specified.
//     *
//     * This is used for the case where a connector may or may not implement
//     * certain operations depending in the configuration. The default value of
//     * "empty array" is special in that it means that this property is
//     * applicable to all operations.
//     */
//    private Set<Class<? extends APIOperation>> getOperations();
    
    

}
