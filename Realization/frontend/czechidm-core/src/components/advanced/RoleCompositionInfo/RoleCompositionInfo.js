import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
//
import * as Basic from '../../basic';
import { RoleCompositionManager } from '../../../redux';
import AbstractEntityInfo from '../EntityInfo/AbstractEntityInfo';
import EntityInfo from '../EntityInfo/EntityInfo';

const manager = new RoleCompositionManager();

/**
 * Role composition basic information (info card).
 *
 * @author Vít Švanda
 * @author Radek Tomiška
 */
export class RoleCompositionInfo extends AbstractEntityInfo {

  getManager() {
    return manager;
  }

  showLink() {
    if (!super.showLink()) {
      return false;
    }
    const { _permissions } = this.props;
    if (!manager.canRead(this.getEntity(), _permissions)) {
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
    return `/role/${ encodeURIComponent(entity.superior) }/compositions`;
  }

  /**
   * Returns popovers title
   *
   * @param  {object} entity
   */
  getPopoverTitle() {
    return this.i18n('entity.RoleComposition._type');
  }

  getTableChildren() {
    // component are used in #getPopoverContent => skip default column resolving
    return [
      <Basic.Column property="label"/>,
      <Basic.Column property="value"/>
    ];
  }

  /**
   * Returns popover info content.
   *
   * @param  {array} table data
   */
  getPopoverContent(entity) {
    return [
      {
        label: this.i18n('entity.RoleComposition.superior.label'),
        value: (
          <EntityInfo
            entityType="role"
            entity={ entity._embedded ? entity._embedded.superior : null }
            entityIdentifier={ entity.superior }
            showIcon
            face="popover" />
        )
      },
      {
        label: this.i18n('entity.RoleComposition.sub.label'),
        value: (
          <EntityInfo
            entityType="role"
            entity={ entity._embedded ? entity._embedded.sub : null }
            entityIdentifier={ entity.sub }
            showIcon
            face="popover" />
        )
      }
    ];
  }

  /**
   * Returns entity icon (null by default - icon will not be rendered)
   *
   * @param  {object} entity
   */
  getEntityIcon() {
    return 'component:business-roles';
  }
}

RoleCompositionInfo.propTypes = {
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
RoleCompositionInfo.defaultProps = {
  ...AbstractEntityInfo.defaultProps,
  entity: null,
  showLink: true,
  face: 'link',
  _showLoading: true,
};

function select(state, component) {
  const { entityIdentifier, entity } = component;
  let entityId = entityIdentifier;
  if (!entityId && entity) {
    entityId = entity.id;
  }
  //
  return {
    _entity: manager.getEntity(state, entityId),
    _showLoading: manager.isShowLoading(state, null, entityId),
    _permissions: manager.getPermissions(state, null, entityId)
  };
}
export default connect(select)(RoleCompositionInfo);
