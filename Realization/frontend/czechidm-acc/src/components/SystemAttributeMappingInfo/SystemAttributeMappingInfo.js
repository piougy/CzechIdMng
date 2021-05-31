import PropTypes from 'prop-types';
import { connect } from 'react-redux';
//
import { Advanced, Managers } from 'czechidm-core';
import { SystemAttributeMappingManager } from '../../redux';

const manager = new SystemAttributeMappingManager();

/**
 * Mapped attribute basic information (info card)
 *
 * @author Vít Švanda
 */
export class SystemAttributeMappingInfo extends Advanced.AbstractEntityInfo {

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
    if (entity && entity._embedded && entity._embedded.systemMapping) {
      const systemId = entity._embedded.systemMapping._embedded.objectClass._embedded.system.id;
      return `/system/${encodeURIComponent(systemId)}/attribute-mappings/${encodeURIComponent(entity.id)}/detail`;
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
    const {popoverTitle} = this.props;
    if (!!popoverTitle) {
      return popoverTitle;
    }
    return this.i18n('acc:entity.SystemAttributeMapping._type');
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
        label: this.i18n('entity.name.label'),
        value: this.getManager().getNiceLabel(entity)
      }
    ];
  }
}

SystemAttributeMappingInfo.propTypes = {
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
SystemAttributeMappingInfo.defaultProps = {
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
export default connect(select)(SystemAttributeMappingInfo);
