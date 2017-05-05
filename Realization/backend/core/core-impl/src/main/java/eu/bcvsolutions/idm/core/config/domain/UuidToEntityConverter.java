package eu.bcvsolutions.idm.core.config.domain;

import static eu.bcvsolutions.idm.core.api.utils.EntityUtils.getFirstFieldInClassHierarchy;

import java.lang.reflect.Field;
import java.util.UUID;

import javax.persistence.EntityManager;

import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;
import org.modelmapper.spi.PropertyMapping;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.exception.CoreException;
import eu.bcvsolutions.idm.core.api.service.EntityLookupService;
import eu.bcvsolutions.idm.core.api.service.ReadDtoService;
import eu.bcvsolutions.idm.core.api.service.ReadEntityService;

/**
 * Converter for transform fields (marked with {@link Embedded} annotation) from
 * UUID to BaseEntity.
 * 
 * @author svandav
 *
 */
public class UuidToEntityConverter implements Converter<UUID, BaseEntity> {

	private EntityLookupService lookupService;
	private ApplicationContext applicationContext;
	private EntityManager entityManager; // TODO: dto / entity lookup 

	/**
	 * {@link ApplicationContext} is required, because we need use
	 * {@link EntityLookupService}. This service but is not initialised in model
	 * mapper create phase.
	 * 
	 * @param applicationContext
	 */
	public UuidToEntityConverter(ApplicationContext applicationContext, EntityManager entityManager) {
		Assert.notNull(applicationContext, "Application context is required!");
		this.applicationContext = applicationContext;
		this.entityManager = entityManager;
	}

	@Override
	public BaseEntity convert(MappingContext<UUID, BaseEntity> context) {
		if (context != null && context.getSource() != null) {
			UUID sourceUUID = context.getSource();
			Class<BaseEntity> entityClass = context.getDestinationType();

			MappingContext<?, ?> parentContext = context.getParent();

			PropertyMapping propertyMapping = (PropertyMapping) context.getMapping();
			// Find name of field by property mapping
			String field = propertyMapping.getLastDestinationProperty().getName();
			ReadDtoService<? extends BaseDto, ? extends BaseFilter> dtoService = null;
			try {
				// Find field in DTO class
				Field fieldTyp = getFirstFieldInClassHierarchy(parentContext.getSourceType(), field);
				if (fieldTyp.isAnnotationPresent(Embedded.class)) {
					Embedded embeddedAnnotation = fieldTyp.getAnnotation(Embedded.class);
					if (embeddedAnnotation.enabled()) {
						dtoService = getLookupService().getDtoService(embeddedAnnotation.dtoClass());
					}
				}
			} catch (NoSuchFieldException | SecurityException e) {
				throw new CoreException(e);
			}

			if (dtoService == null) {
				// We do not have dto service. We try load service for entity
				ReadEntityService<?, ?> entityService = getLookupService().getEntityService(entityClass);
				return entityService.get(sourceUUID);
			} else {
				if (dtoService instanceof ReadDtoService) {
					return entityManager.find(entityClass, sourceUUID);
					// return ((AbstractReadDtoService<?, ?, ?>) dtoService).getEntity(sourceUUID);
				}
			}

		}
		return null;
	}

	private EntityLookupService getLookupService() {
		if (this.lookupService == null) {
			this.lookupService = this.applicationContext.getBean(EntityLookupService.class);
		}
		return this.lookupService;
	}
}