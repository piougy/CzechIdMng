package eu.bcvsolutions.idm.acc.service.impl;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import eu.bcvsolutions.idm.acc.domain.ProvisioningOperation;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningAttribute;
import eu.bcvsolutions.idm.acc.repository.SysProvisioningAttributeRepository;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningAttributeService;
import eu.bcvsolutions.idm.ic.api.IcAttribute;

/**
 * Helper service only. For simple CRUD use {@link SysProvisioningAttributeRepository}.
 * 
 * @see SysProvisioningAttributeRepository
 * @author Radek Tomiška
 * @since 9.6.3
 */
public class DefaultSysProvisioningAttributeService implements SysProvisioningAttributeService{

	private final SysProvisioningAttributeRepository repository;
	
	@Autowired
	public DefaultSysProvisioningAttributeService(SysProvisioningAttributeRepository repository) {
		Assert.notNull(repository, "Repository is required.");
		//
		this.repository = repository;
	}
	
	@Override
	@Transactional
	public void saveAttributes(ProvisioningOperation operation) {
		Assert.notNull(operation, "Operation is required.");
		Assert.notNull(operation.getId(), "Operation identifier is required.");
		//
		if (operation.getProvisioningContext().getConnectorObject() != null) {
			List<IcAttribute> connectorAttributes = operation.getProvisioningContext().getConnectorObject().getAttributes();
			if (!CollectionUtils.isEmpty(connectorAttributes)) {
				for (IcAttribute connectorAttribute : connectorAttributes) {
					SysProvisioningAttribute provisioningAttribute = new SysProvisioningAttribute(operation.getId(), connectorAttribute.getName());
					if (CollectionUtils.isEmpty(connectorAttribute.getValues())) {
						provisioningAttribute.setRemoved(true);
					} else {
						provisioningAttribute.setRemoved(
								connectorAttribute
									.getValues()
									.stream()
									.allMatch(v -> {
										return v == null || StringUtils.isEmpty(v.toString());
									}));
					}
					repository.save(provisioningAttribute);
				}
			}
		}		
	}

	@Override
	@Transactional
	public int deleteAttributes(ProvisioningOperation operation) {
		Assert.notNull(operation, "Operation is required.");
		Assert.notNull(operation.getId(), "Operation identifier is required.");
		//
		return repository.deleteByProvisioningId(operation.getId());
	}
	
	@Override
	@Transactional
	public int cleanupAttributes() {
		return repository.cleanupAttributes();
	}

}
