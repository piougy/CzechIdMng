package eu.bcvsolutions.idm.core.rest;

import java.io.Serializable;
import java.util.Set;
import java.util.UUID;

import org.springframework.core.GenericTypeResolver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;

/**
 * AbstractBaseDtoService uses in case when we don't have an Entity, but only DTO. For example in workflow services.
 * @author svandav
 *
 * @param <DTO>
 * @param <F>
 */
public class AbstractBaseDtoService<DTO extends BaseDto, F extends BaseFilter> implements  ReadWriteDtoService<DTO, F>  {

	private final Class<F> filterClass;
	private final Class<DTO> dtoClass;
	
	@SuppressWarnings("unchecked")
	public AbstractBaseDtoService() {
		Class<?>[] genericTypes = GenericTypeResolver.resolveTypeArguments(getClass(), AbstractBaseDtoService.class);
		dtoClass = (Class<DTO>) genericTypes[0];
		filterClass = (Class<F>) genericTypes[1];
	}
	
	@Override
	public boolean supports(Class<?> delimiter) {
		return dtoClass.isAssignableFrom(delimiter);
	}

	@Override
	public Class<F> getFilterClass() {
		return filterClass;
	}
	

	@Override
	public Class<DTO> getDtoClass() {
		return dtoClass;
	}

	
	@Override
	public boolean isNew(DTO dto) {
		Assert.notNull(dto);
		//
		return dto.getId() == null;
	}
	
	
	@Override
	public DTO checkAccess(DTO dto, BasePermission... permission) {
		return dto;
	}

	@Override
	public DTO get(Serializable id, BasePermission... permission) {
		return null;
	}

	@Override
	public Page<DTO> find(Pageable pageable, BasePermission... permission) {
		return null;
	}

	@Override
	public Page<DTO> find(F filter, Pageable pageable, BasePermission... permission) {
		return null;
	}
	
	@Override
	public long count(F filter, BasePermission... permission) {
		return 0;
	}

	@Override
	public Page<UUID> findIds(Pageable pageable, BasePermission... permission) {
		return null;
	}
	
	@Override
	public Page<UUID> findIds(F filter, Pageable pageable, BasePermission... permission) {
		return null;
	}

	@Override
	public Set<String> getPermissions(Serializable id) {
		return null;
	}
	
	@Override
	public Set<String> getPermissions(DTO dto) {
		return null;
	}
	
	@Override
	public Class<? extends BaseEntity> getEntityClass() {
		return null;
	}


	@Override
	public DTO save(DTO dto, BasePermission... permission) {
		return null;
	}

	@Override
	public Iterable<DTO> saveAll(Iterable<DTO> dtos, BasePermission... permission) {
		return null;
	}

	@Override
	public void delete(DTO dto, BasePermission... permission) {
	}

	@Override
	public void deleteById(Serializable id, BasePermission... permission) {
	}

	@Override
	public DTO saveInternal(DTO dto) {
		return null;
	}

	@Override
	public void deleteInternal(DTO dto) {
	}

	@Override
	public void deleteInternalById(Serializable id) {
	}

}
