import PropTypes from 'prop-types';
import { connect } from 'react-redux';
//
import {RoleFormAttributeManager, SecurityManager} from '../../../redux';
import AbstractEntityInfo from '../EntityInfo/AbstractEntityInfo';

const manager = new RoleFormAttributeManager();

/**
 * Role catalogue - relation from a role - basic information (info card)
 *
 * @author Vít Švanda
 */
export class RoleFormAttributeInfo extends AbstractEntityInfo {

  getManager() {
    return manager;
  }

  showLink() {
    if (!super.showLink()) {
      return false;
    }
    if (!SecurityManager.hasAccess({ type: 'HAS_ANY_AUTHORITY', authorities: ['ROLEFORMATTRIBUTE_READ'] })) {
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
    return `/role/${encodeURIComponent(entity.superior)}/form-attributes`;
  }

  /**
   * Returns popover info content
   *
   * @param  {array} table data
   */
  getPopoverContent(entity) {
    if (!entity._embedded || !entity._embedded.formAttribute || !entity._embedded.role) {
      return [
        {
          label: this.i18n('entity.RoleGuarantee.role.label'),
          value: entity.role
        },
        {
          label: this.i18n('entity.RoleFormAttribute.formAttribute.label'),
          value: entity.formAttribute
        }
      ];
    }
    return [
      {
        label: this.i18n('entity.RoleGuarantee.role.label'),
        value: entity._embedded.role.name
      },
      {
        label: this.i18n('entity.RoleFormAttribute.formAttribute.label'),
        value: entity._embedded.formAttribute.name
      }
    ];
  }

  /**
   * Returns entity icon (null by default - icon will not be rendered)
   *
   * @param  {object} entity
   */
  getEntityIcon() {
    return 'fa:th-list';
  }

  /**
   * Returns popovers title
   *
   * @param  {object} entity
   */
  getPopoverTitle() {
    return this.i18n('entity.RoleFormAttribute._type');
  }
}

RoleFormAttributeInfo.propTypes = {
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
RoleFormAttributeInfo.defaultProps = {
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
export default connect(select)(RoleFormAttributeInfo);
