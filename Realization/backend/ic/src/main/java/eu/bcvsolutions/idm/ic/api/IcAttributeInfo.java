package eu.bcvsolutions.idm.ic.api;

/**
 * <i>IcAttributeInfo</i> is meta data responsible for describing an
 * {@link IcAttribute}.
 * 
 * @author svandav
 *
 */
public interface IcAttributeInfo {
	
	public static final String NAME = "__NAME__";
	public static final String PASSWORD = "__PASSWORD__";
	public static final String UID = "__UID__";
	public static final String ENABLE = "__ENABLE__";

	/**
	 * The name of the attribute. This the attribute name as it is known by the
	 * framework. It may be derived from the native attribute name. Or it may be
	 * one of the special names such as __NAME__ or __PASSWORD__.
	 *
	 * @return the name of the attribute its describing.
	 */
	String getName();

	/**
	 * The basic type associated with this attribute. All primitives are
	 * supported.
	 *
	 * @return the native type if uses.
	 */
	String getClassType();

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
	String getNativeName();

	/**
	 * Determines whether this attribute is required for creates.
	 *
	 * @return true if the attribute is required for an object else false.
	 */
	boolean isRequired();

	/**
	 * Determines if this attribute can handle multiple values.
	 *
	 * There is a special case with byte[] since in most instances this denotes
	 * a single object.
	 *
	 * @return true if the attribute is multi-value otherwise false.
	 */
	boolean isMultivalued();

	/**
	 * Determines if the attribute is writable on create.
	 *
	 * @return true if the attribute is writable on create else false.
	 */
	boolean isCreateable();

	/**
	 * Determines if the attribute is writable on update.
	 *
	 * @return true if the attribute is writable on update else false.
	 */
	boolean isUpdateable();

	/**
	 * Determines if the attribute is readable.
	 *
	 * @return true if the attribute is readable else false.
	 */
	boolean isReadable();

	/**
	 * Determines if the attribute is returned by default.
	 *
	 * Indicates if an attribute will be returned during search, sync or get
	 * operations inside a connector object by default. The default value is
	 * <code>true</code>.
	 *
	 * @return false if the attribute should not be returned by default.
	 */
	boolean isReturnedByDefault();

}
