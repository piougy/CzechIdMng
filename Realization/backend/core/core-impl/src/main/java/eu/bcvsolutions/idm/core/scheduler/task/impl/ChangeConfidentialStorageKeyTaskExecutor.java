package eu.bcvsolutions.idm.core.scheduler.task.impl;

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.quartz.DisallowConcurrentExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.IdmConfidentialStorageValueDto;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.ConfidentialStorage;
import eu.bcvsolutions.idm.core.api.service.IdmConfidentialStorageValueService;
import eu.bcvsolutions.idm.core.scheduler.api.service.AbstractSchedulableTaskExecutor;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;

/**
 * Task change all values in confidential storage with new key from file or application properties.
 * This task required start this task after you change key to new.
 * TODO: change parameter oldKey to input type password (now isn't this required because as parameter is send old key)
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@Service
@DisallowConcurrentExecution
@Description("Change all crypted values in confidential storage to new. This task required start after you changed key!")
public class ChangeConfidentialStorageKeyTaskExecutor extends AbstractSchedulableTaskExecutor<Boolean> {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ChangeConfidentialStorageKeyTaskExecutor.class);

	public static String PARAMETER_OLD_CONFIDENTIAL_KEY = "oldCryptKey";

	private GuardedString oldCryptKey = null;
	private int PAGE_SIZE = 100;
	private int KEY_LENGTH = 16;

	@Autowired
	private IdmConfidentialStorageValueService confidetialStorageValueService;
	@Autowired
	private ConfidentialStorage confidentialStorage;

	@Override
	public void init(Map<String, Object> properties) {
		super.init(properties);
		//
		String oldKeyInString = getParameterConverter().toString(properties, PARAMETER_OLD_CONFIDENTIAL_KEY);
		
		if (oldKeyInString == null || oldKeyInString.isEmpty()) {
			LOG.error("Old key cannot be null or empty.");
			throw new ResultCodeException(CoreResultCode.BAD_VALUE, "Old key cannot be null or empty.");
		}
		if (oldKeyInString.length() != KEY_LENGTH) {
			LOG.error("Length of old key has to be [{}] characters.", KEY_LENGTH);
			throw new ResultCodeException(CoreResultCode.BAD_VALUE,
					MessageFormat.format("Length of old key has to be [{0}] characters.", KEY_LENGTH));
		}
		this.oldCryptKey = new GuardedString(oldKeyInString);
	}

	@Override
	public List<String> getPropertyNames() {
		List<String> parameters = super.getPropertyNames();
		parameters.add(PARAMETER_OLD_CONFIDENTIAL_KEY);
		return parameters;
	}

	@Override
	public Boolean process() {
		int page = 0;
		boolean canContinue = true;
		counter = 0L;
		//
		do {
			Page<IdmConfidentialStorageValueDto> values = confidetialStorageValueService.find(new PageRequest(page, PAGE_SIZE));
			//
			if (count == null) {
				count = values.getTotalElements();
			}
			//
			for (Iterator<IdmConfidentialStorageValueDto> iterator = values.iterator(); iterator.hasNext() && canContinue;) {
				IdmConfidentialStorageValueDto value = iterator.next();
				Assert.notNull(value);
				Assert.notNull(value.getId());
				//
				try {
					confidentialStorage.changeCryptKey(value, oldCryptKey);
					counter++;
					//
					this.logItemProcessed(value, new OperationResult.Builder(OperationState.EXECUTED).build());
				} catch (Exception ex) {
					LOG.error("Error during change confidential storage key. For key [{}].", value.getKey(), ex);
					this.logItemProcessed(value, new OperationResult.Builder(OperationState.EXCEPTION).setCause(ex).build());
				}
				//
 				canContinue &= this.updateState();
			}
			canContinue &= values.hasNext();			
			++page;
			//
		} while (canContinue);

		return Boolean.TRUE;
	}
}
