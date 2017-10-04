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
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.Assert;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.security.api.service.CryptService;

/**
 * Default implementation of {@link CryptService}
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * 
 *
 */
public class DefaultCryptService implements CryptService {	
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
	 * Initialization vector
	 */
	private static byte [] IV = { 48, 104, 118, 113, 103, 116, 51, 114, 107, 54, 51, 57, 108, 121, 119, 101 };
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultCryptService.class);
	private final ConfigurableEnvironment env;
	private static Cipher encryptCipher;
	private static Cipher decryptCipher;
	
	public DefaultCryptService(ConfigurableEnvironment env) {
		Assert.notNull(env);
		//
		this.env = env;
	}
	
	@PostConstruct
	private void init() {
		try {
			decryptCipher = initCipher(Cipher.DECRYPT_MODE);
			encryptCipher = initCipher(Cipher.ENCRYPT_MODE);
			LOG.info("Initializing Cipher succeeded - Confidetial storage will be crypted.");
		} catch (InvalidKeyException e) {
			decryptCipher = null;
			encryptCipher = null;
			LOG.error("Problem with initializing Cipher. Error: [{}]. Confidetial storage is not crypted! ",e.getMessage());
		}
	}
	
	@Override
	public String encryptString(String value) {
		byte[] encryptValue = this.encrypt(value.getBytes(StandardCharsets.UTF_8));
		return Base64.encodeBase64String(encryptValue);
	}

	@Override
	public String decryptString(String value) {
		byte[] serializableValue = this.decrypt(Base64.decodeBase64(value));
		return new String(serializableValue, StandardCharsets.UTF_8);
	}
	
	@Override
	public byte[] decrypt(byte[] value) {
		byte[] decryptValue = null;
		// cipher isn't initialized
		if (decryptCipher == null) {
			return value;
		}
		//
		try {
			decryptValue = decryptCipher.doFinal(value);
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			LOG.error("Decrypt problem! Password will not be decrypthed! Error: {}", e.getLocalizedMessage());
			throw new ResultCodeException(CoreResultCode.CRYPT_INITIALIZATION_PROBLEM, ImmutableMap.of("algorithm", ALGORITHM), e);
		}
		return decryptValue;
	}
	
	@Override
	public byte[] encrypt(byte[] value) {
		byte[] encryptValue = null;
		// cipher isn't initialized
		if (encryptCipher == null) {
			return value;
		}
		//
		try {
			encryptValue = encryptCipher.doFinal(value);
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			LOG.error("Encrypt problem! Password will not be encrypted! Error: {}", e);
			throw new ResultCodeException(CoreResultCode.CRYPT_INITIALIZATION_PROBLEM, ImmutableMap.of("algorithm", ALGORITHM), e);
		}
		
		return encryptValue;
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
		BufferedReader in = null;
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
				try {
					in = new BufferedReader(new FileReader(keyPath));
					key = in.readLine();
					if (key == null || key.isEmpty()) {
						LOG.warn("File with key is empty or not found. Key path: [{}].", keyPath);
						return null;
					}
					LOG.info("For crypt confidetial storage will be use key in file: [{}].", APPLICATION_PROPERTIES_KEY_PATH);
					return new SecretKeySpec(key.getBytes(ENCODING), ALGORITHM);
				} catch (IOException e) {
					LOG.warn("Error while read file: [{}], error message: [{}]", keyPath, e.getMessage(), e);
				} finally {
					IOUtils.closeQuietly(in);
				}
			} else {
				LOG.info("For crypt service is define key with path: [{}], but this file isn't exist.", keyPath);
			}
		}
		LOG.warn("WARNING: Confidential storage isn't crypted, application cannot be used in production!!!"
				+ " Please set one of these application properties: [{}] or [{}]. See documention [{}]", 
				APPLICATION_PROPERTIES_KEY, 
				APPLICATION_PROPERTIES_KEY_PATH,
				// TODO: move to configuration, use version property etc.
				"https://wiki.czechidm.com/devel/dev/security/confidential-storage");
		return null;
	}
	
	/**
	 * Method init {@link Cipher} by encrypt mode {@link Cipher}
	 * @param encryptMode
	 * @param key
	 * @throws InvalidKeyException
	 */
	private Cipher initCipher(int encryptMode) throws InvalidKeyException {
		Cipher cipher = null;
		try {
			SecretKey key = this.getKey();
			if (key == null) {
				return null;
			}
			cipher = Cipher.getInstance(ALGORITHM + "/" + ALGORITHM_MODE + "/" + ALGORITHM_PADDING);
			cipher.init(encryptMode, key, new IvParameterSpec(IV));
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException | UnsupportedEncodingException e) {
			LOG.error("Cipher can't be initialized!");
			throw new ResultCodeException(CoreResultCode.CRYPT_INITIALIZATION_PROBLEM, ImmutableMap.of("algorithm", ALGORITHM), e);
		}
		return cipher;
	}
}
