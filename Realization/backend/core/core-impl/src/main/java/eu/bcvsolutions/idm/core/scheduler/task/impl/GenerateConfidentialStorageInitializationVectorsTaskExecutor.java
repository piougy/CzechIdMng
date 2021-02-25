package eu.bcvsolutions.idm.core.scheduler.task.impl;

import java.util.Iterator;

import org.quartz.DisallowConcurrentExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.IdmConfidentialStorageValueDto;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity_;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.service.ConfidentialStorage;
import eu.bcvsolutions.idm.core.api.service.IdmConfidentialStorageValueService;
import eu.bcvsolutions.idm.core.scheduler.api.service.AbstractSchedulableTaskExecutor;
import eu.bcvsolutions.idm.core.security.service.impl.DefaultCryptService;

/**
 * The task processes every value in the confidential storage, generates a new initialization vector
 * and encrypts the value using this new IV.
 * This can be used when upgrading CzechIdM from the versions older than 10.6 where every value used
 * the same initialization vector - {@link DefaultCryptService}.
 * 
 * @author Alena Peterová
 * @since 10.8.0
 */
@DisallowConcurrentExecution
@Component(GenerateConfidentialStorageInitializationVectorsTaskExecutor.TASK_NAME)
public class GenerateConfidentialStorageInitializationVectorsTaskExecutor extends AbstractSchedulableTaskExecutor<Boolean> {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory
			.getLogger(GenerateConfidentialStorageInitializationVectorsTaskExecutor.class);
	public static final String TASK_NAME = "core-generate-confidential-storage-initialization-vectors-long-running-task";

	private static final int PAGE_SIZE = 100;

	@Autowired
	private IdmConfidentialStorageValueService confidentialStorageValueService;
	@Autowired
	private ConfidentialStorage confidentialStorage;

	@Override
	public String getName() {
		return TASK_NAME;
	}

	@Override
	public Boolean process() {
		int page = 0;
		boolean canContinue = true;
		counter = 0L;
		//
		do {
			// Sorting by the date of creation, so we won't skip anything, if a new value is added to confidential storage
			// when this task is running
			Page<IdmConfidentialStorageValueDto> values = confidentialStorageValueService
					.find(PageRequest.of(page, PAGE_SIZE, new Sort(Direction.ASC, AbstractEntity_.created.getName())));
			//
			if (count == null) {
				count = values.getTotalElements();
				LOG.info("Starting to generate a new initialization vector for [{}] values in the confidential storage.", count);
			}
			//
			for (Iterator<IdmConfidentialStorageValueDto> iterator = values.iterator(); iterator.hasNext()
					&& canContinue;) {
				IdmConfidentialStorageValueDto value = iterator.next();
				Assert.notNull(value, "Value is required.");
				//
				try {
					confidentialStorage.renewVector(value);
					//
					counter++;
					this.logItemProcessed(value, new OperationResult.Builder(OperationState.EXECUTED).build());
				} catch (Exception ex) {
					LOG.error("Error during generating new initialization vector for confidential storage value ID [{}], key [{}].",
							value.getId(), value.getKey(), ex);
					this.logItemProcessed(value,
							new OperationResult.Builder(OperationState.EXCEPTION).setCause(ex).build());
				}
				//
				canContinue &= this.updateState();
			}
			canContinue &= values.hasNext();
			++page;
			//
		} while (canContinue);
		//
		LOG.info("New initialization vector was generated for [{}/{}] values in the confidential storage.", counter, count);
		//
		return Boolean.TRUE;
	}
}
