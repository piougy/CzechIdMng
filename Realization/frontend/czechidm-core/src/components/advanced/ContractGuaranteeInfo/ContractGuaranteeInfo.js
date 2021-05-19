import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
//
import * as Basic from '../../basic';
import { ContractGuaranteeManager, IdentityContractManager, IdentityManager } from '../../../redux';
import AbstractEntityInfo from '../EntityInfo/AbstractEntityInfo';
import EntityInfo from '../EntityInfo/EntityInfo';

const manager = new ContractGuaranteeManager();
const identityContractManager = new IdentityContractManager();
const identityManager = new IdentityManager();

/**
 * Contract guarantee information (info card)
 *
 * @author Radek Tomiška
 * @since 10.8.0
 */
export class ContractGuaranteeInfo extends AbstractEntityInfo {

  getManager() {
    return manager;
  }

  _isNotLoaded(permissions) {
    return permissions === undefined || permissions === null || permissions === false;
  }

  onEnter() {
    super.onEnter();
    //
    const entity = this.getEntity();
    if (entity && entity.identityContract) {
      const _contractPermissions = identityContractManager.getPermissions(this.context.store.getState(), null, entity.identityContract);
      if (this._isNotLoaded(_contractPermissions)) {
        this.context.store.dispatch(identityContractManager.fetchPermissions(entity.identityContract));
      }
      if (entity._embedded && entity._embedded.identityContract) {
        const _identityPermissions = identityManager.getPermissions(this.context.store.getState(), null, entity._embedded.identityContract.identity);
        if (this._isNotLoaded(_identityPermissions)) {
          this.context.store.dispatch(identityManager.fetchPermissions(entity._embedded.identityContract.identity));
        }
      }
    }
    if (entity && entity.guarantee) {
      const _guaranteePermissions = identityManager.getPermissions(this.context.store.getState(), null, entity.guarantee);
      if (this._isNotLoaded(_guaranteePermissions)) {
        this.context.store.dispatch(identityManager.fetchPermissions(entity.guarantee));
      }
    }
  }

  getNiceLabel() {
    const _entity = this.getEntity();
    //
    return this.getManager().getNiceLabel(_entity); // ~ show identity contract
  }

  showLink() {
    if (!super.showLink()) {
      return false;
    }
    const { _permissions } = this.props;
    if (!this.getManager().canRead(this.getEntity(), _permissions)) {
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
    if (!_entity._embedded || !_entity._embedded.identityContract._embedded) {
      return null;
    }
    //
    const identityIdentifier = encodeURIComponent(_entity._embedded.identityContract._embedded.identity.username);
    return `/identity/${ identityIdentifier }/identity-contract/${ _entity.identityContract }/guarantees`;
  }

  /**
   * Returns entity icon (null by default - icon will not be rendered)
   *
   * @param  {object} entity
   */
  getEntityIcon() {
    return 'component:contract-guarantee';
  }

  /**
   * Returns popovers title
   *
   * @param  {object} entity
   */
  getPopoverTitle() {
    return this.i18n('entity.ContractGuarantee._type');
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
        value: !entity._embedded || !entity._embedded.identityContract || !entity._embedded.identityContract._embedded ||
          <EntityInfo
            entityType="identity"
            entity={ entity._embedded.identityContract._embedded.identity }
            entityIdentifier={ entity._embedded.identityContract.identity }
            face="link" />
      },
      {
        label: this.i18n('entity.IdentityContract._type'),
        value: (
          <EntityInfo
            entityType="identityContract"
            entity={ entity._embedded ? entity._embedded.identityContract : null }
            entityIdentifier={ entity.identityContract }
            showIdentity={ !entity._embedded }
            face="link" />
        )
      },
      {
        label: this.i18n('entity.ContractGuarantee.guarantee.label'),
        value: (
          <EntityInfo
            entityType="identity"
            entity={ entity._embedded ? entity._embedded.guarantee : null }
            entityIdentifier={ entity.guarantee }
            face="link" />
        )
      }
    ];
  }
}

ContractGuaranteeInfo.propTypes = {
  ...AbstractEntityInfo.propTypes,
  /**
   * Selected entity - has higher priority
   */
  entity: PropTypes.object,
  /**
   * Selected entity's id - entity will be loaded automatically
   */
  entityIdentifier: PropTypes.string,
  //
  _showLoading: PropTypes.bool,
  _permissions: PropTypes.oneOfType([
    PropTypes.bool,
    PropTypes.arrayOf(PropTypes.string)
  ])
};
ContractGuaranteeInfo.defaultProps = {
  ...AbstractEntityInfo.defaultProps,
  entity: null,
  face: 'link',
  _showLoading: true
};

function select(state, component) {
  return {
    _entity: manager.getEntity(state, component.entityIdentifier),
    _showLoading: manager.isShowLoading(state, null, component.entityIdentifier),
    _permissions: manager.getPermissions(state, null, component.entityIdentifier)
  };
}
export default connect(select)(ContractGuaranteeInfo);
