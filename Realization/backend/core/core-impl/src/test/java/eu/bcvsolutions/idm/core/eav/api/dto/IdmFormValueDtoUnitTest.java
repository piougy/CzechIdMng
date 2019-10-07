package eu.bcvsolutions.idm.core.eav.api.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.test.api.AbstractUnitTest;

/**
 * Form value conversion
 * 
 * TODO: all persistent types
 * 
 * @author Radek Tomiška
 *
 */
public class IdmFormValueDtoUnitTest extends AbstractUnitTest {
	
	@Test
	public void testNullValues() {
		for (PersistentType persistentType : PersistentType.values()) {
			IdmFormValueDto formValue = new IdmFormValueDto();
			formValue.setPersistentType(persistentType);
			formValue.setValue(null);
			//
			Assert.assertNull(formValue.getValue());
			Assert.assertTrue(formValue.isEmpty());
			Assert.assertTrue(formValue.isNull());
		}
	}
	
	@Test
	public void testConstructByAttribute() {
		IdmFormAttributeDto attribute = new IdmFormAttributeDto();
		attribute.setId(UUID.randomUUID());
		attribute.setPersistentType(PersistentType.SHORTTEXT);
		attribute.setConfidential(true);
		//
		IdmFormValueDto formValue = new IdmFormValueDto(attribute);
		//
		Assert.assertEquals(attribute.getId(), formValue.getFormAttribute());
		Assert.assertEquals(attribute.getPersistentType(), formValue.getPersistentType());
		Assert.assertEquals(attribute.isConfidential(), formValue.isConfidential());
		Assert.assertEquals(attribute, DtoUtils.getEmbedded(formValue, IdmFormValueDto.PROPERTY_FORM_ATTRIBUTE));
	}

	@Test
	public void testDateTimeValueAsDateTime() {
		IdmFormValueDto formValue = new IdmFormValueDto();
		formValue.setPersistentType(PersistentType.DATETIME);
		
		ZonedDateTime current = ZonedDateTime.now();
		
		formValue.setValue(current);
		
		Assert.assertEquals(current, formValue.getValue());
	}
	
	@Test
	public void testLocalDateTimeValueAsDate() {
		IdmFormValueDto formValue = new IdmFormValueDto();
		formValue.setPersistentType(PersistentType.DATETIME);
		
		LocalDateTime current = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);
		
		formValue.setValue(Date.from(current.atZone(ZoneId.systemDefault()).toInstant()));
		
		Assert.assertEquals(current.atZone(ZoneId.systemDefault()), formValue.getValue());
	}
	
	@Test
	public void testZonedDateTimeValueAsDate() {
		IdmFormValueDto formValue = new IdmFormValueDto();
		formValue.setPersistentType(PersistentType.DATETIME);
		
		ZonedDateTime current = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);
		
		formValue.setValue(Date.from(current.toInstant()));
		
		Assert.assertEquals(current, formValue.getValue());
	}
	
	@Test
	public void testDateTimeValueAsLong() {
		IdmFormValueDto formValue = new IdmFormValueDto();
		formValue.setPersistentType(PersistentType.DATETIME);
		
		ZonedDateTime current = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);
		
		formValue.setValue(current.toInstant().toEpochMilli());
		
		Assert.assertEquals(current, formValue.getValue());
	}
	
	@Test
	public void testDateTimeValueAsLocalDate() {
		IdmFormValueDto formValue = new IdmFormValueDto();
		formValue.setPersistentType(PersistentType.DATETIME);
		
		ZonedDateTime current = LocalDate.now().atStartOfDay(ZoneId.systemDefault());
		
		formValue.setValue(current.toLocalDate());
		
		Assert.assertEquals(current, formValue.getValue());
	}
	
	@Test
	public void testDateTimeValueAsJodaDateTime() {
		IdmFormValueDto formValue = new IdmFormValueDto();
		formValue.setPersistentType(PersistentType.DATETIME);
		
		ZonedDateTime current = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);
		DateTime currentAsJoda = new DateTime(Date.from(current.toInstant()).getTime());
		
		formValue.setValue(currentAsJoda);
		
		Assert.assertEquals(current, formValue.getValue());
	}
	
	@Test
	public void testDateTimeValueAsJodaLocalDate() {
		IdmFormValueDto formValue = new IdmFormValueDto();
		formValue.setPersistentType(PersistentType.DATETIME);
		
		ZonedDateTime current = LocalDate.now().atStartOfDay(ZoneId.systemDefault());
		org.joda.time.LocalDate currentAsJoda = new org.joda.time.LocalDate(Date.from(current.toInstant()).getTime());
		
		formValue.setValue(currentAsJoda);
		
		Assert.assertEquals(current, formValue.getValue());
	}
	
	@Test
	public void testDateTimeValueAsStringWithTimeZone() {
		IdmFormValueDto formValue = new IdmFormValueDto();
		formValue.setPersistentType(PersistentType.DATETIME);
		//
		ZonedDateTime current = ZonedDateTime.now();		
		
		formValue.setValue(current.toString());
		// time zone default vs. constructed
		Assert.assertTrue(current.isEqual((ZonedDateTime) formValue.getValue()));
	}
	
	@Test
	public void testDateTimeValueAsStringWithUtc() {
		IdmFormValueDto formValue = new IdmFormValueDto();
		formValue.setPersistentType(PersistentType.DATETIME);
		//
		ZonedDateTime current = ZonedDateTime.now(ZoneId.of("UTC"));		
		
		formValue.setValue(current.toString());
		
		Assert.assertEquals(current, formValue.getValue());
	}
	
	@Test
	public void testDateValueAsString() {
		IdmFormValueDto formValue = new IdmFormValueDto();
		formValue.setPersistentType(PersistentType.DATE);
		//
		LocalDate current = LocalDate.now();		
		
		formValue.setValue(current.toString());
		
		Assert.assertEquals(current, ((ZonedDateTime) formValue.getValue()).toLocalDate());
	}
	
	@Test(expected = ResultCodeException.class)
	public void testWrongDate() {
		IdmFormValueDto formValue = new IdmFormValueDto();
		formValue.setPersistentType(PersistentType.DATETIME);
		
		formValue.setValue("wrong");
	}
	
	@Test(expected = ResultCodeException.class)
	public void testWrongDateObject() {
		IdmFormValueDto formValue = new IdmFormValueDto();
		formValue.setPersistentType(PersistentType.DATETIME);
		
		formValue.setValue(new IdmFormValueDto());
	}
	
	@Test
	public void uuidAsString() {
		IdmFormValueDto formValue = new IdmFormValueDto();
		formValue.setPersistentType(PersistentType.UUID);
		UUID uuid = UUID.randomUUID();
		
		formValue.setValue(uuid.toString());
		
		Assert.assertEquals(uuid, formValue.getValue());
	}
	
	@Test
	public void attachmentAsString() {
		IdmFormValueDto formValue = new IdmFormValueDto();
		formValue.setPersistentType(PersistentType.ATTACHMENT);
		UUID uuid = UUID.randomUUID();
		
		formValue.setValue(uuid.toString());
		
		Assert.assertEquals(uuid, formValue.getValue());
	}
	
	@Test
	public void testLongValueAsString() {
		IdmFormValueDto formValue = new IdmFormValueDto();
		formValue.setPersistentType(PersistentType.LONG);
		//
		Long current = 14896357892248L;		
		
		formValue.setValue(current.toString());
		
		Assert.assertEquals(current, formValue.getValue());
	}
	
	@Test
	public void testIntValueAsString() {
		IdmFormValueDto formValue = new IdmFormValueDto();
		formValue.setPersistentType(PersistentType.INT);
		//
		Long current = 14896357892248L;		
		
		formValue.setValue(current.toString());
		
		Assert.assertEquals(current.intValue(), formValue.getValue());
	}
	
	@Test
	public void testCharValue() {
		IdmFormValueDto formValue = new IdmFormValueDto();
		formValue.setPersistentType(PersistentType.CHAR);
		//
		char current = 'a';
		//
		formValue.setValue(current);
		//
		Assert.assertEquals(current, formValue.getValue());
	}
	
	@Test
	public void testCharValueAsObject() {
		IdmFormValueDto formValue = new IdmFormValueDto();
		formValue.setPersistentType(PersistentType.CHAR);
		//
		Character current = 'a';
		//
		formValue.setValue(current);
		//
		Assert.assertEquals(current, formValue.getValue());
	}
	
	@Test
	public void testCharValueAsString() {
		IdmFormValueDto formValue = new IdmFormValueDto();
		formValue.setPersistentType(PersistentType.CHAR);
		//
		Character current = 'a';
		//
		formValue.setValue(current.toString());
		//
		Assert.assertEquals(current, formValue.getValue());
	}
	
	@Test
	public void testBooleanValueAsString() {
		IdmFormValueDto formValue = new IdmFormValueDto();
		formValue.setPersistentType(PersistentType.BOOLEAN);
		//
		Boolean current = Boolean.TRUE;		
		
		formValue.setValue(current.toString());
		Assert.assertEquals(current, formValue.getValue());
		formValue.setValue("ff");
		Assert.assertFalse((Boolean) formValue.getValue());
		formValue.setValue("false");
		Assert.assertFalse((Boolean) formValue.getValue());
		formValue.setValue("TRUE");
		Assert.assertTrue((Boolean) formValue.getValue());
	}
	
	@Test
	public void testCodelistValue() {
		IdmFormValueDto formValue = new IdmFormValueDto();
		formValue.setPersistentType(PersistentType.CODELIST);
		//
		String value = "codeOne";
		//
		formValue.setValue(value);
		//
		Assert.assertEquals(value, formValue.getValue());
		Assert.assertEquals(value, formValue.getShortTextValue());
	}
	
	@Test
	public void testEnumerationValue() {
		IdmFormValueDto formValue = new IdmFormValueDto();
		formValue.setPersistentType(PersistentType.ENUMERATION);
		//
		String value = "codeOne";
		//
		formValue.setValue(value);
		//
		Assert.assertEquals(value, formValue.getValue());
		Assert.assertEquals(value, formValue.getShortTextValue());
	}
}
