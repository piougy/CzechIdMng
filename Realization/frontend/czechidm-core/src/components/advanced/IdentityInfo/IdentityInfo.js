import React, { PropTypes } from 'react';
import classNames from 'classnames';
import { connect } from 'react-redux';
import { Link } from 'react-router';
//
import * as Basic from '../../basic';
import * as Utils from '../../../utils';
import { IdentityManager, SecurityManager } from '../../../redux/';
import UuidInfo from '../UuidInfo/UuidInfo';
import AbstractEntityInfo from '../EntityInfo/AbstractEntityInfo';

const identityManager = new IdentityManager();

/**
 * Identity basic information (info card)
 *
 * @author Radek Tomiška
 */
export class IdentityInfo extends AbstractEntityInfo {

  constructor(props, context) {
    super(props, context);
    this.state = {
      error: null
    };
  }

  getManager() {
    return identityManager;
  }

  /**
   * if username is setted and identity is not - then load identity
   */
  loadEntityIfNeeded() {
    const { entity, _identity } = this.props;
    if (this._id() && !entity && !_identity) {
      const uiKey = identityManager.resolveUiKey(null, this._id());
      const error = Utils.Ui.getError(this.context.store.getState(), uiKey);
      if (!Utils.Ui.isShowLoading(this.context.store.getState(), uiKey)
          && (!error || error.statusCode === 401)) { // show loading check has to be here - new state is needed
        this.context.store.dispatch(identityManager.fetchEntityIfNeeded(this._id(), null, () => {}));
      }
    }
  }

  _id() {
    const { username, entityIdentifier } = this.props;
    // id ha higher priority
    return entityIdentifier || username;
  }

  showLink() {
    const { showLink } = this.props;
    if (!showLink) {
      return false;
    }
    if (!SecurityManager.hasAccess({ 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['IDENTITY_READ']})) {
      return false;
    }
    return true;
  }

  /**
   * Renders full info card
   */
  _renderFull() {
    const { className, entity, style } = this.props;
    //
    let _identity = this.props._identity;
    if (entity) { // identity prop has higher priority
      _identity = entity;
    }
    //
    const panelClassNames = classNames(
      'identity-info',
      { 'panel-success': _identity && !_identity.disabled },
      { 'panel-warning': _identity && _identity.disabled },
      className
    );
    //
    return (
      <Basic.Panel className={panelClassNames} style={style}>
        <Basic.PanelHeader>
          <Basic.Row>
            <div className="col-lg-2">
              {
                _identity.disabled
                ?
                <Basic.Icon type="fa" icon="user-times" className="fa-4x"/>
                :
                <Basic.Icon type="fa" icon="user" className="fa-4x"/>
              }
            </div>
            <div className="col-lg-10">
              <div><strong>{identityManager.getNiceLabel(_identity)}</strong></div>
              <div>{_identity.email}</div>
              <div>{_identity.phone}</div>
              <div><i>{_identity.disabled ? this.i18n('component.advanced.IdentityInfo.disabledInfo') : null}</i></div>
              {
                !this.showLink()
                ||
                <div>
                  <Link to={`/identity/${this._id()}/profile`}>
                    <Basic.Icon value="fa:angle-double-right"/>
                    {' '}
                    {this.i18n('component.advanced.IdentityInfo.profileLink')}
                  </Link>
                </div>
              }
            </div>
          </Basic.Row>
        </Basic.PanelHeader>
      </Basic.Panel>
    );
  }

  render() {
    const { rendered, showLoading, className, entity, face, _showLoading, style } = this.props;
    //
    if (!rendered) {
      return null;
    }
    let _identity = this.props._identity;
    if (entity) { // identity prop has higher priority
      _identity = entity;
    }
    //
    if (showLoading || (_showLoading && this._id() && !_identity)) {
      switch (face) {
        case 'text':
        case 'link':
        case 'popover': {
          return (
            <Basic.Icon value="refresh" showLoading className={className} style={style}/>
          );
        }
        default: {
          return (
            <Basic.Well showLoading className={`identity-info ${className}`} style={style}/>
          );
        }
      }
    }
    if (!_identity) {
      if (!this._id()) {
        return null;
      }
      return (<UuidInfo className={className} value={ this._id() } style={style} />);
    }
    //
    switch (face) {
      case 'text':
      case 'link': {
        if (!this.showLink() || face === 'text') {
          return (
            <span className={className} style={style}>{ identityManager.getNiceLabel(_identity) }</span>
          );
        }
        return (
          <Link to={`/identity/${this._id()}/profile`}>{identityManager.getNiceLabel(_identity)}</Link>
        );
      }
      case 'popover': {
        return (
          <Basic.Popover
            trigger="click"
            value={this._renderFull()}
            className="identity-info-popover">
            {
              <span
                className={ classNames }
                style={ style }>
                <a href="#" onClick={ (e) => e.preventDefault() }>{ identityManager.getNiceLabel(_identity) }</a>
              </span>
            }
          </Basic.Popover>
        );
      }
      default: {
        return this._renderFull();
      }
    }
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
   */
  entityIdentifier: PropTypes.string,
  /**
   * Internal identity loaded by given username
   */
  _identity: PropTypes.object,
  _showLoading: PropTypes.bool
};
IdentityInfo.defaultProps = {
  ...AbstractEntityInfo.defaultProps,
  entity: null,
  face: 'full',
  _showLoading: true,
};

function select(state, component) {
  return {
    _identity: identityManager.getEntity(state, component.entityIdentifier || component.username),
    _showLoading: identityManager.isShowLoading(state, null, component.entityIdentifier || component.username),
    userContext: state.security.userContext // is needed for refresh after login
  };
}
export default connect(select)(IdentityInfo);
