import PropTypes from 'prop-types';
import { connect } from 'react-redux';
//
import { Advanced, Managers } from 'czechidm-core';
import { ProvisioningBreakRecipientManager } from '../../redux';

const manager = new ProvisioningBreakRecipientManager();

/**
 * System provisioning break config basic information (info card)
 *
 * @author Vít Švanda
 */
export class BreakConfigRecipientInfo extends Advanced.AbstractEntityInfo {

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
    if (entity._embedded && entity._embedded.breakConfig) {
      const systemId = entity._embedded.breakConfig.system;
      return `/system/${encodeURIComponent(systemId)}/break-configs/${encodeURIComponent(entity.breakConfig)}/detail`;
    }
    return null;
  }

  /**
   * Returns entity icon (null by default - icon will not be rendered)
   *
   * @param  {object} entity
   */
  getEntityIcon() {
    return 'fa:stop-circle-o';
  }

  /**
   * Returns popovers title
   *
   * @param  {object} entity
   */
  getPopoverTitle() {
    return this.i18n('acc:entity.ProvisioningBreakConfigRecipient._type');
  }

  /**
   * Returns popover info content
   *
   * @param  {array} table data
   */
  getPopoverContent(entity) {
    // FIXME: no informations are here - add system, identity or role ...
    return [
      {
        label: this.i18n('entity.name.label'),
        value: this.getManager().getNiceLabel(entity)
      }
    ];
  }
}

BreakConfigRecipientInfo.propTypes = {
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
BreakConfigRecipientInfo.defaultProps = {
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
export default connect(select)(BreakConfigRecipientInfo);
