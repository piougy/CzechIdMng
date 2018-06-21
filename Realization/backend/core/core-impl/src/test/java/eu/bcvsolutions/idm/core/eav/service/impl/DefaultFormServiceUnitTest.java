package eu.bcvsolutions.idm.core.eav.service.impl;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.modelmapper.ModelMapper;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormValueService;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormAttributeService;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormDefinitionService;
import eu.bcvsolutions.idm.test.api.AbstractUnitTest;

/**
 * Form service unit tests
 * - resolve previous form values
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultFormServiceUnitTest extends AbstractUnitTest {

	@Mock private IdmFormDefinitionService formDefinitionService;
	@Mock private IdmFormAttributeService formAttributeService;
	@Mock private List<? extends FormValueService<?>> formValueServices;
	@Mock private EntityEventManager entityEventManager;
	@Mock private LookupService lookupService;
	@Spy private ModelMapper modelMapper = new ModelMapper();
	//
	@InjectMocks private DefaultFormService formService;
	
	@Test
	public void testSingleValueWithIds() {
		IdmFormValueDto valueOne = new IdmFormValueDto(UUID.randomUUID());
		//
		Map<UUID, IdmFormValueDto> unprocessedPreviousValues = new HashMap<>();
		unprocessedPreviousValues.put(valueOne.getId(), valueOne);
		List<IdmFormValueDto> newValues = Lists.newArrayList(valueOne);
		//
		IdmFormValueDto[] previousValues = formService.resolvePreviousValues(unprocessedPreviousValues, newValues);
		//
		Assert.assertEquals(1, previousValues.length);
		Assert.assertEquals(valueOne, previousValues[0]);
		Assert.assertTrue(unprocessedPreviousValues.isEmpty());
	}
	
	@Test
	public void testSingleValueWithTheSameValue() {
		IdmFormValueDto valueOne = new IdmFormValueDto();
		valueOne.setPersistentType(PersistentType.SHORTTEXT);
		valueOne.setShortTextValue("random");
		//
		Map<UUID, IdmFormValueDto> unprocessedPreviousValues = new HashMap<>();
		unprocessedPreviousValues.put(valueOne.getId(), valueOne);
		List<IdmFormValueDto> newValues = Lists.newArrayList(valueOne);
		//
		IdmFormValueDto[] previousValues = formService.resolvePreviousValues(unprocessedPreviousValues, newValues);
		//
		Assert.assertEquals(1, previousValues.length);
		Assert.assertEquals(valueOne, previousValues[0]);
		Assert.assertTrue(unprocessedPreviousValues.isEmpty());
	}
	
	@Test
	public void testSingleValueNotSameButWithPreviousValue() {
		IdmFormValueDto valueOne = new IdmFormValueDto(UUID.randomUUID());
		IdmFormValueDto valueTwo = new IdmFormValueDto(UUID.randomUUID());
		Map<UUID, IdmFormValueDto> unprocessedPreviousValues = new LinkedHashMap<>();
		unprocessedPreviousValues.put(valueTwo.getId(), valueTwo);
		unprocessedPreviousValues.put(valueOne.getId(), valueOne);
		//
		IdmFormValueDto valueThree = new IdmFormValueDto(UUID.randomUUID());
		valueThree.setPersistentType(PersistentType.SHORTTEXT);
		valueThree.setShortTextValue("random");
		List<IdmFormValueDto> newValues = Lists.newArrayList(valueThree);
		//
		IdmFormValueDto[] previousValues = formService.resolvePreviousValues(unprocessedPreviousValues, newValues);
		//
		Assert.assertEquals(1, previousValues.length);
		Assert.assertEquals(valueTwo, previousValues[0]); // the first one - linked hash map
		Assert.assertEquals(1, unprocessedPreviousValues.size());
		Assert.assertEquals(valueOne, unprocessedPreviousValues.values().iterator().next());
	}
	
	@Test
	public void testSingleValueWithoutPreviousValue() {
		Map<UUID, IdmFormValueDto> unprocessedPreviousValues = new HashMap<>();		
		IdmFormValueDto valueThree = new IdmFormValueDto(UUID.randomUUID());
		valueThree.setPersistentType(PersistentType.SHORTTEXT);
		valueThree.setShortTextValue("random");
		List<IdmFormValueDto> newValues = Lists.newArrayList(valueThree);
		//
		IdmFormValueDto[] previousValues = formService.resolvePreviousValues(unprocessedPreviousValues, newValues);
		//
		Assert.assertEquals(1, previousValues.length);
		Assert.assertNull(previousValues[0]);
	}
	
	@Test
	public void testMultipleValueWithIds() {
		IdmFormValueDto valueOne = new IdmFormValueDto(UUID.randomUUID());
		IdmFormValueDto valueTwo = new IdmFormValueDto(UUID.randomUUID());
		IdmFormValueDto valueThree = new IdmFormValueDto(UUID.randomUUID());
		IdmFormValueDto valueFour = new IdmFormValueDto(UUID.randomUUID());
		IdmFormValueDto valueFive = new IdmFormValueDto(UUID.randomUUID());
		//
		Map<UUID, IdmFormValueDto> unprocessedPreviousValues = new HashMap<>();
		unprocessedPreviousValues.put(valueFive.getId(), valueFive);
		unprocessedPreviousValues.put(valueThree.getId(), valueThree);
		unprocessedPreviousValues.put(valueOne.getId(), valueOne);
		unprocessedPreviousValues.put(valueFour.getId(), valueFour);
		unprocessedPreviousValues.put(valueTwo.getId(), valueTwo);
		List<IdmFormValueDto> newValues = Lists.newArrayList(valueTwo, valueThree, valueFour);
		//
		IdmFormValueDto[] previousValues = formService.resolvePreviousValues(unprocessedPreviousValues, newValues);
		//
		Assert.assertEquals(3, previousValues.length);
		Assert.assertEquals(valueTwo, previousValues[0]);
		Assert.assertEquals(valueThree, previousValues[1]);
		Assert.assertEquals(valueFour, previousValues[2]);
		Assert.assertEquals(2, unprocessedPreviousValues.size());
		Assert.assertTrue(unprocessedPreviousValues.values().stream().anyMatch(v -> v.equals(valueOne)));
		Assert.assertTrue(unprocessedPreviousValues.values().stream().anyMatch(v -> v.equals(valueFive)));
	}
	
	@Test
	public void testMultipleValueWithhoutPrevious() {
		Map<UUID, IdmFormValueDto> unprocessedPreviousValues = new HashMap<>();		
		IdmFormValueDto valueOne = new IdmFormValueDto(UUID.randomUUID());
		IdmFormValueDto valueTwo = new IdmFormValueDto(UUID.randomUUID());
		IdmFormValueDto valueThree = new IdmFormValueDto(UUID.randomUUID());
		valueThree.setPersistentType(PersistentType.SHORTTEXT);
		valueThree.setShortTextValue("random");
		List<IdmFormValueDto> newValues = Lists.newArrayList(valueOne, valueThree, valueTwo);
		//
		IdmFormValueDto[] previousValues = formService.resolvePreviousValues(unprocessedPreviousValues, newValues);
		//
		Assert.assertEquals(3, previousValues.length);
		Assert.assertNull(previousValues[0]);
		Assert.assertNull(previousValues[1]);
		Assert.assertNull(previousValues[2]);
	}
	
	@Test
	public void testMultipleValueWithoutSomePrevious() {
		IdmFormValueDto valueOne = new IdmFormValueDto(UUID.randomUUID());
		IdmFormValueDto valueTwo = new IdmFormValueDto(UUID.randomUUID());
		IdmFormValueDto valueThree = new IdmFormValueDto(UUID.randomUUID());
		valueThree.setPersistentType(PersistentType.SHORTTEXT);
		valueThree.setShortTextValue("random");
		Map<UUID, IdmFormValueDto> unprocessedPreviousValues = new HashMap<>();		
		unprocessedPreviousValues.put(valueThree.getId(), valueThree);
		//
		IdmFormValueDto newValue = new IdmFormValueDto();
		newValue.setPersistentType(PersistentType.SHORTTEXT);
		newValue.setShortTextValue("random");
		
		List<IdmFormValueDto> newValues = Lists.newArrayList(valueOne, newValue, valueTwo);
		//
		IdmFormValueDto[] previousValues = formService.resolvePreviousValues(unprocessedPreviousValues, newValues);
		//
		Assert.assertEquals(3, previousValues.length);
		Assert.assertNull(previousValues[0]);
		Assert.assertEquals(valueThree, previousValues[1]);
		Assert.assertNull(previousValues[2]);
	}
	
	@Test
	public void testMultipleValueWithoutSomePreviousWithSomeIdSpecified() {
		IdmFormValueDto valueOne = new IdmFormValueDto(UUID.randomUUID());
		IdmFormValueDto valueTwo = new IdmFormValueDto(UUID.randomUUID());
		IdmFormValueDto valueThree = new IdmFormValueDto(UUID.randomUUID());
		valueThree.setPersistentType(PersistentType.SHORTTEXT);
		valueThree.setShortTextValue("random");
		Map<UUID, IdmFormValueDto> unprocessedPreviousValues = new HashMap<>();		
		unprocessedPreviousValues.put(valueTwo.getId(), valueTwo);
		unprocessedPreviousValues.put(valueThree.getId(), valueThree);
		//
		IdmFormValueDto newValue = new IdmFormValueDto(valueTwo.getId());
		newValue.setPersistentType(PersistentType.SHORTTEXT);
		newValue.setShortTextValue("random");
		
		List<IdmFormValueDto> newValues = Lists.newArrayList(
				valueOne, 
				newValue, 
				valueTwo);
		//
		IdmFormValueDto[] previousValues = formService.resolvePreviousValues(unprocessedPreviousValues, newValues);
		//
		Assert.assertEquals(3, previousValues.length);
		Assert.assertEquals(valueThree, previousValues[0]);
		Assert.assertEquals(valueTwo, previousValues[1]);
		Assert.assertNull(previousValues[2]);
	}
	
	@Test
	public void testMultipleValueWithSeqSpecified() {
		IdmFormValueDto valueOne = new IdmFormValueDto(UUID.randomUUID());
		IdmFormValueDto valueTwo = new IdmFormValueDto(UUID.randomUUID());
		IdmFormValueDto valueThree = new IdmFormValueDto(UUID.randomUUID());
		valueThree.setPersistentType(PersistentType.SHORTTEXT);
		valueThree.setSeq((short) 3);
		Map<UUID, IdmFormValueDto> unprocessedPreviousValues = new HashMap<>();		
		unprocessedPreviousValues.put(valueTwo.getId(), valueTwo);
		unprocessedPreviousValues.put(valueThree.getId(), valueThree);
		//
		IdmFormValueDto newValue = new IdmFormValueDto(valueTwo.getId());
		newValue.setPersistentType(PersistentType.SHORTTEXT);
		newValue.setSeq((short) 3);
		
		List<IdmFormValueDto> newValues = Lists.newArrayList(
				valueOne, 
				newValue, 
				valueTwo);
		//
		IdmFormValueDto[] previousValues = formService.resolvePreviousValues(unprocessedPreviousValues, newValues);
		//
		Assert.assertEquals(3, previousValues.length);
		Assert.assertEquals(valueThree, previousValues[0]);
		Assert.assertEquals(valueTwo, previousValues[1]);
		Assert.assertNull(previousValues[2]);
	}
}
