import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
//
import { Basic, Advanced, Managers } from 'czechidm-core';
import { RemoteServerManager } from '../../redux';

const manager = new RemoteServerManager();

/**
 * Remote server basic information (info card).
 *
 * @author Radek Tomiška
 * @since 10.8.0
 */
export class RemoteServerInfo extends Advanced.AbstractEntityInfo {

  getManager() {
    return manager;
  }

  showLink() {
    if (!super.showLink()) {
      return false;
    }
    if (!Managers.SecurityManager.hasAccess({ type: 'HAS_ANY_AUTHORITY', authorities: ['REMOTESERVER_READ'] })) {
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
    return `/remote-servers/${ encodeURIComponent(this.getEntityId()) }/detail`;
  }

  /**
   * Returns entity icon (null by default - icon will not be rendered)
   *
   * @param  {object} entity
   */
  getEntityIcon() {
    return 'component:server';
  }

  /**
   * Returns popovers title
   *
   * @param  {object} entity
   */
  getPopoverTitle() {
    return this.i18n('acc:entity.RemoteServer._type');
  }

  /**
   * Returns popover info content
   *
   * @param  {array} table data
   */
  getPopoverContent(entity) {
    const content = [
      {
        label: this.i18n('acc:entity.RemoteServer.host.label'),
        value: entity.host
      },
      {
        label: this.i18n('acc:entity.RemoteServer.port.label'),
        value: entity.port
      },
      {
        label: this.i18n('acc:entity.RemoteServer.useSsl.label'),
        value: (entity.useSsl ? this.i18n('label.yes') : this.i18n('label.no'))
      },
      {
        label: this.i18n('acc:entity.RemoteServer.timeout.label'),
        value: entity.timeout
      }
    ];
    //
    if (entity.description) {
      content.push({
        label: this.i18n('entity.description.label'),
        value: (
          <Basic.ShortText value={ entity.description } maxLength={ 100 }/>
        )
      });
    }
    //
    return content;
  }
}

RemoteServerInfo.propTypes = {
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
RemoteServerInfo.defaultProps = {
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
export default connect(select)(RemoteServerInfo);
