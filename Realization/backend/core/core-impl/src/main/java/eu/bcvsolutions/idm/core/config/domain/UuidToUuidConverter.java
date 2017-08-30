package eu.bcvsolutions.idm.core.config.domain;

import static eu.bcvsolutions.idm.core.api.utils.EntityUtils.getFirstFieldInClassHierarchy;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;

import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;
import org.modelmapper.spi.PropertyMapping;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.exception.CoreException;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.api.service.ReadDtoService;

/**
 * Converter for UUID to UUID. This converter must be set for workaround fixed error in ModelMapper.
 * When is in DTO field (applicant for example) with type UUID (with conversion to IdmIdentity) and other UUID field (for example modifierId),
 * but with same value as first field, then mapper will be set converted value from first field (applicant) to second field -> Class cast exception will be throw. 
 * 
 * Additionally this converter allows load DTO and put him to embedded map.
 * 
 * @author svandav
 *
 */
public class UuidToUuidConverter implements Converter<UUID, UUID> {

	private LookupService lookupService;
	private ApplicationContext applicationContext;
	
	/**
	 * {@link ApplicationContext} is required, because we need use
	 * {@link LookupService}. This service but is not initialised in model
	 * mapper create phase.
	 * 
	 * @param applicationContext
	 */
	public UuidToUuidConverter(ApplicationContext applicationContext) {
		Assert.notNull(applicationContext, "Application context is required!");
		this.applicationContext = applicationContext;
	}
	
	@Override
	public UUID convert(MappingContext<UUID, UUID> context) {
		if (context != null && context.getSource() != null && context.getSource() instanceof UUID) {
			MappingContext<?, ?> parentContext = context.getParent();
			if (parentContext != null && parentContext.getDestination() != null
					&& AbstractDto.class.isAssignableFrom(parentContext.getDestinationType())
					&& parentContext.getSource() != null
					&& BaseEntity.class.isAssignableFrom(parentContext.getSourceType())) {

				try {
					AbstractDto parentDto = (AbstractDto) parentContext.getDestination();
					UUID entityId = (UUID) context.getSource();
					Map<String, BaseDto> embedded = parentDto.getEmbedded();

					PropertyMapping propertyMapping = (PropertyMapping) context.getMapping();
					// Find name of field by property mapping
					String field = propertyMapping.getLastDestinationProperty().getName();
					// Find field in DTO class
					Field fieldTyp = getFirstFieldInClassHierarchy(parentContext.getDestinationType(), field);
					if (fieldTyp.isAnnotationPresent(Embedded.class)) {
						Embedded embeddedAnnotation = fieldTyp.getAnnotation(Embedded.class);
						if (embeddedAnnotation.enabled()) {
							// Load DTO service by dtoClass and get DTO by UUID
							ReadDtoService<?, ?> lookup = getLookupService().getDtoService(embeddedAnnotation.dtoClass());
							if (lookup != null) {
								 AbstractDto dto = (AbstractDto) lookup.get(entityId);
								 dto.setTrimmed(true);
								 embedded.put(field, dto);
								 // Add filled DTO to embedded map to parent DTO
								 parentDto.setEmbedded(embedded);
							}
						}
					}
				} catch (NoSuchFieldException | SecurityException e) {
					throw new CoreException(e);
				}
			}
		}
		return (UUID)context.getSource();
	}
	
	private LookupService getLookupService() {
		if (this.lookupService == null) {
			this.lookupService = this.applicationContext.getBean(LookupService.class);
		}
		return this.lookupService;
	}
}