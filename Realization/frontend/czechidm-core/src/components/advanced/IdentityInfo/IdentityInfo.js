import React, { PropTypes } from 'react';
import classNames from 'classnames';
import { connect } from 'react-redux';
import { Link } from 'react-router';
//
import * as Basic from '../../basic';
import * as Utils from '../../../utils';
import { IdentityManager, SecurityManager } from '../../../redux/';

const identityManager = new IdentityManager();

export class IdentityInfo extends Basic.AbstractContextComponent {

  constructor(props, context) {
    super(props, context);
  }

  componentDidMount() {
    this._loadIdentityIfNeeded();
  }

  componentDidUpdate() {
    this._loadIdentityIfNeeded();
  }

  /**
   * if username is setted and identity is not - then load identity
   */
  _loadIdentityIfNeeded() {
    const { identity, _identity } = this.props;
    if (this._id() && !identity && !_identity) {
      const uiKey = identityManager.resolveUiKey(null, this._id());
      if (!Utils.Ui.isShowLoading(this.context.store.getState(), uiKey)
          && !Utils.Ui.getError(this.context.store.getState(), uiKey)) { // show loading check has to be here - new state is needed
        this.context.store.dispatch(identityManager.fetchEntityIfNeeded(this._id(), null, () => {}));
      }
    }
  }

  _id() {
    const { username, id } = this.props;
    // id ha higher priority
    return id || username;
  }

  _showLink() {
    const { showLink } = this.props;
    if (!showLink) {
      return false;
    }
    if (!SecurityManager.hasAccess({ 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['IDENTITY_READ']})) {
      return false;
    }
    return true;
  }

  render() {
    const { rendered, showLoading, className, identity, face, _showLoading, style } = this.props;
    //
    if (!rendered) {
      return null;
    }
    let _identity = this.props._identity;
    if (identity) { // identity prop has higher priority
      _identity = identity;
    }

    //
    const panelClassNames = classNames(
      'identity-info',
      { 'panel-success': _identity && !_identity.disabled },
      { 'panel-warning': _identity && _identity.disabled },
      className
    );
    if (showLoading || (_showLoading && this._id() && !_identity)) {
      switch (face) {
        case 'link': {
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
      return (<span>{this._id()}</span>);
    }
    //
    switch (face) {
      case 'link': {
        if (!this._showLink()) {
          return identityManager.getNiceLabel(_identity);
        }
        return (
          <Link to={`/identity/${this._id()}/profile`}>{identityManager.getNiceLabel(_identity)}</Link>
        );
      }
      default: {
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
                    !this._showLink()
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
    }
  }
}

IdentityInfo.propTypes = {
  ...Basic.AbstractContextComponent.propTypes,
  /**
   * Selected identity
   */
  identity: PropTypes.object,
  /**
   * Selected identity's username - identity will be loaded automatically
   */
  username: PropTypes.string,
  /**
   * Selected identity's id (username alias) - identity will be loaded automatically
   */
  id: PropTypes.string,
  /**
   * Internal identity loaded by given username
   */
  _identity: PropTypes.object,
  /**
   * Decorator
   */
  face: PropTypes.oneOf(['full', 'link']),
  /**
   * Shows link to full identity detail (if currently logged user has appropriate permission)
   */
  showLink: PropTypes.bool,
  _showLoading: PropTypes.bool
};
IdentityInfo.defaultProps = {
  ...Basic.AbstractContextComponent.defaultProps,
  identity: null,
  face: 'full',
  showLink: true,
  _showLoading: true,
};

function select(state, component) {
  return {
    _identity: identityManager.getEntity(state, component.id || component.username),
    _showLoading: identityManager.isShowLoading(state, null, component.id || component.username)
  };
}
export default connect(select)(IdentityInfo);
