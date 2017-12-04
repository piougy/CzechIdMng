package eu.bcvsolutions.idm.rpt.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
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
	
	@Autowired
	public DefaultReportManager(
			ApplicationContext context,
			PluginRegistry<ReportExecutor, String> reportExecutorRegistry,
			PluginRegistry<ReportRenderer, String> reportRendererRegistry,
			RptReportService reportService,
			EnabledEvaluator enabledEvaluator,
			EntityEventManager entityEventManager,
			LongRunningTaskManager taskManager) {
		Assert.notNull(context);
		Assert.notNull(reportExecutorRegistry, "Report executor registry is required!");
		Assert.notNull(reportRendererRegistry, "Report renderer registry is required!");
		Assert.notNull(reportService);
		Assert.notNull(enabledEvaluator);
		Assert.notNull(entityEventManager);
		Assert.notNull(taskManager);
		//
		this.context = context;
		this.reportExecutorRegistry = reportExecutorRegistry;
		this.reportRendererRegistry = reportRendererRegistry;
		this.reportService = reportService;
		this.enabledEvaluator = enabledEvaluator;
		this.entityEventManager = entityEventManager;
		this.taskManager = taskManager;
	}
	
	@Override
	public RptReportDto generate(RptReportDto report) {
		Assert.notNull(report, "Report is required!");
		// save or update report and their filter
		fillReportName(report, getExecutor(report));
		report = reportService.save(report);
		// publish event
		return entityEventManager.process(new ReportEvent(ReportEventType.GENERATE, report)).getContent();
	}
	
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
		executor = (ReportExecutor) AutowireHelper.createBean(executor.getClass());
		// set event - event will continue after executor is processed
		executor.setEvent(event);
		// check if lrt for report is already prepared by scheduler
		boolean newTask = true;
		if (report.getLongRunningTask() != null) {
			// preserve exists lrt - execute only
			executor.setLongRunningTaskId(report.getLongRunningTask());
			newTask = false;
		}
		// set lrt into report for getting state
		LongRunningFutureTask<RptReportDto> lrt = taskManager.execute(executor);
		// set new lrt for report 
		if(newTask) {
			report.setLongRunningTask(lrt.getExecutor().getLongRunningTaskId());
			fillReportName(report, executor);
			reportService.save(report);
		}
	}

	@Override
	public RptRenderedReportDto render(RptReportDto report, String rendererName) {
		Assert.notNull(report);
		Assert.hasLength(rendererName);
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
}
