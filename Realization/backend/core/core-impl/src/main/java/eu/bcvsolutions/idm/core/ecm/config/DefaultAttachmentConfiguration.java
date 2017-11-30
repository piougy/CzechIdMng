package eu.bcvsolutions.idm.core.ecm.config;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.config.domain.AbstractConfiguration;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.ecm.api.config.AttachmentConfiguration;

/**
 * Configuration for attachments
 * 
 * @author Radek Tomiška
 *
 */
@Component("attachmentConfiguration")
public class DefaultAttachmentConfiguration extends AbstractConfiguration implements AttachmentConfiguration {	
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultAttachmentConfiguration.class);
	private static final String DEFAULT_STORAGE_PATH = "/idm_data";
	private boolean storageChecked = false;
	private boolean tempChecked = false;
	
	@Override
	public String getStoragePath() {
		String storagePath = getConfigurationService().getValue(PROPERTY_STORAGE_PATH);
		if (StringUtils.isEmpty(storagePath)) {	
			storagePath = System.getProperty("java.io.tmpdir") + DEFAULT_STORAGE_PATH;
			LOG.warn("Attachments are saved under java.io.tmpdir [" + storagePath + "]. ");
			//
			if (!storageChecked) {
				try {				
					FileUtils.forceMkdir(new File(storagePath));
					storageChecked = true;
				} catch (IOException ex) {
					throw new ResultCodeException(CoreResultCode.ATTACHMENT_INIT_DEFAULT_STORAGE_FAILED, ImmutableMap.of(
							"path", storagePath)
							, ex);
				}
			}
		}
		return storagePath;
	}
	
	@Override
	public String getTempPath() {
		String tempPath = getConfigurationService().getValue(PROPERTY_TEMP_PATH);
		if (StringUtils.isEmpty(tempPath)) {
			tempPath = getStoragePath() + "/temp";			
		}
		if (!tempChecked) {
			try {					
				FileUtils.forceMkdir(new File(tempPath));
				tempChecked = true;
			} catch (IOException ex) {
				throw new ResultCodeException(CoreResultCode.ATTACHMENT_INIT_DEFAULT_TEMP_FAILED, ImmutableMap.of(
						"path", tempPath)
						, ex);
			}	
		}
		return tempPath;
	}
	
	@Override
	public long getTempTtl() {
		return getConfigurationService().getLongValue(PROPERTY_TEMP_TTL, DEFAULT_TEMP_TTL);
	}
	
	@Override
	public void setTempTtl(long ttl) {
		// TODO: setLongValue
		getConfigurationService().setValue(PROPERTY_TEMP_TTL, Long.valueOf(ttl).toString());
	}
}
