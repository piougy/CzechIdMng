package eu.bcvsolutions.idm.acc.service.impl;

import java.io.Serializable;
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

public class DefaultSysSystemFormValueService extends AbstractFormValueService<SysSystem, SysSystemFormValue>
		implements SysSystemFormValueService {

	private final EntityManager entityManager;
	private final ConfidentialStorage confidentialStorage;

	public DefaultSysSystemFormValueService(AbstractFormValueRepository<SysSystem, SysSystemFormValue> repository,
			ConfidentialStorage confidentialStorage, EntityManager entityManager) {
		super(repository, confidentialStorage);

		Assert.notNull(entityManager);
		Assert.notNull(confidentialStorage);

		this.entityManager = entityManager;
		this.confidentialStorage = confidentialStorage;
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

	@Override
	public SysSystemFormValue duplicate(UUID id, SysSystem owner) {
		Asserts.notNull(owner, "Owner (system) must be set!");
		SysSystemFormValue cloned = this.clone(id);
		cloned.setOwner(owner);
		cloned = this.save(cloned);
		
		// For confidential we will load guarded value by old ID and save for new value.
		if (cloned.isConfidential()) {
			Serializable guardedValue = this.getConfidentialPersistentValue(this.get(id));
			this.confidentialStorage.save(cloned.getId(), SysSystemFormValue.class,
					this.getConfidentialStorageKey(cloned.getFormAttribute()), guardedValue);
		}
		return cloned;
	}

}
