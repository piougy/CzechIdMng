package eu.bcvsolutions.idm.core.model.service.api;

import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.api.service.ReadWriteEntityService;
import eu.bcvsolutions.idm.core.model.dto.filter.PasswordPolicyFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmPasswordPolicy;

@Service
public interface IdmPasswordPolicyService extends ReadWriteEntityService<IdmPasswordPolicy, PasswordPolicyFilter> {

}
