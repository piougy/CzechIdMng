import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import classNames from 'classnames';
//
import * as Basic from '../../basic';
import { IdentityManager, DataManager, ConfigurationManager, FormProjectionManager } from '../../../redux';
import AbstractEntityInfo from '../EntityInfo/AbstractEntityInfo';
import AuditableInfo from '../EntityInfo/AuditableInfo';
import ConfigLoader from '../../../utils/ConfigLoader';

const manager = new IdentityManager();
const projectionManager = new FormProjectionManager();

/**
 * Identity basic information (info card)
 *
 * @author Radek Tomiška
 */
export class IdentityInfo extends AbstractEntityInfo {

  getComponentKey() {
    return 'component.advanced.IdentityInfo';
  }

  getManager() {
    return manager;
  }

  onEnter() {
    super.onEnter();
    //
    if (this.getEntityId()) {
      this.context.store.dispatch(this.getManager().downloadProfileImage(this.getEntityId()));
    }
  }

  /**
   * Override super.getEntityId() - adds username for backward compatibility
   *
   * @return {string} id
   */
  getEntityId() {
    const { username, entityIdentifier, entity } = this.props;
    // id has higher priority
    if (entityIdentifier) {
      return entityIdentifier;
    }
    if (username) {
      return username;
    }
    if (entity) {
      return entity.username;
    }
    return null;
  }

  showLink() {
    if (!super.showLink()) {
      return false;
    }
    // evaluate authorization policies
    const { _permissions } = this.props;
    if (!this.getManager().canRead(this.getEntity(), _permissions)) {
      return false;
    }
    return true;
  }

  /**
   * Get link to detail (`url`).
   *
   * @return {string}
   */
  getLink(entity, skipDashboard = false) {
    if (!entity) {
      entity = this.getEntity();
    }
    //
    if (!skipDashboard) {
      return `/identity/${ encodeURIComponent(this.getEntityId(entity)) }/dashboard`;
    }
    return manager.getDetailLink(entity);
  }

  /**
   * Show identity detail by configured projection
   *
   * @param  {event} event
   * @since 10.2.0
   */
  showDetail(entity, event) {
    if (event) {
      event.preventDefault();
    }
    //
    const { skipDashboard } = this.props;
    const ctrlKey = !event ? false : event.ctrlKey;
    //
    this.context.history.push(this.getLink(entity, skipDashboard || ctrlKey));
  }

  /**
   * Returns entity icon (null by default - icon will not be rendered)
   *
   * @param  {object} entity
   */
  getEntityIcon(entity) {
    if (this.isDisabled(entity)) {
      return 'component:disabled-identity';
    }
    return 'component:enabled-identity';
  }

  /**
   * @Override
   * Renders nicelabel used in text and link face
   */
  _renderNiceLabel() {
    const { showOnlyUsername } = this.props;
    //
    if (!showOnlyUsername) {
      return super._renderNiceLabel();
    }
    const _entity = this.getEntity();
    if (_entity && _entity.username) {
      return _entity.username;
    }
    return '';
  }

  renderImage(entity) {
    const { _imageLoading, _imageUrl } = this.props;
    //
    let content = null;
    if (_imageLoading) {
      content = (
        <div className="text-center img-thumbnail img-loading" style={{ backgroundColor: '#DFF0D8' }}>
          <Basic.Icon
            value="fa:refresh"
            showLoading
            color="#FFFFFF" />
        </div>
      );
    } else if (_imageUrl) {
      content = (
        <img src={ _imageUrl } className="img-thumbnail" alt="profile" />
      );
    } else {
      content = (
        <Basic.Icon
          value="component:identity"
          identity={ entity }
          className="text-center img-thumbnail img-none"
          style={{ backgroundColor: this.isDisabled(entity) ? '#FCF8E3' : '#DFF0D8' }}
          color="#FFFFFF" />
      );
    }
    //
    return (
      <div className="image-wrapper">
        { content }
      </div>
    );
  }

  renderRow(icon, entityAttr) {
    if (!entityAttr) {
      return null;
    }
    //
    return (
      <tr>
        <td>
          <Basic.Icon value={ icon } style={{ marginRight: 5 }} />
          { entityAttr }
        </td>
      </tr>
    );
  }

  _renderFull() {
    const { className, style } = this.props;
    const { showAuditableInfo, expandInfo } = this.state;
    const _entity = this.getEntity();
    //
    const panelClassNames = classNames(
      'abstract-entity-info',
      'identity-info',
      { 'panel-success': _entity && !this.isDisabled(_entity) },
      { 'panel-warning': _entity && this.isDisabled(_entity) },
      className
    );
    //
    return (
      <Basic.Panel className={ panelClassNames } style={ style }>
        <Basic.PanelHeader>
          <Basic.Div style={{ display: 'flex', alignItems: 'center' }}>
            <Basic.Div style={{ flex: 1 }}>
              <Basic.Icon value={ this.getEntityIcon(_entity) } style={{ marginRight: 5 }}/>
              { this.getPopoverTitle(_entity) }
            </Basic.Div>
            <Basic.Div>
              {
                !this.isDisabled(_entity)
                ||
                <Basic.Label text={ this.i18n('label.disabled') } className="label-disabled"/>
              }
              { this._renderSystemInformationIcon() }
              { this._renderSystemCollapsIcon() }
            </Basic.Div>
          </Basic.Div>
        </Basic.PanelHeader>
        {
          showAuditableInfo
          ?
          <AuditableInfo entity={ _entity } face="content"/>
          :
          <Basic.Div rendered={ expandInfo } className="image-field-container">
            <Basic.Div className="image-col">
              { this.renderImage(_entity) }
            </Basic.Div>
            <Basic.Div className="field-col">
              <table className="table table-condensed">
                <tbody>
                  { this.renderRow('fa:envelope', _entity.email) }
                  { this.renderRow('fa:phone', _entity.phone) }
                  {
                    !_entity._embedded || !_entity._embedded.formProjection
                    ||
                    this.renderRow(
                      projectionManager.getLocalization(_entity._embedded.formProjection, 'icon', 'fa:user'),
                      (
                        <span title={ this.i18n('entity.Identity.formProjection.label') }>
                          { projectionManager.getLocalization(_entity._embedded.formProjection, 'label', _entity._embedded.formProjection.code) }
                        </span>
                      )
                    )
                  }
                  { this.renderRow(null, (
                    <a href="#" onClick={ this.showDetail.bind(this, _entity) }>
                      <Basic.Icon value="fa:angle-double-right"/>
                      {' '}
                      { this.i18n('link.profile.label') }
                    </a>
                  )) }
                </tbody>
              </table>
            </Basic.Div>
          </Basic.Div>
        }

      </Basic.Panel>
    );
  }
}

IdentityInfo.propTypes = {
  ...AbstractEntityInfo.propTypes,
  /**
   * Selected entity - has higher priority
   */
  entity: PropTypes.object,
  /**
   * Selected identity's username - identity will be loaded automatically  (username entityIdentifier)
   */
  username: PropTypes.string,
  /**
   * Selected identity's id (username alias) - identity will be loaded automatically
   * Selected identity's id - identity will be loaded automatically
   */
  entityIdentifier: PropTypes.string,
  /**
   * If true, then show only username instead niceLabel
   */
  showOnlyUsername: PropTypes.bool,
  //
  _showLoading: PropTypes.bool,
  _permissions: PropTypes.oneOfType([
    PropTypes.bool,
    PropTypes.arrayOf(PropTypes.string)
  ]),
  _imageLoading: PropTypes.bool,
};
IdentityInfo.defaultProps = {
  ...AbstractEntityInfo.defaultProps,
  entity: null,
  face: 'full',
  showOnlyUsername: false,
  _showLoading: true,
  _imageLoading: true,
  _imageUrl: null
};

function select(state, component) {
  const identifier = component.entityIdentifier || component.username;
  const identity = component.entity || manager.getEntity(state, identifier);
  const profileUiKey = manager.resolveProfileUiKey(identifier);
  const profile = DataManager.getData(state, profileUiKey);
  //
  return {
    _entity: identity,
    _showLoading: manager.isShowLoading(state, null, identifier),
    _imageLoading: DataManager.isShowLoading(state, profileUiKey),
    _imageUrl: profile ? profile.imageUrl : null,
    userContext: state.security.userContext, // is needed for refresh after login
    _permissions: manager.getPermissions(state, null, identity),
    skipDashboard: ConfigurationManager.getPublicValueAsBoolean(
      state,
      'idm.pub.core.identity.dashboard.skip',
      ConfigLoader.getConfig('identity.dashboard.skip', false)
    )
  };
}
export default connect(select)(IdentityInfo);
