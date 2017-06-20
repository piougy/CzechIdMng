package eu.bcvsolutions.idm.test.api;

import org.junit.After;
import org.junit.Ignore;

/**
 * Unit test will use mockito and junit test framework. Verifies mock interactions after each test automatically.
 * 
 * @author Radek Tomiška 
 *
 */
@Ignore
public abstract class AbstractVerifiableUnitTest extends AbstractUnitTest {

	/**
	 * Verifies that all mocks had only interactions specified by test.
	 * 
	 * @throws Exception if something is broken
	 */
	@After
	public void verifyMocks() throws Exception {
		super.verifyMocks();
	}
}
