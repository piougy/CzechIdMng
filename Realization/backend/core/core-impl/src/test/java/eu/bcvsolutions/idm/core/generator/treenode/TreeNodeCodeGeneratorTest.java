package eu.bcvsolutions.idm.core.generator.treenode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmGenerateValueDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeTypeDto;
import eu.bcvsolutions.idm.core.api.generator.AbstractGeneratorTest;
import eu.bcvsolutions.idm.core.api.service.IdmTreeNodeService;

/**
 * Tests for {@link TreeNodeCodeGenerator}
 *
 * @author Ondrej Kopr
 * @since 9.6.2
 *
 */
public class TreeNodeCodeGeneratorTest extends AbstractGeneratorTest {

	@Autowired
	private IdmTreeNodeService treeNodeService;

	@Test
	public void testGreenLine() {
		IdmTreeTypeDto type = getHelper().createTreeType();
		IdmTreeNodeDto node = new IdmTreeNodeDto();
		node.setName("node-" + System.currentTimeMillis());
		node.setTreeType(type.getId());
		
		IdmGenerateValueDto generator = this.createGenerator(getDtoType(), getGeneratorType(), null, 1, null);
		
		IdmTreeNodeDto generated = valueGeneratorManager.generate(node);
		
		assertNotNull(generated.getCode());
		assertEquals(node.getName(), generated.getCode());
		
		generatedAttributeService.delete(generator);
	}

	@Test
	public void testUpperCase() {
		IdmTreeTypeDto type = getHelper().createTreeType();
		IdmTreeNodeDto node = new IdmTreeNodeDto();
		node.setTreeType(type.getId());
		node.setName("NODE-" + System.currentTimeMillis());
		node.setTreeType(type.getId());
		
		IdmGenerateValueDto generator = this.createGenerator(getDtoType(), getGeneratorType(), null, 1, null);
		
		IdmTreeNodeDto generated = valueGeneratorManager.generate(node);
		
		assertNotNull(generated.getCode());
		assertEquals(node.getName().toLowerCase(), generated.getCode());

		generatedAttributeService.delete(generator);
	}

	@Test
	public void testWhiteSpace() {
		IdmTreeTypeDto type = getHelper().createTreeType();
		IdmTreeNodeDto node = new IdmTreeNodeDto();
		node.setTreeType(type.getId());
		node.setName("  no  de-  test123");
		
		IdmGenerateValueDto generator = this.createGenerator(getDtoType(), getGeneratorType(), null, 1, null);
		
		IdmTreeNodeDto generated = valueGeneratorManager.generate(node);
		
		assertNotNull(generated.getCode());
		assertEquals("node-test123", generated.getCode());

		generatedAttributeService.delete(generator);
	}

	@Test
	public void testDiacritics() {
		IdmTreeTypeDto type = getHelper().createTreeType();
		IdmTreeNodeDto node = new IdmTreeNodeDto();
		node.setTreeType(type.getId());
		node.setName("abcdřřřžžžééé");
		
		IdmGenerateValueDto generator = this.createGenerator(getDtoType(), getGeneratorType(), null, 1, null);
		
		IdmTreeNodeDto generated = valueGeneratorManager.generate(node);
		
		assertNotNull(generated.getCode());
		assertEquals("abcdrrrzzzeee", generated.getCode());

		generatedAttributeService.delete(generator);
	}

	@Test
	public void testAllCombination() {
		IdmTreeTypeDto type = getHelper().createTreeType();
		IdmTreeNodeDto node = new IdmTreeNodeDto();
		node.setTreeType(type.getId());
		node.setName("ABCDa    bc dř Ř ř");
		
		IdmGenerateValueDto generator = this.createGenerator(getDtoType(), getGeneratorType(), null, 1, null);
		
		IdmTreeNodeDto generated = valueGeneratorManager.generate(node);
		
		assertNotNull(generated.getCode());
		assertEquals("abcdabcdrrr", generated.getCode());

		generatedAttributeService.delete(generator);
	}

	@Test
	public void testAllCombinationWithClassicSave() {
		IdmTreeTypeDto type = getHelper().createTreeType();
		IdmTreeNodeDto node = new IdmTreeNodeDto();
		node.setTreeType(type.getId());
		node.setName("AB  CDa    bc dř Ř ř");
		
		IdmGenerateValueDto generator = this.createGenerator(getDtoType(), getGeneratorType(), null, 1, null);
		
		IdmTreeNodeDto generated = treeNodeService.save(node);
		
		assertNotNull(generated.getCode());
		assertEquals("abcdabcdrrr", generated.getCode());

		generatedAttributeService.delete(generator);
	}

	@Test
	public void testNullName() {
		IdmTreeTypeDto type = getHelper().createTreeType();
		IdmTreeNodeDto node = new IdmTreeNodeDto();
		node.setTreeType(type.getId());
		node.setName(null);
		
		IdmGenerateValueDto generator = this.createGenerator(getDtoType(), getGeneratorType(), null, 1, null);
		
		IdmTreeNodeDto generated = valueGeneratorManager.generate(node);
		
		assertNull(generated.getCode());

		generatedAttributeService.delete(generator);
	}

	@Test
	public void testEmptyName() {
		IdmTreeTypeDto type = getHelper().createTreeType();
		IdmTreeNodeDto node = new IdmTreeNodeDto();
		node.setTreeType(type.getId());
		node.setName("");
		
		IdmGenerateValueDto generator = this.createGenerator(getDtoType(), getGeneratorType(), null, 1, null);
		
		IdmTreeNodeDto generated = valueGeneratorManager.generate(node);
		
		assertNotNull(generated.getCode());
		assertTrue(generated.getCode().isEmpty());

		generatedAttributeService.delete(generator);
	}

	@Override
	protected Class<? extends AbstractDto> getDtoType() {
		return IdmTreeNodeDto.class;
	}

	@Override
	protected String getGeneratorType() {
		return TreeNodeCodeGenerator.class.getCanonicalName();
	}

}
