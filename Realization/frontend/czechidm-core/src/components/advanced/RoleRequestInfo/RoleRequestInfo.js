import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
//
import * as Basic from '../../basic';
import { RoleRequestManager, SecurityManager } from '../../../redux';
import AbstractEntityInfo from '../EntityInfo/AbstractEntityInfo';
import EntityInfo from '../EntityInfo/EntityInfo';
import DateValue from '../DateValue/DateValue';
import RoleRequestStateEnum from '../../../enums/RoleRequestStateEnum';

const manager = new RoleRequestManager();


/**
 * Component for rendering nice identifier for role request, similar function as roleInfo
 *
 * @author Radek Tomiška
 * @since 9.7.0
 */
export class RoleRequestInfo extends AbstractEntityInfo {

  constructor(props, context) {
    super(props, context);
  }

  getManager() {
    return manager;
  }

  showLink() {
    if (!super.showLink()) {
      return false;
    }
    if (!SecurityManager.hasAccess({ 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['ROLEREQUEST_READ']})) {
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
    const { entityIdentifier } = this.props;
    //
    return `/role-requests/${entityIdentifier}/detail`;
  }

  /**
   * Returns entity icon (null by default - icon will not be rendered)
   *
   * @param  {object} entity
   */
  getEntityIcon() {
    return 'component:role-request';
  }

  /**
   * Returns popovers title
   *
   * @param  {object} entity
   */
  getPopoverTitle() {
    return this.i18n('entity.RoleRequest._type');
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
        label: this.i18n('entity.RoleRequest.applicant'),
        value: (
          <EntityInfo
            entityType="identity"
            entity={ entity._embedded ? entity._embedded.applicant : null }
            entityIdentifier={ entity.applicant }
            face="popover" />
        )
      },
      {
        label: this.i18n('entity.RoleRequest.created'),
        value: (<DateValue value={ entity.created } showTime />)
      },
      {
        label: this.i18n('entity.RoleRequest.state'),
        value: (
          <Basic.EnumValue
            enum={ RoleRequestStateEnum }
            value={ entity.state } />
        )
      }
    ];
  }
}

RoleRequestInfo.propTypes = {
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
RoleRequestInfo.defaultProps = {
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
export default connect(select)(RoleRequestInfo);
