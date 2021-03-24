package eu.bcvsolutions.idm.rpt.report.general;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.bulk.action.BulkActionManager;
import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormAttributeService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.rpt.RptModuleDescriptor;
import eu.bcvsolutions.idm.rpt.api.dto.RptRenderedReportDto;
import eu.bcvsolutions.idm.rpt.api.dto.RptReportDto;
import eu.bcvsolutions.idm.rpt.api.dto.filter.RptReportFilter;
import eu.bcvsolutions.idm.rpt.api.service.ReportManager;
import eu.bcvsolutions.idm.rpt.api.service.RptReportService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * General formable entity export tests.
 * 
 * @author Peter
 * @author Radek Tomiška
 */
public class GeneralFormableEntityExportIntegrationTest extends AbstractIntegrationTest {

	@Autowired private BulkActionManager bulkActionManager;
	@Autowired private RptReportService reportService;
	@Autowired private IdmIdentityService identityService;
	@Autowired private FormService formService;
	@Autowired private ReportManager reportManager;
	@Autowired private IdmFormAttributeService formAttributeService;
	@Autowired private LongRunningTaskManager longRunningTaskManager;

	@Before
	public void before() {
		// report checks authorization policies - we need to log in
		getHelper().loginAdmin();
	}

	@After
	public void after() {
		super.logout();
	}

	@Test
	public void testReportWithoutValueSpecified() throws IOException {
		// prepare test identities
		IdmIdentityDto identityOne = getHelper().createIdentity((GuardedString) null);
		IdmIdentityDto identityDisabled = getHelper().createIdentity((GuardedString) null);
		identityService.disable(identityDisabled.getId());

		IdmFormAttributeDto testAttrRpt = getHelper().createEavAttribute("testAttrRpt", IdmIdentity.class, PersistentType.SHORTTEXT);
		getHelper().setEavValue(identityOne, testAttrRpt, IdmIdentity.class, "TESTVAL", PersistentType.SHORTTEXT);

		IdmFormAttributeDto testAttrRptMulti = getHelper().createEavAttribute("testAttrRptMulti", IdmIdentity.class, PersistentType.SHORTTEXT);
		testAttrRptMulti.setMultiple(true);
		formAttributeService.save(testAttrRptMulti);
		//
		formService.saveValues(identityOne, testAttrRptMulti, Arrays.asList("B", "A"));
		//
		IdmBulkActionDto bulkAction = new IdmBulkActionDto();
		bulkAction.setEntityClass(IdmIdentity.class.getCanonicalName());
		bulkAction.setFilterClass(IdmIdentityFilter.class.getCanonicalName());
		bulkAction.setModule(RptModuleDescriptor.MODULE_ID);
		bulkAction.setIdentifiers(new HashSet<UUID>(Arrays.asList(identityOne.getId(), identityDisabled.getId())));
		bulkAction.setId(AbstractFormableEntityExport.REPORT_NAME);
		bulkAction.setName(AbstractFormableEntityExport.REPORT_NAME);

		bulkActionManager.processAction(bulkAction);

		RptReportFilter reportFilter = new RptReportFilter();
		reportFilter.setText(AbstractFormableEntityExport.REPORT_NAME);

		List<RptReportDto> content = reportService.find(reportFilter, null).getContent();

		Assert.assertFalse(content.isEmpty());
		Assert.assertEquals(1, content.size());

		RptReportDto reportDto = content.get(0);
		
		Assert.assertEquals(
				CoreResultCode.LONG_RUNNING_TASK_PARTITIAL_DOWNLOAD.getCode(), 
				longRunningTaskManager.getLongRunningTask(reportDto.getLongRunningTask()).getResult().getCode()
		);

		RptRenderedReportDto render = reportManager.render(reportDto, FormableEntityXlsxRenderer.NAME);

		Assert.assertNotNull(render);

		XSSFSheet sheetAt = null;
		try (XSSFWorkbook workbook = new XSSFWorkbook(render.getRenderedReport())) {
			sheetAt = workbook.getSheetAt(0);
		}

		Map<String, Map<String, String>> parsed = sheetToMap(sheetAt);

		Assert.assertEquals(2, parsed.size());
		Assert.assertNotNull(parsed.get(identityOne.getId().toString()));
		Assert.assertNotNull(parsed.get(identityDisabled.getId().toString()));
		Assert.assertEquals("TESTVAL", parsed.get(identityOne.getId().toString()).get("testAttrRpt"));
		Assert.assertEquals("[B, A]", parsed.get(identityOne.getId().toString()).get("testAttrRptMulti"));
	}

	private Map<String, Map<String, String>> sheetToMap(XSSFSheet sheet) {
		final Map<String, Map<String,String>> result= new HashMap<>();
		if (sheet.getPhysicalNumberOfRows() < 2) {
			return result;
		}

		final List<String> header = rowToList(sheet.getRow(0));

		IntStream.range(1,sheet.getPhysicalNumberOfRows())
				.forEach(i -> {
					final Map<String,String> row = rowToMap(sheet.getRow(i), header);
					result.put(row.get("id"), row);
				});

		return Collections.unmodifiableMap(result);
	}

	private List<String> rowToList(XSSFRow row) {
		final List<String> result = new ArrayList<>();
		row.cellIterator().forEachRemaining(val -> result.add(val.getStringCellValue()));
		return Collections.unmodifiableList(result);
	}

	private Map<String,String> rowToMap(XSSFRow row, List<String> header) {
		Map<String,String> result = new HashMap<>();
		//
		IntStream.range(0, header.size()-1)
				.forEach(i -> result.put(header.get(i), row.getCell(i).getStringCellValue()));
		//
		return Collections.unmodifiableMap(result);
	}

}
