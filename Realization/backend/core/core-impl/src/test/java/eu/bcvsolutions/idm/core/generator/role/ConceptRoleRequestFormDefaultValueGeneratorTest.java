package eu.bcvsolutions.idm.core.generator.role;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.domain.RoleRequestedByType;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmGenerateValueDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.generator.AbstractGeneratorTest;
import eu.bcvsolutions.idm.core.api.service.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmGenerateValueService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleFormAttributeService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormAttributeService;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormDefinitionService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;

/**
 * Tests for {@link ConceptRoleRequestFormDefaultValueGenerator}
 *
 * @author Ondrej Kopr
 * @since 9.4.0
 *
 */
public class ConceptRoleRequestFormDefaultValueGeneratorTest extends AbstractGeneratorTest {

	@Autowired
	private IdmFormDefinitionService formDefinitionService;
	@Autowired
	private IdmFormAttributeService formAttributeService;
	@Autowired
	private IdmRoleService roleService;
	@Autowired
	private IdmGenerateValueService generateValueService;
	@Autowired
	private IdmRoleRequestService roleRequestService;
	@Autowired
	private IdmConceptRoleRequestService conceptRoleRequestService;
	@Autowired
	private IdmRoleFormAttributeService roleFormAttributeService;

	@Test
	public void testGreenLine() {
		IdmRoleDto role = getHelper().createRole();

		// prepare new form definition
		IdmFormDefinitionDto formDefinition = createFormDefinition();
		role.setIdentityRoleAttributeDefinition(formDefinition.getId());
		role = roleService.save(role);

		// prepare form attribute 1
		String attrCode1 = getHelper().createName();
		String attrDefaultValue1 = "666";
		IdmFormAttributeDto att1 = createAttribute(attrCode1, attrDefaultValue1, PersistentType.LONG,
				formDefinition.getId());
		roleFormAttributeService.addAttributeToSubdefintion(role, att1);

		// prepare form attribute 2
		String attrCode2 = getHelper().createName();
		String attrDefaultValue2 = getHelper().createName() + getHelper().createName();
		IdmFormAttributeDto att2 = createAttribute(attrCode2, attrDefaultValue2, PersistentType.SHORTTEXT,
				formDefinition.getId());
		roleFormAttributeService.addAttributeToSubdefintion(role, att2);

		// prepare form attribute 3 without default value
		String attrCode3 = getHelper().createName();
		IdmFormAttributeDto att3 = createAttribute(attrCode3, null, PersistentType.SHORTTEXT, formDefinition.getId());
		roleFormAttributeService.addAttributeToSubdefintion(role, att3);

		IdmConceptRoleRequestDto roleRequestDto = new IdmConceptRoleRequestDto();
		roleRequestDto.setRole(role.getId());

		// check eav before
		List<IdmFormInstanceDto> eavs = roleRequestDto.getEavs();
		assertTrue(eavs.isEmpty());

		createGenerator();

		// generate and check values after
		roleRequestDto = this.valueGeneratorManager.generate(roleRequestDto);
		eavs = roleRequestDto.getEavs();
		assertFalse(eavs.isEmpty());

		// get newly generated eav only for given form definition
		IdmFormInstanceDto generatedEav = eavs.stream()
				.filter(eav -> eav.getFormDefinition().getCode().equals(formDefinition.getCode())).findFirst()
				.orElse(null);
		assertNotNull(generatedEav);

		// check values
		List<IdmFormValueDto> values = generatedEav.getValues().stream()
				.filter(val -> val.getFormAttribute().equals(att1.getId())).collect(Collectors.toList());
		assertEquals(1, values.size());
		IdmFormValueDto value = values.get(0);
		assertEquals(attrDefaultValue1, value.getValue().toString());

		values = generatedEav.getValues().stream().filter(val -> val.getFormAttribute().equals(att2.getId()))
				.collect(Collectors.toList());
		assertEquals(1, values.size());
		value = values.get(0);
		assertEquals(attrDefaultValue2, value.getValue().toString());

		values = generatedEav.getValues().stream().filter(val -> val.getFormAttribute().equals(att3.getId()))
				.collect(Collectors.toList());
		assertEquals(0, values.size());
	}

	@Test
	public void testGreenLineWithSave() {
		// prepare identity
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmRoleDto role = getHelper().createRole();
		IdmIdentityContractDto primeContract = getHelper().getPrimeContract(identity);

		// prepare new form definition
		IdmFormDefinitionDto formDefinition = createFormDefinition();
		role.setIdentityRoleAttributeDefinition(formDefinition.getId());
		role = roleService.save(role);

		// prepare form attribute 1
		String attrCode1 = getHelper().createName();
		String attrDefaultValue1 = "100200";
		IdmFormAttributeDto att1 = createAttribute(attrCode1, attrDefaultValue1, PersistentType.LONG,
				formDefinition.getId());
		roleFormAttributeService.addAttributeToSubdefintion(role, att1);

		// prepare form attribute 2
		String attrCode2 = getHelper().createName();
		String attrDefaultValue2 = getHelper().createName() + getHelper().createName();
		IdmFormAttributeDto att2 = createAttribute(attrCode2, attrDefaultValue2, PersistentType.SHORTTEXT,
				formDefinition.getId());
		roleFormAttributeService.addAttributeToSubdefintion(role, att2);

		// prepare form attribute 3 without default value
		String attrCode3 = getHelper().createName();
		IdmFormAttributeDto att3 = createAttribute(attrCode3, null, PersistentType.SHORTTEXT, formDefinition.getId());
		roleFormAttributeService.addAttributeToSubdefintion(role, att3);
		
		IdmRoleRequestDto requestDto = new IdmRoleRequestDto();
		requestDto.setExecuteImmediately(true);
		requestDto.setApplicant(identity.getId());
		requestDto.setRequestedByType(RoleRequestedByType.MANUALLY);
		requestDto = roleRequestService.save(requestDto);

		IdmConceptRoleRequestDto roleRequestDto = new IdmConceptRoleRequestDto();
		roleRequestDto.setRole(role.getId());
		roleRequestDto.setIdentityContract(primeContract.getId());
		roleRequestDto.setRoleRequest(requestDto.getId());

		// check eav before
		List<IdmFormInstanceDto> eavs = roleRequestDto.getEavs();
		assertTrue(eavs.isEmpty());

		createGenerator();

		// generate and check values after
		roleRequestDto = this.conceptRoleRequestService.save(roleRequestDto);
		eavs = roleRequestDto.getEavs();
		assertFalse(eavs.isEmpty());
		assertEquals(1, eavs.size());

		IdmFormInstanceDto generatedEav = eavs.get(0);
		assertNotNull(generatedEav);

		// check values
		List<IdmFormValueDto> values = generatedEav.getValues().stream()
				.filter(val -> val.getFormAttribute().equals(att1.getId())).collect(Collectors.toList());
		assertEquals(1, values.size());
		IdmFormValueDto value = values.get(0);
		assertEquals(attrDefaultValue1, value.getValue().toString());

		values = generatedEav.getValues().stream().filter(val -> val.getFormAttribute().equals(att2.getId()))
				.collect(Collectors.toList());
		assertEquals(1, values.size());
		value = values.get(0);
		assertEquals(attrDefaultValue2, value.getValue().toString());

		values = generatedEav.getValues().stream().filter(val -> val.getFormAttribute().equals(att3.getId()))
				.collect(Collectors.toList());
		assertEquals(0, values.size());
	}

	@Test
	public void testGivenValue() {
		String overrideValue = "text-" + System.currentTimeMillis();
		// prepare identity
		IdmRoleDto role = getHelper().createRole();

		// prepare new form definition
		IdmFormDefinitionDto formDefinition = createFormDefinition();
		role.setIdentityRoleAttributeDefinition(formDefinition.getId());
		role = roleService.save(role);

		// prepare form attribute 1
		String attrCode1 = getHelper().createName();
		String attrDefaultValue1 = "666";
		IdmFormAttributeDto att1 = createAttribute(attrCode1, attrDefaultValue1, PersistentType.LONG,
				formDefinition.getId());
		roleFormAttributeService.addAttributeToSubdefintion(role, att1);

		// prepare form attribute 2
		String attrCode2 = getHelper().createName();
		String attrDefaultValue2 = getHelper().createName() + getHelper().createName();
		IdmFormAttributeDto att2 = createAttribute(attrCode2, attrDefaultValue2, PersistentType.SHORTTEXT,
				formDefinition.getId());
		roleFormAttributeService.addAttributeToSubdefintion(role, att2);

		// prepare form attribute 3 without default value
		String attrCode3 = getHelper().createName();
		IdmFormAttributeDto att3 = createAttribute(attrCode3, null, PersistentType.SHORTTEXT, formDefinition.getId());
		roleFormAttributeService.addAttributeToSubdefintion(role, att3);

		IdmConceptRoleRequestDto roleRequestDto = new IdmConceptRoleRequestDto();
		roleRequestDto.setRole(role.getId());

		IdmFormInstanceDto formInstanceDto = new IdmFormInstanceDto();
		formInstanceDto.setFormDefinition(formDefinition);
		IdmFormValueDto formValueDto = new IdmFormValueDto();
		formValueDto.setFormAttribute(att2.getId());
		formValueDto.setShortTextValue(overrideValue);
		formInstanceDto.setValues(Lists.newArrayList(formValueDto));
		roleRequestDto.getEavs().clear();
		roleRequestDto.getEavs().add(formInstanceDto);

		// check eav before
		List<IdmFormInstanceDto> eavs = roleRequestDto.getEavs();
		assertFalse(eavs.isEmpty());

		createGenerator();

		// generate and check values after
		roleRequestDto = this.valueGeneratorManager.generate(roleRequestDto);
		eavs = roleRequestDto.getEavs();
		assertFalse(eavs.isEmpty());

		// get newly generated eav only for given form definition
		IdmFormInstanceDto generatedEav = eavs.stream()
				.filter(eav -> eav.getFormDefinition().getCode().equals(formDefinition.getCode())).findFirst()
				.orElse(null);
		assertNotNull(generatedEav);

		// check values
		List<IdmFormValueDto> values = generatedEav.getValues().stream()
				.filter(val -> val.getFormAttribute().equals(att1.getId())).collect(Collectors.toList());
		assertEquals(1, values.size());
		IdmFormValueDto value = values.get(0);
		assertEquals(attrDefaultValue1, value.getValue().toString());

		values = generatedEav.getValues().stream().filter(val -> val.getFormAttribute().equals(att2.getId()))
				.collect(Collectors.toList());
		assertEquals(1, values.size());
		value = values.get(0);
		assertNotEquals(attrDefaultValue2, value.getValue().toString());
		assertEquals(overrideValue, value.getValue().toString());

		values = generatedEav.getValues().stream().filter(val -> val.getFormAttribute().equals(att3.getId()))
				.collect(Collectors.toList());
		assertEquals(0, values.size());
	}

	@Override
	protected Class<? extends AbstractDto> getDtoType() {
		return IdmConceptRoleRequestDto.class;
	}

	@Override
	protected String getGeneratorType() {
		return ConceptRoleRequestFormDefaultValueGenerator.class.getCanonicalName();
	}

	/**
	 * Create new attribute with default value and given code.
	 *
	 * @param attCode
	 * @param attDefaultValue
	 * @param type
	 * @param formDefinition
	 * @return
	 */
	private IdmFormAttributeDto createAttribute(String attCode, String attDefaultValue, PersistentType type,
			UUID formDefinition) {
		IdmFormAttributeDto attributeDto = new IdmFormAttributeDto(attCode, attCode, type);
		attributeDto.setDefaultValue(attDefaultValue);
		attributeDto.setFormDefinition(formDefinition);
		return formAttributeService.save(attributeDto);
	}

	/**
	 * Create new definition for identity
	 *
	 * @return
	 */
	private IdmFormDefinitionDto createFormDefinition() {
		return createFormDefinition(getHelper().createName());
	}

	/**
	 * Create new definition for identity role with name
	 *
	 * @return
	 */
	private IdmFormDefinitionDto createFormDefinition(String name) {
		IdmFormDefinitionDto formDefinition = new IdmFormDefinitionDto();
		formDefinition.setCode(name);
		formDefinition.setName(name);
		formDefinition.setType(IdmIdentityRole.class.getCanonicalName());
		return formDefinitionService.save(formDefinition);
	}

	/**
	 * Method create generator for this test and remove all another generators.
	 * 
	 * @return
	 *
	 */
	private IdmGenerateValueDto createGenerator() {
		IdmGenerateValueDto generateValue = new IdmGenerateValueDto();
		generateValue.setDtoType(getDtoType().getCanonicalName());
		generateValue.setGeneratorType(getGeneratorType());
		generateValue.setSeq((short) 100);
		generateValue.setUnmodifiable(true);
		return generateValueService.save(generateValue);
	}
}
