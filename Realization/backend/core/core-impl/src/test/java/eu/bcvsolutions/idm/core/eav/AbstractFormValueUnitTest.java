package eu.bcvsolutions.idm.core.eav;

import static org.junit.Assert.assertEquals;

import org.joda.time.DateTime;
import org.junit.Test;

import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.model.entity.eav.IdmIdentityFormValue;
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
		IdmIdentityFormValue formValue = new IdmIdentityFormValue();
		formValue.setPersistentType(PersistentType.DATE);
		
		DateTime current = new DateTime();
		
		formValue.setValue(current);
		
		assertEquals(current, formValue.getValue());
	}
	
	@Test
	public void testDateValueAsDate() {
		IdmIdentityFormValue formValue = new IdmIdentityFormValue();
		formValue.setPersistentType(PersistentType.DATE);
		
		DateTime current = new DateTime();
		
		formValue.setValue(current.toDate());
		
		assertEquals(current, formValue.getValue());
	}
	
	@Test
	public void testDateValueAsLong() {
		IdmIdentityFormValue formValue = new IdmIdentityFormValue();
		formValue.setPersistentType(PersistentType.DATE);
		
		DateTime current = new DateTime();
		
		formValue.setValue(current.toDate().getTime());
		
		assertEquals(current, formValue.getValue());
	}
	
	@Test(expected = ResultCodeException.class)
	public void testWrongDate() {
		IdmIdentityFormValue formValue = new IdmIdentityFormValue();
		formValue.setPersistentType(PersistentType.DATE);
		
		formValue.setValue("wrong");
	}
}
