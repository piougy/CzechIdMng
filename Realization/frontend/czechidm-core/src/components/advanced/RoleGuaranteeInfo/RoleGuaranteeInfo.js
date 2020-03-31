import PropTypes from 'prop-types';
import { connect } from 'react-redux';
//
import {RoleGuaranteeManager, SecurityManager} from '../../../redux';
import AbstractEntityInfo from '../EntityInfo/AbstractEntityInfo';

const manager = new RoleGuaranteeManager();

/**
 * Role guarantee by identity - basic information (info card)
 *
 * @author Vít Švanda
 */
export class RoleGuaranteeInfo extends AbstractEntityInfo {

  getManager() {
    return manager;
  }

  showLink() {
    if (!super.showLink()) {
      return false;
    }
    if (!SecurityManager.hasAccess({ type: 'HAS_ANY_AUTHORITY', authorities: ['ROLEGUARANTEE_READ'] })) {
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
    return this.i18n('entity.RoleGuarantee._type');
  }

  /**
   * Returns popover info content
   *
   * @param  {array} table data
   */
  getPopoverContent(entity) {
    if (!entity._embedded || !entity._embedded.guarantee) {
      return [
        {
          label: this.i18n('entity.RoleGuarantee.guarantee.label'),
          value: entity.guarantee
        }
      ];
    }
    return [
      {
        label: this.i18n('entity.RoleGuarantee.guarantee.label'),
        value: entity._embedded.guarantee.username
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

RoleGuaranteeInfo.propTypes = {
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
RoleGuaranteeInfo.defaultProps = {
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
export default connect(select)(RoleGuaranteeInfo);
