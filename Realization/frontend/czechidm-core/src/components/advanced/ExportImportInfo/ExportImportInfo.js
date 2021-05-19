import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
//
import * as Basic from '../../basic';
import { ExportImportManager, SecurityManager } from '../../../redux';
import AbstractEntityInfo from '../EntityInfo/AbstractEntityInfo';
import OperationStateEnum from '../../../enums/OperationStateEnum';
import ExportImportTypeEnum from '../../../enums/ExportImportTypeEnum';

const manager = new ExportImportManager();

/**
 * Export/Import info card.
 *
 * @author Vít Švanda
 */
export class ExportImportInfo extends AbstractEntityInfo {

  getManager() {
    return manager;
  }

  showLink() {
    if (!super.showLink()) {
      return false;
    }
    if (!SecurityManager.hasAccess({ type: 'HAS_ANY_AUTHORITY', authorities: ['EXPORTIMPORT_READ'] })) {
      return false;
    }
    return true;
  }

  /**
   * Get link to detail (`url`).
   *
   * @return {string}
   */
  getLink() {
    const entity = this.getEntity();
    return `/export-imports/${encodeURIComponent(entity.id)}`;
  }

  getTableChildren() {
    // component are used in #getPopoverContent => skip default column resolving
    return [
      <Basic.Column property="label"/>,
      <Basic.Column property="value"/>
    ];
  }

  /**
   * Returns popover info content
   *
   * @param  {array} table data
   */
  getPopoverContent(entity) {
    return [
      {
        label: this.i18n('entity.ExportImport.name.label'),
        value: entity.name
      },
      {
        label: this.i18n('entity.ExportImport.type.label'),
        value: ExportImportTypeEnum.getNiceLabel(entity.type)
      },
      {
        label: this.i18n('entity.ExportImport.result.state'),
        value: entity.result && entity.result.state
          ? OperationStateEnum.getNiceLabel(entity.result.state) : null
      },
      {
        label: this.i18n('entity.ExportImport.executorName.label'),
        value: entity._embedded
          && entity._embedded.longRunningTask
          && entity._embedded.longRunningTask.taskProperties
          && entity._embedded.longRunningTask.taskProperties['core:bulkAction']
            ? this.i18n(`${entity._embedded.longRunningTask.taskProperties['core:bulkAction'].module}:eav.bulk-action.${entity.executorName}.title`)
            : entity.executorName
      }
    ];
  }

  /**
   * Returns entity icon (null by default - icon will not be rendered)
   *
   * @param  {object} entity
   */
  getEntityIcon() {
    return 'export';
  }
}

ExportImportInfo.propTypes = {
  ...AbstractEntityInfo.propTypes,
  /**
   * Selected entity - has higher priority
   */
  entity: PropTypes.object,
  /**
   * Selected entity's id - entity will be loaded automatically
   */
  entityIdentifier: PropTypes.string,
  /**
   * Internal entity loaded by given identifier
   */
  _entity: PropTypes.object,
  _showLoading: PropTypes.bool
};
ExportImportInfo.defaultProps = {
  ...AbstractEntityInfo.defaultProps,
  entity: null,
  showLink: true,
  face: 'link',
  _showLoading: true,
};

function select(state, component) {
  return {
    _entity: manager.getEntity(state, component.entityIdentifier),
    _showLoading: manager.isShowLoading(state, null, component.entityIdentifier)
  };
}
export default connect(select)(ExportImportInfo);
