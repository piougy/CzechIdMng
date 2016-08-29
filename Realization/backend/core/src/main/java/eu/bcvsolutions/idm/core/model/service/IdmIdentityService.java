package eu.bcvsolutions.idm.core.model.service;

import org.activiti.engine.runtime.ProcessInstance;

import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;

public interface IdmIdentityService {

	IdmIdentity getByUsername(String username);

	String getNiceLabel(IdmIdentity identity);

	IdmIdentity get(Long id);

	/**
	 * Start workflow for change permissions
	 * @param identity
	 * @return
	 */
	ProcessInstance changePermissions(IdmIdentity identity);
}
