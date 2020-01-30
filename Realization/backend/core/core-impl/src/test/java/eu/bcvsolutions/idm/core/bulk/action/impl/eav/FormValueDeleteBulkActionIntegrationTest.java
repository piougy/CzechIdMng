package eu.bcvsolutions.idm.core.bulk.action.impl.eav;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.api.dto.ResultModels;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.dto.filter.IdmFormValueFilter;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormValue;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.scheduler.ObserveLongRunningTaskEndProcessor;
import eu.bcvsolutions.idm.core.scheduler.api.config.SchedulerConfiguration;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.test.api.AbstractBulkActionTest;

/**
 * Delete form values:
 * - by id / filter
 * - required form value cannot be removed
 * 
 * @author Radek Tomiška
 *
 */
public class FormValueDeleteBulkActionIntegrationTest extends AbstractBulkActionTest {
	
	private final static String FORM_VALUE_ONE = "one";
	private final static String FORM_VALUE_TWO = "two";
	//
	@Autowired private FormService formService;
	
	@Before
	public void login() {
		loginAsAdmin();
	}
	
	@After
	public void logout() {
		super.logout();
	}
	
	@Test
	public void processBulkActionByIds() {
		Identifiable owner = getHelper().createIdentity((GuardedString) null);
		//
		// create definition with attribute
		IdmFormAttributeDto attribute = new IdmFormAttributeDto();
		String attributeName = getHelper().createName();
		attribute.setCode(attributeName);
		attribute.setName(attribute.getCode());
		attribute.setMultiple(true);
		attribute.setPersistentType(PersistentType.SHORTTEXT);
		IdmFormDefinitionDto formDefinitionOne = formService.createDefinition(IdmIdentity.class, getHelper().createName(), Lists.newArrayList(attribute));
		attribute = formDefinitionOne.getMappedAttributeByCode(attribute.getCode());
		//
		// fill values
		formService.saveValues(owner, attribute, Lists.newArrayList(FORM_VALUE_ONE, FORM_VALUE_TWO));
		Map<String, List<IdmFormValueDto>> m = formService.getFormInstance(owner, formDefinitionOne).toValueMap();
		//
		// check value and persistent type
		Assert.assertEquals(2, m.get(attributeName).size());
		Assert.assertTrue(m.get(attributeName).stream().anyMatch(v -> v.getValue().equals(FORM_VALUE_ONE)));
		Assert.assertTrue(m.get(attributeName).stream().anyMatch(v -> v.getValue().equals(FORM_VALUE_TWO)));
		//
		IdmBulkActionDto bulkAction = findBulkAction(IdmFormValue.class, FormValueDeleteBulkAction.NAME);
		
		Set<UUID> ids = new HashSet<>();
		ids.add(m.get(attributeName).stream().filter(v -> v.getValue().equals(FORM_VALUE_ONE)).findFirst().get().getId());
		bulkAction.setIdentifiers(ids);
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		
		checkResultLrt(processAction, 1l, null, null);
		
		m = formService.getFormInstance(owner, formDefinitionOne).toValueMap();
		//
		Assert.assertEquals(1, m.get(attributeName).size());
		Assert.assertTrue(m.get(attributeName).stream().anyMatch(v -> v.getValue().equals(FORM_VALUE_TWO)));
	}
	
	@Test
	public void processBulkActionByFilter() {
		Identifiable owner = getHelper().createIdentity((GuardedString) null);
		//
		// create definition with attribute
		IdmFormAttributeDto attribute = new IdmFormAttributeDto();
		String attributeName = getHelper().createName();
		attribute.setCode(attributeName);
		attribute.setName(attribute.getCode());
		attribute.setMultiple(true);
		attribute.setPersistentType(PersistentType.SHORTTEXT);
		IdmFormDefinitionDto formDefinitionOne = formService.createDefinition(IdmIdentity.class, getHelper().createName(), Lists.newArrayList(attribute));
		attribute = formDefinitionOne.getMappedAttributeByCode(attribute.getCode());
		//
		// fill values
		formService.saveValues(owner, attribute, Lists.newArrayList(FORM_VALUE_ONE, FORM_VALUE_TWO));
		Map<String, List<IdmFormValueDto>> m = formService.getFormInstance(owner, formDefinitionOne).toValueMap();
		//
		// check value and persistent type
		Assert.assertEquals(2, m.get(attributeName).size());
		Assert.assertTrue(m.get(attributeName).stream().anyMatch(v -> v.getValue().equals(FORM_VALUE_ONE)));
		Assert.assertTrue(m.get(attributeName).stream().anyMatch(v -> v.getValue().equals(FORM_VALUE_TWO)));
		//
		IdmBulkActionDto bulkAction = findBulkAction(IdmFormValue.class, FormValueDeleteBulkAction.NAME);
		
		IdmFormValueFilter<?> filter = new IdmFormValueFilter<>();
		filter.setDefinitionId(formDefinitionOne.getId());
		filter.setShortTextValue(FORM_VALUE_ONE);
		filter.setAttributeId(attribute.getId());
		
		bulkAction.setTransformedFilter(filter);
		bulkAction.setFilter(toMap(filter));
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, 1l, null, null);
		
		m = formService.getFormInstance(owner, formDefinitionOne).toValueMap();
		//
		Assert.assertEquals(1, m.get(attributeName).size());
		Assert.assertTrue(m.get(attributeName).stream().anyMatch(v -> v.getValue().equals(FORM_VALUE_TWO)));
	}
	
	@Test
	public void prevalidationBulkActionByIds() {
		Identifiable owner = getHelper().createIdentity((GuardedString) null);
		//
		// create definition with attribute
		IdmFormAttributeDto attribute = new IdmFormAttributeDto();
		String attributeName = getHelper().createName();
		attribute.setCode(attributeName);
		attribute.setName(attribute.getCode());
		attribute.setMultiple(true);
		attribute.setPersistentType(PersistentType.SHORTTEXT);
		IdmFormDefinitionDto formDefinitionOne = formService.createDefinition(IdmIdentity.class, getHelper().createName(), Lists.newArrayList(attribute));
		attribute = formDefinitionOne.getMappedAttributeByCode(attribute.getCode());
		//
		// fill values
		formService.saveValues(owner, attribute, Lists.newArrayList(FORM_VALUE_ONE, FORM_VALUE_TWO));
		Map<String, List<IdmFormValueDto>> m = formService.getFormInstance(owner, formDefinitionOne).toValueMap();
		//
		// check value and persistent type
		Assert.assertEquals(2, m.get(attributeName).size());

		// Running prevalidation which is supposed to return no validation errors 
		// because this mapping is not used in any synchronization settings.
		IdmBulkActionDto bulkAction = findBulkAction(IdmFormValue.class, FormValueDeleteBulkAction.NAME);
		Set<UUID> ids = new HashSet<>();
		ids.add(m.get(attributeName).stream().filter(v -> v.getValue().equals(FORM_VALUE_ONE)).findFirst().get().getId());
		ids.add(m.get(attributeName).stream().filter(v -> v.getValue().equals(FORM_VALUE_TWO)).findFirst().get().getId());
		bulkAction.setIdentifiers(ids);
		//
		ResultModels resultModels = bulkActionManager.prevalidate(bulkAction);
		//
		Assert.assertTrue(resultModels.getInfos().isEmpty());
		//
		// create attribute required
		attribute.setRequired(true);
		formService.saveAttribute(attribute);
		//
		resultModels = bulkActionManager.prevalidate(bulkAction);
		Assert.assertEquals(1, resultModels.getInfos().size());
		Assert.assertEquals(2L, resultModels.getInfos().get(0).getParameters().get("count"));
		Assert.assertEquals(attribute.getCode(), resultModels.getInfos().get(0).getParameters().get("attribute"));
	}
	
	@Test
	public void testDeleteOneRequiredAttributes() {
		Identifiable owner = getHelper().createIdentity((GuardedString) null);
		//
		// create definition with attribute
		IdmFormAttributeDto attribute = new IdmFormAttributeDto();
		String attributeName = getHelper().createName();
		attribute.setCode(attributeName);
		attribute.setName(attribute.getCode());
		attribute.setMultiple(true);
		attribute.setRequired(true);
		attribute.setPersistentType(PersistentType.SHORTTEXT);
		IdmFormDefinitionDto formDefinitionOne = formService.createDefinition(IdmIdentity.class, getHelper().createName(), Lists.newArrayList(attribute));
		attribute = formDefinitionOne.getMappedAttributeByCode(attribute.getCode());
		//
		// fill values
		formService.saveValues(owner, attribute, Lists.newArrayList(FORM_VALUE_ONE, FORM_VALUE_TWO));
		Map<String, List<IdmFormValueDto>> m = formService.getFormInstance(owner, formDefinitionOne).toValueMap();
		//
		// check value and persistent type
		Assert.assertEquals(2, m.get(attributeName).size());
		Assert.assertTrue(m.get(attributeName).stream().anyMatch(v -> v.getValue().equals(FORM_VALUE_ONE)));
		Assert.assertTrue(m.get(attributeName).stream().anyMatch(v -> v.getValue().equals(FORM_VALUE_TWO)));
		//
		IdmBulkActionDto bulkAction = findBulkAction(IdmFormValue.class, FormValueDeleteBulkAction.NAME);
		
		Set<UUID> ids = new HashSet<>();
		ids.add(m.get(attributeName).stream().filter(v -> v.getValue().equals(FORM_VALUE_ONE)).findFirst().get().getId());
		bulkAction.setIdentifiers(ids);
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		
		checkResultLrt(processAction, 1L, null, null);
		
		m = formService.getFormInstance(owner, formDefinitionOne).toValueMap();
		//
		Assert.assertEquals(1, m.get(attributeName).size());
		Assert.assertTrue(m.get(attributeName).stream().anyMatch(v -> v.getValue().equals(FORM_VALUE_TWO)));
	}
	
	@Test
	public void testDeleteBothRequiredAttributesFailed() throws InterruptedException {
		try {
			getHelper().setConfigurationValue(SchedulerConfiguration.PROPERTY_TASK_ASYNCHRONOUS_ENABLED, true);
			
			Identifiable owner = getHelper().createIdentity((GuardedString) null);
			//
			// create definition with attribute
			IdmFormAttributeDto attribute = new IdmFormAttributeDto();
			String attributeName = getHelper().createName();
			attribute.setCode(attributeName);
			attribute.setName(attribute.getCode());
			attribute.setMultiple(true);
			attribute.setRequired(true);
			attribute.setPersistentType(PersistentType.SHORTTEXT);
			IdmFormDefinitionDto formDefinitionOne = formService.createDefinition(IdmIdentity.class, getHelper().createName(), Lists.newArrayList(attribute));
			attribute = formDefinitionOne.getMappedAttributeByCode(attribute.getCode());
			//
			// fill values
			formService.saveValues(owner, attribute, Lists.newArrayList(FORM_VALUE_ONE, FORM_VALUE_TWO));
			Map<String, List<IdmFormValueDto>> m = formService.getFormInstance(owner, formDefinitionOne).toValueMap();
			//
			// check value and persistent type
			Assert.assertEquals(2, m.get(attributeName).size());
			Assert.assertTrue(m.get(attributeName).stream().anyMatch(v -> v.getValue().equals(FORM_VALUE_ONE)));
			Assert.assertTrue(m.get(attributeName).stream().anyMatch(v -> v.getValue().equals(FORM_VALUE_TWO)));
			//
			IdmBulkActionDto bulkAction = findBulkAction(IdmFormValue.class, FormValueDeleteBulkAction.NAME);
			
			Set<UUID> ids = new HashSet<>();
			ids.add(m.get(attributeName).stream().filter(v -> v.getValue().equals(FORM_VALUE_ONE)).findFirst().get().getId());
			ids.add(m.get(attributeName).stream().filter(v -> v.getValue().equals(FORM_VALUE_TWO)).findFirst().get().getId());
			bulkAction.setIdentifiers(ids);
			
			IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
			ObserveLongRunningTaskEndProcessor.listenTask(processAction.getLongRunningTaskId().toString());
			ObserveLongRunningTaskEndProcessor.waitForEnd(processAction.getLongRunningTaskId().toString());
			
			checkResultLrt(processAction, 1L, 1L, null);
			
			m = formService.getFormInstance(owner, formDefinitionOne).toValueMap();
			//
			Assert.assertEquals(1, m.get(attributeName).size());
		} finally {
			getHelper().setConfigurationValue(SchedulerConfiguration.PROPERTY_TASK_ASYNCHRONOUS_ENABLED, false);
		}
	}
}
