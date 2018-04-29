package eu.bcvsolutions.idm.core.model.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.api.dto.IdmBulkOperationItemDto;
import eu.bcvsolutions.idm.core.api.dto.filter.EmptyFilter;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.service.IdmBulkOperationItemService;
import eu.bcvsolutions.idm.core.model.entity.IdmBulkOperationItem;
import eu.bcvsolutions.idm.core.model.repository.IdmBulkOperationItemRepository;

@Service("bulkOperationItemService")
public class DefaultIdmBulkOperationItemService
		extends AbstractReadWriteDtoService<IdmBulkOperationItemDto, IdmBulkOperationItem, EmptyFilter>
		implements IdmBulkOperationItemService {

	@Autowired
	public DefaultIdmBulkOperationItemService(IdmBulkOperationItemRepository repository) {
		super(repository);
	}

}
