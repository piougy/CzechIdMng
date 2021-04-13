import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import * as Utils from '../../../utils';
//
import { AuthorizationPolicyManager, SecurityManager } from '../../../redux';
import AbstractEntityInfo from '../EntityInfo/AbstractEntityInfo';

const manager = new AuthorizationPolicyManager();

/**
 * Authorization policy basic information (info card)
 *
 * @author Vít Švanda
 */
export class AuthorizationPolicyInfo extends AbstractEntityInfo {

  getManager() {
    return manager;
  }

  showLink() {
    if (!super.showLink()) {
      return false;
    }
    if (!SecurityManager.hasAccess({ type: 'HAS_ANY_AUTHORITY', authorities: ['AUTHORIZATIONPOLICY_READ'] })) {
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

    return `/role/${encodeURIComponent(entity.role)}/authorization-policies`;
  }

  /**
   * Returns popover info content
   *
   * @param  {array} table data
   */
  getPopoverContent(entity) {
    return [
      {
        label: this.i18n('entity.name.label'),
        value: Utils.Ui.getSimpleJavaType(entity.authorizableType)
      },
      {
        label: this.i18n('entity.type'),
        value: Utils.Ui.getSimpleJavaType(entity.evaluatorType)
      }
    ];
  }

  /**
   * Returns entity icon (null by default - icon will not be rendered)
   *
   * @param  {object} entity
   */
  getEntityIcon() {
    return 'fa:shield-alt';
  }
}

AuthorizationPolicyInfo.propTypes = {
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
AuthorizationPolicyInfo.defaultProps = {
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
export default connect(select)(AuthorizationPolicyInfo);
