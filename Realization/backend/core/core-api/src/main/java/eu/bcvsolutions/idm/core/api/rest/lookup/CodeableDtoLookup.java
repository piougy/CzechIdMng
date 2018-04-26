package eu.bcvsolutions.idm.core.api.rest.lookup;

import java.io.Serializable;

import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.domain.Codeable;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.service.CodeableService;

/**
 * Idetifiable by {@link Codeable} lookup.
 * UUID identifier has higher priority.
 * 
 * @param <DTO> {@link BaseDto} type
 * @author Radek Tomiška
 */
public class CodeableDtoLookup<DTO extends BaseDto> extends AbstractDtoLookup<DTO>{
	
	private CodeableService<DTO> service;
	
	public CodeableDtoLookup() {
	}
	
	public CodeableDtoLookup(CodeableService<DTO> service) {
		this.service = service;
	}
	
	protected CodeableService<DTO> getService() {
		return service;
	}
	
	protected void setService(CodeableService<DTO> service) {
		Assert.notNull(service);
		//
		this.service = service;
	}
	
	@Override
	public Serializable getIdentifier(DTO codeable) {
		if (codeable instanceof Codeable) {
			return ((Codeable) codeable).getCode();
		} else {
			return codeable.getId();
		}
	}

	@Override
	public DTO lookup(Serializable id) {
		CodeableService<DTO> codeableService = getService();
		Assert.notNull(codeableService, "Service for this lookup is null. Inicialize service for this lookup.");
		//
		DTO dto = null;
		try {
			dto = codeableService.get(id);
		} catch (IllegalArgumentException ex) {
			// simply not found
		}
		if (dto == null) {
			dto = codeableService.getByCode(id.toString());
		}
		return dto;
	}
}
