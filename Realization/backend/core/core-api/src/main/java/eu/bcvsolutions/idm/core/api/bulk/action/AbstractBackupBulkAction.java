package eu.bcvsolutions.idm.core.api.bulk.action;



import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.Codeable;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.ResultModel;
import eu.bcvsolutions.idm.core.api.dto.ResultModels;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.exception.DefaultErrorModel;
import eu.bcvsolutions.idm.core.api.service.Recoverable;
import eu.bcvsolutions.idm.core.api.utils.ZipUtils;
import eu.bcvsolutions.idm.core.ecm.api.dto.IdmAttachmentDto;
import eu.bcvsolutions.idm.core.ecm.api.entity.AttachableEntity;
import eu.bcvsolutions.idm.core.ecm.api.service.AttachmentManager;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;


/**
 * Abstract backup bulk operation
 * for entities the service of which implements {@link Recoverable}.
 * 
 * @author Ondrej Husnik
 * @author Radek Tomiška
 * @since 10.6.0
 */
public abstract class AbstractBackupBulkAction<DTO extends AbstractDto, F extends BaseFilter>
		extends AbstractBulkAction<DTO, F> {
	
	@Autowired private AttachmentManager attachmentManager;
	//
	private File zipFolder;
	
	@Override
	protected boolean start() {
		zipFolder = attachmentManager.createTempDirectory(null).toFile();
		//
		return super.start();
	}

	@Override
	@SuppressWarnings("unchecked")
	protected OperationResult processDto(DTO dto) {
		try {
			Assert.notNull(dto, "Entity to backup is required!");
			Assert.notNull(dto.getId(), "Id of entity to backup is required!");
			Assert.isTrue(getService() instanceof Recoverable, "Entity service has to implement recoverable interface!");
			Recoverable<DTO> service = (Recoverable<DTO>) getService();
			// call backup
			File backupFile = service.backup(dto);
			// rename to zip folder
			String fileName = dto.getId().toString();
			if (dto instanceof Codeable) {
				fileName = attachmentManager.getValidFileName(((Codeable) dto).getCode());
			}
			File targetFile = new File(zipFolder.toString(), String.format("%s.xml", fileName));
			// and copy file
			FileUtils.copyFile(backupFile, targetFile);
			//
			return new OperationResult(OperationState.EXECUTED);
		} catch (Exception ex) {
			return new OperationResult//
					.Builder(OperationState.EXCEPTION)//
							.setCause(ex)//
							.build();//
		}
	}	
	
	@Override
	@SuppressWarnings("unchecked")
	public ResultModels prevalidate() {
		ResultModels results = new ResultModels();
		Recoverable<DTO> service = (Recoverable<DTO>) getService();
		String backupFolder = service.getBackupFolder();
		if (StringUtils.isEmpty(backupFolder)) {
			ResultModel result = new DefaultErrorModel(CoreResultCode.BACKUP_FOLDER_NOT_FOUND, ImmutableMap.of("property", Recoverable.BACKUP_FOLDER_CONFIG));
			results.addInfo(result);
		} else {
			ResultModel result = new DefaultErrorModel(
					CoreResultCode.BACKUP_FOLDER_FOUND, 
					ImmutableMap.of("backupFolder", backupFolder)
			);
			results.addInfo(result);
		}
		return results;
	}
	
	@Override
	protected OperationResult end(OperationResult result, Exception exception) {
		if (exception != null 
				|| (result != null && OperationState.EXECUTED != result.getState())) {
			return super.end(result, exception);
		}
		//
		IdmLongRunningTaskDto task = getLongRunningTaskService().get(getLongRunningTaskId());
		//
		File zipFile = attachmentManager.createTempFile();
		try {
			// zip file
			ZipUtils.compress(zipFolder, zipFile.getPath());
			// create attachment
			IdmAttachmentDto attachment = new IdmAttachmentDto();
			attachment.setInputData(new FileInputStream(zipFile));
			attachment.setEncoding(AttachableEntity.DEFAULT_ENCODING);
			attachment.setMimetype(AttachableEntity.DEFAULT_MIMETYPE); // zip ~ octet stream
			attachment.setName(String.format("%s.zip", getName()));
			attachment = attachmentManager.saveAttachment(task, attachment);
			//
			ResultModel resultModel = new DefaultResultModel(CoreResultCode.LONG_RUNNING_TASK_PARTITIAL_DOWNLOAD,
					ImmutableMap.of(
							AttachableEntity.PARAMETER_DOWNLOAD_URL, String.format("long-running-tasks/%s/download/%s",
									task.getId(), attachment.getId())
					));
			//
			return super.end(new OperationResult.Builder(OperationState.EXECUTED).setModel(resultModel).build(), null);
		} catch(IOException ex) {
			return super.end(result, ex);
		} finally {
			FileUtils.deleteQuietly(zipFile);
			FileUtils.deleteQuietly(zipFolder);
		}
	}
	
	@Override
	public int getOrder() {
		return super.getOrder() + 1100;
	}
}
