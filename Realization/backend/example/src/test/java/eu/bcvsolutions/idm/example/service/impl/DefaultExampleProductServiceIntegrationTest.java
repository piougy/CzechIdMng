package eu.bcvsolutions.idm.example.service.impl;

import static org.junit.Assert.assertEquals;

import java.util.UUID;

import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;

import eu.bcvsolutions.idm.example.dto.ExampleProductDto;
import eu.bcvsolutions.idm.example.dto.filter.ExampleProductFilter;
import eu.bcvsolutions.idm.example.service.api.ExampleProductService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Example product service tests
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultExampleProductServiceIntegrationTest extends AbstractIntegrationTest {
	
	@Autowired private ApplicationContext context;
	//
	private ExampleProductService service;

	@Before
	public void init() {
		service = context.getAutowireCapableBeanFactory().createBean(DefaultExampleProductService.class);
	}
	
//	@BeforeClass
//	public static void disableTestsOnCiServer() {
//	    //String profilesFromConsole = System.getProperty("spring.profiles.active", "");
//	    Assume.assumeFalse(true);
//	}
	
	@Test
	public void testQuickFilter(){
		String productPrefix = "test";
		String productOneName = String.format("%sOne", productPrefix);
		String productTwoName = String.format("%sTwo", productPrefix);
		//
		ExampleProductFilter filter = new ExampleProductFilter();
		filter.setText(productOneName);
		Page<ExampleProductDto> results = service.find(filter, null);
		assertEquals(0, results.getTotalElements());
		//
		createProduct(productOneName);
		createProduct(productTwoName);		
		results = service.find(filter, null);
		assertEquals(1, results.getTotalElements());
		assertEquals(productOneName, results.getContent().get(0).getName());
		//
		filter.setText(productPrefix);
		results = service.find(filter, null);
		assertEquals(2, results.getTotalElements());
	}
	
	private ExampleProductDto createProduct(String name) {
		ExampleProductDto product = new ExampleProductDto();
		product.setName(name);
		product.setCode(UUID.randomUUID().toString());
		return service.save(product);
	}
}
