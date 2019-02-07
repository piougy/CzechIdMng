package eu.bcvsolutions.idm.core.model.service.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.math.BigDecimal;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.ImmutableList;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleFormAttributeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleFormAttributeFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.IdmRoleFormAttributeService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleFormAttribute_;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Basic role form attribute service test
 * 
 * @author Vít Švanda
 */
public class DefaultIdmRoleFormAttributeServiceIntegrationTest extends AbstractIntegrationTest {

	@Autowired
	private IdmRoleService roleService;
	@Autowired
	private FormService formService;
	@Autowired
	private IdmRoleFormAttributeService roleFormAttributeService;

	private final static String IP = "IP";
	private final static String NUMBER_OF_FINGERS = "NUMBER_OF_FINGERS";

	@Test
	public void testCreateRoleFormAttributeByFormAttribute() {

		// Create role with attribute (include the sub-definition)
		IdmRoleDto role = createRoleWithAttributes();
		IdmRoleFormAttributeFilter filter = new IdmRoleFormAttributeFilter();
		filter.setRole(role.getId());
		List<IdmRoleFormAttributeDto> list = roleFormAttributeService.find(filter, null).getContent();
		Assert.assertEquals(2, list.size());

		list.forEach(roleFormAttributeDto -> {
			IdmFormAttributeDto formAttributeDto = DtoUtils.getEmbedded(roleFormAttributeDto,
					IdmRoleFormAttribute_.formAttribute.getName(), IdmFormAttributeDto.class);
			Assert.assertEquals(roleFormAttributeDto.getDefaultValue(), formAttributeDto.getDefaultValue());
			Assert.assertEquals(roleFormAttributeDto.isRequired(), formAttributeDto.isRequired());
		});

	}

	@Test(expected = ResultCodeException.class)
	public void testChangeOfSuperdefinitionNotAllowed() {

		// Create role with attribute (include the sub-definition)
		IdmRoleDto role = createRoleWithAttributes();
		IdmRoleFormAttributeFilter filter = new IdmRoleFormAttributeFilter();
		filter.setRole(role.getId());
		List<IdmRoleFormAttributeDto> list = roleFormAttributeService.find(filter, null).getContent();
		Assert.assertEquals(2, list.size());

		IdmFormDefinitionDto definitionTwo = formService.createDefinition(IdmIdentityRole.class,
				getHelper().createName(), ImmutableList.of());
		role.setIdentityRoleAttributeDefinition(definitionTwo.getId());
		// Save role - change of definition is not allowed (if exists some
		// role-form-attribute) -> throw exception
		roleService.save(role);
	}

	@Test
	public void testChangeOfSuperdefinitionAllowed() {

		// Create role with attribute (include the sub-definition)
		IdmRoleDto role = createRoleWithAttributes();
		IdmRoleFormAttributeFilter filter = new IdmRoleFormAttributeFilter();
		filter.setRole(role.getId());
		List<IdmRoleFormAttributeDto> list = roleFormAttributeService.find(filter, null).getContent();
		Assert.assertEquals(2, list.size());

		// Before change of definition we will delete all attributes in sub-definition
		// first
		list.forEach(attribute -> {
			roleFormAttributeService.delete(attribute);
		});

		IdmFormDefinitionDto definitionTwo = formService.createDefinition(IdmIdentityRole.class,
				getHelper().createName(), ImmutableList.of());
		role.setIdentityRoleAttributeDefinition(definitionTwo.getId());
		// Save role - change of definition is allowed (none role-form-attribute exists)
		roleService.save(role);
	}

	@Test
	public void testSubDefinition() {

		// Create role with attribute (include the sub-definition)
		IdmRoleDto role = createRoleWithAttributes();
		IdmRoleFormAttributeFilter filter = new IdmRoleFormAttributeFilter();
		filter.setRole(role.getId());
		List<IdmRoleFormAttributeDto> list = roleFormAttributeService.find(filter, null).getContent();
		Assert.assertEquals(2, list.size());

		IdmFormDefinitionDto formAttributeSubdefinition = roleService.getFormAttributeSubdefinition(role);
		Assert.assertEquals(2, formAttributeSubdefinition.getFormAttributes().size());

		// Delete shortText attribute from the sub-definition
		list.stream().filter(roleFormAttributeDto -> {
			IdmFormAttributeDto formAttributeDto = DtoUtils.getEmbedded(roleFormAttributeDto,
					IdmRoleFormAttribute_.formAttribute.getName(), IdmFormAttributeDto.class);
			return formAttributeDto.getCode().equals(NUMBER_OF_FINGERS);
		}).forEach(roleFormAttributeDto -> roleFormAttributeService.delete(roleFormAttributeDto));

		formAttributeSubdefinition = roleService.getFormAttributeSubdefinition(role);
		Assert.assertEquals(1, formAttributeSubdefinition.getFormAttributes().size());
		Assert.assertEquals(IP, formAttributeSubdefinition.getFormAttributes().get(0).getCode());

	}

	@Test
	public void testSubDefinitionOverrideDefaultValue() {

		// Create role with attribute (include the sub-definition)
		IdmRoleDto role = createRoleWithAttributes();
		IdmRoleFormAttributeFilter filter = new IdmRoleFormAttributeFilter();
		filter.setRole(role.getId());
		List<IdmRoleFormAttributeDto> list = roleFormAttributeService.find(filter, null).getContent();
		Assert.assertEquals(2, list.size());

		IdmFormDefinitionDto formAttributeSubdefinition = roleService.getFormAttributeSubdefinition(role);
		Assert.assertEquals(2, formAttributeSubdefinition.getFormAttributes().size());

		// Set unique defaultValue to IP attribute in the sub-definition
		String uniqueDefaultValue = this.getHelper().createName();
		list.stream().filter(roleFormAttributeDto -> {
			IdmFormAttributeDto formAttributeDto = DtoUtils.getEmbedded(roleFormAttributeDto,
					IdmRoleFormAttribute_.formAttribute.getName(), IdmFormAttributeDto.class);
			return formAttributeDto.getCode().equals(IP);
		}).forEach(roleFormAttributeDto -> {
			roleFormAttributeDto.setDefaultValue(uniqueDefaultValue);
			roleFormAttributeService.save(roleFormAttributeDto);
		});

		// Load sub-definition by role
		formAttributeSubdefinition = roleService.getFormAttributeSubdefinition(role);
		Assert.assertEquals(2, formAttributeSubdefinition.getFormAttributes().size());
		IdmFormAttributeDto ipFormAttribute = formAttributeSubdefinition.getFormAttributes().stream()
				.filter(attributeDto -> {
					return attributeDto.getCode().equals(IP);
				}).findFirst().orElse(null);

		Assert.assertNotNull(ipFormAttribute);
		Assert.assertEquals(uniqueDefaultValue, ipFormAttribute.getDefaultValue());
	}

	@Test
	public void testSubDefinitionOverrideValidationRequired() {

		// Create role with attribute (include the sub-definition)
		IdmRoleDto role = createRoleWithAttributes();
		IdmRoleFormAttributeFilter filter = new IdmRoleFormAttributeFilter();
		filter.setRole(role.getId());
		List<IdmRoleFormAttributeDto> list = roleFormAttributeService.find(filter, null).getContent();
		Assert.assertEquals(2, list.size());

		IdmFormDefinitionDto formAttributeSubdefinition = roleService.getFormAttributeSubdefinition(role);
		Assert.assertEquals(2, formAttributeSubdefinition.getFormAttributes().size());

		// Set required validation on false to IP attribute in the sub-definition
		list.stream().filter(roleFormAttributeDto -> {
			IdmFormAttributeDto formAttributeDto = DtoUtils.getEmbedded(roleFormAttributeDto,
					IdmRoleFormAttribute_.formAttribute.getName(), IdmFormAttributeDto.class);
			return formAttributeDto.getCode().equals(IP);
		}).forEach(roleFormAttributeDto -> {
			Assert.assertTrue(roleFormAttributeDto.isRequired());
			roleFormAttributeDto.setRequired(false);
			roleFormAttributeService.save(roleFormAttributeDto);
		});

		// Load sub-definition by role
		formAttributeSubdefinition = roleService.getFormAttributeSubdefinition(role);
		Assert.assertEquals(2, formAttributeSubdefinition.getFormAttributes().size());
		IdmFormAttributeDto ipFormAttribute = formAttributeSubdefinition.getFormAttributes().stream()
				.filter(attributeDto -> {
					return attributeDto.getCode().equals(IP);
				}).findFirst().orElse(null);

		Assert.assertNotNull(ipFormAttribute);
		Assert.assertFalse(ipFormAttribute.isRequired());
	}
	
	@Test
	public void testSubDefinitionOverrideValidationUnique() {

		// Create role with attribute (include the sub-definition)
		IdmRoleDto role = createRoleWithAttributes();
		IdmRoleFormAttributeFilter filter = new IdmRoleFormAttributeFilter();
		filter.setRole(role.getId());
		List<IdmRoleFormAttributeDto> list = roleFormAttributeService.find(filter, null).getContent();
		Assert.assertEquals(2, list.size());

		IdmFormDefinitionDto formAttributeSubdefinition = roleService.getFormAttributeSubdefinition(role);
		Assert.assertEquals(2, formAttributeSubdefinition.getFormAttributes().size());

		// Set unique validation on false to IP attribute in the sub-definition
		list.stream().filter(roleFormAttributeDto -> {
			IdmFormAttributeDto formAttributeDto = DtoUtils.getEmbedded(roleFormAttributeDto,
					IdmRoleFormAttribute_.formAttribute.getName(), IdmFormAttributeDto.class);
			return formAttributeDto.getCode().equals(IP);
		}).forEach(roleFormAttributeDto -> {
			Assert.assertFalse(roleFormAttributeDto.isUnique());
			roleFormAttributeDto.setUnique(true);
			roleFormAttributeService.save(roleFormAttributeDto);
		});

		// Load sub-definition by role
		formAttributeSubdefinition = roleService.getFormAttributeSubdefinition(role);
		Assert.assertEquals(2, formAttributeSubdefinition.getFormAttributes().size());
		IdmFormAttributeDto ipFormAttribute = formAttributeSubdefinition.getFormAttributes().stream()
				.filter(attributeDto -> {
					return attributeDto.getCode().equals(IP);
				}).findFirst().orElse(null);

		Assert.assertNotNull(ipFormAttribute);
		Assert.assertTrue(ipFormAttribute.isUnique());
	}
	
	@Test
	public void testSubDefinitionOverrideValidationReqex() {
		String regex = "regex";

		// Create role with attribute (include the sub-definition)
		IdmRoleDto role = createRoleWithAttributes();
		IdmRoleFormAttributeFilter filter = new IdmRoleFormAttributeFilter();
		filter.setRole(role.getId());
		List<IdmRoleFormAttributeDto> list = roleFormAttributeService.find(filter, null).getContent();
		Assert.assertEquals(2, list.size());

		IdmFormDefinitionDto formAttributeSubdefinition = roleService.getFormAttributeSubdefinition(role);
		Assert.assertEquals(2, formAttributeSubdefinition.getFormAttributes().size());

		// Set regex validation on false to IP attribute in the sub-definition
		list.stream().filter(roleFormAttributeDto -> {
			IdmFormAttributeDto formAttributeDto = DtoUtils.getEmbedded(roleFormAttributeDto,
					IdmRoleFormAttribute_.formAttribute.getName(), IdmFormAttributeDto.class);
			return formAttributeDto.getCode().equals(IP);
		}).forEach(roleFormAttributeDto -> {
			Assert.assertNull(roleFormAttributeDto.getRegex());
			roleFormAttributeDto.setRegex(regex);
			roleFormAttributeService.save(roleFormAttributeDto);
		});

		// Load sub-definition by role
		formAttributeSubdefinition = roleService.getFormAttributeSubdefinition(role);
		Assert.assertEquals(2, formAttributeSubdefinition.getFormAttributes().size());
		IdmFormAttributeDto ipFormAttribute = formAttributeSubdefinition.getFormAttributes().stream()
				.filter(attributeDto -> {
					return attributeDto.getCode().equals(IP);
				}).findFirst().orElse(null);

		Assert.assertNotNull(ipFormAttribute);
		Assert.assertEquals(regex, ipFormAttribute.getRegex());
	}
	
	@Test
	public void testSubDefinitionOverrideValidationMessage() {
		String validationMessage = getHelper().createName();

		// Create role with attribute (include the sub-definition)
		IdmRoleDto role = createRoleWithAttributes();
		IdmRoleFormAttributeFilter filter = new IdmRoleFormAttributeFilter();
		filter.setRole(role.getId());
		List<IdmRoleFormAttributeDto> list = roleFormAttributeService.find(filter, null).getContent();
		Assert.assertEquals(2, list.size());

		IdmFormDefinitionDto formAttributeSubdefinition = roleService.getFormAttributeSubdefinition(role);
		Assert.assertEquals(2, formAttributeSubdefinition.getFormAttributes().size());

		// Set validation message on false to IP attribute in the sub-definition
		list.stream().filter(roleFormAttributeDto -> {
			IdmFormAttributeDto formAttributeDto = DtoUtils.getEmbedded(roleFormAttributeDto,
					IdmRoleFormAttribute_.formAttribute.getName(), IdmFormAttributeDto.class);
			return formAttributeDto.getCode().equals(IP);
		}).forEach(roleFormAttributeDto -> {
			Assert.assertNull(roleFormAttributeDto.getValidationMessage());
			roleFormAttributeDto.setValidationMessage(validationMessage);
			roleFormAttributeService.save(roleFormAttributeDto);
		});

		// Load sub-definition by role
		formAttributeSubdefinition = roleService.getFormAttributeSubdefinition(role);
		Assert.assertEquals(2, formAttributeSubdefinition.getFormAttributes().size());
		IdmFormAttributeDto ipFormAttribute = formAttributeSubdefinition.getFormAttributes().stream()
				.filter(attributeDto -> {
					return attributeDto.getCode().equals(IP);
				}).findFirst().orElse(null);

		Assert.assertNotNull(ipFormAttribute);
		Assert.assertEquals(validationMessage, ipFormAttribute.getValidationMessage());
	}
	
	@Test
	public void testSubDefinitionOverrideValidationMin() {

		// Create role with attribute (include the sub-definition)
		IdmRoleDto role = createRoleWithAttributes();
		IdmRoleFormAttributeFilter filter = new IdmRoleFormAttributeFilter();
		filter.setRole(role.getId());
		List<IdmRoleFormAttributeDto> list = roleFormAttributeService.find(filter, null).getContent();
		Assert.assertEquals(2, list.size());

		IdmFormDefinitionDto formAttributeSubdefinition = roleService.getFormAttributeSubdefinition(role);
		Assert.assertEquals(2, formAttributeSubdefinition.getFormAttributes().size());

		// Delete IP attribute from the sub-definition
		list.stream().filter(roleFormAttributeDto -> {
			IdmFormAttributeDto formAttributeDto = DtoUtils.getEmbedded(roleFormAttributeDto,
					IdmRoleFormAttribute_.formAttribute.getName(), IdmFormAttributeDto.class);
			return formAttributeDto.getCode().equals(IP);
		}).forEach(roleFormAttributeDto -> roleFormAttributeService.delete(roleFormAttributeDto));

		// Set MIN
		list.stream().filter(roleFormAttributeDto -> {
			IdmFormAttributeDto formAttributeDto = DtoUtils.getEmbedded(roleFormAttributeDto,
					IdmRoleFormAttribute_.formAttribute.getName(), IdmFormAttributeDto.class);
			return formAttributeDto.getCode().equals(NUMBER_OF_FINGERS);
		}).forEach(roleFormAttributeDto -> {
			Assert.assertNull(roleFormAttributeDto.getMin());
			roleFormAttributeDto.setMin(BigDecimal.valueOf(111));
			roleFormAttributeService.save(roleFormAttributeDto);
		});

		// Load sub-definition by role
		formAttributeSubdefinition = roleService.getFormAttributeSubdefinition(role);
		Assert.assertEquals(1, formAttributeSubdefinition.getFormAttributes().size());
		IdmFormAttributeDto numberAttribute = formAttributeSubdefinition.getFormAttributes().stream()
				.filter(attributeDto -> {
					return attributeDto.getCode().equals(NUMBER_OF_FINGERS);
				}).findFirst().orElse(null);

		Assert.assertNotNull(numberAttribute);
		Assert.assertEquals(111, numberAttribute.getMin().intValue());
	}
	
	@Test
	public void testSubDefinitionOverrideValidationMax() {

		// Create role with attribute (include the sub-definition)
		IdmRoleDto role = createRoleWithAttributes();
		IdmRoleFormAttributeFilter filter = new IdmRoleFormAttributeFilter();
		filter.setRole(role.getId());
		List<IdmRoleFormAttributeDto> list = roleFormAttributeService.find(filter, null).getContent();
		Assert.assertEquals(2, list.size());

		IdmFormDefinitionDto formAttributeSubdefinition = roleService.getFormAttributeSubdefinition(role);
		Assert.assertEquals(2, formAttributeSubdefinition.getFormAttributes().size());

		// Delete IP attribute from the sub-definition
		list.stream().filter(roleFormAttributeDto -> {
			IdmFormAttributeDto formAttributeDto = DtoUtils.getEmbedded(roleFormAttributeDto,
					IdmRoleFormAttribute_.formAttribute.getName(), IdmFormAttributeDto.class);
			return formAttributeDto.getCode().equals(IP);
		}).forEach(roleFormAttributeDto -> roleFormAttributeService.delete(roleFormAttributeDto));
		
		// Set Max
		list.stream().filter(roleFormAttributeDto -> {
			IdmFormAttributeDto formAttributeDto = DtoUtils.getEmbedded(roleFormAttributeDto,
					IdmRoleFormAttribute_.formAttribute.getName(), IdmFormAttributeDto.class);
			return formAttributeDto.getCode().equals(NUMBER_OF_FINGERS);
		}).forEach(roleFormAttributeDto -> {
			Assert.assertNull(roleFormAttributeDto.getMax());
			roleFormAttributeDto.setMax(BigDecimal.valueOf(111));
			roleFormAttributeService.save(roleFormAttributeDto);
		});

		// Load sub-definition by role
		formAttributeSubdefinition = roleService.getFormAttributeSubdefinition(role);
		Assert.assertEquals(1, formAttributeSubdefinition.getFormAttributes().size());
		IdmFormAttributeDto numberAttribute = formAttributeSubdefinition.getFormAttributes().stream()
				.filter(attributeDto -> {
					return attributeDto.getCode().equals(NUMBER_OF_FINGERS);
				}).findFirst().orElse(null);

		Assert.assertNotNull(numberAttribute);
		Assert.assertEquals(111, numberAttribute.getMax().intValue()); 
	}
	
	@Test(expected = ResultCodeException.class)
	public void testIntegrityDeleteAttributeDefinition() {

		// Create role with attribute (include the sub-definition)
		IdmRoleDto role = createRoleWithAttributes();
		IdmRoleFormAttributeFilter filter = new IdmRoleFormAttributeFilter();
		filter.setRole(role.getId());
		List<IdmRoleFormAttributeDto> list = roleFormAttributeService.find(filter, null).getContent();
		Assert.assertEquals(2, list.size());

		IdmFormDefinitionDto formAttributeSubdefinition = roleService.getFormAttributeSubdefinition(role);
		Assert.assertEquals(2, formAttributeSubdefinition.getFormAttributes().size());
		// Find attribute definition
		IdmFormAttributeDto formAttributeDto = DtoUtils.getEmbedded(list.get(0),
				IdmRoleFormAttribute_.formAttribute.getName(), IdmFormAttributeDto.class);
		Assert.assertNotNull(formAttributeDto);

		// Definition of this attribute is using in the sub-definition -> exception must
		// be throws
		formService.deleteAttribute(formAttributeDto);
	}

	private IdmRoleDto createRoleWithAttributes() {
		IdmRoleDto role = getHelper().createRole();
		assertNull(role.getIdentityRoleAttributeDefinition());

		IdmFormAttributeDto ipAttribute = new IdmFormAttributeDto(IP);
		ipAttribute.setPersistentType(PersistentType.TEXT);
		ipAttribute.setRequired(true);
		ipAttribute.setDefaultValue(getHelper().createName());

		IdmFormAttributeDto numberOfFingersAttribute = new IdmFormAttributeDto(NUMBER_OF_FINGERS);
		numberOfFingersAttribute.setPersistentType(PersistentType.DOUBLE);
		numberOfFingersAttribute.setRequired(false);
		
		IdmFormDefinitionDto definition = formService.createDefinition(IdmIdentityRole.class, getHelper().createName(),
				ImmutableList.of(ipAttribute, numberOfFingersAttribute));
		role.setIdentityRoleAttributeDefinition(definition.getId());
		role = roleService.save(role);
		assertNotNull(role.getIdentityRoleAttributeDefinition());
		IdmRoleDto roleFinal = role;
		definition.getFormAttributes().forEach(attribute -> {
			roleFormAttributeService.addAttributeToSubdefintion(roleFinal, attribute);
		});

		return role;
	}
}
