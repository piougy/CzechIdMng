import PropTypes from 'prop-types';
import { connect } from 'react-redux';
//
import {DelegationDefinitionManager, IdentityManager} from '../../../redux';
import AbstractEntityInfo from '../EntityInfo/AbstractEntityInfo';

const manager = new DelegationDefinitionManager();
const identityManager = new IdentityManager();

/**
 * Delegation definition - info card
 *
 * @author Vít Švanda
 */
export class DelegationDefinitionInfo extends AbstractEntityInfo {

  getManager() {
    return manager;
  }

  showLink() {
    if (!super.showLink()) {
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
    return `/delegation-definitions/${encodeURIComponent(this.getEntityId())}/detail`;
  }

  /**
   * Returns popover info content
   *
   * @param  {array} table data
   */
  getPopoverContent(entity) {
    return [
      {
        label: this.i18n('entity.DelegationDefinition.delegator.label'),
        value: identityManager.getNiceLabel(entity._embedded.delegator)
      },
      {
        label: this.i18n('entity.DelegationDefinition.delegate.label'),
        value: identityManager.getNiceLabel(entity._embedded.delegate)
      },
      {
        label: this.i18n('entity.DelegationDefinition.type.label'),
        value: this._getType(entity)
      },
      {
        label: this.i18n('entity.DelegationDefinition.description.label'),
        value: entity.description
      }
    ];
  }

  _getType(entity) {
    if (!entity || !entity.type) {
      return null;
    }
    const type = entity.type;
    // TODO: Localization is only for core (I don't have module id here).
    return this.i18n(`core:content.delegation-definitions.types.${type}.label`, type);
  }

  /**
   * Returns entity icon (null by default - icon will not be rendered)
   *
   * @param  {object} entity
   */
  getEntityIcon() {
    return 'fa:dolly';
  }
}

DelegationDefinitionInfo.propTypes = {
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
DelegationDefinitionInfo.defaultProps = {
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
export default connect(select)(DelegationDefinitionInfo);
