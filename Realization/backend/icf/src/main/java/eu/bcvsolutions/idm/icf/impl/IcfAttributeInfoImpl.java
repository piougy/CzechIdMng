package eu.bcvsolutions.idm.icf.impl;

import eu.bcvsolutions.idm.icf.api.IcfAttribute;
import eu.bcvsolutions.idm.icf.api.IcfAttributeInfo;

/**
 * <i>IcfAttributeInfo</i> is meta data responsible for describing an
 * {@link IcfAttribute}.
 * 
 * @author svandav
 *
 */
public class IcfAttributeInfoImpl implements IcfAttributeInfo {

	private String name;
	private String classType;
	private String nativeName;
	private boolean required;
	private boolean multivalued;
	private boolean createable;
	private boolean updateable;
	private boolean readable;
	private boolean returnedByDefault;

	/**
	 * The name of the attribute. This the attribute name as it is known by the
	 * framework. It may be derived from the native attribute name. Or it may be
	 * one of the special names such as __NAME__ or __PASSWORD__.
	 *
	 * @return the name of the attribute its describing.
	 */
	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * The basic type associated with this attribute. All primitives are
	 * supported.
	 *
	 * @return the native type if uses.
	 */
	@Override
	public String getClassType() {
		return classType;
	}

	public void setClassType(String classType) {
		this.classType = classType;
	}

	/**
	 * The native name of the attribute. This is the attribute name as it is
	 * known by the resource. It is especially useful for attributes with
	 * special names such as __NAME__ or __PASSWORD__. In this case the
	 * nativeName will contain the real name of the attribute. The nativeName
	 * may be null. In such a case it is assumed that the native name is the
	 * same as name.
	 *
	 * @return the native name of the attribute its describing.
	 */
	@Override
	public String getNativeName() {
		return nativeName;
	}

	public void setNativeName(String nativeName) {
		this.nativeName = nativeName;
	}

	/**
	 * Determines whether this attribute is required for creates.
	 *
	 * @return true if the attribute is required for an object else false.
	 */
	@Override
	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	/**
	 * Determines if this attribute can handle multiple values.
	 *
	 * There is a special case with byte[] since in most instances this denotes
	 * a single object.
	 *
	 * @return true if the attribute is multi-value otherwise false.
	 */
	@Override
	public boolean isMultivalued() {
		return multivalued;
	}

	public void setMultivalued(boolean multivalued) {
		this.multivalued = multivalued;
	}

	/**
	 * Determines if the attribute is writable on create.
	 *
	 * @return true if the attribute is writable on create else false.
	 */
	@Override
	public boolean isCreateable() {
		return createable;
	}

	public void setCreateable(boolean createable) {
		this.createable = createable;
	}

	/**
	 * Determines if the attribute is writable on update.
	 *
	 * @return true if the attribute is writable on update else false.
	 */
	@Override
	public boolean isUpdateable() {
		return updateable;
	}

	public void setUpdateable(boolean updateable) {
		this.updateable = updateable;
	}

	/**
	 * Determines if the attribute is readable.
	 *
	 * @return true if the attribute is readable else false.
	 */
	@Override
	public boolean isReadable() {
		return readable;
	}

	public void setReadable(boolean readable) {
		this.readable = readable;
	}

	/**
	 * Determines if the attribute is returned by default.
	 *
	 * Indicates if an attribute will be returned during search, sync or get
	 * operations inside a connector object by default. The default value is
	 * <code>true</code>.
	 *
	 * @return false if the attribute should not be returned by default.
	 */
	@Override
	public boolean isReturnedByDefault() {
		return returnedByDefault;
	}

	public void setReturnedByDefault(boolean returnedByDefault) {
		this.returnedByDefault = returnedByDefault;
	}

	@Override
	public String toString() {
		return "IcfSchemaAttributeInfo [name=" + name + ", classType=" + classType + "]";
	}

}
