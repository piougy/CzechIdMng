package eu.bcvsolutions.idm.core.security.api.service;

import java.util.Base64;

import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;

/**
 * Service for encrypt and decrypt string values.
 * Encrypt is based as AES with key from resources and IV (initialization vector).
 * 
 * @author Ondrej Kopr
 */
public interface CryptService {
	
	String APPLICATION_PROPERTIES_KEY = "cipher.crypt.secret.key";
	String APPLICATION_PROPERTIES_KEY_PATH = "cipher.crypt.secret.keyPath";
	
	/**
	 * Encrypt string value by {@link Base64} and method encodeBase64String
	 * 
	 * @param value
	 * @deprecated method used old behavior with static vector, please use method {@link CryptService#encryptString(String, byte[])}
	 * @return
	 */
	@Deprecated
	String encryptString(String value);

	/**
	 * Encrypt string value by {@link Base64} and method encodeBase64String
	 * 
	 * @param value
	 * @param iv
	 * @return
	 */
	String encryptString(String value, byte[] iv);

	/**
	 * Decrypt string value with {@link Base64} and method Base64
	 * 
	 * @param value
	 * @deprecated method used old behavior with static vector, please use method {@link CryptService#decryptString(String, byte[])}
	 * @return
	 */
	@Deprecated
	String decryptString(String value);

	/**
	 * Decrypt string value with {@link Base64} and method Base64
	 *
	 * @param value
	 * @param iv
	 * @return
	 */
	String decryptString(String value, byte[] iv);

	/**
	 * Basic encrypt byte array with key from resource and initialization vector define by this class.
	 * 
	 * @param value
	 * @deprecated method used old behavior with static vector, please use method {@link CryptService#encrypt(byte[], byte[])}
	 * @return
	 */
	@Deprecated
	byte[] encrypt(byte[] value);

	/**
	 * Basic encrypt byte array with key from resource and initialization vector define by this class.
	 *
	 * @param value
	 * @param iv
	 * @return
	 */
	byte[] encrypt(byte[] value, byte[] iv);

	/**
	 * Basic decrypt byte array with key from resoruce and initialization vector define by this class.
     *
	 * @param value
	 * @deprecated method used old behavior with static vector, please use method {@link CryptService#decrypt(byte[], byte[])}
	 * @return
	 */
	@Deprecated
	byte[] decrypt(byte[] value);

	/**
	 * Basic decrypt byte array with key from resoruce and initialization vector define by this class.
	 *
	 * @param value
	 * @param iv
	 * @return
	 */
	byte[] decrypt(byte[] value, byte[] iv);

	/**
	 * Decrypt given value with given {@link GuardedString}. Method will be used for change confidential storage key.
	 * If key will be null method use key classic confidential key defined in application properties or file.
	 *
	 * @param value
	 * @deprecated method used old behavior with static vector, please use method {@link CryptService#decryptWithKey(byte[], GuardedString, byte[])}
	 * @param key
	 * @return
	 */
	@Deprecated
	byte[] decryptWithKey(byte[] value, GuardedString key);

	/**
	 * Decrypt given value with given {@link GuardedString}. Method will be used for change confidential storage key.
	 * If key will be null method use key classic confidential key defined in application properties or file.
	 *
	 * @param value
	 * @param key
	 * @param iv
	 * @return
	 */
	byte[] decryptWithKey(byte[] value, GuardedString key, byte[] iv);
}
