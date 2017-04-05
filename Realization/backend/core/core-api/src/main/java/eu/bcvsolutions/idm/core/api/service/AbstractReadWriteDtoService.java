package eu.bcvsolutions.idm.core.api.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;

/**
 * Abstract implementation for generic CRUD operations on a repository for a
 * specific type.
 * 
 * @author Svanda
 *
 */
public abstract class AbstractReadWriteDtoService<DTO extends BaseDto, E extends BaseEntity, F extends BaseFilter>
		extends AbstractReadDtoService<DTO, E, F> implements ReadWriteDtoService<DTO, E, F> {

	public AbstractReadWriteDtoService(AbstractEntityRepository<E, F> repository) {
		super(repository);
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * Override for include event processing etc.
	 */
	@Override
	@Transactional
	public DTO save(DTO dto) {
		return save(dto, null);
	}
	
	@Override
	@Transactional
	public DTO save(DTO dto, BasePermission permission) {
		E persistEntity = null;
		if (dto.getId() != null) {
			persistEntity = this.get(dto.getId());
			if (persistEntity != null && permission != null) {
				// check access on previous entity - update is needed
				checkAccess(persistEntity, IdmBasePermission.UPDATE);
			}
		}
		persistEntity = checkAccess(toEntity(dto, persistEntity), permission); // TODO: remove one checkAccess?
		E entity = getRepository().save(persistEntity);
		return toDto(entity);
	}

	@Override
	@Transactional
	public DTO saveInternal(DTO dto) {
		Assert.notNull(dto);
		//
		E persistedEntity = null;
		if (dto.getId() != null) {
			persistedEntity = this.get(dto.getId());
		}
		E entity = getRepository().save(toEntity(dto, persistedEntity));
		return toDto(entity);
	}

	@Override
	@Transactional
	public Iterable<DTO> saveAll(Iterable<DTO> dtos) {
		Assert.notNull(dtos);
		//
		List<DTO> savedDtos = new ArrayList<>();
		dtos.forEach(entity -> {
			savedDtos.add(save(entity));
		});
		return savedDtos;
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * Override for include event processing etc.
	 * 
	 * @param dto
	 * @throws IllegalArgumentException
	 *  in case the given DTO is {@literal null}.
	 */
	@Override
	@Transactional
	public void delete(DTO dto) {
		delete(dto, null);
	}
	
	@Override
	@Transactional
	public void delete(DTO dto, BasePermission permission) {
		checkAccess(this.get(dto.getId()), permission);
		//
		deleteInternal(dto);
	}

	@Override
	@Transactional
	public void deleteInternal(DTO dto) {
		Assert.notNull(dto);
		//
		getRepository().delete((UUID) dto.getId());
	}

	@Override
	@Transactional
	public void deleteInternalById(Serializable id) {
		Assert.notNull(id);
		//
		getRepository().delete((UUID) id);
	}

}
