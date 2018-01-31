package eu.bcvsolutions.idm.core.api.service;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidatorFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

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
 * @author Radek Tomiška
 *
 * @param <DTO> dto type
 * @param <E> entity type
 * @param <F> filter type
 */
public abstract class AbstractReadWriteDtoService<DTO extends BaseDto, E extends BaseEntity, F extends BaseFilter>
		extends AbstractReadDtoService<DTO, E, F> implements ReadWriteDtoService<DTO, F> {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractReadWriteDtoService.class);
	//
	@Autowired(required = false) // optional dependency for support automatic JSR303 validations
	private ValidatorFactory validatorFactory;
	
	public AbstractReadWriteDtoService(AbstractEntityRepository<E> repository) {
		super(repository);
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * Override for include event processing etc.
	 */
	@Override
	@Transactional
	public DTO save(DTO dto, BasePermission... permission) {
		Assert.notNull(dto);
		//
		if (!ObjectUtils.isEmpty(permission)) {
			E persistEntity = null;
			if (dto.getId() != null) {
				persistEntity = this.getEntity(dto.getId());
				if (persistEntity != null) {
					// check access on previous entity - update is needed
					checkAccess(persistEntity, IdmBasePermission.UPDATE);
				}
			}
			checkAccess(toEntity(dto, persistEntity), permission); // TODO: remove one checkAccess?
		}
		//
		return saveInternal(dto);
	}

	@Override
	@Transactional
	public DTO saveInternal(DTO dto) {
		Assert.notNull(dto);
		dto = validateDto(dto);
		//
		E persistedEntity = null;
		if (dto.getId() != null) {
			persistedEntity = this.getEntity(dto.getId());
		}
		// convert to entity
		E entity = toEntity(dto, persistedEntity);
		// validate
		entity = validateEntity(entity);
		// then persist
		entity = getRepository().saveAndFlush(entity);
		// finally convert to dto
		return toDto(entity);
	}

	@Override
	@Transactional
	public Iterable<DTO> saveAll(Iterable<DTO> dtos, BasePermission... permission) {
		Assert.notNull(dtos);
		//
		List<DTO> savedDtos = new ArrayList<>();
		dtos.forEach(entity -> {
			savedDtos.add(save(entity, permission));
		});
		return savedDtos;
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * Override for include event processing etc.
	 */
	@Override
	@Transactional
	public void delete(DTO dto, BasePermission... permission) {
		checkAccess(this.getEntity(dto.getId()), permission);
		//
		deleteInternal(dto);
	}	

	@Override
	@Transactional
	public void deleteById(Serializable id, BasePermission... permission) {
		Assert.notNull(id);
		//
		delete(get(id), permission);
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
		DTO dto = get(id);
		if (dto != null) {
			deleteInternal(dto);
		}
	}
	
	/**
	 * Validates JRS303 before dto is saved
	 * 
	 * @param dto
	 * @return
	 */
	protected DTO validateDto(DTO dto) {
		return validate(dto);
	}
	
	/**
	 * Validates JRS303 before entity is saved
	 * 
	 * @param dto
	 * @return
	 */
	protected E validateEntity(E entity) {
		return validate(entity);
	}
	
	/**
	 * Validates JRS303 before object is saved
	 * 
	 * @param object
	 * @return
	 */
	private <T extends Object> T validate(T object) {
		Assert.notNull(object);
		//
		if (validatorFactory == null) {
			LOG.debug("JSR303 Validation are disabled. Configure validation factory properly.");
			return object;
		}
		Set<ConstraintViolation<T>> errors = validatorFactory.getValidator().validate(object);
		if(!errors.isEmpty()) {
			throw new ConstraintViolationException(
					MessageFormat.format("Validation failed for [{0}], errors [{1}]", object.getClass().getSimpleName(), errors),
					errors);
		}
		return object;
	}

}
