package eu.bcvsolutions.idm.core.rest.impl;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import eu.bcvsolutions.idm.core.api.rest.BaseEntityController;
import eu.bcvsolutions.idm.core.api.service.EntityLookupService;
import eu.bcvsolutions.idm.core.model.domain.IdmGroupPermission;
import eu.bcvsolutions.idm.core.model.domain.IdmScriptCategory;
import eu.bcvsolutions.idm.core.model.dto.filter.ScriptFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmScript;

/**
 * Default controller for scripts, basic methods.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@RepositoryRestController
@RequestMapping(value = BaseEntityController.BASE_PATH + "/scripts")
public class IdmScriptController extends DefaultReadWriteEntityController<IdmScript, ScriptFilter> {

	@Autowired
	public IdmScriptController(EntityLookupService entityLookupService) {
		super(entityLookupService);
	}

	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.SCRIPT_WRITE + "')")
	public ResponseEntity<?> create(HttpServletRequest nativeRequest, PersistentEntityResourceAssembler assembler)
			throws HttpMessageNotReadableException {
		return super.create(nativeRequest, assembler);
	}

	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.SCRIPT_DELETE + "')")
	public ResponseEntity<?> delete(@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}

	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.SCRIPT_WRITE + "')")
	public ResponseEntity<?> update(@PathVariable @NotNull String backendId, HttpServletRequest nativeRequest,
			PersistentEntityResourceAssembler assembler) throws HttpMessageNotReadableException {
		return super.update(backendId, nativeRequest, assembler);
	}

	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.SCRIPT_WRITE + "')")
	public ResponseEntity<?> patch(@PathVariable @NotNull String backendId, HttpServletRequest nativeRequest,
			PersistentEntityResourceAssembler assembler) throws HttpMessageNotReadableException {
		return super.patch(backendId, nativeRequest, assembler);
	}

	@Override
	protected ScriptFilter toFilter(MultiValueMap<String, Object> parameters) {
		ScriptFilter filter = new ScriptFilter();
		filter.setText(getParameterConverter().toString(parameters, "name"));
		filter.setCategory(getParameterConverter().toEnum(parameters, "category", IdmScriptCategory.class));
		filter.setDescription(getParameterConverter().toString(parameters, "description"));
		return filter;
	}
}
