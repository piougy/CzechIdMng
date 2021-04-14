package eu.bcvsolutions.idm.vs.event.processor;


import java.util.List;
import java.util.Objects;

import org.apache.commons.codec.binary.StringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;


import eu.bcvsolutions.idm.vs.TestHelper;
import eu.bcvsolutions.idm.vs.entity.VsAccount;
import eu.bcvsolutions.idm.vs.service.api.VsSystemService;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.event.SystemEvent;
import eu.bcvsolutions.idm.acc.event.SystemEvent.SystemEventType;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * VS specific duplication processor test
 * 
 * @author Ondrej Husnik
 *
 */
public class VsSpecificDuplicationProcessorIntegrationTest extends AbstractIntegrationTest {

	@Autowired 
	private TestHelper helper;
	@Autowired 
	private SysSystemService systemService;
	@Autowired 
	private VsSystemService vsSystemService;
	@Autowired 
	private FormService formService;
	
	@Before
	public void init() {
		loginAsAdmin();
	}

	@After
	public void logout() {
		super.logout();
	}
	
	
	@Test
	public void testVsConfigurationDuplication() {
		SysSystemDto origSystem = helper.createVirtualSystem(helper.createName());
		EntityEvent<SysSystemDto> event = new SystemEvent(SystemEventType.DUPLICATE, origSystem);
		SysSystemDto newSystem = systemService.publish(event).getContent();
		Assert.assertNotNull(newSystem);
		Assert.assertNotEquals(origSystem.getId(), newSystem.getId());
		
		String type = VsAccount.class.getName();
		String origKey = vsSystemService.createVsFormDefinitionKey(origSystem);
		String newKey = vsSystemService.createVsFormDefinitionKey(newSystem);
		
		IdmFormDefinitionDto origFormDef = formService.getDefinition(type, origKey);
		IdmFormDefinitionDto newFormDef = formService.getDefinition(type, newKey);
		Assert.assertNotNull(origFormDef);
		Assert.assertNotNull(newFormDef);
		Assert.assertNotEquals(origFormDef.getId(), newFormDef.getId());
		Assert.assertTrue(compareFormDefinition(newFormDef, origFormDef));
		
		List<IdmFormAttributeDto> origFormAttrs = formService.getAttributes(origFormDef);
		List<IdmFormAttributeDto> newFormAttrs = formService.getAttributes(newFormDef);
		Assert.assertEquals(origFormAttrs.size(), newFormAttrs.size());
		Assert.assertTrue(compareFormAttributeLists(origFormAttrs, newFormAttrs));
	}
	
	/**
	 * Compare form definition if equivalent
	 * @param def1
	 * @param def2
	 * @return
	 */
	private boolean compareFormDefinition(IdmFormDefinitionDto def1, IdmFormDefinitionDto def2) {
		return StringUtils.equals(def1.getType(), def2.getType()) &&
				StringUtils.equals(def1.getDescription(), def2.getDescription()) &&
				StringUtils.equals(def1.getModule(), def2.getModule()) &&
				def1.isMain() == def2.isMain() &&
				def1.isUnmodifiable() == def2.isUnmodifiable();
	}
	
	/**
	 * Compare lists of form attributes if equivalent
	 * @param attrs1
	 * @param attrs2
	 * @return
	 */
	private boolean compareFormAttributeLists(List<IdmFormAttributeDto> attrs1, List<IdmFormAttributeDto> attrs2) {
		for (IdmFormAttributeDto attr1: attrs1) {
			boolean exists = attrs2.stream()
			.filter((attr2) -> {return compareFormAttribute(attr1, attr2);})
			.count() != 0;
			if (!exists) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Form attribute comparison predicate
	 * 
	 * @param attr1
	 * @param attr2
	 * @return
	 */
	private boolean compareFormAttribute(IdmFormAttributeDto attr1, IdmFormAttributeDto attr2) {
		return StringUtils.equals(attr1.getCode(), attr2.getCode()) &&
				StringUtils.equals(attr1.getName(), attr2.getName()) &&
				StringUtils.equals(attr1.getDescription(), attr2.getDescription()) &&
				StringUtils.equals(attr1.getPlaceholder(), attr2.getPlaceholder()) &&
				StringUtils.equals(attr1.getFaceType(), attr2.getFaceType()) &&
				StringUtils.equals(attr1.getDefaultValue(), attr2.getDefaultValue()) &&
				StringUtils.equals(attr1.getRegex(), attr2.getRegex()) &&
				StringUtils.equals(attr1.getValidationMessage(), attr2.getValidationMessage()) &&
				StringUtils.equals(attr1.getModule(), attr2.getModule()) &&
				
				attr1.isMultiple() == attr2.isMultiple() &&
				attr1.isRequired() == attr2.isRequired() &&
				attr1.isReadonly() == attr2.isReadonly() &&
				attr1.isConfidential() == attr2.isConfidential() &&
				attr1.isUnmodifiable() == attr2.isUnmodifiable() &&
				attr1.isUnique() == attr2.isUnique() &&
				
				Objects.equals(attr1.getMin(), attr2.getMin()) &&
				Objects.equals(attr1.getMax(), attr2.getMax()) &&
				Objects.equals(attr1.getSeq(), attr2.getSeq()) &&
				Objects.equals(attr1.getPersistentType(), attr2.getPersistentType()) &&
				Objects.equals(attr1.getProperties().toMap(), attr2.getProperties().toMap());
	}
	
		
}
