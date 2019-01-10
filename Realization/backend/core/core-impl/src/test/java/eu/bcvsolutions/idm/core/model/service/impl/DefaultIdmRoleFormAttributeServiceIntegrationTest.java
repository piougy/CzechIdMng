package eu.bcvsolutions.idm.core.model.service.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

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
	private final static String SHORT_TEXT = "SHORT_TEXT";

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
			return formAttributeDto.getCode().equals(SHORT_TEXT);
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

	private IdmRoleDto createRoleWithAttributes() {
		IdmRoleDto role = getHelper().createRole();
		assertNull(role.getIdentityRoleAttributeDefinition());

		IdmFormAttributeDto ipAttribute = new IdmFormAttributeDto(IP);
		ipAttribute.setPersistentType(PersistentType.TEXT);
		ipAttribute.setRequired(true);
		ipAttribute.setDefaultValue(getHelper().createName());

		IdmFormAttributeDto shortAttribute = new IdmFormAttributeDto(SHORT_TEXT);
		shortAttribute.setPersistentType(PersistentType.SHORTTEXT);
		shortAttribute.setRequired(false);

		IdmFormDefinitionDto definition = formService.createDefinition(IdmIdentityRole.class, getHelper().createName(),
				ImmutableList.of(ipAttribute, shortAttribute));
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
