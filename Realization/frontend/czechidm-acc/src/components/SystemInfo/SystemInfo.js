import PropTypes from 'prop-types';
import { connect } from 'react-redux';
//
import { Advanced, Managers } from 'czechidm-core';
import { SystemManager } from '../../redux';

const manager = new SystemManager();

/**
 * Target system basic information (info card)
 *
 * @author Radek Tomiška
 */
export class SystemInfo extends Advanced.AbstractEntityInfo {

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
    return `/system/${encodeURIComponent(this.getEntityId())}/detail`;
  }

  /**
   * Returns entity icon (null by default - icon will not be rendered)
   *
   * @param  {object} entity
   */
  getEntityIcon() {
    return 'component:system';
  }

  /**
   * Returns popovers title
   *
   * @param  {object} entity
   */
  getPopoverTitle() {
    return this.i18n('acc:entity.System._type');
  }

  /**
   * Returns popover info content
   *
   * @param  {array} table data
   */
  getPopoverContent(entity) {
    let state = this.i18n('label.enabled');
    if (entity.disabledProvisioning) {
      if (entity.disabled) {
        state = this.i18n('acc:entity.System.disabledProvisioning.label');
      } else {
        state = this.i18n('acc:entity.System.readonlyDisabledProvisioning.label');
      }
    } else if (entity.disabled) {
      state = this.i18n('acc:entity.System.disabled.label');
    } else if (entity.readonly) {
      state = this.i18n('acc:entity.System.readonly.label');
    }
    //
    return [
      {
        label: this.i18n('entity.name'),
        value: this.getManager().getNiceLabel(entity)
      },
      {
        label: this.i18n('acc:entity.System.queue.label'),
        value: (entity.queue ? this.i18n('label.yes') : this.i18n('label.no'))
      },
      {
        label: this.i18n('acc:entity.System.state.label'),
        value: state
      }
    ];
  }
}

SystemInfo.propTypes = {
  ...Advanced.AbstractEntityInfo.propTypes,
  /**
   * Selected entity - has higher priority
   */
  entity: PropTypes.object,
  /**
   * Selected entity's id - entity will be loaded automatically
   */
  entityIdentifier: PropTypes.string,
  //
  _showLoading: PropTypes.bool
};
SystemInfo.defaultProps = {
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
export default connect(select)(SystemInfo);
