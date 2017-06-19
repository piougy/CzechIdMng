package eu.bcvsolutions.idm.acc.service.impl;
import java.util.UUID;

import javax.persistence.EntityManager;

import org.apache.http.util.Asserts;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystemFormValue;
import eu.bcvsolutions.idm.acc.service.api.SysSystemFormValueService;
import eu.bcvsolutions.idm.core.api.service.ConfidentialStorage;
import eu.bcvsolutions.idm.core.api.utils.EntityUtils;
import eu.bcvsolutions.idm.core.eav.repository.AbstractFormValueRepository;
import eu.bcvsolutions.idm.core.eav.service.impl.AbstractFormValueService;

public class DefaultSysSystemFormValueService extends AbstractFormValueService<SysSystem, SysSystemFormValue> implements SysSystemFormValueService {

	private final EntityManager entityManager;
	
	public DefaultSysSystemFormValueService(AbstractFormValueRepository<SysSystem, SysSystemFormValue> repository,
			ConfidentialStorage confidentialStorage, EntityManager entityManager) {
		super(repository, confidentialStorage);
		
		Assert.notNull(entityManager);
		this.entityManager = entityManager;
	
	}

	@Override
	public SysSystemFormValue clone(UUID id) {
		SysSystemFormValue original = this.get(id);
		Asserts.notNull(original, "System form value must be found!");
		
		// We do detach this entity (and set id to null)
		entityManager.detach(original);
		original.setId(null);
		EntityUtils.clearAuditFields(original);
		return original;
	}

}
