package eu.bcvsolutions.idm.icf.dto;

import java.util.ArrayList;

import org.identityconnectors.framework.api.operations.CreateApiOp;
import org.identityconnectors.framework.api.operations.DeleteApiOp;
import org.identityconnectors.framework.api.operations.GetApiOp;
import org.identityconnectors.framework.api.operations.SearchApiOp;
import org.identityconnectors.framework.api.operations.UpdateApiOp;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.Schema;
import org.identityconnectors.framework.common.objects.Uid;

import eu.bcvsolutions.idm.icf.api.IcfUidAttribute;

/**
 * A single-valued attribute that represents the <i>unique identifier</i> of an
 * object within the name-space of the target resource. If possible, this unique
 * identifier also should be immutable.
 * <p/>
 * When an application creates an object on a target resource, the
 * {@link CreateApiOp#create create} operation returns as its result the
 * <code>Uid</code> of the created object. An application also can use the
 * {@link SearchApiOp#search search} operation to discover the <code>Uid</code>
 * value for an existing object. An application must use the <code>Uid</code>
 * value to identify the object in any subsequent call to
 * {@link GetApiOp#getObject get}, {@link DeleteApiOp#delete delete} or
 * {@link UpdateApiOp#update update} that object. See the documentation for
 * {@link Name} for comparison.
 * <p/>
 * Ideally, the value of <code>Uid</code> would be a <i>Globally Unique
 * IDentifier (GUID)</i>. However, not every target resource provides a globally
 * unique and immutable identifier for each of its objects. For some connector
 * implementations, therefore, the <code>Uid</code> value is only <i>locally</i>
 * unique and may change when an object is modified. For instance, an LDAP
 * directory service that lacks GUID might use <i>Distinguished Name (DN)</i> as
 * the <code>Uid</code> for each object. A connector that represents each object
 * as a row in a database table might use the value of the <i>primary key</i> as
 * the <code>Uid</code> of an object. The fact that changing an object might
 * change its <code>Uid</code> is the reason that {@link UpdateApiOp#update
 * update} returns <code>Uid</code>.
 * <p/>
 * {@link Uid} by definition must be a single-valued attribute. Its value must
 * always convert to a string, regardless of the underlying type of the native
 * identifier on the target. The string value of any native id must be
 * canonical.
 * <p/>
 * Uid is never allowed to appear in the {@link Schema}, nor may Uid appear in
 * the attribute set of a {@link CreateApiOp#create create} operation. This is
 * because Uid is not a true attribute of an object, but rather a reference to
 * that object. Uid extends {@link Attribute} only so that Uid can be searchable
 * and compatible with the filter translators.
 */
public class IcfUidAttributeDto extends IcfAttributeDto implements IcfUidAttribute {

	private final String revision;

	public IcfUidAttributeDto(String name, String uid, String revision) {
		super();
		this.name = name;
		this.values = new ArrayList<>();
		if (uid != null) {
			this.values.add(uid);
		}
		multiValue = false;
		this.revision = revision;
	}

	 /**
     * Obtain a string representation of the value of this attribute, which
     * value uniquely identifies a {@link ConnectorObject object} on the target
     * resource.
     *
     * @return value that uniquely identifies an object.
     */
	@Override
	public String getUidValue() {
		if (this.multiValue || (this.values != null && this.values.size() > 1)) {
			throw new IllegalArgumentException("Must be a single value.");
		}
		if (this.values == null || this.values.isEmpty()) {
			return null;
		}
		if (!(this.values.get(0) instanceof String)) {
			throw new IllegalArgumentException("Must be a String value.");
		}
		return (String) this.values.get(0);

	}

	@Override
	public String getRevision() {
		return this.revision;
	}

}
