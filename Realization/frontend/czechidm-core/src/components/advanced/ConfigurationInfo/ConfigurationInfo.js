import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
//
import * as Basic from '../../basic';
import { ConfigurationManager } from '../../../redux';
import AbstractEntityInfo from '../EntityInfo/AbstractEntityInfo';

const manager = new ConfigurationManager();

/**
 * Component for rendering information about password, similar function as roleInfo
 *
 * @author Radek Tomiška
 * @since 9.7.0
 */
export class ConfigurationInfo extends AbstractEntityInfo {

  getManager() {
    return manager;
  }

  /**
   * Returns entity icon (null by default - icon will not be rendered)
   *
   * @param  {object} entity
   */
  getEntityIcon() {
    return 'component:setting';
  }

  /**
   * Returns popovers title
   *
   * @param  {object} entity
   */
  getPopoverTitle() {
    return this.i18n('entity.Configuration._type');
  }

  getTableChildren() {
    // component are used in #getPopoverContent => skip default column resolving
    return [
      <Basic.Column property="label" />,
      <Basic.Column property="value" />
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
        label: this.i18n('entity.Configuration.name'),
        value: entity.name
      },
      {
        label: this.i18n('entity.Configuration.value'),
        value: entity.value
      },
      {
        label: this.i18n('entity.Configuration.confidential'),
        value: (entity.confidential ? this.i18n('label.yes') : this.i18n('label.no'))
      },
      {
        label: this.i18n('entity.Configuration.public'),
        value: (entity.public ? this.i18n('label.yes') : this.i18n('label.no'))
      }
    ];
  }
}

ConfigurationInfo.propTypes = {
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
ConfigurationInfo.defaultProps = {
  ...AbstractEntityInfo.defaultProps,
  entity: null,
  face: 'link',
  _showLoading: true
};

function select(state, component) {
  return {
    _entity: manager.getEntity(state, component.entityIdentifier),
    _showLoading: manager.isShowLoading(state, null, component.entityIdentifier)
  };
}
export default connect(select)(ConfigurationInfo);
