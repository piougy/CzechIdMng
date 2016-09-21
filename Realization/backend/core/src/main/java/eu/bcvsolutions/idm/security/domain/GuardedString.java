package eu.bcvsolutions.idm.security.domain;

import java.nio.charset.Charset;
import java.util.Arrays;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.model.service.IdmConfigurationService;

/**
 * Guarded string used for storing sensitive data.
 * 
 * TODO: use for passwords (login, passwords in secured configuration)
 * 
 * @author BCV
 *
 */
public final class GuardedString {

	/**
	 * Proxy string will be shown instead target string value
	 */
	public static final String SECRED_PROXY_STRING = "********";
	/**
	 * Property name supposed to be guarded
	 */
	public static final String[] GUARDED_PROPERTY_NAMES = new String[] {"password", "token"};
	/**
	 * We support utf-8 only
	 */
	private static final Charset CHARSET = Charset.forName("UTF-8");
	/**
	 * Target string value
	 */
	private byte[] value;

	protected GuardedString() {
	}

	public GuardedString(final String value) {
		if (value == null) {
			this.value = new byte[0];
		} else {
			this.value = value.getBytes(CHARSET);
		}
	}

	public GuardedString(final byte[] value) {
		this(value, true);
	}

	public GuardedString(final byte[] value, boolean copyValue) {
		if (!copyValue) {
			this.value = value;
		} else {
			this.value = Arrays.copyOf(value, value.length);
		}
	}

	/**
	 * If this object owns the value, it clears it.
	 */
	public void clear() {
		Arrays.fill(value, (byte) 0);
	}

	public byte[] getValue() {
		return value;
	}

	@Override
	public String toString() {
		return SECRED_PROXY_STRING;
	}

	@Override
	protected void finalize() throws Throwable {
		clear();
	}

	public String asString() {
		return new String(value, CHARSET);
	}

	public byte[] asBytes() {
		return value;
	}

	public static GuardedString concat(GuardedString... gs) {
		int size = 0;
		for (GuardedString g : gs) {
			size += g.asBytes().length;
		}

		byte[] newGs = new byte[size];

		int i = 0;
		for (GuardedString g : gs) {
			System.arraycopy(g.asBytes(), 0, newGs, i, g.asBytes().length);
			i += g.asBytes().length;
		}

		return new GuardedString(newGs, false);
	}

	public GuardedString clone() {
		GuardedString gs = new GuardedString();

		gs.value = new byte[value.length];
		System.arraycopy(value, 0, gs.value, 0, value.length);

		return gs;
	}
	
	public static boolean shouldBeGuarded(String propertyName) {
		Assert.notNull(propertyName, "Property name is required");
		//
		if(CollectionUtils.containsAny(
				Arrays.asList(propertyName.split(IdmConfigurationService.SPLIT_PROPERTY_SEPARATOR)), 
				Arrays.asList(GuardedString.GUARDED_PROPERTY_NAMES))) {
			return true;
		}
		return false;
	}
}
