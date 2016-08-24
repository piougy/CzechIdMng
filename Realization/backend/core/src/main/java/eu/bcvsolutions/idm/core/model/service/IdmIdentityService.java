package eu.bcvsolutions.idm.core.model.service;

import org.activiti.engine.runtime.ProcessInstance;

import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;

public interface IdmIdentityService {

	IdmIdentity getByUsername(String username);

	String getNiceLabel(IdmIdentity identity);

	IdmIdentity get(Long id);

	ProcessInstance changePermissions(IdmIdentity identity);
}
