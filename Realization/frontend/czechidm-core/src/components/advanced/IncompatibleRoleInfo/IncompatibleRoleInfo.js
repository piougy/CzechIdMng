import PropTypes from 'prop-types';
import { connect } from 'react-redux';
//
import {IncompatibleRoleManager, SecurityManager} from '../../../redux';
import AbstractEntityInfo from '../EntityInfo/AbstractEntityInfo';

const manager = new IncompatibleRoleManager();

/**
 * Incompatible role basic information (info card)
 *
 * @author Vít Švanda
 */
export class IncompatibleRoleInfo extends AbstractEntityInfo {

  getManager() {
    return manager;
  }

  showLink() {
    if (!super.showLink()) {
      return false;
    }
    if (!SecurityManager.hasAccess({ type: 'HAS_ANY_AUTHORITY', authorities: ['INCOMPATIBLEROLE_READ'] })) {
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
    return `/role/${encodeURIComponent(entity.superior)}/incompatible-roles`;
  }

  /**
   * Returns popover info content
   *
   * @param  {array} table data
   */
  getPopoverContent(entity) {
    if (!entity._embedded || !entity._embedded.sub || !entity._embedded.superior) {
      return [
        {
          label: this.i18n('entity.IncompatibleRole.superior.label'),
          value: entity.superior
        },
        {
          label: this.i18n('entity.IncompatibleRole.sub.label'),
          value: entity.sub
        }
      ];
    }
    return [
      {
        label: this.i18n('entity.IncompatibleRole.superior.label'),
        value: entity._embedded.superior.name
      },
      {
        label: this.i18n('entity.IncompatibleRole.sub.label'),
        value: entity._embedded.sub.name
      }
    ];
  }

  /**
   * Returns entity icon (null by default - icon will not be rendered)
   *
   * @param  {object} entity
   */
  getEntityIcon() {
    return 'fa:times-circle';
  }
}

IncompatibleRoleInfo.propTypes = {
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
IncompatibleRoleInfo.defaultProps = {
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
export default connect(select)(IncompatibleRoleInfo);
