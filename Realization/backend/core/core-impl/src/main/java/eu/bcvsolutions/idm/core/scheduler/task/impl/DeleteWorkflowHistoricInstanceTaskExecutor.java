package eu.bcvsolutions.idm.core.scheduler.task.impl;

import java.util.List;
import java.util.Map;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.joda.time.DateTime;
import org.quartz.DisallowConcurrentExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.eav.api.domain.BaseFaceType;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.scheduler.api.service.AbstractSchedulableTaskExecutor;

/**
 * Delete historic workflow process instances
 * 
 * @author Radek Tomiška
 * @since 9.7.12 
 */
@Service(DeleteWorkflowHistoricInstanceTaskExecutor.TASK_NAME)
@DisallowConcurrentExecution
@Description("Delete historic workflow process instances.")
public class DeleteWorkflowHistoricInstanceTaskExecutor extends AbstractSchedulableTaskExecutor<Boolean> {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory .getLogger(DeleteWorkflowHistoricInstanceTaskExecutor.class);
	public static final String TASK_NAME = "core-delete-workflow-historic-instance-long-running-task";
	public static final String PARAMETER_PROCESS_DEFINITION_KEY = "processDefinitionKey";
	public static final String PARAMETER_NUMBER_OF_DAYS = "numberOfDays"; // events older than
	public static final int DEFAULT_NUMBER_OF_DAYS = 180; // half year by default
	//
	private String processDefinitionKey = null;
	private int numberOfDays = 0; // optional
	//
	@Autowired private ProcessEngine processEngine;
	@Autowired private PlatformTransactionManager platformTransactionManager;
	
	@Override
	public String getName() {
		return TASK_NAME;
	}

	@Override
	public void init(Map<String, Object> properties) {
		super.init(properties);
		//
		Long givenNumberOfDays = getParameterConverter().toLong(properties, PARAMETER_NUMBER_OF_DAYS);
		if (givenNumberOfDays != null) {
			numberOfDays = Math.toIntExact(givenNumberOfDays);
		} else {
			numberOfDays = 0;
		}
		this.processDefinitionKey = getParameterConverter().toString(properties, PARAMETER_PROCESS_DEFINITION_KEY);
	}
	
	@Override
	protected boolean start() {
		LOG.warn("Start deleting historic workflow process instaces older than [{}] days (finished before).", numberOfDays);
		//
		return super.start();
	}
	
	@Override
	protected Boolean end(Boolean result, Exception ex) {
		result = super.end(result, ex);
		LOG.warn("End deleting historic workflow process instaces older than [{}] days (finished before). Processed instances [{}].",
				numberOfDays, counter);
		return result;
	}

	@Override
	public Boolean process() {
		Assert.notNull(processDefinitionKey, "Workflow definition key is requred.");
		//
		HistoricProcessInstanceQuery query = processEngine
				.getHistoryService()
				.createHistoricProcessInstanceQuery()
				.processDefinitionKey(processDefinitionKey)
				.finished()
				.excludeSubprocesses(true);
		if (numberOfDays > 0) {
			query.finishedBefore(DateTime.now().withTimeAtStartOfDay().minusDays(numberOfDays).toDate());
		}
		
		List<HistoricProcessInstance> historicInstances = query.list();
		count = (long) historicInstances.size();
		counter = 0L;
		//
		for (HistoricProcessInstance processInstance : historicInstances) {
			TransactionTemplate template = new TransactionTemplate(platformTransactionManager);
			template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
			//
			// delete historic instance in new transaction => large data support or if task is ended with exception, then prevent to rollback
			template.execute(new TransactionCallback<OperationResult>() {
				public OperationResult doInTransaction(TransactionStatus transactionStatus) {
					processEngine.getHistoryService().deleteHistoricProcessInstance(processInstance.getId());
					//
					return new OperationResult(OperationState.EXECUTED);
				}
			});
			++counter;
			//
			boolean canContinue = updateState();
			if (!canContinue) {
				break;
			}
		}
		return Boolean.TRUE;
	}

	@Override
	public List<String> getPropertyNames() {
		List<String> parameters = super.getPropertyNames();
		parameters.add(PARAMETER_NUMBER_OF_DAYS);
		parameters.add(PARAMETER_PROCESS_DEFINITION_KEY);
		//
		return parameters;
	}
	
	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = super.getProperties();
		properties.put(PARAMETER_NUMBER_OF_DAYS, numberOfDays);
		properties.put(PARAMETER_PROCESS_DEFINITION_KEY, processDefinitionKey);
		//
		return properties;
	}
	
	@Override
	public List<IdmFormAttributeDto> getFormAttributes() {
		IdmFormAttributeDto numberOfDaysAttribute = new IdmFormAttributeDto(PARAMETER_NUMBER_OF_DAYS, PARAMETER_NUMBER_OF_DAYS, PersistentType.LONG);
		numberOfDaysAttribute.setDefaultValue(String.valueOf(DEFAULT_NUMBER_OF_DAYS));
		//
		IdmFormAttributeDto processDefinitionKeyAttribute = new IdmFormAttributeDto(
				PARAMETER_PROCESS_DEFINITION_KEY, PARAMETER_PROCESS_DEFINITION_KEY, PersistentType.SHORTTEXT);
		processDefinitionKeyAttribute.setRequired(true);
		processDefinitionKeyAttribute.setFaceType(BaseFaceType.WORKFLOW_DEFINITION_SELECT);
		//
		return Lists.newArrayList(numberOfDaysAttribute, processDefinitionKeyAttribute);
	}
}
