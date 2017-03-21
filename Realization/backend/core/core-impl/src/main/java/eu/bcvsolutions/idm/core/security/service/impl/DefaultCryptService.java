package eu.bcvsolutions.idm.core.security.service.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.security.api.service.CryptService;

/**
 * Default implementation of {@link CryptService}
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Service
public class DefaultCryptService implements CryptService {
	
	public static String DEMO_KEY_FILE_PATH = "eu/bcvsolutions/idm/confidential/demo_key.key";
	public static String KEY_FILE_PATH = "eu/bcvsolutions/idm/confidential/key.key";
	
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
	
	@Override
	public String encryptString(String value) {
		byte[] encryptValue = this.encrypt(value.getBytes());
		return Base64.encodeBase64String(encryptValue);
	}

	@Override
	public String decryptString(String value) {
		byte[] serializableValue = this.decrypt(Base64.decodeBase64(value));
		return new String(serializableValue);
	}
	
	@Override
	public byte[] decrypt(byte[] value) {
		byte[] decryptValue = null;
		try {
			Cipher cipher = initCipher(Cipher.DECRYPT_MODE);
			// cipher is not initialized
			if (cipher == null) {
				throw new ResultCodeException(CoreResultCode.CRYPT_DEMO_KEY_NOT_FOUND, ImmutableMap.of("demoKey", DEMO_KEY_FILE_PATH, "primaryKey", KEY_FILE_PATH));
			}
			decryptValue = cipher.doFinal(value);
		} catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			LOG.error("[DefaultCryptService] Decrypt problem! Password will not be decrypthed! Error: {}", e.getLocalizedMessage());
			throw new ResultCodeException(CoreResultCode.CRYPT_INITIALIZATION_PROBLEM, ImmutableMap.of("algorithm", ALGORITHM), e);
		}
		return decryptValue;
	}
	
	@Override
	public byte[] encrypt(byte[] value) {
		byte[] encryptValue = null;
		try {
			Cipher cipher = initCipher(Cipher.ENCRYPT_MODE);
			// cipher is not initialized
			if (cipher == null) {
				throw new ResultCodeException(CoreResultCode.CRYPT_DEMO_KEY_NOT_FOUND, ImmutableMap.of("demoKey", DEMO_KEY_FILE_PATH, "primaryKey", KEY_FILE_PATH));
			}
			encryptValue = cipher.doFinal(value);
		} catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			LOG.error("[DefaultCryptService] Encrypt problem! Password will not be encrypted! Error: {}", e);
			throw new ResultCodeException(CoreResultCode.CRYPT_INITIALIZATION_PROBLEM, ImmutableMap.of("algorithm", ALGORITHM), e);
		}
		
		return encryptValue;
	}
	
	/**
	 * Method return {@link SecretKey} that defined key from resource file. File name is defined by 
	 * application property
	 * @return
	 * @throws UnsupportedEncodingException 
	 * @throws IOException
	 */
	private SecretKey getKeyFromResource() throws UnsupportedEncodingException {
		List<String> lines = null;
		try {
			// get primary key
			URL fileUrl = ClassLoader.getSystemClassLoader().getResource(KEY_FILE_PATH);
			if (fileUrl == null) {
				LOG.warn("[DefaultCryptService] Using DEMO key! Please create new file with key file: {}", KEY_FILE_PATH);
				// get demo key, primary key doesnt exists
				fileUrl = ClassLoader.getSystemClassLoader().getResource(DEMO_KEY_FILE_PATH);
			}
			if (fileUrl == null) {
				LOG.warn("[DefaultCryptService] Demo key dost exists!");
				return null;
			}
			String keyPath = fileUrl.getPath();
			lines = Files.readAllLines(Paths.get(keyPath));
			if (lines.isEmpty() || lines.size() > 1) {
				LOG.warn("[DefaultCryptService] File with key has more or nothing keys. Size: {} ", lines.size());
				return null;
			}
		} catch (IOException e) {
			LOG.warn("[DefaultCryptService] Problem with load key file!", e);
			throw new ResultCodeException(CoreResultCode.CRYPT_DEMO_KEY_NOT_FOUND, ImmutableMap.of("demoKey", DEMO_KEY_FILE_PATH, "primaryKey", KEY_FILE_PATH), e);
		}
		return new SecretKeySpec(lines.get(0).getBytes(ENCODING), ALGORITHM);
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
			SecretKey key = this.getKeyFromResource();
			if (key == null) {
				return null;
			}
			cipher = Cipher.getInstance(ALGORITHM + "/" + ALGORITHM_MODE + "/" + ALGORITHM_PADDING);
			cipher.init(encryptMode, key, new IvParameterSpec(IV));
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException | UnsupportedEncodingException e) {
			LOG.error("[DefaultCryptService] Cipher can't be initialized!");
			throw new ResultCodeException(CoreResultCode.CRYPT_INITIALIZATION_PROBLEM, ImmutableMap.of("algorithm", ALGORITHM), e);
		}
		return cipher;
	}

	@Override
	public boolean existsKeyFile() {
		URL fileUrl = ClassLoader.getSystemClassLoader().getResource(KEY_FILE_PATH);
		if (fileUrl != null) {
			return true;
		}
		// try to load demo key
		fileUrl = ClassLoader.getSystemClassLoader().getResource(DEMO_KEY_FILE_PATH);
		if (fileUrl != null) {
			LOG.warn("[DefaultCryptService] Using DEMO key! Please create new file with key file: {}", KEY_FILE_PATH);
			return true;
		}
		LOG.warn("[DefaultCryptService] Demo key dost exists!");
		return false;
	}
}
