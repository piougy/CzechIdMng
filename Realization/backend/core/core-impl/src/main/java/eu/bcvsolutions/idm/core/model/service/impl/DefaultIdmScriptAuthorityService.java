package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.model.dto.IdmScriptAuthorityDto;
import eu.bcvsolutions.idm.core.model.dto.filter.ScriptAuthorityFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmScriptAuthority;
import eu.bcvsolutions.idm.core.model.repository.IdmScriptAuthorityRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmScriptAuthorityService;

@Service("scriptAuthorityService")
public class DefaultIdmScriptAuthorityService extends AbstractReadWriteDtoService<IdmScriptAuthorityDto, IdmScriptAuthority, ScriptAuthorityFilter> implements IdmScriptAuthorityService {

	@Autowired
	public DefaultIdmScriptAuthorityService(
			IdmScriptAuthorityRepository repository) {
		super(repository);
	}

	@Override
	public void deleteAllByScript(UUID scriptId) {
		ScriptAuthorityFilter filter = new ScriptAuthorityFilter();
		filter.setScriptId(scriptId);
		//
		// remove internal by id each script authority
		find(filter, null).getContent().forEach(scriptAuthority -> this.deleteInternalById(scriptAuthority.getId()));
	}

}
