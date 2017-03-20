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

import eu.bcvsolutions.idm.core.security.api.service.CryptService;

/**
 * Default implementation of {@link CryptService}
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Service
public class DefaultEncryptService implements CryptService {
	
	public static String DEMO_KEY_FILE_PATH = "eu/bcvsolutions/idm/core/confidential/demo_key.key";
	public static String KEY_FILE_PATH = "eu/bcvsolutions/idm/core/confidential/key.key";
	
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
	private static byte [] IV = { 98, 99, 118, 115, 111, 108, 117, 116, 105, 111, 110, 115, 101, 99, 117, 114 };
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultEncryptService.class);
	
	@Override
	public String encryptString(String value) {
		byte[] encryptValue = this.encrypt(value.getBytes());
		if (encryptValue == null) {
			return null;
		}
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
			decryptValue = cipher.doFinal(value);
		} catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			LOG.error("[DefaultEncryptService] Decrypt problem! Password will not be decrypthed! Error: {}", e.getLocalizedMessage());
			return null;
		}
		return decryptValue;
	}
	
	@Override
	public byte[] encrypt(byte[] value) {
		byte[] encryptValue = null;
		try {
			Cipher cipher = initCipher(Cipher.ENCRYPT_MODE);
			encryptValue = cipher.doFinal(value);
		} catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			LOG.error("[DefaultEncryptService] Encrypt problem! Password will not be encrypted! Error: {}", e);
			return null;
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
			URL fileUrl = this.getClass().getClassLoader().getResource(KEY_FILE_PATH);
			if (fileUrl == null) {
				LOG.warn("[DefaultEncryptService] Using DEMO key! Please create new file with key and put it on this path: {}", KEY_FILE_PATH);
				fileUrl = this.getClass().getClassLoader().getResource(DEMO_KEY_FILE_PATH);
			}
			if (fileUrl == null) {
				LOG.warn("[DefaultEncryptService] Demo key dost exists! Password will be saved in plaintext!");
				return null;
			}
			String keyPath = fileUrl.getPath();
			lines = Files.readAllLines(Paths.get(keyPath));
			if (lines.isEmpty() || lines.size() > 1) {
				LOG.warn("[DefaultEncryptService] File with key has more or nothing keys. Size: {} ", lines.size());
				return null;
			}
		} catch (IOException e) {
			LOG.warn("[DefaultEncryptService] Problem with load key file! Password will be saved in plaintext!", e);
			return null;
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
			LOG.error("[DefaultEncryptService] Cipher cant be initialized! Password will not be encrypted!", e);
		}
		return cipher;
	}

	@Override
	public boolean existsKeyFile() {
		try {
			if (getKeyFromResource() != null) {
				return true;
			}
			return false;
		} catch (UnsupportedEncodingException e) {
			return false;
		}
	}
}
