import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
//
import * as Basic from '../../basic';
import { PasswordManager, SecurityManager } from '../../../redux';
import AbstractEntityInfo from '../EntityInfo/AbstractEntityInfo';
import EntityInfo from '../EntityInfo/EntityInfo';
import DateValue from '../DateValue/DateValue';

const manager = new PasswordManager();

/**
 * Component for rendering information about password, similar function as roleInfo
 *
 * @author Radek Tomiška
 * @since 9.7.0
 */
export class PasswordInfo extends AbstractEntityInfo {

  getManager() {
    return manager;
  }

  showLink() {
    if (!super.showLink()) {
      return false;
    }
    if (!SecurityManager.hasAccess({ type: 'HAS_ANY_AUTHORITY', authorities: ['PASSWORD_READ']})) {
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
    const _entity = this.getEntity();
    //
    return `/identity/${ encodeURIComponent(_entity._embedded.identity.username) }/password/detail`;
  }

  /**
   * Returns entity icon (null by default - icon will not be rendered)
   *
   * @param  {object} entity
   */
  getEntityIcon() {
    return 'component:password';
  }

  getNiceLabel(entity) {
    const _entity = entity || this.getEntity();
    let label = this.i18n('entity.Password._type');
    if (_entity && _entity._embedded && _entity._embedded.identity) {
      label = `${ label } - (${ _entity._embedded.identity.username })`;
    }
    return label;
  }

  /**
   * Returns popovers title
   *
   * @param  {object} entity
   */
  getPopoverTitle() {
    return this.i18n('entity.Password._type');
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
        label: this.i18n('entity.Identity._type'),
        value: (
          <EntityInfo
            entityType="identity"
            entity={ entity._embedded ? entity._embedded.identity : null }
            entityIdentifier={ entity.identity }
            face="popover" />
        )
      },
      {
        label: this.i18n('entity.validFrom'),
        value: (<DateValue value={ entity.validFrom }/>)
      },
      {
        label: this.i18n('entity.validTill'),
        value: (<DateValue value={ entity.validTill }/>)
      },
      {
        label: this.i18n('content.password.lastSuccessfulLogin.label'),
        value: (<DateValue value={ entity.lastSuccessfulLogin }/>)
      }
    ];
  }
}

PasswordInfo.propTypes = {
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
PasswordInfo.defaultProps = {
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
export default connect(select)(PasswordInfo);
