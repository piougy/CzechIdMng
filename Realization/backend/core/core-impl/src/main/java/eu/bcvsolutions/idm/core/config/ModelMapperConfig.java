package eu.bcvsolutions.idm.core.config;

import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.modelmapper.Condition;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.TypeMap;
import org.modelmapper.convention.MatchingStrategies;
import org.modelmapper.spi.MappingContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.config.domain.EntityToUuidConverter;
import eu.bcvsolutions.idm.core.config.domain.OperationResultConverter;
import eu.bcvsolutions.idm.core.config.domain.StringToStringConverter;
import eu.bcvsolutions.idm.core.config.domain.UuidToUuidConverter;
import eu.bcvsolutions.idm.core.model.entity.IdmConceptRoleRequest;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.config.domain.UuidToEntityConverter;

/**
 * Configuration for model mapper. Set specific converters ...
 * 
 * @author svandav
 *
 */
@Configuration(ModelMapperConfig.NAME)
@Order(Ordered.HIGHEST_PRECEDENCE + 90)
public class ModelMapperConfig {
	
	public static final String NAME = "modelMapperConfig";

	@PersistenceContext
	private EntityManager entityManager;
	@Autowired
	private ApplicationContext applicationContext;

	@SuppressWarnings("unchecked")
	@Bean
	public ModelMapper modelMapper() {
		ModelMapper modeler = new ModelMapper();
		// We want use STRICT matching strategy ... others can be ambiguous
		modeler.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

		// Convert BaseEntity to UIID (get ID)
		Converter<? extends BaseEntity, UUID> entityToUiid = new EntityToUuidConverter(modeler, applicationContext);

		// Convert UIID to Entity
		Converter<UUID, ? extends BaseEntity> uiidToEntity = new UuidToEntityConverter(applicationContext);

		// This converter must be set for only one purpose... workaround fixed
		// error in ModelMapper.
		// When is in DTO field (applicant for example) with type UUID (with
		// conversion to IdmIdentity) and other UUID field (for example
		// modifierId), but with same value as first field, then mapper will be
		// set converted value from first field (applicant) to second field (IdmIdentity to UUID) ->
		// Class cast exception will be throw.
		
		//  + Additionally this converter allows load DTO (by UUID) and put him to embedded map.
		Converter<UUID, UUID> uuidToUiid = new UuidToUuidConverter(applicationContext);
		modeler.createTypeMap(UUID.class, UUID.class).setConverter(uuidToUiid);
		
		// Converter for resolve problem with 0x00 character in Postgress.
		modeler.createTypeMap(String.class, String.class).setConverter(new StringToStringConverter());
		// Converter OperationResult for resolve problem with 0x00 character in Postgress.
		modeler.createTypeMap(OperationResult.class, OperationResult.class).setConverter(new OperationResultConverter(modeler));
		 
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
							((AbstractDto) parentContext.getDestination()).setTrimmed(true);
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
		
		// configure default type map for entities
		// this behavior must be placed in this class, not in toDto methods (getEmbedded use mapper for map entity to dto)
		
		// identity role and backward compatibility with automatic role
		TypeMap<IdmIdentityRole, IdmIdentityRoleDto> typeMapIdentityRole = modeler.getTypeMap(IdmIdentityRole.class, IdmIdentityRoleDto.class);
		if (typeMapIdentityRole == null) {
			modeler.createTypeMap(IdmIdentityRole.class, IdmIdentityRoleDto.class);
			typeMapIdentityRole = modeler.getTypeMap(IdmIdentityRole.class, IdmIdentityRoleDto.class);
			typeMapIdentityRole.addMappings(new PropertyMap<IdmIdentityRole, IdmIdentityRoleDto>() {
				
				@Override
				protected void configure() {
					this.skip().setAutomaticRole(this.source.getAutomaticRole() != null);
				}
			});
		}
		
		// concept role request and automatic role backward compatibility
		TypeMap<IdmConceptRoleRequest, IdmConceptRoleRequestDto> typeMapRoleConcept = modeler.getTypeMap(IdmConceptRoleRequest.class, IdmConceptRoleRequestDto.class);
		if (typeMapRoleConcept == null) {
			modeler.createTypeMap(IdmConceptRoleRequest.class, IdmConceptRoleRequestDto.class);
			typeMapRoleConcept = modeler.getTypeMap(IdmConceptRoleRequest.class, IdmConceptRoleRequestDto.class);
			typeMapRoleConcept.addMappings(new PropertyMap<IdmConceptRoleRequest, IdmConceptRoleRequestDto>() {
				
				@Override
				protected void configure() {
					this.skip().setAutomaticRole(null);
				}
			});
		}

		return modeler;
	}

}
