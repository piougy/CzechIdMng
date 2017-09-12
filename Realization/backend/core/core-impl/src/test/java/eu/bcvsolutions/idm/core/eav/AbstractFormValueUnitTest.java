package eu.bcvsolutions.idm.core.eav;

import static org.junit.Assert.assertEquals;

import org.joda.time.DateTime;
import org.junit.Test;

import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.test.api.AbstractUnitTest;

/**
 * Form value conversion
 * 
 * TODO: all persistent types
 * 
 * @author Radek Tomiška
 *
 */
public class AbstractFormValueUnitTest extends AbstractUnitTest {

	@Test
	public void testDateValueAsDateTime() {
		IdmFormValueDto formValue = new IdmFormValueDto();
		formValue.setPersistentType(PersistentType.DATETIME);
		
		DateTime current = new DateTime();
		
		formValue.setValue(current);
		
		assertEquals(current, formValue.getValue());
	}
	
	@Test
	public void testDateValueAsDate() {
		IdmFormValueDto formValue = new IdmFormValueDto();
		formValue.setPersistentType(PersistentType.DATETIME);
		
		DateTime current = new DateTime();
		
		formValue.setValue(current.toDate());
		
		assertEquals(current, formValue.getValue());
	}
	
	@Test
	public void testDateValueAsLong() {
		IdmFormValueDto formValue = new IdmFormValueDto();
		formValue.setPersistentType(PersistentType.DATETIME);
		
		DateTime current = new DateTime();
		
		formValue.setValue(current.toDate().getTime());
		
		assertEquals(current, formValue.getValue());
	}
	
	@Test(expected = ResultCodeException.class)
	public void testWrongDate() {
		IdmFormValueDto formValue = new IdmFormValueDto();
		formValue.setPersistentType(PersistentType.DATETIME);
		
		formValue.setValue("wrong");
	}
}
