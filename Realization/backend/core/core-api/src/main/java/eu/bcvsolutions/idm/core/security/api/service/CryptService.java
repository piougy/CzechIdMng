package eu.bcvsolutions.idm.core.security.api.service;

import java.util.Base64;

/**
 * Service for encrypt and decrypt string values.
 * Encrypt is based as AES with key from resources and IV (initialization vector).
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 */
public interface CryptService {
	
	static String APPLICATION_PROPERTIES_KEY = "cipher.crypt.secret.key";
	static String APPLICATION_PROPERTIES_KEY_PATH = "cipher.crypt.secret.keyPath";
	
	/**
	 * Encrypt string value by {@link Base64} and method encodeBase64String
	 * 
	 * @param value
	 * @return
	 */
	String encryptString(String value);
	
	/**
	 * Decrypt string value with {@link Base64} and method Base64
	 * 
	 * @param value
	 * @return
	 */
	String decryptString(String value);
	
	/**
	 * Basic encrypt byte array with key from resource and initialization vector define by this class.
	 * 
	 * @param value
	 * @return
	 */
	byte[] encrypt(byte[] value);
	
	/**
	 * Basic decrypt byte array with key from resoruce and initialization vector define by this class.
     *
	 * @param value
	 * @return
	 */
	byte[] decrypt(byte[] value);
}
