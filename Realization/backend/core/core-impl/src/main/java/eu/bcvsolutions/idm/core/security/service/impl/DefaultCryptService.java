package eu.bcvsolutions.idm.core.security.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.annotation.PostConstruct;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.Assert;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.service.CryptService;

/**
 * Default implementation of {@link CryptService}
 * 
 * @author Ondrej Kopr
 * 
 */
public class DefaultCryptService implements CryptService {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultCryptService.class);
	/**
	 * Algorithm, mode and block padding
	 */
	private static String ALGORITHM = "AES";
	private static String ALGORITHM_MODE = "CBC";
	private static String ALGORITHM_PADDING = "PKCS5PADDING";
	
	/**
	 * Default encoding
	 */
	private static String ENCODING = "UTF-8";
	/**
	 * Initialization vector - old behavior with static vector
	 * @deprecated @since 10.6.0
	 */
	@Deprecated
	private static byte [] IV = { 48, 104, 118, 113, 103, 116, 51, 114, 107, 54, 51, 57, 108, 121, 119, 101 };
	//
	private final ConfigurableEnvironment env;
	private SecretKey key;
	
	public DefaultCryptService(ConfigurableEnvironment environment) {
		Assert.notNull(environment, "Environment is required.");
		//
		this.env = environment;
	}
	
	@PostConstruct
	private void init() {
		try {
			key = this.getKey();
			// just for key validation ... will be in log
			initCipher(Cipher.DECRYPT_MODE, key, IV);
			initCipher(Cipher.ENCRYPT_MODE, key, IV);
			LOG.info("Initializing Cipher succeeded - Confidetial storage will be crypted.");
		} catch (InvalidKeyException | UnsupportedEncodingException ex) {
			LOG.error("Problem with initializing Cipher. Error: [{}]. Confidetial storage is not crypted! ", ex.getMessage());
		}
	}
	
	@Override
	public String encryptString(String value, byte[] iv) {
		byte[] encryptValue = this.encrypt(value.getBytes(StandardCharsets.UTF_8), iv);
		return Base64.encodeBase64String(encryptValue);
	}

	@Override
	public String encryptString(String value) {
		LOG.warn("IdM use old behavior with static vector. Please don't use this deprecated.");
		return encryptString(value, IV);
	}

	@Override
	public String decryptString(String value, byte[] iv) {
		byte[] serializableValue = this.decrypt(Base64.decodeBase64(value), iv);
		return new String(serializableValue, StandardCharsets.UTF_8);
	}

	@Override
	public String decryptString(String value) {
		LOG.warn("IdM use old behavior with static vector. Please don't use this deprecated.");
		return decryptString(value, IV);
	}
	
	@Override
	public byte[] decrypt(byte[] value) {
		LOG.warn("IdM use old behavior with static vector. Please don't use this deprecated.");
		return decrypt(value, IV);
	}
	
	@Override
	public byte[] decrypt(byte[] value, byte[] iv) {
		return decrypt(value, this.key, iv);
	}
	
	@Override
	public byte[] decryptWithKey(byte[] value, GuardedString guardedKey) {
		LOG.warn("IdM use old behavior with static vector. Please don't use this deprecated.");
		
		return decryptWithKey(value, guardedKey, IV);
	}
	
	@Override
	public byte[] decryptWithKey(byte[] value, GuardedString guardedKey, byte[] iv) {
		Assert.notNull(value, "Value is required.");
		// key is not => value is not encrypted
		if (guardedKey == null) {
			return value;
		}

		return decrypt(value, new SecretKeySpec(guardedKey.asBytes(), ALGORITHM), iv);
	}
	
	private byte[] decrypt(byte[] value, SecretKey key, byte[] iv) {
		Assert.notNull(value, "Value is required.");
		//
		try {
			Cipher decryptCipher = initCipher(Cipher.DECRYPT_MODE, key, iv);
			if (decryptCipher == null) {
				logNotCryptedWarning();
				return value;
			}
			return decryptCipher.doFinal(value);
		} catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
			LOG.error("Decrypt problem! Value will not be decrypthed! Error: {}", e.getLocalizedMessage(), e);
			throw new ResultCodeException(CoreResultCode.CRYPT_INITIALIZATION_PROBLEM, ImmutableMap.of("algorithm", ALGORITHM), e);
		}
	}

	@Override
	public byte[] encrypt(byte[] value) {
		LOG.warn("IdM use old behavior with static vector. Please don't use this deprecated.");
		return encrypt(value, IV);
	}
	
	@Override
	public byte[] encrypt(byte[] value, byte[] iv) {
		try {
			Cipher encryptCipher = initCipher(Cipher.ENCRYPT_MODE, key, iv);
			if (encryptCipher == null) {
				logNotCryptedWarning();
				return value;
			}
			return encryptCipher.doFinal(value);
		} catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException ex) {
			LOG.error("Encrypt problem! Value will not be encrypted! Error:", ex);
			throw new ResultCodeException(CoreResultCode.CRYPT_INITIALIZATION_PROBLEM, ImmutableMap.of("algorithm", ALGORITHM), ex);
		}
	}

	/**
	 * Method return {@link SecretKey} that defined key from resource file. File name is defined by 
	 * application property
	 * 
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	private SecretKey getKey() throws UnsupportedEncodingException {
		String key;
		// try found key in application properties
		key = env.getProperty(APPLICATION_PROPERTIES_KEY);
		if (!Strings.isNullOrEmpty(key)) {
			LOG.info("For crypt confidetial storage will be use key from application properties. Key: [{}]", APPLICATION_PROPERTIES_KEY);
			return new SecretKeySpec(key.getBytes(ENCODING), ALGORITHM);
		}
		// key was not found in application properties, try found application properties path 
		String keyPath = env.getProperty(APPLICATION_PROPERTIES_KEY_PATH);
		if (!Strings.isNullOrEmpty(keyPath)) {
			File keyFile = new File(keyPath);
			if (keyFile.exists()) {
				try (BufferedReader in = new BufferedReader(new FileReader(keyPath))) {
					key = in.readLine();
					if (key == null || key.isEmpty()) {
						LOG.warn("File with key is empty or not found. Key path: [{}].", keyPath);
						return null;
					}
					LOG.info("For crypt confidetial storage will be use key in file: [{}].", APPLICATION_PROPERTIES_KEY_PATH);
					return new SecretKeySpec(key.getBytes(ENCODING), ALGORITHM);
				} catch (IOException e) {
					LOG.warn("Error while read file: [{}], error message: [{}]", keyPath, e.getMessage(), e);
				}
			} else {
				LOG.info("For crypt service is define key with path: [{}], but this file isn't exist.", keyPath);
			}
		}
		logNotCryptedWarning();
		//
		return null;
	}
	/**
	 * warning about confidential storage is not crypted.
	 */
	private void logNotCryptedWarning() {
		LOG.warn("WARNING: Confidential storage isn't crypted, application cannot be used in production!!!"
				+ " Please set one of these application properties: [{}] or [{}]. See documention [{}]", 
				APPLICATION_PROPERTIES_KEY, 
				APPLICATION_PROPERTIES_KEY_PATH,
				// TODO: move to configuration, use version property etc.
				"https://wiki.czechidm.com/devel/documentation/security/dev/confidential-storage");
	}
	
	/**
	 * Method init {@link Cipher} by encrypt mode {@link Cipher}. 
	 * Cipher is not thread safe and is not reusable, has to be constructed for all requests.
	 * 
	 * 
	 * @param encryptMode
	 * @param key
	 * @throws InvalidKeyException
	 */
	private Cipher initCipher(int encryptMode, SecretKey key, byte[] iv) throws InvalidKeyException {
		if (key == null) {
			return null;
		}

		if (iv == null) {
			LOG.warn("Confidential storage value hasn't defined dynamic vector.");
		}

		Cipher cipher = null;
		try {
			cipher = Cipher.getInstance(ALGORITHM + "/" + ALGORITHM_MODE + "/" + ALGORITHM_PADDING);
			cipher.init(encryptMode, key, new IvParameterSpec(iv));
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException e) {
			LOG.error("Cipher can't be initialized!");
			throw new ResultCodeException(CoreResultCode.CRYPT_INITIALIZATION_PROBLEM, ImmutableMap.of("algorithm", ALGORITHM), e);
		}
		return cipher;
	}
}
