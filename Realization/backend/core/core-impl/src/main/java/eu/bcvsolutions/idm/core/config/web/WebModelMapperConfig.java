package eu.bcvsolutions.idm.core.config.web;

import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.modelmapper.Condition;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.modelmapper.spi.MappingContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.service.EntityLookupService;
import eu.bcvsolutions.idm.core.config.domain.EntityToUiidConverter;
import eu.bcvsolutions.idm.core.config.domain.UiidToEntityConverter;


/**
 * Configuration for model mapper. Set specific converters ...
 * @author svandav
 *
 */
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE + 90)
public class WebModelMapperConfig {

	@PersistenceContext
	private EntityManager entityManager;
	
	@Autowired
	private EntityLookupService lookupService;


	@SuppressWarnings("unchecked")
	@Bean
	public ModelMapper modelMapper() {
		ModelMapper modeler = new ModelMapper();

		// Convert BaseEntity to UIID (get ID)
		Converter<? extends BaseEntity, UUID> entityToUiid = new EntityToUiidConverter(modeler);
		
		// Convert UIID to Entity
		Converter<UUID, ? extends BaseEntity> uiidToEntity = new UiidToEntityConverter(lookupService);


		// Condition for property ... if is property list and dto is trimmed,
		// then will be not used (set null)
		// or if is property list and have parent dto, then will be to set null
		// (only two levels are allowed).
		Condition<Object, Object> trimmListCondition = new Condition<Object, Object>() {

			@Override
			public boolean applies(MappingContext<Object, Object> context) {
				if (List.class.isAssignableFrom(context.getDestinationType())) {
					MappingContext<?, ?> parentContext = context.getParent();
					MappingContext<?, ?> superContext = parentContext != null ? parentContext.getParent() : null;

					if (superContext != null) {
						if (parentContext != null && parentContext.getDestination() instanceof AbstractDto) {
							((AbstractDto) parentContext).setTrimmed(true);
						}
						return false;
					}
					if (parentContext != null && parentContext.getDestination() instanceof AbstractDto
							&& ((AbstractDto) parentContext.getDestination()).isTrimmed()) {
						return false;
					}
				}
				return true;
			}

		};

		modeler.getConfiguration().setPropertyCondition(trimmListCondition);

		// entity to uiid converters will be set for all entities
		entityManager.getMetamodel().getEntities().forEach(entityType -> {
			if (entityType.getJavaType() == null) {
				return;
			}
			@SuppressWarnings("rawtypes")
			TypeMap typeMapEntityToUiid = modeler.createTypeMap(entityType.getJavaType(), UUID.class);
			typeMapEntityToUiid.setConverter(entityToUiid);
			
			@SuppressWarnings("rawtypes")
			TypeMap typeMapUiidToEntity = modeler.createTypeMap(UUID.class, entityType.getJavaType());
			typeMapUiidToEntity.setConverter(uiidToEntity);
		});

		return modeler;
	}

}
