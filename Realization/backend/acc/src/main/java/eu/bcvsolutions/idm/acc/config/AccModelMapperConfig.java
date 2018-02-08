package eu.bcvsolutions.idm.acc.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.TypeMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.acc.dto.SysSyncLogDto;
import eu.bcvsolutions.idm.acc.entity.SysSyncLog;
import eu.bcvsolutions.idm.core.config.ModelMapperConfig;

/**
 * Acc model mapper config, add mapper configuration for acc module.
 * Model mapper is initialized by core module. In acc module we must only add type map.
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@Component(AccModelMapperConfig.NAME)
@DependsOn(ModelMapperConfig.NAME)
public class AccModelMapperConfig implements ApplicationListener<ContextRefreshedEvent> {

	public static final String NAME = "accModelMapperConfig";
	
	@Autowired
	private ModelMapper modelMapper;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		// configure default type map for entities
		// this behavior must be placed in this class, not in toDto methods (getEmbedded use mapper for map entity to dto)
		
		// synchronization item logs list
		TypeMap<SysSyncLog, SysSyncLogDto> typeMap = modelMapper.getTypeMap(SysSyncLog.class, SysSyncLogDto.class);
		if (typeMap == null) {
			modelMapper.createTypeMap(SysSyncLog.class, SysSyncLogDto.class);
			typeMap = modelMapper.getTypeMap(SysSyncLog.class, SysSyncLogDto.class);
			typeMap.addMappings(new PropertyMap<SysSyncLog, SysSyncLogDto>() {
				
				@Override
				protected void configure() {
					this.skip().setSyncActionLogs(null);
				}
			});
		}
	}
}
