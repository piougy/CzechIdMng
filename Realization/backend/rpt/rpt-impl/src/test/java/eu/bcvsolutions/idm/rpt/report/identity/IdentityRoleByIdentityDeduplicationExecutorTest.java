package eu.bcvsolutions.idm.rpt.report.identity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeNodeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.ecm.api.service.AttachmentManager;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.rpt.api.dto.RptReportDto;
import eu.bcvsolutions.idm.rpt.dto.RptIdentityRoleByRoleDeduplicationDto;
import eu.bcvsolutions.idm.rpt.dto.RptIdentityRoleByRoleDeduplicationDuplicityDto;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Tests for {@link IdentityRoleByIdentityDeduplicationExecutor}
 *
 * @author Ondrej Kopr
 * @since 9.5.0
 *
 */
public class IdentityRoleByIdentityDeduplicationExecutorTest extends AbstractIntegrationTest {

	@Autowired 
	private IdentityRoleByIdentityDeduplicationExecutor reportExecutor;
	@Autowired
	private ObjectMapper mapper;
	@Autowired
	private AttachmentManager attachmentManager;
	@Autowired
	private IdentityRoleByIdentityDeduplicationXlsxRenderer xlsxRenderer;

	@Before
	public void before() {
		getHelper().loginAdmin();
	}
	
	@After
	public void after() {
		super.logout();
	}

	@Test
	public void testExecuteReportOneContract() throws JsonParseException, JsonMappingException, IOException {
		String roleCode = "test-" + System.currentTimeMillis(); 
		IdmTreeNodeDto treeNode = getHelper().createTreeNode();
		IdmIdentityDto identity = getHelper().createIdentity(new GuardedString());
		IdmIdentityContractDto contact = getHelper().createIdentityContact(identity, treeNode);
		
		IdmRoleDto role = getHelper().createRole(roleCode);

		getHelper().createIdentityRole(contact, role);
		getHelper().createIdentityRole(contact, role);

		RptReportDto report = new RptReportDto(UUID.randomUUID());
		report.setExecutorName(reportExecutor.getName());
		IdmFormDto filter = new IdmFormDto();
		IdmFormDefinitionDto definition = reportExecutor.getFormDefinition();
		IdmFormValueDto treeNodeParameter = new IdmFormValueDto(definition.getMappedAttributeByCode(IdentityRoleByIdentityDeduplicationExecutor.PARAMETER_TREE_NODE));
		treeNodeParameter.setValue(treeNode.getId());
		filter.getValues().add(treeNodeParameter);
		filter.setFormDefinition(definition.getId());
		report.setFilter(filter);

		report = reportExecutor.generate(report);
		Assert.assertNotNull(report.getData());
		List<RptIdentityRoleByRoleDeduplicationDto> reportItems = mapper.readValue(
				attachmentManager.getAttachmentData(report.getData()), 
				new TypeReference<List<RptIdentityRoleByRoleDeduplicationDto>>(){});

		assertEquals(1, reportItems.size());
		RptIdentityRoleByRoleDeduplicationDto item = reportItems.get(0);
		assertNotNull(item.getIdentity());
		assertEquals(identity.getId(), item.getIdentity().getId());
		assertNotNull(item.getWorkPosition());
		assertEquals(treeNode.getId(), item.getWorkPosition().getId());
		assertNotNull(item.getIdentityContract());
		assertEquals(contact.getId(), item.getIdentityContract().getId());

		List<RptIdentityRoleByRoleDeduplicationDuplicityDto> duplicities = item.getDuplicity();
		assertEquals(1, duplicities.size());
		RptIdentityRoleByRoleDeduplicationDuplicityDto duplicity = duplicities.get(0);
		assertNotNull(duplicity.getRole());
		assertEquals(role.getId(), duplicity.getRole().getId());

		attachmentManager.deleteAttachments(report);
	}

	@Test
	public void testExecuteReportOneContractMoreRoles() throws JsonParseException, JsonMappingException, IOException {
		String roleCode = "test-" + System.currentTimeMillis(); 
		IdmTreeNodeDto treeNode = getHelper().createTreeNode();
		IdmIdentityDto identity = getHelper().createIdentity(new GuardedString());
		IdmIdentityContractDto contact = getHelper().createIdentityContact(identity, treeNode);
		
		IdmRoleDto role = getHelper().createRole(roleCode);

		getHelper().createIdentityRole(contact, role);
		getHelper().createIdentityRole(contact, role);
		getHelper().createIdentityRole(contact, role);
		getHelper().createIdentityRole(contact, role);

		RptReportDto report = new RptReportDto(UUID.randomUUID());
		report.setExecutorName(reportExecutor.getName());
		IdmFormDto filter = new IdmFormDto();
		IdmFormDefinitionDto definition = reportExecutor.getFormDefinition();
		IdmFormValueDto treeNodeParameter = new IdmFormValueDto(definition.getMappedAttributeByCode(IdentityRoleByIdentityDeduplicationExecutor.PARAMETER_TREE_NODE));
		treeNodeParameter.setValue(treeNode.getId());
		filter.getValues().add(treeNodeParameter);
		filter.setFormDefinition(definition.getId());
		report.setFilter(filter);

		report = reportExecutor.generate(report);
		Assert.assertNotNull(report.getData());
		List<RptIdentityRoleByRoleDeduplicationDto> reportItems = mapper.readValue(
				attachmentManager.getAttachmentData(report.getData()), 
				new TypeReference<List<RptIdentityRoleByRoleDeduplicationDto>>(){});

		assertEquals(1, reportItems.size());
		RptIdentityRoleByRoleDeduplicationDto item = reportItems.get(0);

		List<RptIdentityRoleByRoleDeduplicationDuplicityDto> duplicities = item.getDuplicity();
		assertEquals(3, duplicities.size());
		for (RptIdentityRoleByRoleDeduplicationDuplicityDto duplicity : duplicities) {
			assertEquals(role.getId(), duplicity.getRole().getId());
		}

		attachmentManager.deleteAttachments(report);
	}

	@Test
	public void testExecuteReportOneContractMoreRolesWithValidity() throws JsonParseException, JsonMappingException, IOException {
		String roleCode = "test-" + System.currentTimeMillis(); 
		IdmTreeNodeDto treeNode = getHelper().createTreeNode();
		IdmIdentityDto identity = getHelper().createIdentity(new GuardedString());
		IdmIdentityContractDto contact = getHelper().createIdentityContact(identity, treeNode);
		
		IdmRoleDto role = getHelper().createRole(roleCode);

		getHelper().createIdentityRole(contact, role, LocalDate.now().minusDays(40), LocalDate.now().plusDays(40));
		getHelper().createIdentityRole(contact, role, LocalDate.now().minusDays(30), LocalDate.now().plusDays(30));
		getHelper().createIdentityRole(contact, role, LocalDate.now().minusDays(20), LocalDate.now().plusDays(20));
		getHelper().createIdentityRole(contact, role, LocalDate.now().minusDays(10), LocalDate.now().plusDays(10));

		RptReportDto report = new RptReportDto(UUID.randomUUID());
		report.setExecutorName(reportExecutor.getName());
		IdmFormDto filter = new IdmFormDto();
		IdmFormDefinitionDto definition = reportExecutor.getFormDefinition();
		IdmFormValueDto treeNodeParameter = new IdmFormValueDto(definition.getMappedAttributeByCode(IdentityRoleByIdentityDeduplicationExecutor.PARAMETER_TREE_NODE));
		treeNodeParameter.setValue(treeNode.getId());
		filter.getValues().add(treeNodeParameter);
		filter.setFormDefinition(definition.getId());
		report.setFilter(filter);

		report = reportExecutor.generate(report);
		Assert.assertNotNull(report.getData());
		List<RptIdentityRoleByRoleDeduplicationDto> reportItems = mapper.readValue(
				attachmentManager.getAttachmentData(report.getData()), 
				new TypeReference<List<RptIdentityRoleByRoleDeduplicationDto>>(){});

		assertEquals(1, reportItems.size());
		RptIdentityRoleByRoleDeduplicationDto item = reportItems.get(0);

		List<RptIdentityRoleByRoleDeduplicationDuplicityDto> duplicities = item.getDuplicity();
		assertEquals(3, duplicities.size());
		for (RptIdentityRoleByRoleDeduplicationDuplicityDto duplicity : duplicities) {
			assertEquals(role.getId(), duplicity.getRole().getId());
			if (duplicity.getValidTill().isEqual(duplicity.getValidFrom())) {
				fail();
			}
			if (!duplicity.getValidTill().isBefore(LocalDate.now().plusDays(35))) {
				fail();
			}
			if (!duplicity.getValidFrom().isAfter(LocalDate.now().minusDays(35))) {
				fail();
			}
		}

		attachmentManager.deleteAttachments(report);
	}

	@Test
	public void testExecuteReportTwoContractNoDuplicity() throws JsonParseException, JsonMappingException, IOException {
		String roleCode = "test-" + System.currentTimeMillis(); 
		IdmTreeNodeDto treeNode = getHelper().createTreeNode();
		IdmIdentityDto identity = getHelper().createIdentity(new GuardedString());
		IdmIdentityContractDto contactOne = getHelper().createIdentityContact(identity, treeNode);
		IdmIdentityContractDto contactTwo = getHelper().createIdentityContact(identity, treeNode);
		
		IdmRoleDto role = getHelper().createRole(roleCode);

		getHelper().createIdentityRole(contactOne, role);
		getHelper().createIdentityRole(contactTwo, role);

		RptReportDto report = new RptReportDto(UUID.randomUUID());
		report.setExecutorName(reportExecutor.getName());
		IdmFormDto filter = new IdmFormDto();
		IdmFormDefinitionDto definition = reportExecutor.getFormDefinition();
		IdmFormValueDto treeNodeParameter = new IdmFormValueDto(definition.getMappedAttributeByCode(IdentityRoleByIdentityDeduplicationExecutor.PARAMETER_TREE_NODE));
		treeNodeParameter.setValue(treeNode.getId());
		filter.getValues().add(treeNodeParameter);
		filter.setFormDefinition(definition.getId());
		report.setFilter(filter);

		report = reportExecutor.generate(report);
		Assert.assertNotNull(report.getData());
		List<RptIdentityRoleByRoleDeduplicationDto> reportItems = mapper.readValue(
				attachmentManager.getAttachmentData(report.getData()), 
				new TypeReference<List<RptIdentityRoleByRoleDeduplicationDto>>(){});

		assertEquals(0, reportItems.size());

		attachmentManager.deleteAttachments(report);
	}

	@Test
	public void testExecuteReportTwoContract() throws JsonParseException, JsonMappingException, IOException {
		String roleCode = "test-" + System.currentTimeMillis(); 
		IdmTreeNodeDto treeNode = getHelper().createTreeNode();
		IdmIdentityDto identity = getHelper().createIdentity(new GuardedString());
		IdmIdentityContractDto contactOne = getHelper().createIdentityContact(identity, treeNode);
		IdmIdentityContractDto contactTwo = getHelper().createIdentityContact(identity, treeNode);
		
		IdmRoleDto role = getHelper().createRole(roleCode);

		getHelper().createIdentityRole(contactOne, role);
		getHelper().createIdentityRole(contactOne, role);
		getHelper().createIdentityRole(contactTwo, role);
		getHelper().createIdentityRole(contactTwo, role);

		RptReportDto report = new RptReportDto(UUID.randomUUID());
		report.setExecutorName(reportExecutor.getName());
		IdmFormDto filter = new IdmFormDto();
		IdmFormDefinitionDto definition = reportExecutor.getFormDefinition();
		IdmFormValueDto treeNodeParameter = new IdmFormValueDto(definition.getMappedAttributeByCode(IdentityRoleByIdentityDeduplicationExecutor.PARAMETER_TREE_NODE));
		treeNodeParameter.setValue(treeNode.getId());
		filter.getValues().add(treeNodeParameter);
		filter.setFormDefinition(definition.getId());
		report.setFilter(filter);

		report = reportExecutor.generate(report);
		Assert.assertNotNull(report.getData());
		List<RptIdentityRoleByRoleDeduplicationDto> reportItems = mapper.readValue(
				attachmentManager.getAttachmentData(report.getData()), 
				new TypeReference<List<RptIdentityRoleByRoleDeduplicationDto>>(){});

		assertEquals(2, reportItems.size());
		for (RptIdentityRoleByRoleDeduplicationDto item : reportItems) {
			assertNotNull(item.getIdentity());
			assertEquals(identity.getId(), item.getIdentity().getId());
			assertNotNull(item.getWorkPosition());
			assertEquals(treeNode.getId(), item.getWorkPosition().getId());
			assertNotNull(item.getIdentityContract());
			if (item.getIdentityContract().getId().equals(contactOne.getId())) {
				// Success
			} else if (item.getIdentityContract().getId().equals(contactTwo.getId())) {
				// Success
			} else {
				fail();
			}
			List<RptIdentityRoleByRoleDeduplicationDuplicityDto> duplicities = item.getDuplicity();
			assertEquals(1, duplicities.size());
		}

		attachmentManager.deleteAttachments(report);
	}

	@Test
	public void testRenderers() {
		IdmTreeNodeDto treeNode = getHelper().createTreeNode();
		IdmIdentityDto identity = getHelper().createIdentity(new GuardedString());
		IdmIdentityContractDto contact = getHelper().createIdentityContact(identity, treeNode);
		
		IdmRoleDto role = getHelper().createRole();

		getHelper().createIdentityRole(contact, role);
		getHelper().createIdentityRole(contact, role);

		RptReportDto report = new RptReportDto(UUID.randomUUID());
		report.setExecutorName(reportExecutor.getName());
		IdmFormDto filter = new IdmFormDto();
		IdmFormDefinitionDto definition = reportExecutor.getFormDefinition();
		IdmFormValueDto treeNodeParameter = new IdmFormValueDto(definition.getMappedAttributeByCode(IdentityRoleByIdentityDeduplicationExecutor.PARAMETER_TREE_NODE));
		treeNodeParameter.setValue(treeNode.getId());
		filter.getValues().add(treeNodeParameter);
		filter.setFormDefinition(definition.getId());
		report.setFilter(filter);
		report = reportExecutor.generate(report);
		Assert.assertNotNull(report.getData());

		Assert.assertNotNull(xlsxRenderer.render(report));

		attachmentManager.deleteAttachments(report);
	}
}
