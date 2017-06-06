package eu.bcvsolutions.idm.core.script.evaluator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.plugin.core.Plugin;

import com.google.common.collect.ImmutableMap;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.IdmScriptCategory;
import eu.bcvsolutions.idm.core.api.domain.ScriptAuthorityType;
import eu.bcvsolutions.idm.core.api.dto.IdmScriptAuthorityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmScriptDto;
import eu.bcvsolutions.idm.core.api.dto.filter.ScriptAuthorityFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.script.ScriptEnabled;
import eu.bcvsolutions.idm.core.api.service.GroovyScriptService;
import eu.bcvsolutions.idm.core.model.entity.IdmScript;
import eu.bcvsolutions.idm.core.model.entity.IdmScriptAuthority;
import eu.bcvsolutions.idm.core.model.repository.IdmScriptRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmScriptAuthorityService;


/**
 * Abstract interface for evaluators. Subclass is defined by
 * {@link IdmScriptCategory}. Subclass resolve permissions for each 
 * evaluated script.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public abstract class AbstractScriptEvaluator implements Plugin<IdmScriptCategory>, ScriptEnabled {
	
	public static String SCRIPT_EVALUATOR = "scriptEvaluator";
	
	@Autowired
	private GroovyScriptService groovyScriptService;
	
	@Autowired
	private IdmScriptRepository scriptRepository;
	
	@Autowired
	private IdmScriptAuthorityService scriptAuthorityService;
	
	@Autowired
	private ApplicationContext applicationContext;
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractScriptEvaluator.class);
	
	/**
	 * Evaluated given script with parameters. Check if this we have permission for evaluated this script.
	 * @param scriptCode
	 * @param parameters
	 * @return
	 * @throws ClassNotFoundException 
	 */
	protected Object evaluate(String scriptCode, Map<String, Object> parameters) {
		IdmScript script = scriptRepository.findOneByCode(scriptCode);
		//
		if (script == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("script", scriptCode));
		}
		//
		if (!this.supports(script.getCategory())) {
			throw new ResultCodeException(CoreResultCode.GROOVY_SCRIPT_INVALID_CATEGORY, ImmutableMap.of("scriptCategory", script.getCategory()));
		}
		//
		List<IdmScriptAuthorityDto> scriptAuthorities = getScriptAuthorityForScript(script.getId());
		//
		List<Class<?>> extraAllowedClasses = new ArrayList<>();
		//
		// Add builder
		extraAllowedClasses.add(Builder.class);
		//
		for (IdmScriptAuthorityDto scriptAuthority : scriptAuthorities) {
			if (scriptAuthority.getType() == ScriptAuthorityType.CLASS_NAME) {
				try {
					extraAllowedClasses.add(Class.forName(scriptAuthority.getClassName()));
				} catch (ClassNotFoundException e) {
					LOG.error("[AbstractScriptEvaluator] - " + e.getLocalizedMessage());
					throw new ResultCodeException(CoreResultCode.BAD_VALUE, ImmutableMap.of("class", scriptAuthority.getClassName()), e);
				}
			} else {
				parameters.put(scriptAuthority.getService(), applicationContext.getBean(scriptAuthority.getService()));
			}
		}
		//
		return groovyScriptService.evaluate(script.getScript(), parameters, extraAllowedClasses);
	}
	
	/**
	 * Method generate template with use for scripts
	 * @return
	 */
	public abstract String generateTemplate(IdmScriptDto script);
	
	public Builder newBuilder() {
		return new Builder();
	}
	
	/**
	 * Evaluated script with parameters given in {@link Builder}. Recall method eval with scriptName and parameters.
	 * Check if this we have permission for evaluated this script.
	 * @param builder
	 * @return
	 */
	public Object evaluate(Builder builder) {
		return this.evaluate(builder.getScriptCode(), builder.getParameters());
	}
	
	/**
	 * Method find {@link IdmScriptAuthority} for {@link IdmScript} id
	 * @param scriptId
	 * @return
	 */
	private List<IdmScriptAuthorityDto> getScriptAuthorityForScript(UUID scriptId) {
		ScriptAuthorityFilter filter = new ScriptAuthorityFilter();
		filter.setScriptId(scriptId);
		return scriptAuthorityService.find(filter, null).getContent();
	}
	
	public static class Builder {
		
		private Map<String, Object> parameters;
		
		private String scriptCode;
		
		public Builder() {
		}

		public Map<String, Object> getParameters() {
			return parameters;
		}

		public String getScriptCode() {
			return scriptCode;
		}
		
		public Builder setScriptCode(String scriptCode) {
			this.scriptCode = scriptCode;
			return this;
		}
		
		public Builder setParameters(Map<String, Object> parameters) {
			this.parameters = parameters;
			return this;
		}
		
		public Builder addParameter(String key, Object value) {
			if (this.parameters == null) {
				this.parameters = new HashMap<>();
			}
			this.parameters.put(key, value);
			return this;
		}
		
		public Builder build() {
			return this;
		}
	}
}
