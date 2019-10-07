package eu.bcvsolutions.idm.core.workflow;

import static eu.bcvsolutions.idm.core.workflow.service.WorkflowProcessDefinitionService.SORT_BY_NAME;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.test.Deployment;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import eu.bcvsolutions.idm.core.AbstractCoreWorkflowIntegrationTest;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowFilterDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowProcessDefinitionDto;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowProcessDefinitionService;

/**
 * Test filtering workflow processes
 *
 * @author Kolychev Artem
 */
public class DefaultWorkflowProcessDefinitionServiceFilterTest extends AbstractCoreWorkflowIntegrationTest {

	@Autowired
	private WorkflowProcessDefinitionService definitionService;

	@Autowired
	private RepositoryService repositoryService;

	@Before
	@Deployment(resources = {"eu/bcvsolutions/idm/workflow/filter/*"})
	public void init() {
	}


	@Test
	public void testFilterFindAllDefinitions() {
		ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();
		query.active().latestVersion();
		List<ProcessDefinition> processDefinitions = query.list();
		List<WorkflowProcessDefinitionDto> allProcessDefinitions = definitionService.findAllProcessDefinitions();
		assertEquals(processDefinitions.size(), allProcessDefinitions.size());
	}

	@Test
	public void testEmptyFilter() {
		WorkflowFilterDto filterDto = new WorkflowFilterDto();

		List<WorkflowProcessDefinitionDto> allProcessDefinitions = definitionService.findAllProcessDefinitions();
		assertEquals(definitionService.find(filterDto, null).getTotalElements(), allProcessDefinitions.size());
	}

	@Test
	public void testFilterCategoryAll() {
		WorkflowFilterDto filterDto = new WorkflowFilterDto();
		filterDto.setCategory("filterTestCategoryWorkflowProcessDefinition");

		assertEquals(definitionService.find(filterDto, null).getTotalElements(), 3);
	}

	@Test
	public void testFilterCategoryOne() {
		WorkflowFilterDto filterDto = new WorkflowFilterDto();
		filterDto.setCategory("filterTestCategoryWorkflowProcessDefinition1");

		assertEquals(definitionService.find(filterDto, null).getTotalElements(), 1);
	}

	@Test
	public void testFilterCategoryNotExist() {
		WorkflowFilterDto filterDto = new WorkflowFilterDto();
		filterDto.setCategory("filterTestCategoryWorkflowProcessDefinitionNotExist");

		assertEquals(definitionService.find(filterDto, null).getTotalElements(), 0);
	}

	@Test
	public void testFilterCategoryLikePrefix() {
		WorkflowFilterDto filterDto = new WorkflowFilterDto();
		filterDto.setCategory("%terTestCategoryWorkflowProcessDefinition");

		assertEquals(definitionService.find(filterDto, null).getTotalElements(), 3);
	}

	//sort
	@Test
	public void testSortCategoryDESC() {
		WorkflowFilterDto filterDto = new WorkflowFilterDto();
		filterDto.setCategory("filterTestCategoryWorkflowProcessDefinition");
		Page<WorkflowProcessDefinitionDto> processesDefinitionDto =
				definitionService.find(
						filterDto,
						PageRequest.of(0, 3, Sort.Direction.DESC, SORT_BY_NAME)
				);
		assertEquals("Process for test filtering #3", processesDefinitionDto.getContent().get(0).getName());
	}

	@Test
	public void testSortCategoryASC() {
		WorkflowFilterDto filterDto = new WorkflowFilterDto();
		filterDto.setCategory("filterTestCategoryWorkflowProcessDefinition");
		Page<WorkflowProcessDefinitionDto> processesDefinitionDto =
				definitionService.find(
						filterDto,
						PageRequest.of(0, 3, Sort.Direction.ASC, SORT_BY_NAME)
				);
		assertEquals("Process for test filtering #1", processesDefinitionDto.getContent().get(0).getName());
	}
	//DefinitionKey

	@Test
	public void testFilterDefinitionKeyAll() {
		WorkflowFilterDto filterDto = new WorkflowFilterDto();
		filterDto.setProcessDefinitionKey("filterTestWorkflowProcessDefinition");
		Page<WorkflowProcessDefinitionDto> processesDefinitionDto = definitionService.find(filterDto, null);
		assertEquals(3, processesDefinitionDto.getTotalElements());
	}

	@Test
	public void testFilterDefinitionKeyOne() {
		WorkflowFilterDto filterDto = new WorkflowFilterDto();
		filterDto.setProcessDefinitionKey("filterTestWorkflowProcessDefinition1");

		assertEquals(definitionService.find(filterDto, null).getTotalElements(), 1);
	}

	@Test
	public void testFilterDefinitionKeyNotExist() {
		WorkflowFilterDto filterDto = new WorkflowFilterDto();
		filterDto.setProcessDefinitionKey("filterTestWorkflowProcessDefinitionNotExist");

		assertEquals(definitionService.find(filterDto, null).getTotalElements(), 0);
	}

	@Test
	public void testFilterDefinitionKeyLikePrefix() {
		WorkflowFilterDto filterDto = new WorkflowFilterDto();
		filterDto.setProcessDefinitionKey("%lterTestWorkflowProcessDefinition");

		assertEquals(definitionService.find(filterDto, null).getTotalElements(), 3);
	}

	//DefinitionKey

	@Test
	public void testFilterNameAll() {
		WorkflowFilterDto filterDto = new WorkflowFilterDto();
		filterDto.setName("Process for test filtering");

		assertEquals(definitionService.find(filterDto, null).getTotalElements(), 3);
	}

	@Test
	public void testFilterNameOne() {
		WorkflowFilterDto filterDto = new WorkflowFilterDto();
		filterDto.setName("Process for test filtering %1");

		assertEquals(definitionService.find(filterDto, null).getTotalElements(), 1);
	}

	@Test
	public void testFilterNameNotExist() {
		WorkflowFilterDto filterDto = new WorkflowFilterDto();
		filterDto.setName("Process for test filtering Not Exist");

		assertEquals(definitionService.find(filterDto, null).getTotalElements(), 0);
	}

	@Test
	public void testFilterNameLikePrefix() {
		WorkflowFilterDto filterDto = new WorkflowFilterDto();
		filterDto.setName("%for test filtering #");

		assertEquals(definitionService.find(filterDto, null).getTotalElements(), 3);
	}

}
