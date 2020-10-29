package eu.bcvsolutions.idm.core.model.service.impl;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.plugin.core.OrderAwarePluginRegistry;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.IdmScriptCategory;
import eu.bcvsolutions.idm.core.api.domain.ScriptAuthorityType;
import eu.bcvsolutions.idm.core.api.dto.IdmScriptAuthorityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmScriptDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmScriptAuthorityFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmScriptFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.AbstractRecoverableService;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.GroovyScriptService;
import eu.bcvsolutions.idm.core.api.service.IdmScriptAuthorityService;
import eu.bcvsolutions.idm.core.api.service.IdmScriptService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmScript;
import eu.bcvsolutions.idm.core.model.entity.IdmScriptAuthority_;
import eu.bcvsolutions.idm.core.model.entity.IdmScript_;
import eu.bcvsolutions.idm.core.model.jaxb.IdmScriptAllowClassType;
import eu.bcvsolutions.idm.core.model.jaxb.IdmScriptAllowClassesType;
import eu.bcvsolutions.idm.core.model.jaxb.IdmScriptServiceType;
import eu.bcvsolutions.idm.core.model.jaxb.IdmScriptServicesType;
import eu.bcvsolutions.idm.core.model.jaxb.IdmScriptType;
import eu.bcvsolutions.idm.core.model.repository.IdmScriptRepository;
import eu.bcvsolutions.idm.core.script.evaluator.AbstractScriptEvaluator;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * Default service for script.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 */
@Service("scriptService")
public class DefaultIdmScriptService 
		extends AbstractRecoverableService<IdmScriptType, IdmScriptDto, IdmScript, IdmScriptFilter>
		implements IdmScriptService {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultIdmScriptService.class);
	
	private static final String SCRIPT_FILE_SUFIX = "idm.sec.core.script.fileSuffix";
	private static final String DEFAULT_SCRIPT_FILE_SUFIX = "**/**.xml";
	private static final String SCRIPT_DEFAULT_BACKUP_FOLDER = "scripts/";
	private static final String SCRIPT_DEFAULT_TYPE = "groovy";

	@Autowired private GroovyScriptService groovyScriptService;
	@Autowired private IdmScriptAuthorityService scriptAuthorityService;
	@Autowired private ApplicationContext applicationContext;
	@Autowired private ConfigurationService configurationService;
	//
	private final IdmScriptRepository repository;
	private final PluginRegistry<AbstractScriptEvaluator, IdmScriptCategory> pluginExecutors;

	@Autowired
	public DefaultIdmScriptService(
			IdmScriptRepository repository,
			EntityEventManager entityEventManager,
			List<AbstractScriptEvaluator> evaluators) {
		super(repository, entityEventManager);
		//
		Assert.notNull(evaluators, "Script evaluators is required.");
		//
		pluginExecutors = OrderAwarePluginRegistry.create(evaluators);
		this.repository = repository;
	}
	
	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(CoreGroupPermission.SCRIPT, getEntityClass());
	}

	@Override
	@Transactional(readOnly = true)
	public IdmScriptDto get(Serializable id, BasePermission... permission) {
		IdmScriptDto dto = super.get(id, permission);
		//
		if (dto != null && !dto.isTrimmed()) {
			AbstractScriptEvaluator evaluator = pluginExecutors.getPluginFor(dto.getCategory());
			if (evaluator != null) {
				dto.setTemplate(evaluator.generateTemplate(dto));
			}
		}
		//
		return dto;
	}

	@Override
	@Transactional
	public void deleteInternal(IdmScriptDto dto) {
		// remove all IdmScriptAuthority for this script
		scriptAuthorityService.deleteAllByScript(dto.getId());
		//
		super.deleteInternal(dto);
	}

	@Override
	@Transactional
	public IdmScriptDto save(IdmScriptDto dto, BasePermission... permission) {
		if (dto.getScript() != null) {
			groovyScriptService.validateScript(dto.getScript());
		}
		return super.save(dto, permission);
	}
	
	@Override
	public IdmScriptDto getByCode(String code) {
		return this.toDto(repository.findOneByCode(code));
	}

	@Override
	@Transactional
	public void init() {
		for (IdmScriptType scriptType : findScripts().values()) {
			IdmScriptDto script = this.getByCode(scriptType.getCode());
			// if script exist don't save it again => init only
			if (script != null) {
				LOG.info("Load script with code [{}], script is already initialized, skipping.", scriptType.getCode());
				continue;
			}
			//
			LOG.info("Load script with code [{}], script will be initialized.", scriptType.getCode());
			// save script
			script = this.save(toDto(scriptType, null));
			// save authorities
			this.scriptAuthorityService.saveAll(authorityTypeToDto(scriptType, script));
		}
	}
	
	@Override
	protected String getBackupFolderName() {
		return SCRIPT_DEFAULT_BACKUP_FOLDER;
	}

	@Override
	@Transactional
	public IdmScriptDto redeploy(IdmScriptDto dto, BasePermission... permission) {
		IdmScriptType foundType = findScripts().get(dto.getCode());
		//
		if (foundType == null) {
			throw new ResultCodeException(CoreResultCode.SCRIPT_XML_FILE_NOT_FOUND,
					ImmutableMap.of("code", dto.getCode()));
		}
		//
		return backupAndDeploy(dto, foundType, permission);
	}

	/**
	 * Return list of {@link IdmScriptType} from resources.
	 * {@link IdmScriptType} are found by configured locations and by priority - last one wins.
	 * So default location should be configured first, then external, etc. 
	 * 
	 * @return <code, script>
	 */
	private Map<String, IdmScriptType> findScripts() {
		// last script with the same is used
		// => last location has the highest priority
		Map<String, IdmScriptType> scripts = new HashMap<>();
		//
		for (String location : configurationService.getValues(SCRIPT_FOLDER)) {
			location = location + configurationService.getValue(SCRIPT_FILE_SUFIX, DEFAULT_SCRIPT_FILE_SUFIX);
			Map<String, IdmScriptType> locationScripts = new HashMap<>();
			try {
				Resource[] resources = applicationContext.getResources(location);
				LOG.debug("Found [{}] resources on location [{}]", resources == null ? 0 : resources.length, location);
				//
				if (ArrayUtils.isEmpty(resources)) {
					continue;
				}
				//
				for (Resource resource : resources) {
					try {
						IdmScriptType scriptType = readType(location, resource.getInputStream());
						//
						// log error, if script with the same code was found twice in one resource
						if (locationScripts.containsKey(scriptType.getCode())) {
							LOG.error("More scripts with code [{}], category [{}] found on the same location [{}].",
									scriptType.getCode(),
									scriptType.getCategory(),
									location);
						}
						// last one wins
						locationScripts.put(scriptType.getCode(), scriptType);
					} catch (IOException ex) {
						LOG.error("Failed get input stream from, file name [{}].", resource.getFilename(), ex);
					}							
				}
				scripts.putAll(locationScripts);
			} catch (IOException ex) {
				throw new ResultCodeException(CoreResultCode.DEPLOY_ERROR, ImmutableMap.of("path", location), ex);
			}
		}
		return scripts;
	}

	/**
	 * Transform type to dto, if second parameter is null it will be created new
	 * dto.
	 * 
	 * @param type
	 * @param script
	 * @return
	 */
	@Override
	protected IdmScriptDto toDto(IdmScriptType type, IdmScriptDto script) {
		if (script == null) {
			script = new IdmScriptDto();
		}
		//
		if (type == null) {
			return script;
		}
		// transform type to DTO
		script.setCode(type.getCode());
		script.setName(type.getName());
		script.setCategory(type.getCategory());
		script.setScript(type.getBody());
		script.setDescription(type.getDescription());
		// parameter isn't implemented yet
		// script.setParameter(type.getParameters());
		// attribute TYPE from IdmScriptType isn't implemented yet.
		return script;
	}

	/**
	 * Generate list of authorities from {@ IdmScriptType}
	 * 
	 * @param type
	 * @return
	 */
	private List<IdmScriptAuthorityDto> authorityTypeToDto(IdmScriptType type, IdmScriptDto scriptDto) {
		List<IdmScriptAuthorityDto> authorities = new ArrayList<>();
		if (type.getAllowClasses() != null && type.getAllowClasses().getAllowClasses() != null) {
			for (IdmScriptAllowClassType allowClass : type.getAllowClasses().getAllowClasses()) {
				try {
					Class.forName(allowClass.getClassName());
				} catch (ClassNotFoundException e) {
					LOG.error(
							"Class [{}] isn't reachable, for script [{}] skip add this authority",
							allowClass.getClassName(), type.getCode(), e);
					continue;
				}
				IdmScriptAuthorityDto authDto = new IdmScriptAuthorityDto();
				authDto.setType(ScriptAuthorityType.CLASS_NAME);
				authDto.setClassName(allowClass.getClassName());
				authDto.setScript(scriptDto.getId());
				authorities.add(authDto);
			}
		}
		//
		if (type.getServices() != null && type.getServices().getServices() != null) {
			for (IdmScriptServiceType service : type.getServices().getServices()) {
				if (scriptAuthorityService.isServiceReachable(service.getName(), service.getClassName())) {
					IdmScriptAuthorityDto authDto = new IdmScriptAuthorityDto();
					authDto.setType(ScriptAuthorityType.SERVICE);
					authDto.setClassName(service.getClassName());
					authDto.setService(service.getName());
					authDto.setScript(scriptDto.getId());
					authorities.add(authDto);
				} else {
					LOG.error(
							"Service [{}] [{}] isn't reachable, for script [{}] skip add this authority",
							service.getName(), service.getClassName(), type.getCode());
					continue;
				}
			}
		}
		//
		return authorities;
	}

	/**
	 * Transform dto to type.
	 * 
	 * @param dto
	 * @return
	 */
	@Override
	protected IdmScriptType toType(IdmScriptDto dto) {
		IdmScriptType type = new IdmScriptType();
		if (dto == null) {
			return type;
		}
		// transform DTO to type
		type.setCode(dto.getCode());
		type.setName(dto.getName());
		// parameter isn't implemented yet
		// type.setParameters(dto.getParameter());
		type.setBody(dto.getScript());
		type.setCategory(dto.getCategory());
		type.setDescription(dto.getDescription());
		type.setType(SCRIPT_DEFAULT_TYPE);
		//
		if (dto.getId() == null) {
			return type;
		}
		IdmScriptAuthorityFilter filter = new IdmScriptAuthorityFilter();
		filter.setScriptId(dto.getId());
		List<IdmScriptAuthorityDto> authorities = scriptAuthorityService
				.find(
						filter, 
						PageRequest.of(
								0, 
								Integer.MAX_VALUE, 
								Sort.by(
										IdmScriptAuthority_.type.getName(),
										IdmScriptAuthority_.service.getName(),
										IdmScriptAuthority_.className.getName()
								)
						)
				)
				.getContent();
		if (authorities.isEmpty()) {
			return type;
		}
		//
		List<IdmScriptAllowClassType> classes = new ArrayList<>();
		List<IdmScriptServiceType> services = new ArrayList<>();
		for (IdmScriptAuthorityDto auth : authorities) {
			if (auth.getType() == ScriptAuthorityType.CLASS_NAME) {
				IdmScriptAllowClassType classType = new IdmScriptAllowClassType();
				classType.setClassName(auth.getClassName());
				classes.add(classType);
			} else {
				IdmScriptServiceType service = new IdmScriptServiceType();
				service.setClassName(auth.getClassName());
				service.setName(auth.getService());
				services.add(service);
			}
		}
		if (!classes.isEmpty()) {
			type.setAllowClasses(new IdmScriptAllowClassesType());
			type.getAllowClasses().setAllowClasses(classes);
		}
		if (!services.isEmpty()) {
			type.setServices(new IdmScriptServicesType());
			type.getServices().setServices(services);
		}
		//
		return type;
	}
	
	@Override
	protected IdmScriptDto backupAndDeploy(IdmScriptDto resource, IdmScriptType type, BasePermission... permission) {
		resource = super.backupAndDeploy(resource, type, permission);
		//
		// remove all authorities and save newly created
		// FIXME: drop and create in audits ...
		// FIXME: propagate permission? Now is controlled by script (~owner) only
		scriptAuthorityService.deleteAllByScript(resource.getId());
		scriptAuthorityService.saveAll(authorityTypeToDto(type, resource));
		//
		return resource;
	}
	
	@Override
	protected List<Predicate> toPredicates(Root<IdmScript> root, CriteriaQuery<?> query, CriteriaBuilder builder, IdmScriptFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		// quick - "fulltext"
		if (StringUtils.isNotEmpty(filter.getText())) {
			predicates.add(builder.or(
					builder.like(builder.lower(root.get(IdmScript_.code)), "%" + filter.getText().toLowerCase() + "%"),
					builder.like(builder.lower(root.get(IdmScript_.description)), "%" + filter.getText().toLowerCase() + "%"),
					builder.like(builder.lower(root.get(IdmScript_.name)), "%" + filter.getText().toLowerCase() + "%")			
					));
		}
		//code name of script
		if (StringUtils.isNotEmpty(filter.getCode())) {
			predicates.add(builder.equal(root.get(IdmScript_.code), filter.getCode()));
		}
		//description of script
		if (StringUtils.isNotEmpty(filter.getDescription())) {
			predicates.add(builder.like(builder.lower(root.get(IdmScript_.description)),( "%" + filter.getDescription().toLowerCase() + "%")));
		}
		//category of script
		if (filter.getCategory() != null) {
			predicates.add(builder.equal(root.get(IdmScript_.category), filter.getCategory()));
		}
		//categories of script - or
		List<IdmScriptCategory> categories = filter.getInCategory();
		if (!categories.isEmpty()) {
		  predicates.add(root.get(IdmScript_.category).in(categories));
		}
		//usedIn of script - finds in which scripts is used in
		if (StringUtils.isNotEmpty(filter.getUsedIn())) {
			predicates.add(builder.like(root.get(IdmScript_.script),( "%setScriptCode('" + filter.getUsedIn() + "')%")));
		}
		//
		return predicates;
	}
}
