import PropTypes from 'prop-types';
import { connect } from 'react-redux';
//
import {RoleGuaranteeRoleManager, SecurityManager} from '../../../redux';
import AbstractEntityInfo from '../EntityInfo/AbstractEntityInfo';

const manager = new RoleGuaranteeRoleManager();

/**
 * Role guarantee by role - basic information (info card)
 *
 * @author Vít Švanda
 */
export class RoleGuaranteeRoleInfo extends AbstractEntityInfo {

  getManager() {
    return manager;
  }

  showLink() {
    if (!super.showLink()) {
      return false;
    }
    if (!SecurityManager.hasAccess({ type: 'HAS_ANY_AUTHORITY', authorities: ['ROLEGUARANTEEROLE_READ'] })) {
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

    return `/role/${encodeURIComponent(entity.role)}/guarantees`;
  }

  /**
   * Returns popovers title
   *
   * @param  {object} entity
   */
  getPopoverTitle() {
    return this.i18n('entity.RoleGuaranteeRole._type');
  }

  /**
   * Returns popover info content
   *
   * @param  {array} table data
   */
  getPopoverContent(entity) {
    if (!entity._embedded || !entity._embedded.guaranteeRole) {
      return [
        {
          label: this.i18n('entity.RoleGuaranteeRole.guaranteeRole.label'),
          value: entity.guaranteeRole
        }
      ];
    }
    return [
      {
        label: this.i18n('entity.RoleGuaranteeRole.guaranteeRole.label'),
        value: entity._embedded.guaranteeRole.name
      }
    ];
  }

  /**
   * Returns entity icon (null by default - icon will not be rendered)
   *
   * @param  {object} entity
   */
  getEntityIcon() {
    return 'fa:group';
  }
}

RoleGuaranteeRoleInfo.propTypes = {
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
RoleGuaranteeRoleInfo.defaultProps = {
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
export default connect(select)(RoleGuaranteeRoleInfo);
