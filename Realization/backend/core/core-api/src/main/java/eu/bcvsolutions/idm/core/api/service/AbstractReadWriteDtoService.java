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
	 * Saves a given dto.
	 * 
	 * @param dto
	 * @return the saved entity
	 */
	@Override
	@Transactional
	public DTO saveDto(DTO dto) {
		Assert.notNull(dto);
		//
		E persistedEntity = null;
		if (dto.getId() != null) {
			persistedEntity = this.get(dto.getId());
		}
		E entity = getRepository().save(toEntity(dto, persistedEntity));
		return toDto(entity);
	}

	/**
	 * Saves all given dtos.
	 * 
	 * @param dtos
	 * @return the saved dtos
	 * @throws IllegalArgumentException
	 *             in case the given dto is {@literal null}.
	 */
	@Override
	@Transactional
	public Iterable<DTO> saveAllDto(Iterable<DTO> dtos) {
		Assert.notNull(dtos);
		//
		List<DTO> savedDtos = new ArrayList<>();
		dtos.forEach(entity -> {
			savedDtos.add(saveDto(entity));
		});
		return savedDtos;
	}

	/**
	 * Deletes a given DTO.
	 * 
	 * @param dto
	 * @throws IllegalArgumentException
	 *  in case the given DTO is {@literal null}.
	 */
	@Override
	@Transactional
	public void deleteDto(DTO dto) {
		Assert.notNull(dto);
		//
		getRepository().delete((UUID) dto.getId());
	}

	/**
	 * Deletes a given entity.
	 * 
	 * @param entity
	 * @throws IllegalArgumentException
	 *  in case the given entity is {@literal null}.
	 */
	@Override
	@Transactional
	public void deleteDtoById(Serializable id) {
		Assert.notNull(id);
		//
		getRepository().delete((UUID) id);
	}

}
