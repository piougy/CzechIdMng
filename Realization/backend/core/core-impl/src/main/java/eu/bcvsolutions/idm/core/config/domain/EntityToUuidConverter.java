package eu.bcvsolutions.idm.core.config.domain;

import static eu.bcvsolutions.idm.core.api.utils.EntityUtils.getFirstFieldInClassHierarchy;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;

import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.spi.MappingContext;
import org.modelmapper.spi.PropertyMapping;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.exception.CoreException;

/**
 * Converter for transform fields (marked with {@link Embedded} annotation) from BaseEntity to UUID 
 * and add entity to embedded part main DTO.
 * 
 * @author svandav
 *
 */
public class EntityToUuidConverter implements Converter<BaseEntity, UUID> {

	private ModelMapper modeler;

	public EntityToUuidConverter(ModelMapper modeler) {
		Assert.notNull(modeler, "Modeler is required!");
		this.modeler = modeler;
	}

	@Override
	public UUID convert(MappingContext<BaseEntity, UUID> context) {
		if (context != null && context.getSource() != null && context.getSource().getId() instanceof UUID) {
			MappingContext<?, ?> parentContext = context.getParent();
			if (parentContext != null && parentContext.getDestination() != null
					&& AbstractDto.class.isAssignableFrom(parentContext.getDestinationType())
					&& parentContext.getSource() != null
					&& BaseEntity.class.isAssignableFrom(parentContext.getSourceType())) {

				try {
					AbstractDto parentDto = (AbstractDto) parentContext.getDestination();
					BaseEntity entity = (BaseEntity) context.getSource();
					Map<String, BaseDto> embedded = parentDto.getEmbedded();

					PropertyMapping propertyMapping = (PropertyMapping) context.getMapping();
					// Find name of field by property mapping
					String field = propertyMapping.getLastDestinationProperty().getName();
					// Find field in DTO class
					Field fieldTyp = getFirstFieldInClassHierarchy(parentContext.getDestinationType(), field);
					if (fieldTyp.isAnnotationPresent(Embedded.class)) {
						Embedded embeddedAnnotation = fieldTyp.getAnnotation(Embedded.class);
						if (embeddedAnnotation.enabled()) {
							// If has field Embedded (enabled) annotation, then
							// we will create new
							// instance of DTO
							AbstractDto dto = embeddedAnnotation.dtoClass().newInstance();
							dto.setTrimmed(true);
							// Separate map entity to new embedded DTO
							modeler.map(entity, dto);
							embedded.put(field, dto);
							// Add filled DTO to embedded map to parent DTO
							parentDto.setEmbedded(embedded);
						}
					}
				} catch (InstantiationException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
					throw new CoreException(e);
				}
			}
			return (UUID) context.getSource().getId();
		}
		return null;
	}
}