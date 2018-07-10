package eu.bcvsolutions.idm.example.repository.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.example.dto.ExampleProductDto;
import eu.bcvsolutions.idm.example.dto.filter.ExampleProductFilter;
import eu.bcvsolutions.idm.example.entity.ExampleProduct;
import eu.bcvsolutions.idm.example.service.api.ExampleProductService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Test for {@link TextExampleProductFilter}
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@Transactional
public class TextExampleProductFilterTest extends AbstractIntegrationTest {

	@Autowired
	private ExampleProductService productService;
	@Autowired
	private TextExampleProductFilter textProductFilter;
	
	@Before
	public void before() {
		this.loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
	}

	@After
	public void after() {
		super.logout();
	}

	@Test
	public void testFilteringFound() {
		String textValue = getHelper().createName() + System.currentTimeMillis();
		ExampleProductDto productOne = createProduct(getHelper().createName() + textValue.toLowerCase() + System.currentTimeMillis());
		ExampleProductDto productTwo = createProduct(getHelper().createName());
		ExampleProductDto productThree = createProduct(getHelper().createName());
		productTwo.setCode(textValue + getHelper().createName());
		productThree.setDescription(getHelper().createName() + textValue.toUpperCase() + getHelper().createName());
		
		productService.save(productTwo);
		productService.save(productThree);
		
		ExampleProductFilter filter = new ExampleProductFilter();
		filter.setText(textValue);
		List<ExampleProduct> products = textProductFilter.find(filter, null).getContent();
		
		assertEquals(3, products.size());
		
		// list must contains all products
		ExampleProduct product = products.stream().filter(prod -> prod.getId().equals(productOne.getId())).findFirst().get();
		assertNotNull(product);
		
		product = products.stream().filter(prod -> prod.getId().equals(productTwo.getId())).findFirst().get();
		assertNotNull(product);
		
		product = products.stream().filter(prod -> prod.getId().equals(productThree.getId())).findFirst().get();
		assertNotNull(product);
	}
	
	@Test
	public void testFilteringNotFound() {
		String textValue = "textValue" + System.currentTimeMillis();
		createProduct("123" + textValue + System.currentTimeMillis());
		ExampleProductDto productTwo = createProduct(getHelper().createName());
		ExampleProductDto productThree = createProduct(getHelper().createName());
		productTwo.setCode(textValue + getHelper().createName());
		productThree.setDescription(getHelper().createName() + textValue + getHelper().createName());
		
		productService.save(productTwo);
		productService.save(productThree);
		
		ExampleProductFilter filter = new ExampleProductFilter();
		filter.setText("textValue" + System.currentTimeMillis()); // different value than in variable textValue
		List<ExampleProduct> products = textProductFilter.find(filter, null).getContent();
		
		assertEquals(0, products.size());
	}

	/**
	 * Create example product DTO.
	 * TODO: is possible create example test helper. This is second similar method in Example module.
	 *
	 * @param name
	 * @return
	 */
	private ExampleProductDto createProduct(String name) {
		ExampleProductDto product = new ExampleProductDto();
		product.setName(name);
		product.setCode(UUID.randomUUID().toString());
		return productService.save(product);
	}
}
