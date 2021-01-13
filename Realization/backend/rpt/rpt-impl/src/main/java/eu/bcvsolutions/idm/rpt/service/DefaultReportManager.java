package eu.bcvsolutions.idm.rpt.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.LongRunningFutureTask;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import eu.bcvsolutions.idm.core.security.api.service.EnabledEvaluator;
import eu.bcvsolutions.idm.rpt.RptModuleDescriptor;
import eu.bcvsolutions.idm.rpt.api.dto.RptRenderedReportDto;
import eu.bcvsolutions.idm.rpt.api.dto.RptReportDto;
import eu.bcvsolutions.idm.rpt.api.dto.RptReportExecutorDto;
import eu.bcvsolutions.idm.rpt.api.dto.RptReportRendererDto;
import eu.bcvsolutions.idm.rpt.api.event.ReportEvent;
import eu.bcvsolutions.idm.rpt.api.event.ReportEvent.ReportEventType;
import eu.bcvsolutions.idm.rpt.api.executor.ReportExecutor;
import eu.bcvsolutions.idm.rpt.api.renderer.RendererRegistrar;
import eu.bcvsolutions.idm.rpt.api.renderer.ReportRenderer;
import eu.bcvsolutions.idm.rpt.api.service.ReportManager;
import eu.bcvsolutions.idm.rpt.api.service.RptReportService;

/**
 * Default report manager
 * 
 * @author Radek Tomiška
 *
 */
@Service("reportManager")
@Enabled(RptModuleDescriptor.MODULE_ID)
public class DefaultReportManager implements ReportManager {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultReportManager.class);
	//
	private final ApplicationContext context;
	private final PluginRegistry<ReportExecutor, String> reportExecutorRegistry;
	private final PluginRegistry<ReportRenderer, String> reportRendererRegistry;
    private final RptReportService reportService;
	private final EnabledEvaluator enabledEvaluator;
	private final EntityEventManager entityEventManager;
    private final LongRunningTaskManager taskManager;
    //
    @Autowired private LookupService lookupService;
	
	@Autowired
	public DefaultReportManager(
			ApplicationContext context,
			PluginRegistry<ReportExecutor, String> reportExecutorRegistry,
			PluginRegistry<ReportRenderer, String> reportRendererRegistry,
			RptReportService reportService,
			EnabledEvaluator enabledEvaluator,
			EntityEventManager entityEventManager,
			LongRunningTaskManager taskManager) {
		Assert.notNull(context, "Context is required.");
		Assert.notNull(reportExecutorRegistry, "Report executor registry is required!");
		Assert.notNull(reportRendererRegistry, "Report renderer registry is required!");
		Assert.notNull(reportService, "Service is required.");
		Assert.notNull(enabledEvaluator, "Enabled evaluator is required.");
		Assert.notNull(entityEventManager, "Manager is required.");
		Assert.notNull(taskManager, "Manager is required.");
		//
		this.context = context;
		this.reportExecutorRegistry = reportExecutorRegistry;
		this.reportRendererRegistry = reportRendererRegistry;
		this.reportService = reportService;
		this.enabledEvaluator = enabledEvaluator;
		this.entityEventManager = entityEventManager;
		this.taskManager = taskManager;
	}
	
	/**
	 * Publish event only.
	 */
	@Override
	public RptReportDto generate(RptReportDto report) {
		Assert.notNull(report, "Report is required!");
		// save or update report and their filter
		fillReportName(report, getExecutor(report));
		report = reportService.save(report);
		// publish event
		return entityEventManager.process(new ReportEvent(ReportEventType.GENERATE, report)).getContent();
	}
	
	/**
	 * Generate report.
	 */
	@Override
	public void generate(EntityEvent<RptReportDto> event) {
		Assert.notNull(event, "Report event is required!");
		RptReportDto report = event.getContent();
		Assert.notNull(report, "Report is required!");
		Assert.notNull(report.getId(), "Persisted report is required!");
		//
		ReportExecutor executor = reportExecutorRegistry.getPluginFor(report.getExecutorName());
		if (executor == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("reportExecutor", report.getExecutorName()));
		}
		// create new executor instance
		executor = (ReportExecutor) AutowireHelper.createBean(AutowireHelper.getTargetClass(executor));
		// set event - event will continue after executor is processed
		executor.setEvent(event);
		// check if lrt for report is already prepared by scheduler
		boolean newTask = true;
		//
		if (report.getLongRunningTask() != null) {
			// preserve exists lrt - execute only
			executor.setLongRunningTaskId(report.getLongRunningTask());
			//
			report = initFormTask(report, executor);
			//
			newTask = false;
		}
		// set lrt into report for getting state
		LongRunningFutureTask<RptReportDto> lrt = taskManager.execute(executor);
		// set new lrt for report 
		if (newTask) {
			report.setLongRunningTask(lrt.getExecutor().getLongRunningTaskId());
			fillReportName(report, executor);
			//
			saveTaskProperties(report);
			//
			reportService.save(report);
		}
	}

	@Override
	public RptRenderedReportDto render(RptReportDto report, String rendererName) {
		Assert.notNull(report, "Report is required.");
		Assert.hasLength(rendererName, "Renderer name is required.");
		//
		ReportRenderer renderer = reportRendererRegistry.getPluginFor(rendererName);
		if (renderer == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("reportRenderer", rendererName));
		}
		//
		RptRenderedReportDto result = new RptRenderedReportDto();
		result.setInputReport(report);
		result.setRenderer(toDto(renderer));
		result.setRenderedReport(renderer.render(report));
		return result;
	}

	@Override
	public List<RptReportExecutorDto> getExecutors() {
		return reportExecutorRegistry
				.getPlugins()
				.stream()
				.filter(executor -> {
					return enabledEvaluator.isEnabled(executor);
				})
				.map(executor -> {
					try {
						return toDto(executor);
					} catch (ResultCodeException ex) {
						LOG.error("Report executor [{}] has wrong form defition, skipping", executor.getName(), ex);
						return null;
					}
				})
				.filter(Objects::nonNull)
				.sorted(Comparator.comparing(RptReportExecutorDto::getName))
				.collect(Collectors.toList());
	}

	@Override
	public List<RptReportRendererDto> getRenderers(String reportName) {
		List<RptReportRendererDto> renderers = new ArrayList<>();
		context
			.getBeansOfType(RendererRegistrar.class)
			.entrySet()
			.stream()
			.filter(renderer -> {
				return enabledEvaluator.isEnabled(renderer.getValue());
			})
			.forEach(registrarBean -> {
				String[] rendererNames = registrarBean.getValue().register(reportName);
				if (rendererNames != null) {
					for (String rendererName : rendererNames) {
						ReportRenderer renderer = reportRendererRegistry.getPluginFor(rendererName);
						if (enabledEvaluator.isEnabled(renderer)) {
							renderers.add(toDto(renderer));
						}
					}
				}
			});
		return renderers;
	}
	
	private RptReportExecutorDto toDto(ReportExecutor executor) {
		RptReportExecutorDto dto = new RptReportExecutorDto();
		//
		dto.setId(executor.getName());
		dto.setName(executor.getName());
		dto.setModule(executor.getModule());
		dto.setDescription(AutowireHelper.getBeanDescription(executor.getClass()));
		dto.setDisabled(executor.isDisabled());
		dto.setRenderers(getRenderers(dto.getName()));
		dto.setFormDefinition(executor.getFormDefinition());
		//
		return dto;
	}
	
	private RptReportRendererDto toDto(ReportRenderer renderer) {
		RptReportRendererDto dto = new RptReportRendererDto();
		//
		dto.setId(renderer.getName());
		dto.setName(renderer.getName());
		dto.setModule(renderer.getModule());
		dto.setDescription(AutowireHelper.getBeanDescription(renderer.getClass()));
		dto.setDisabled(renderer.isDisabled());
		dto.setFormat(renderer.getFormat());
		dto.setExtension(renderer.getExtension());
		//
		return dto;
	}
	
	private ReportExecutor getExecutor(RptReportDto report) {
		ReportExecutor executor = reportExecutorRegistry.getPluginFor(report.getExecutorName());
		if (executor == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("reportExecutor", report.getExecutorName()));
		}
		return executor;
	}
	
	private void fillReportName(RptReportDto report, ReportExecutor executor) {
		if (StringUtils.isBlank(report.getName())) {
			report.setName(getReportName(report, executor));
		}
	}
	
	private String getReportName(RptReportDto report, ReportExecutor executor) {
		// filter - template - name has the highest priority
		if(report.getFilter() != null) {
			if(StringUtils.isNotBlank(report.getFilter().getName())) {
				return report.getFilter().getName();
			}
		}
		// executor's description
		String executorDescription = AutowireHelper.getBeanDescription(executor.getClass());
		if(StringUtils.isNotBlank(executorDescription)) {
			return executorDescription;
		}
		// executor's name as default
		return executor.getName();
	}
	
	/**
	 * Transfer task properties to report filter parameter (eav).
	 * 
	 * @param task
	 * @param report
	 */
	@SuppressWarnings("rawtypes")
	private RptReportDto initFormTask(RptReportDto report, ReportExecutor reportExecutor) {
		UUID longRunningTaskId = report.getLongRunningTask();
		if (longRunningTaskId == null) {
			// no lrt
			return report;
		}
		
		IdmFormDefinitionDto formDefinition = reportExecutor.getFormDefinition();
		IdmLongRunningTaskDto task = taskManager.getLongRunningTask(longRunningTaskId);
		//
		// publish new event and stop current LRT process (event has to be initialized at first)
		task.setRunning(false);
		task = taskManager.saveLongRunningTask(task);
		//
		List<IdmFormValueDto> values = new ArrayList<>();
		for (Entry<String, Object> property : task.getTaskProperties().entrySet()) {
			String propertyKey = property.getKey();
			Object propertyValue = property.getValue();
			//
			if (propertyValue == null) {
				continue;
			}
			//
			IdmFormAttributeDto formAttribute = formDefinition.getMappedAttributeByCode(propertyKey);
			if (formAttribute == null) {
				continue;
			}
			//
			if (!(propertyValue instanceof Serializable)) {
				LOG.warn("Long running task property [{}] is not serializable, cannot be used in report parameters.", propertyKey);
				continue;
			}
			if (propertyValue instanceof List) {
				// multiple values are stored in List (see bellow)
				for (Object singlePropertyValue : (List) propertyValue) {
					IdmFormValueDto value = toFilledFormValue(formAttribute, propertyKey, singlePropertyValue);
					if (value != null) {
						values.add(value);
					}
				}
			} else {
				// single value
				IdmFormValueDto value = toFilledFormValue(formAttribute, propertyKey, propertyValue);
				if (value != null) {
					values.add(value);
				}
			}
		}
		//
		if (values.isEmpty()) {
			// no filled values
			return report;
		}
		//
		IdmFormDto reportFilter = new IdmFormDto();
		reportFilter.setFormDefinition(formDefinition.getId());
		reportFilter.setValues(values);
		report.setFilter(reportFilter);
		//
		return reportService.save(report);
	}
	
	/**
	 * Convert single property value to form value.
	 */
	private IdmFormValueDto toFilledFormValue(IdmFormAttributeDto formAttribute, String propertyKey, Object propertyValue) {
		if (propertyValue == null) {
			return null;
		}
		if (!(propertyValue instanceof Serializable)) {
			LOG.warn("Long running task property [{}] is not serializable, cannot be used in report parameters.", propertyKey);
			return null;
		}
		//
		IdmFormValueDto value = new IdmFormValueDto(formAttribute);
		value.setValue((Serializable) propertyValue);
		//
		// filled value only
		return value.isEmpty() ? null : value;
	}
	
	/**
	 * Transfer report filter parameter (eav) to task properties.
	 * 
	 * @param task
	 * @param report
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void saveTaskProperties(RptReportDto report) {
		UUID longRunningTaskId = report.getLongRunningTask();
		IdmFormDto reportFilter = report.getFilter();
		if (reportFilter == null || longRunningTaskId == null) {
			// no properties
			return;
		}
		//
		Map<String, Object> taskProperties = new HashMap<>();
		reportFilter
			.getValues()
			.stream()
			.filter(value -> !value.isEmpty())
			.forEach(value -> {
				IdmFormAttributeDto formAttribute = lookupService.lookupEmbeddedDto(value, IdmFormValueDto.PROPERTY_FORM_ATTRIBUTE);
				//
				String attributeCode = formAttribute.getCode();
				if (taskProperties.containsKey(attributeCode)) {
					// multiple properties are supported
					Object propertyValue = taskProperties.get(attributeCode);
					List propertyValues;
					if (propertyValue instanceof List) {
						propertyValues = (List) propertyValue;
					} else {
						propertyValues = Lists.newArrayList(propertyValue);
					}
					propertyValues.add(value.getValue());
					taskProperties.put(formAttribute.getCode(), propertyValues);
				} else {	
					// single property
					taskProperties.put(formAttribute.getCode(), value.getValue());
				}
			});
		// save filled task properties
		if (!taskProperties.isEmpty()) {
			IdmLongRunningTaskDto task = taskManager.getLongRunningTask(longRunningTaskId);
			task.setTaskProperties(taskProperties);
			taskManager.saveLongRunningTask(task);
		}
	}
}
