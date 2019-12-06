package eu.bcvsolutions.idm.core.eav.service.impl;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmCodeListDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmCodeListItemDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Code list tests
 * 
 * @author Radek Tomiška
 *
 */
@Transactional
public class DefaultCodeListManagerIntegrationTest extends AbstractIntegrationTest {
	
	@Autowired private ApplicationContext context;
	@Autowired private FormService formService;
	//
	private DefaultCodeListManager manager;
	
	@Before
	public void init() {
		manager = context.getAutowireCapableBeanFactory().createBean(DefaultCodeListManager.class);
	}
	
	@Test
	public void testReferentialIntegrity() {
		IdmCodeListDto codeList = manager.create(getHelper().createName());
		IdmCodeListItemDto item = manager.createItem(codeList, getHelper().createName(), getHelper().createName());
		//
		Assert.assertNotNull(manager.get(codeList));
		Assert.assertNotNull(manager.getItem(codeList, item.getCode()));
		Assert.assertNotNull(formService.getDefinition(codeList.getFormDefinition().getId()));
		//
		manager.delete(codeList);
		//
		Assert.assertNull(manager.get(codeList));
		Assert.assertNull(manager.getItem(codeList, item.getCode()));
		Assert.assertNull(formService.getDefinition(codeList.getFormDefinition().getId()));
	}

	@Test
	public void testCodeListCRUD() {
		String code = getHelper().createName();
		IdmCodeListDto codeList = manager.create(code);
		codeList = manager.get(code);
		//
		Assert.assertEquals(code, codeList.getCode());
		Assert.assertEquals(code, codeList.getName());
		Assert.assertNotNull(codeList.getFormDefinition());
		IdmFormDefinitionDto formDefinition = codeList.getFormDefinition();
		Assert.assertEquals(code, formDefinition.getCode());
		Assert.assertEquals(code, formDefinition.getName());
		Assert.assertEquals(formService.getDefaultDefinitionType(IdmCodeListItemDto.class), formDefinition.getType());
		//
		String codeUpdate = getHelper().createName();
		String codeNameUpdate = getHelper().createName();
		codeList.setCode(codeUpdate);
		codeList.setName(codeNameUpdate);
		//
		manager.save(codeList);
		codeList = manager.get(codeList.getId());
		//
		Assert.assertEquals(codeUpdate, codeList.getCode());
		Assert.assertEquals(codeNameUpdate, codeList.getName());
		Assert.assertNotNull(codeList.getFormDefinition());
		formDefinition = codeList.getFormDefinition();
		Assert.assertEquals(codeUpdate, formDefinition.getCode());
		Assert.assertEquals(codeNameUpdate, formDefinition.getName());
		//
		manager.delete(codeList);
		//
		Assert.assertNull(manager.get(codeList.getId()));
		formService.getDefinition(formDefinition.getId());
	}
	
	@Test(expected = ResultCodeException.class)
	public void testChangeFormDefintion() {
		IdmCodeListDto codeList = manager.create(getHelper().createName());
		codeList.setFormDefinition(formService.createDefinition(IdmCodeListItemDto.class, getHelper().createName(), null));
		//
		manager.save(codeList);
	}
	
	@Test(expected = ResultCodeException.class)
	public void testSetFormDefintion() {
		IdmCodeListDto codeList = new IdmCodeListDto();
		codeList.setCode(getHelper().createName());
		codeList.setName(getHelper().createName());
		codeList.setFormDefinition(formService.createDefinition(IdmCodeListItemDto.class, getHelper().createName(), null));
		//
		manager.save(codeList);
	}
	
	@Test
	public void testCodeListItemCRUD() {
		String code = getHelper().createName();
		IdmCodeListDto codeList = manager.create(code);
		//
		String itemCode = getHelper().createName();
		String itemName = getHelper().createName();
		IdmCodeListItemDto item = manager.createItem(codeList, itemCode, itemName);
		//
		Assert.assertEquals(itemCode, item.getCode());
		Assert.assertEquals(itemName, item.getName());
		//
		String itemCodeUpdate = getHelper().createName();
		String itemNameUpdate = getHelper().createName();
		item.setCode(itemCodeUpdate);
		item.setName(itemNameUpdate);
		item = manager.saveItem(item);
		//
		Assert.assertEquals(itemCodeUpdate, item.getCode());
		Assert.assertEquals(itemNameUpdate, item.getName());
		//
		Assert.assertNotNull(manager.getItem(codeList, itemCodeUpdate));
		Assert.assertEquals(item.getId(), manager.getItem(codeList, itemCodeUpdate).getId());
		//
		List<IdmCodeListItemDto> items = manager.getItems(codeList, null);
		Assert.assertEquals(1, items.size());
		Assert.assertTrue(items.stream().anyMatch(i -> i.getCode().equals(itemCodeUpdate)));
		//
		manager.deleteItem(codeList, itemCodeUpdate);
		//
		Assert.assertNull(manager.getItem(codeList, itemCodeUpdate));
	}
	
}
