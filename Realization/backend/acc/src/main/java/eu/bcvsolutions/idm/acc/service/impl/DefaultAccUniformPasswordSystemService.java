package eu.bcvsolutions.idm.acc.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.acc.dto.AccUniformPasswordSystemDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccUniformPasswordSystemFilter;
import eu.bcvsolutions.idm.acc.entity.AccUniformPasswordSystem;
import eu.bcvsolutions.idm.acc.repository.AccUniformPasswordSystemRepository;
import eu.bcvsolutions.idm.acc.service.api.AccUniformPasswordSystemService;
import eu.bcvsolutions.idm.core.api.service.AbstractEventableDtoService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * Default implementation of {@link AccUniformPasswordSystemService}.
 *
 * @author Ondrej Kopr
 * @since 10.5.0
 *
 */
@Service("accUniformPasswordSystemService")
public class DefaultAccUniformPasswordSystemService
		extends AbstractEventableDtoService<AccUniformPasswordSystemDto, AccUniformPasswordSystem, AccUniformPasswordSystemFilter>
		implements AccUniformPasswordSystemService {

	@Autowired
	public DefaultAccUniformPasswordSystemService(AccUniformPasswordSystemRepository repository,
			EntityEventManager entityEventManager) {
		super(repository, entityEventManager);
	}

	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(AccGroupPermission.UNIFORMPASSWORD, getEntityClass());
	}

}
