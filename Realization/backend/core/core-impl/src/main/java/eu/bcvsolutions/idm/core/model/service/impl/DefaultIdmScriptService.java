package eu.bcvsolutions.idm.core.model.service.impl;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.lang3.ArrayUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
import eu.bcvsolutions.idm.core.api.jaxb.JaxbCharacterEscapeEncoder;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.GroovyScriptService;
import eu.bcvsolutions.idm.core.api.service.IdmScriptAuthorityService;
import eu.bcvsolutions.idm.core.api.service.IdmScriptService;
import eu.bcvsolutions.idm.core.model.entity.IdmScript;
import eu.bcvsolutions.idm.core.model.jaxb.IdmScriptAllowClassType;
import eu.bcvsolutions.idm.core.model.jaxb.IdmScriptAllowClassesType;
import eu.bcvsolutions.idm.core.model.jaxb.IdmScriptServiceType;
import eu.bcvsolutions.idm.core.model.jaxb.IdmScriptServicesType;
import eu.bcvsolutions.idm.core.model.jaxb.IdmScriptType;
import eu.bcvsolutions.idm.core.model.repository.IdmScriptRepository;
import eu.bcvsolutions.idm.core.script.evaluator.AbstractScriptEvaluator;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;

/**
 * Default service for script
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 */
@Service("scriptService")
public class DefaultIdmScriptService 
		extends AbstractReadWriteDtoService<IdmScriptDto, IdmScript, IdmScriptFilter>
		implements IdmScriptService {

	// TODO: script + common template configuration
	private static final String SCRIPT_FOLDER = "idm.sec.core.script.folder";
	private static final String SCRIPT_FILE_SUFIX = "idm.sec.core.script.fileSuffix";
	private static final String DEFAULT_SCRIPT_FILE_SUFIX = "**/**.xml";
	private static final String SCRIPT_DEFAULT_BACKUP_FOLDER = "scripts/";
	private static final String SCRIPT_DEFAULT_TYPE = "groovy";

	private final GroovyScriptService groovyScriptService;
	private final IdmScriptAuthorityService scriptAuthorityService;
	private final IdmScriptRepository repository;
	private final PluginRegistry<AbstractScriptEvaluator, IdmScriptCategory> pluginExecutors;
	private final ApplicationContext applicationContext;
	private final ConfigurationService configurationService;
	private final SecurityService securityService;

	private JAXBContext jaxbContext = null;

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultIdmScriptService.class);

	@Autowired
	public DefaultIdmScriptService(IdmScriptRepository repository, GroovyScriptService groovyScriptService,
			IdmScriptAuthorityService scriptAuthorityService, List<AbstractScriptEvaluator> evaluators,
			ApplicationContext applicationContext, ConfigurationService configurationService,
			SecurityService securityService) {
		super(repository);
		//
		Assert.notNull(scriptAuthorityService);
		Assert.notNull(groovyScriptService);
		Assert.notNull(repository);
		Assert.notNull(evaluators);
		Assert.notNull(applicationContext);
		Assert.notNull(configurationService);
		Assert.notNull(securityService);
		//
		this.scriptAuthorityService = scriptAuthorityService;
		this.groovyScriptService = groovyScriptService;
		this.repository = repository;
		this.applicationContext = applicationContext;
		this.configurationService = configurationService;
		this.securityService = securityService;
		//
		pluginExecutors = OrderAwarePluginRegistry.create(evaluators);
		//
		try {
			jaxbContext = JAXBContext.newInstance(IdmScriptType.class);
		} catch (JAXBException e) {
			// throw error, or just log error and continue?
			throw new ResultCodeException(CoreResultCode.XML_JAXB_INIT_ERROR, e);
		}
	}
	
	@Override
	protected Page<IdmScript> findEntities(IdmScriptFilter filter, Pageable pageable, BasePermission... permission) {
		if (filter == null) {
			return getRepository().findAll(pageable);
		}
		return repository.find(filter, pageable);
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
	public void deleteInternal(IdmScriptDto dto) {
		// remove all IdmScriptAuthority for this script
		scriptAuthorityService.deleteAllByScript(dto.getId());
		//
		super.deleteInternal(dto);
	}

	@Override
	public IdmScriptDto save(IdmScriptDto dto, BasePermission... permission) {
		if (dto.getScript() != null) {
			groovyScriptService.validateScript(dto.getScript());
		}
		return super.save(dto, permission);
	}

	@Override
	public IdmScriptDto getScriptByName(String name) {
		return this.toDto(repository.findOneByName(name));
	}

	@Override
	@Deprecated
	public IdmScriptDto getScriptByCode(String code) {
		return this.toDto(repository.findOneByCode(code));
	}
	
	@Override
	public IdmScriptDto getByCode(String code) {
		return getScriptByCode(code);
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
			script = this.save(typeToDto(scriptType, null));
			// save authorities
			this.scriptAuthorityService.saveAll(authorityTypeToDto(scriptType, script));
		}
	}

	@Override
	public void backup(IdmScriptDto dto) {
		String directory = getDirectoryForBackup();
		//
		Marshaller jaxbMarshaller = initJaxbMarshaller();
		//
		File backupFolder = new File(directory);
		if (!backupFolder.exists()) {
			boolean success = backupFolder.mkdirs();
			// if make dir after check if exist, throw error.
			if (!success) {
				LOG.error("Backup for script: {} failed, backup folder path: [{}] can't be created.", dto.getCode(),
						backupFolder.getAbsolutePath());
				throw new ResultCodeException(CoreResultCode.BACKUP_FAIL,
						ImmutableMap.of("code", dto.getCode()));
			}
		}
		//
		IdmScriptAuthorityFilter filter = new IdmScriptAuthorityFilter();
		filter.setScriptId(dto.getId());
		IdmScriptType type = dtoToType(dto, this.scriptAuthorityService.find(filter, null).getContent());
		//
		File file = new File(getBackupFileName(directory, dto));
		try {
			jaxbMarshaller.marshal(type, file);
		} catch (JAXBException e) {
			LOG.error("Backup for script: {} failed, error message: {}", dto.getCode(),
					e.getLocalizedMessage(), e);
			throw new ResultCodeException(CoreResultCode.BACKUP_FAIL,
					ImmutableMap.of("code", dto.getCode()), e);
		}
	}

	@Override
	public IdmScriptDto redeploy(IdmScriptDto dto) {
		IdmScriptType foundType = findScripts().get(dto.getCode());
		//
		if (foundType == null) {
			throw new ResultCodeException(CoreResultCode.SCRIPT_XML_FILE_NOT_FOUND,
					ImmutableMap.of("code", dto.getCode()));
		}
		//
		return deployNewAndBackupOld(dto, foundType);
	}

	/**
	 * Return list of {@link IdmScriptType} from resources.
	 * {@link IdmScriptType} are found by configured locations and by priority - last one wins.
	 * So default location should be configured first, then external, etc. 
	 * 
	 * @return <code, script>
	 */
	private Map<String, IdmScriptType> findScripts() {
		Unmarshaller jaxbUnmarshaller = null;
		//
		try {
			jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		} catch (JAXBException e) {
			throw new ResultCodeException(CoreResultCode.XML_JAXB_INIT_ERROR, e);
		}
		// last script with the same is used
		// => last location has the highest priority
		Map<String, IdmScriptType> scripts = new HashMap<>();
		//
		for(String location : configurationService.getValues(SCRIPT_FOLDER)) {
			location = location + configurationService.getValue(SCRIPT_FILE_SUFIX, DEFAULT_SCRIPT_FILE_SUFIX);
			Map<String, IdmScriptType> locationScripts = new HashMap<>();
			try {
				Resource[] resources = applicationContext.getResources(location);
				LOG.debug("Found [{}] resources on location [{}]", resources == null ? 0 : resources.length, location);
				//
				if (ArrayUtils.isNotEmpty(resources)) {
					for (Resource resource : resources) {
						try {
							IdmScriptType scriptType = (IdmScriptType) jaxbUnmarshaller.unmarshal(resource.getInputStream());
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
						} catch (JAXBException ex) {
							LOG.error("Script validation failed, file name [{}].", resource.getFilename(), ex);
						} catch (IOException ex) {
							LOG.error("Failed get input stream from, file name [{}].", resource.getFilename(), ex);
						}							
					}
					scripts.putAll(locationScripts);
				}
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
	private IdmScriptDto typeToDto(IdmScriptType type, IdmScriptDto script) {
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
	 * Return folder for backups. If isn't folder defined in configuration
	 * properties use default folder from system property java.io.tmpdir.
	 * 
	 * @return
	 */
	private String getDirectoryForBackup() {
		String backupPath = configurationService.getValue(BACKUP_FOLDER_CONFIG);
		if (backupPath == null) {
			// if backup path null throw error, backup folder must be set
			throw new ResultCodeException(CoreResultCode.BACKUP_FOLDER_NOT_FOUND,
					ImmutableMap.of("property", BACKUP_FOLDER_CONFIG));
		}
		// apend script default backup folder
		backupPath = backupPath + "/" + SCRIPT_DEFAULT_BACKUP_FOLDER;
		// add date folder
		DateTime date = new DateTime();
		DecimalFormat decimalFormat = new DecimalFormat("00");
		return backupPath + date.getYear() + decimalFormat.format(date.getMonthOfYear())
				+ decimalFormat.format(date.getDayOfMonth()) + "/";
	}

	/**
	 * Create instance of JaxbMarshaller and set required properties to him.
	 * 
	 * @return
	 */
	private Marshaller initJaxbMarshaller() {
		Marshaller jaxbMarshaller = null;
		try {
			jaxbMarshaller = jaxbContext.createMarshaller();
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			jaxbMarshaller.setProperty(Marshaller.JAXB_ENCODING, StandardCharsets.UTF_8.name());
			jaxbMarshaller.setProperty(ENCODING_HANDLER, new JaxbCharacterEscapeEncoder());
		} catch (JAXBException e) {
			throw new ResultCodeException(CoreResultCode.XML_JAXB_INIT_ERROR, e);
		}
		return jaxbMarshaller;
	}

	/**
	 * Transform dto to type.
	 * 
	 * @param dto
	 * @return
	 */
	private IdmScriptType dtoToType(IdmScriptDto dto, List<IdmScriptAuthorityDto> authorities) {
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
		if (authorities != null && !authorities.isEmpty()) {
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
		}
		return type;
	}

	/**
	 * Method return path for file. That will be save into backup directory.
	 * 
	 * @param directory
	 * @param script
	 * @return
	 */
	private String getBackupFileName(String directory, IdmScriptDto script) {
		return directory + script.getCode() + "_" + securityService.getCurrentUsername() + "_"
				+ System.currentTimeMillis() + EXPORT_FILE_SUFIX;
	}

	/**
	 * Method replace all attribute from dto with type attributes, old dto will
	 * be backup to system folder. Also save authorities.
	 * 
	 * @param oldScript
	 * @param newScript
	 * @return
	 */
	private IdmScriptDto deployNewAndBackupOld(IdmScriptDto oldScript, IdmScriptType newScript) {
		// backup
		this.backup(oldScript);
		// transform new
		oldScript = typeToDto(newScript, oldScript);
		// remove all authorities and save newly created
		scriptAuthorityService.deleteAllByScript(oldScript.getId());
		scriptAuthorityService.saveAll(authorityTypeToDto(newScript, oldScript));
		return this.save(oldScript);
	}
}
