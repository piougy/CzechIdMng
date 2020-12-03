package eu.bcvsolutions.idm.core.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.SerializationUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.service.impl.DefaultCryptService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Crypt service tests.
 * 
 * @author Ondřej Kopr
 * @author Radek Tomiška
 *
 */
@Transactional
public class DefaultCryptServiceIntegrationTest extends AbstractIntegrationTest {
	
	@Autowired
	private ApplicationContext context;
	
	private DefaultCryptService cryptService;

	@Before
	public void init() {
		cryptService = context.getAutowireCapableBeanFactory().createBean(DefaultCryptService.class);
	}
	
	@Test
	public void encryptAndDecryptStringValueBackwardCompatible() {
		String password = "123456";
		String encryptString = cryptService.encryptString(password);
		
		assertNotEquals(password, encryptString);
		
		String decryptString = cryptService.decryptString(encryptString);
		
		assertNotEquals(decryptString, encryptString);
		assertEquals(decryptString, password);
	}

	@Test
	public void encryptAndDecryptStringValue() {
		String password = "123456";
		byte[] vector = cryptService.generateVector();
		String encryptString = cryptService.encryptString(password, vector);
		
		assertNotEquals(password, encryptString);
		
		String decryptString = cryptService.decryptString(encryptString, vector);
		
		assertNotEquals(decryptString, encryptString);
		assertEquals(decryptString, password);
	}
	
	@Test
	public void encryptAndDecryptStringValue255LongBackwardCompatible() {
		Random random = new Random();
		byte[] password255 = new byte[255];
		random.nextBytes(password255);
		String password = new String(password255);
		
		String encryptString = cryptService.encryptString(password);
		
		assertNotEquals(password, encryptString);
		
		String decryptString = cryptService.decryptString(encryptString);
		
		assertNotEquals(decryptString, encryptString);
		assertEquals(decryptString, password);
	}

	@Test
	public void encryptAndDecryptStringValue255Long() {
		Random random = new Random();
		byte[] password255 = new byte[255];
		random.nextBytes(password255);
		String password = new String(password255);
		byte[] vector = cryptService.generateVector();
		
		String encryptString = cryptService.encryptString(password, vector);
		
		assertNotEquals(password, encryptString);
		
		String decryptString = cryptService.decryptString(encryptString, vector);
		
		assertNotEquals(decryptString, encryptString);
		assertEquals(decryptString, password);
	}
	
	@Test
	public void encryptAndDecryptGuardedStringBackwardCompatible() {
		GuardedString password = new GuardedString("123456");
		
		String encryptString = cryptService.encryptString(password.asString());
		
		assertNotEquals(password.asString(), encryptString);
		
		String decryptString = cryptService.decryptString(encryptString);
		
		assertNotEquals(decryptString, encryptString);
		assertEquals(decryptString, password.asString());
	}

	@Test
	public void encryptAndDecryptGuardedString() {
		GuardedString password = new GuardedString("123456");
		byte[] vector = cryptService.generateVector();
		
		String encryptString = cryptService.encryptString(password.asString(),vector);
		
		assertNotEquals(password.asString(), encryptString);
		
		String decryptString = cryptService.decryptString(encryptString, vector);
		
		assertNotEquals(decryptString, encryptString);
		assertEquals(decryptString, password.asString());
	}
	
	@Test
	public void encryptAndDecryptListBackwardCompatible() {
		List<String> list = new ArrayList<>();
		list.add("0");
		list.add("1");
		list.add("2");
		list.add("3");

		byte[] encrypt = cryptService.encrypt(SerializationUtils.serialize((Serializable) list));

		byte[] decrypt = cryptService.decrypt(encrypt);
		
		List<String> listDeserialize = SerializationUtils.deserialize(decrypt);

		assertEquals(listDeserialize.get(0), list.get(0));
		assertEquals(listDeserialize.get(1), list.get(1));
		assertEquals(listDeserialize.get(2), list.get(2));
		assertEquals(listDeserialize.get(3), list.get(3));
	}

	@Test
	public void encryptAndDecryptList() {
		List<String> list = new ArrayList<>();
		list.add("0");
		list.add("1");
		list.add("2");
		list.add("3");
		byte[] vector = cryptService.generateVector();

		byte[] encrypt = cryptService.encrypt(SerializationUtils.serialize((Serializable) list), vector);

		byte[] decrypt = cryptService.decrypt(encrypt, vector);
		
		List<String> listDeserialize = SerializationUtils.deserialize(decrypt);

		assertEquals(listDeserialize.get(0), list.get(0));
		assertEquals(listDeserialize.get(1), list.get(1));
		assertEquals(listDeserialize.get(2), list.get(2));
		assertEquals(listDeserialize.get(3), list.get(3));
	}
	
	@Test
	public void encryptAndDecryptStringBackwardCompatible() {
		String password = "123456";
		byte [] encrypt = cryptService.encrypt(password.getBytes());
		
		byte [] decryptString = cryptService.decrypt(encrypt);
		
		// defensive while is file not found encrypt is not working
		assertNotEquals(password, new String(encrypt));
		
		assertEquals(password, new String(decryptString));
	}

	@Test
	public void encryptAndDecryptString() {
		String password = "123456";
		byte[] vector = cryptService.generateVector();

		byte [] encrypt = cryptService.encrypt(password.getBytes(), vector);
		
		byte [] decryptString = cryptService.decrypt(encrypt, vector);
		
		// defensive while is file not found encrypt is not working
		assertNotEquals(password, new String(encrypt));
		
		assertEquals(password, new String(decryptString));
	}
	
	@Test
	public void testDecryptWithoutKey() {
		String value = "123456";
		
		byte [] decryptString = cryptService.decryptWithKey(value.getBytes(), null, cryptService.generateVector());
		
		Assert.assertEquals(value, new String(decryptString));
	}
}
