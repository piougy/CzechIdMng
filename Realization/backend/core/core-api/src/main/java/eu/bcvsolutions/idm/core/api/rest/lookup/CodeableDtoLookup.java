package eu.bcvsolutions.idm.core.api.rest.lookup;

import java.io.Serializable;

import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.domain.Codeable;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.service.CodeableService;

/**
 * Idetifiable by code lookup
 * 
 * @param <E>
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
		DTO dto = null;
		try {
			dto = getService().get(id);
		} catch (IllegalArgumentException ex) {
			// simply not found
		}
		if (dto == null) {
			dto = getService().getByCode(id.toString());
		}
		return dto;
	}
}
