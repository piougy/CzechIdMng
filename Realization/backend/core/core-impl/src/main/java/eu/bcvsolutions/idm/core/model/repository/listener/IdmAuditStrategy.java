package eu.bcvsolutions.idm.core.model.repository.listener;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;
import org.hibernate.envers.Audited;
import org.hibernate.envers.configuration.spi.AuditConfiguration;
import org.hibernate.envers.strategy.DefaultAuditStrategy;
import org.springframework.beans.factory.annotation.Configurable;

import eu.bcvsolutions.idm.core.api.audit.dto.IdmAuditDto;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.audit.entity.IdmAudit;

/**
 * CzechIdM uses own audit strategy. The strategy extends from
 * {@link DefaultAuditStrategy}. Strategy iterate over all attributes
 * given in data (hash map) and then check changed values and create list
 * of changed columns.
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@Configurable
public class IdmAuditStrategy extends DefaultAuditStrategy {
	
	private Set<String> auditedFieldsFromAbstractEntity;
	
	@Override
	public void perform(Session session, String entityName, AuditConfiguration auditCfg, Serializable id, Object data,
			Object revision) {
		List<String> changedColumns = new ArrayList<>();
		// data contains hash map with values that will be saved into audit tables (suffix _a)
		if (data instanceof HashMap) {
			// Initialize audited fields by abstract entity class
			fillAbstractAuditedFields(auditCfg);
			//
			// get suffix for modified columns (default _m)
			String modifiedFlagSuffix = auditCfg.getGlobalCfg().getModifiedFlagSuffix();
			//
			@SuppressWarnings("unchecked")
			Map<String, Object> dataMap = (Map<String, Object>) data;
			//
			// iterate over all audit data and search mod fields with boolean
			// we create new audit row when is changed some attribute except modified
			// in this case is classic for each faster than stream
			for (Entry<String, Object> entry : dataMap.entrySet()) {
				String key = entry.getKey();
				if (key.endsWith(modifiedFlagSuffix)) {
					// fill changed columns, except attributes from abstract entity
					Boolean modValue = BooleanUtils.toBoolean(entry.getValue().toString());
					if (!this.auditedFieldsFromAbstractEntity.contains(key) && BooleanUtils.isTrue(modValue)) {
						changedColumns.add(StringUtils.remove(key, modifiedFlagSuffix));
					}
				}
			}
		}
		//
		performDefaultStrategy(session, entityName, auditCfg, id, data, revision, changedColumns);
	}
	
	/**
	 * Method perform default strategy by call perform by super class.
	 * Also it will be add changed columns to audit entity.
	 *
	 * @param session
	 * @param entityName
	 * @param auditCfg
	 * @param id
	 * @param data
	 * @param revision
	 * @param changedColumns
	 */
	private void performDefaultStrategy(Session session, String entityName, AuditConfiguration auditCfg, Serializable id, Object data,
			Object revision, List<String> changedColumns) {
		// fill revision with changed columns
		if (revision instanceof IdmAudit) {
			IdmAudit audit = (IdmAudit) revision;
			//
			// is needed transfer changed columns for child revisions as another attribute,
			// this is entity and at is persist
			if (audit.getEntityId() != null) { // child revision
				audit.setTemporaryChangedColumns(StringUtils.join(changedColumns, IdmAuditDto.CHANGED_COLUMNS_DELIMITER));
			} else {
				audit.setChangedAttributes(StringUtils.join(changedColumns, IdmAuditDto.CHANGED_COLUMNS_DELIMITER));
			}
		}
		//
		super.perform(session, entityName, auditCfg, id, data, revision);
	}
	
	/**
	 * Method initialize list with all audited fields from {@link AbstractEntity}.
	 * The initialization will be done only once.
	 *
	 * @param auditCfg
	 */
	private void fillAbstractAuditedFields(AuditConfiguration auditCfg) {
		if (auditedFieldsFromAbstractEntity == null) {
			String modifiedFlagSuffix = auditCfg.getGlobalCfg().getModifiedFlagSuffix();
			//
			auditedFieldsFromAbstractEntity = new LinkedHashSet<>();
			//
			for (Field field : AbstractEntity.class.getDeclaredFields()) {
				Audited annotation = field.getAnnotation(Audited.class);
				if (annotation != null) {
					auditedFieldsFromAbstractEntity.add(field.getName() + modifiedFlagSuffix);
				}
			}
		}
	}
}
