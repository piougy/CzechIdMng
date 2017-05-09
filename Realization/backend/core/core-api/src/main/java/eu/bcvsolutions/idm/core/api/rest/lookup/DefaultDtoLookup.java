package eu.bcvsolutions.idm.core.api.rest.lookup;

import java.io.Serializable;

import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.service.ReadDtoService;

/**
 * Default entity lookup by {@code UUID} entityId
 * 
 * @param <E>
 * @author Radek Tomiška
 */
public class DefaultDtoLookup<DTO extends BaseDto> implements DtoLookup<DTO> {

	private final ReadDtoService<DTO, ?> service;

	public DefaultDtoLookup(ReadDtoService<DTO, ?> service) {
		this.service = service;
	}

	@Override
	public Serializable getIdentifier(DTO entity) {
		return entity.getId();
	}

	@Override
	public DTO lookup(Serializable id) {
		return service.get(id);
	}
	
	@Override
	public boolean supports(Class<?> delimiter) {
		return BaseDto.class.isAssignableFrom(delimiter);
	}

}
