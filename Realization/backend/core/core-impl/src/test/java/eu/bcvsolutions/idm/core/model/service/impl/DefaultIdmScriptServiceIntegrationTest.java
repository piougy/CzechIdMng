package eu.bcvsolutions.idm.core.model.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.DecimalFormat;
import java.time.ZonedDateTime;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.IdmScriptCategory;
import eu.bcvsolutions.idm.core.api.dto.IdmScriptAuthorityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmScriptDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmScriptAuthorityFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.IdmScriptAuthorityService;
import eu.bcvsolutions.idm.core.api.service.Recoverable;
import eu.bcvsolutions.idm.core.api.utils.ZipUtils;
import eu.bcvsolutions.idm.core.ecm.api.dto.IdmAttachmentDto;
import eu.bcvsolutions.idm.core.ecm.api.entity.AttachableEntity;
import eu.bcvsolutions.idm.core.ecm.api.service.AttachmentManager;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Basic test for test backup, deploy and redeploy.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 */
@Transactional
public class DefaultIdmScriptServiceIntegrationTest extends AbstractIntegrationTest {

	private static final String TEST_SCRIPT_CODE_1 = "testScript1";
	private static final String TEST_SCRIPT_CODE_2 = "testScript2";
	private static final String TEST_SCRIPT_CODE_3 = "testScript3";
	private static final String TEST_SCRIPT_CODE_OVERRIDE = "testScriptOverride";
	private static final String TEST_SCRIPT_NAME_1 = "Test script 1";
	private static final String TEST_BACKUP_FOLDER = "/tmp/idm_test_backup/";

	@Autowired 
	private ApplicationContext context;
	@Autowired
	private IdmScriptAuthorityService scriptAuthorityService;
	@Autowired
	private ConfigurationService configurationService;
	@Autowired
	private AttachmentManager attachmentManager;
	//
	private DefaultIdmScriptService scriptService;
	
	@Before
	public void init() {
		loginAsAdmin();
		scriptService = context.getAutowireCapableBeanFactory().createBean(DefaultIdmScriptService.class);
	}
	
	@After
	public void logout() {
		super.logout();
	}

	@Test
	public void initTest() {
		IdmScriptDto script1 = scriptService.getByCode(TEST_SCRIPT_CODE_1);
		IdmScriptDto script2 = scriptService.getByCode(TEST_SCRIPT_CODE_2);

		assertNotNull(script1);
		assertNotNull(script2);

		assertEquals(TEST_SCRIPT_CODE_1, script1.getCode());
		assertEquals(TEST_SCRIPT_CODE_2, script2.getCode());

		IdmScriptAuthorityFilter filter = new IdmScriptAuthorityFilter();
		filter.setScriptId(script1.getId());
		List<IdmScriptAuthorityDto> authorities = scriptAuthorityService.find(filter, null).getContent();

		assertEquals(4, authorities.size());

		filter.setScriptId(script2.getId());
		authorities = scriptAuthorityService.find(filter, null).getContent();

		assertEquals(0, authorities.size());
	}
	
	@Test
	public void initFromMultipleLocations() {
		IdmScriptDto script3 = scriptService.getByCode(TEST_SCRIPT_CODE_3);
		IdmScriptDto scriptOverride = scriptService.getByCode(TEST_SCRIPT_CODE_OVERRIDE);
		
		assertNotNull(script3);
		assertNotNull(scriptOverride);

		assertEquals(TEST_SCRIPT_CODE_3, script3.getCode());
		assertEquals(TEST_SCRIPT_CODE_OVERRIDE, scriptOverride.getCode());
		//
		assertEquals("String overrideUpdate;", scriptOverride.getScript().trim());
	}

	@Test
	public void removeAuthRedeploy() {
		configurationService.setValue(Recoverable.BACKUP_FOLDER_CONFIG, TEST_BACKUP_FOLDER);
		IdmScriptDto script1 = scriptService.getByCode(TEST_SCRIPT_CODE_1);
		assertNotNull(script1);

		IdmScriptAuthorityFilter filter = new IdmScriptAuthorityFilter();
		filter.setScriptId(script1.getId());
		List<IdmScriptAuthorityDto> authorities = scriptAuthorityService.find(filter, null).getContent();

		assertEquals(4, authorities.size());

		scriptAuthorityService.deleteAllByScript(script1.getId());

		authorities = scriptAuthorityService.find(filter, null).getContent();

		assertEquals(0, authorities.size());

		scriptService.redeploy(script1);

		authorities = scriptAuthorityService.find(filter, null).getContent();

		assertEquals(4, authorities.size());
	}

	@Test
	public void deleteScriptRedeploy() {
		IdmScriptDto script1 = scriptService.getByCode(TEST_SCRIPT_CODE_1);
		IdmScriptDto script2 = scriptService.getByCode(TEST_SCRIPT_CODE_2);

		assertNotNull(script1);
		assertNotNull(script2);

		scriptService.delete(script1);
		scriptService.delete(script2);

		script1 = scriptService.getByCode(TEST_SCRIPT_CODE_1);
		script2 = scriptService.getByCode(TEST_SCRIPT_CODE_2);

		assertNull(script1);
		assertNull(script2);

		scriptService.init();

		script1 = scriptService.getByCode(TEST_SCRIPT_CODE_1);
		script2 = scriptService.getByCode(TEST_SCRIPT_CODE_2);

		assertNotNull(script1);
		assertNotNull(script2);
	}

	@Test
	public void tryRedeployMissingScript() {
		IdmScriptDto script = new IdmScriptDto();
		script.setCategory(IdmScriptCategory.SYSTEM);
		script.setCode("test_" + System.currentTimeMillis());
		script.setName("test_" + System.currentTimeMillis());

		script = scriptService.save(script);
		assertNotNull(script);
		assertNotNull(script.getId());

		try {
			scriptService.redeploy(script);
			fail();
		} catch (ResultCodeException e) {
			ResultCodeException resultCode = (ResultCodeException) e;
			assertEquals(resultCode.getError().getError().getStatusEnum(),
					CoreResultCode.SCRIPT_XML_FILE_NOT_FOUND.name());
		}
	}

	@Test
	public void tryRedeployScript() {
		configurationService.setValue(Recoverable.BACKUP_FOLDER_CONFIG, TEST_BACKUP_FOLDER);
		IdmScriptDto script1 = scriptService.getByCode(TEST_SCRIPT_CODE_1);

		assertNotNull(script1);
		
		assertEquals(TEST_SCRIPT_NAME_1, script1.getName());
		
		String changeName = "test_change_" + System.currentTimeMillis();
		script1.setName(changeName);
		script1 = scriptService.save(script1);
		
		assertEquals(changeName, script1.getName());
		
		try {
			script1 = scriptService.redeploy(script1);
			assertEquals(TEST_SCRIPT_NAME_1, script1.getName());
		} catch (ResultCodeException e) {
			fail();
		}
	}
	
	@Test
	public void backupMissingFolderExistEntity() {
		configurationService.setValue(Recoverable.BACKUP_FOLDER_CONFIG, "?/wrong/path");
		
		IdmScriptDto script1 = scriptService.getByCode(TEST_SCRIPT_CODE_1);

		assertNotNull(script1);
		
		try {
			scriptService.backup(script1);
			fail();
		} catch (ResultCodeException e) {
			ResultCodeException resultCode = (ResultCodeException) e;
			assertEquals(resultCode.getError().getError().getStatusEnum(),
					CoreResultCode.BACKUP_FAIL.name());
		} finally {
			configurationService.setValue(Recoverable.BACKUP_FOLDER_CONFIG, null);
		}
	}
	
	@Test
	public void backupMissingFolderNewEntity() {
		configurationService.setValue(Recoverable.BACKUP_FOLDER_CONFIG, "?/wrong/path");
		
		IdmScriptDto script = new IdmScriptDto();
		script.setCategory(IdmScriptCategory.SYSTEM);
		script.setCode(getHelper().createName());
		script.setName(getHelper().createName());

		script = scriptService.save(script);
		assertNotNull(script);
		assertNotNull(script.getId());

		try {
			scriptService.backup(script);
			fail();
		} catch (ResultCodeException e) {
			ResultCodeException resultCode = (ResultCodeException) e;
			assertEquals(resultCode.getError().getError().getStatusEnum(),
					CoreResultCode.BACKUP_FAIL.name());
		} finally {
			configurationService.setValue(Recoverable.BACKUP_FOLDER_CONFIG, null);
		}
	}
	
	@Test
	public void tryBackup() {
		File directory = new File(TEST_BACKUP_FOLDER);
		if (directory.exists() && directory.isDirectory()) {
			try {
				FileUtils.deleteDirectory(directory);
			} catch (IOException e) {
				fail();
			}
		}
		//
		IdmScriptDto script1 = scriptService.getByCode(TEST_SCRIPT_CODE_1);

		assertNotNull(script1);
		
		configurationService.setValue(Recoverable.BACKUP_FOLDER_CONFIG, TEST_BACKUP_FOLDER);
		//
		try {
			IdmScriptDto newDto = scriptService.redeploy(script1);
			assertEquals(script1.getCode(), newDto.getCode());
			//
			ZonedDateTime date = ZonedDateTime.now();
			DecimalFormat decimalFormat = new DecimalFormat("00");
			directory = new File(TEST_BACKUP_FOLDER + "scripts/" + date.getYear()
					+ decimalFormat.format(date.getMonthValue()) + decimalFormat.format(date.getDayOfMonth()) + "/");
			File[] files = directory.listFiles();
			assertEquals(1, files.length);
			File backup = files[0];
			assertTrue(backup.exists());
			assertTrue(backup.getName().contains("admin"));
			assertTrue(backup.getName().contains(script1.getCode()));
		} catch (Exception e) {
			fail();
		}
	}

	@Test
	public void checkCdataTag() {
		String testDescription = getHelper().createName();
		String testBody = getHelper().createName();
		File directory = new File(TEST_BACKUP_FOLDER);
		if (directory.exists() && directory.isDirectory()) {
			try {
				FileUtils.deleteDirectory(directory);
			} catch (IOException e) {
				fail();
			}
		}
		//
		IdmScriptDto script1 = scriptService.getByCode(TEST_SCRIPT_CODE_1);

		assertNotNull(script1);
		
		configurationService.setValue(Recoverable.BACKUP_FOLDER_CONFIG, TEST_BACKUP_FOLDER);

		script1.setDescription(testDescription);
		script1.setScript(testBody);

		try {
			scriptService.backup(script1);
			//
			ZonedDateTime date = ZonedDateTime.now();
			DecimalFormat decimalFormat = new DecimalFormat("00");
			directory = new File(TEST_BACKUP_FOLDER + "scripts/" + date.getYear()
					+ decimalFormat.format(date.getMonthValue()) + decimalFormat.format(date.getDayOfMonth()) + "/");
			File[] files = directory.listFiles();
			assertEquals(1, files.length);
			File backup = files[0];
			assertTrue(backup.exists());
			assertTrue(backup.getName().contains("admin"));
			assertTrue(backup.getName().contains(script1.getCode()));

			String content = new String(Files.readAllBytes(backup.toPath()), StandardCharsets.UTF_8);
			
			assertTrue(content.contains("<description><![CDATA[" + testDescription + "]]></description>"));
			assertTrue(content.contains("<body><![CDATA[" + testBody + "]]></body>"));
			
		} catch (Exception e) {
			fail(e.getLocalizedMessage());
		}
	}
	
	@Test
	public void testDeployScriptFromAttachement() throws IOException {
		String scriptCode = getHelper().createName();
		
		String scriptOne = "<script> <code>" + scriptCode + "</code> <name>Test script 1</name> <body> <![CDATA[ String s; ]]> </body> <type>groovy</type> <category>DEFAULT</category> <parameters>test1</parameters> <services> <service> <name>treeNodeService</name> <className>eu.bcvsolutions.idm.core.model.service.impl.DefaultIdmTreeNodeService</className> </service> <service> <name>identityService</name> <className>eu.bcvsolutions.idm.core.model.service.impl.DefaultIdmIdentityService</className> </service> </services><allowClasses> <allowClass> <className>eu.bcvsolutions.idm.core.model.entity.IdmTreeNode</className> </allowClass> <allowClass> <className>eu.bcvsolutions.idm.core.model.entity.IdmIdentity</className> </allowClass> </allowClasses> </script>";
		// create attachment
		IdmAttachmentDto attachment = new IdmAttachmentDto();
		attachment.setOwnerType(AttachmentManager.TEMPORARY_ATTACHMENT_OWNER_TYPE);
		attachment.setName("scriptOne.xml");
		attachment.setMimetype(MediaType.TEXT_XML_VALUE);
		attachment.setInputData(IOUtils.toInputStream(scriptOne));
		attachment = attachmentManager.saveAttachment(null, attachment); // owner and version is resolved after attachment is saved
		// deploy
		List<IdmScriptDto> results = scriptService.deploy(attachment);
		//
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(scriptCode, results.get(0).getCode());
		//
		// test authorities
		IdmScriptAuthorityFilter authorityFilter = new IdmScriptAuthorityFilter();
		authorityFilter.setScriptId(results.get(0).getId());
		List<IdmScriptAuthorityDto> authorities = scriptAuthorityService.find(authorityFilter, null).getContent();
		Assert.assertEquals(4, authorities.size());
		Assert.assertTrue(authorities.stream().allMatch(a -> StringUtils.isNotEmpty(a.getClassName())));
		Assert.assertTrue(authorities.stream().anyMatch(a -> "treeNodeService".equals(a.getService())));
		Assert.assertTrue(authorities.stream().anyMatch(a -> "identityService".equals(a.getService())));
		Assert.assertTrue(authorities.stream().anyMatch(a -> a.getClassName().equals("eu.bcvsolutions.idm.core.model.entity.IdmTreeNode")));
		Assert.assertTrue(authorities.stream().anyMatch(a -> a.getClassName().equals("eu.bcvsolutions.idm.core.model.entity.IdmIdentity")));
		//
		// deploy from archive
		IdmScriptDto scriptOneDto = scriptService.getByCode(scriptCode);
		String scriptCodeTwo = getHelper().createName();
		String scriptOneUpdateName = getHelper().createName();
		
		scriptOne = "<script> <code>" + scriptCode + "</code> <name>"+ scriptOneUpdateName +"</name> <body> <![CDATA[ String s; ]]> </body> <type>groovy</type> <category>DEFAULT</category> <parameters>test1</parameters> <services> <service> <name>treeNodeService</name> <className>eu.bcvsolutions.idm.core.model.service.impl.DefaultIdmTreeNodeService</className> </service> </services><allowClasses> <allowClass> <className>eu.bcvsolutions.idm.core.model.entity.IdmTreeNode</className> </allowClass> <allowClass> <className>eu.bcvsolutions.idm.core.model.entity.IdmIdentity</className> </allowClass> </allowClasses> </script>";
		String scriptTwo = "<script> <code>" + scriptCodeTwo + "</code> <name>Test script 2</name> <body> <![CDATA[ String s; ]]> </body> <type>groovy</type> <category>DEFAULT</category> <parameters>test1</parameters> <services> <service> <name>treeNodeService</name> <className>eu.bcvsolutions.idm.core.model.service.impl.DefaultIdmTreeNodeService</className> </service> <service> <name>identityService</name> <className>eu.bcvsolutions.idm.core.model.service.impl.DefaultIdmIdentityService</className> </service> </services><allowClasses> <allowClass> <className>eu.bcvsolutions.idm.core.model.entity.IdmTreeNode</className> </allowClass> <allowClass> <className>eu.bcvsolutions.idm.core.model.entity.IdmIdentity</className> </allowClass> </allowClasses> </script>";// create attachment
		IdmAttachmentDto attachmentOne = new IdmAttachmentDto();
		attachmentOne.setOwnerType(AttachmentManager.TEMPORARY_ATTACHMENT_OWNER_TYPE);
		attachmentOne.setName("scriptOne.xml");
		attachmentOne.setMimetype(MediaType.TEXT_XML_VALUE);
		attachmentOne.setInputData(IOUtils.toInputStream(scriptOne));
		attachmentOne = attachmentManager.saveAttachment(null, attachmentOne);
		IdmAttachmentDto attachmentTwo = new IdmAttachmentDto();
		attachmentTwo.setOwnerType(AttachmentManager.TEMPORARY_ATTACHMENT_OWNER_TYPE);
		attachmentTwo.setName("scriptTwo.xml");
		attachmentTwo.setMimetype(MediaType.TEXT_XML_VALUE);
		attachmentTwo.setInputData(IOUtils.toInputStream(scriptTwo));
		attachmentTwo = attachmentManager.saveAttachment(null, attachmentTwo);
		// zip
		File zipFolder = attachmentManager.createTempDirectory(null).toFile();
		File targetFileOne = new File(zipFolder.toString(), String.format("%s.xml", attachmentOne.getName()));
		Files.copy(attachmentManager.getAttachmentData(attachmentOne.getId()), targetFileOne.toPath(), StandardCopyOption.REPLACE_EXISTING);
		File targetFileTwo = new File(zipFolder.toString(), String.format("%s.xml", attachmentTwo.getName()));
		Files.copy(attachmentManager.getAttachmentData(attachmentTwo.getId()), targetFileTwo.toPath(), StandardCopyOption.REPLACE_EXISTING);
		// compress
		File zipFile = attachmentManager.createTempFile();
		ZipUtils.compress(zipFolder, zipFile.getPath());
		IdmAttachmentDto attachmentZip = new IdmAttachmentDto();
		attachmentZip.setOwnerType(AttachmentManager.TEMPORARY_ATTACHMENT_OWNER_TYPE);
		attachmentZip.setInputData(new FileInputStream(zipFile));
		attachmentZip.setEncoding(AttachableEntity.DEFAULT_ENCODING);
		attachmentZip.setMimetype(AttachableEntity.DEFAULT_MIMETYPE); // zip ~ octet stream
		attachmentZip.setName("backup.zip");
		attachmentZip = attachmentManager.saveAttachment(null, attachmentZip);
		// deploy
		results = scriptService.deploy(attachmentZip);
		//
		Assert.assertEquals(2, results.size());
		Assert.assertTrue(results.stream().anyMatch(s -> s.getId().equals(scriptOneDto.getId())));
		Assert.assertTrue(results.stream().anyMatch(s -> s.getCode().equals(scriptCode) && s.getName().equals(scriptOneUpdateName)));
		Assert.assertTrue(results.stream().anyMatch(s -> s.getCode().equals(scriptCodeTwo)));
		//
		// test authorities
		authorityFilter = new IdmScriptAuthorityFilter();
		authorityFilter.setScriptId(scriptOneDto.getId());
		authorities = scriptAuthorityService.find(authorityFilter, null).getContent();
		Assert.assertEquals(3, authorities.size());
		Assert.assertTrue(authorities.stream().allMatch(a -> StringUtils.isNotEmpty(a.getClassName())));
		Assert.assertTrue(authorities.stream().anyMatch(a -> "treeNodeService".equals(a.getService())));
		Assert.assertTrue(authorities.stream().anyMatch(a -> a.getClassName().equals("eu.bcvsolutions.idm.core.model.entity.IdmTreeNode")));
		Assert.assertTrue(authorities.stream().anyMatch(a -> a.getClassName().equals("eu.bcvsolutions.idm.core.model.entity.IdmIdentity")));
		//
		authorityFilter = new IdmScriptAuthorityFilter();
		authorityFilter.setScriptId(scriptService.getByCode(scriptCodeTwo).getId());
		authorities = scriptAuthorityService.find(authorityFilter, null).getContent();
		Assert.assertEquals(4, authorities.size());
		Assert.assertTrue(authorities.stream().allMatch(a -> StringUtils.isNotEmpty(a.getClassName())));
		Assert.assertTrue(authorities.stream().anyMatch(a -> "treeNodeService".equals(a.getService())));
		Assert.assertTrue(authorities.stream().anyMatch(a -> "identityService".equals(a.getService())));
		Assert.assertTrue(authorities.stream().anyMatch(a -> a.getClassName().equals("eu.bcvsolutions.idm.core.model.entity.IdmTreeNode")));
		Assert.assertTrue(authorities.stream().anyMatch(a -> a.getClassName().equals("eu.bcvsolutions.idm.core.model.entity.IdmIdentity")));
		
	
	}
}
