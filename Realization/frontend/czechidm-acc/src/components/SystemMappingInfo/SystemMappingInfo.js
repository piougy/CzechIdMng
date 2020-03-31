import PropTypes from 'prop-types';
import { connect } from 'react-redux';
//
import { Advanced, Managers } from 'czechidm-core';
import { SystemMappingManager } from '../../redux';

const manager = new SystemMappingManager();

/**
 * Mapped attribute basic information (info card)
 *
 * @author Vít Švanda
 */
export class SystemMappingInfo extends Advanced.AbstractEntityInfo {

  getManager() {
    return manager;
  }

  showLink() {
    if (!super.showLink()) {
      return false;
    }
    if (!Managers.SecurityManager.hasAccess({ type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_READ'] })) {
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
    if (entity && entity._embedded && entity._embedded.objectClass) {
      const systemId = entity._embedded.objectClass.system;
      return `/system/${encodeURIComponent(systemId)}/mappings/${encodeURIComponent(entity.id)}/detail`;
    }
    return null;
  }

  /**
   * Returns entity icon (null by default - icon will not be rendered)
   *
   * @param  {object} entity
   */
  getEntityIcon() {
    return 'fa:list-alt';
  }

  /**
   * Returns popovers title
   *
   * @param  {object} entity
   */
  getPopoverTitle() {
    return this.i18n('acc:entity.SystemMapping._type');
  }

  /**
   * Returns popover info content
   *
   * @param  {array} table data
   */
  getPopoverContent(entity) {
    //
    return [
      {
        label: this.i18n('entity.name'),
        value: this.getManager().getNiceLabel(entity)
      }
    ];
  }
}

SystemMappingInfo.propTypes = {
  ...Advanced.AbstractEntityInfo.propTypes,
  /**
   * Selected entity - has higher priority.
   */
  entity: PropTypes.object,
  /**
   * Selected entity's id - entity will be loaded automatically.
   */
  entityIdentifier: PropTypes.string,
  //
  _showLoading: PropTypes.bool
};
SystemMappingInfo.defaultProps = {
  ...Advanced.AbstractEntityInfo.defaultProps,
  entity: null,
  face: 'link',
  _showLoading: true,
};

function select(state, component) {
  return {
    _entity: manager.getEntity(state, component.entityIdentifier),
    _showLoading: manager.isShowLoading(state, null, component.entityIdentifier)
  };
}
export default connect(select)(SystemMappingInfo);
