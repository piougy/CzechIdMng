package eu.bcvsolutions.idm.core.config.domain;

import static eu.bcvsolutions.idm.core.api.utils.EntityUtils.getFirstFieldInClassHierarchy;

import java.lang.reflect.Field;
import java.util.UUID;

import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;
import org.modelmapper.spi.PropertyMapping;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.exception.CoreException;
import eu.bcvsolutions.idm.core.api.rest.lookup.EntityLookup;
import eu.bcvsolutions.idm.core.api.service.LookupService;

/**
 * Converter for transform fields (marked with {@link Embedded} annotation) from
 * UUID to BaseEntity.
 * 
 * @author svandav
 * @author Radek Tomiška
 *
 */
public class UuidToEntityConverter implements Converter<UUID, BaseEntity> {

	private LookupService lookupService;
	private ApplicationContext applicationContext;

	/**
	 * {@link ApplicationContext} is required, because we need use
	 * {@link LookupService}. This service but is not initialised in model
	 * mapper create phase.
	 * 
	 * @param applicationContext
	 */
	public UuidToEntityConverter(ApplicationContext applicationContext) {
		Assert.notNull(applicationContext, "Application context is required!");
		this.applicationContext = applicationContext;
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
			try {
				// Find field in DTO class
				Field fieldTyp = getFirstFieldInClassHierarchy(parentContext.getSourceType(), field);
				if (fieldTyp.isAnnotationPresent(Embedded.class)) {
					Embedded embeddedAnnotation = fieldTyp.getAnnotation(Embedded.class);
					if (embeddedAnnotation.enabled()) {
						EntityLookup<?> lookup = getLookupService().getEntityLookup(embeddedAnnotation.dtoClass());
						if (lookup != null) {
							return lookup.lookup(sourceUUID);
						}
					}
				}
			} catch (NoSuchFieldException | SecurityException e) {
				throw new CoreException(e);
			}

			// We do not have lookup by embedded annotation. We try load service for entity
			EntityLookup<?> lookup = getLookupService().getEntityLookup(entityClass);
			if (lookup != null) {
				return lookup.lookup(sourceUUID);
			}
		}
		return null;
	}

	private LookupService getLookupService() {
		if (this.lookupService == null) {
			this.lookupService = this.applicationContext.getBean(LookupService.class);
		}
		return this.lookupService;
	}
}