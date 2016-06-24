'use strict';

import React, { PropTypes } from 'react';
import classNames from 'classnames';
import { connect } from 'react-redux';
//
import * as Basic from '../../basic';
import { IdentityManager } from '../../../modules/core/redux/data';

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
    const { identity, _identity, username } = this.props;
    if (username && !identity && !_identity) {
      this.context.store.dispatch(identityManager.fetchEntityIfNeeded(username));
    }
  }

  render() {
    const { rendered, showLoading, className, username, identity, ...others } = this.props;
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
      { 'panel-success': _identity && !_identity.disabled },
      { 'panel-warning': _identity && _identity.disabled },
      className
    );
    //
    return (
      <div>
        {
          showLoading || (username && !_identity)
          ?
          <Basic.Well showLoading={true}/>
          :
          !_identity
          ?
          null
          :
          <Basic.Panel className={panelClassNames} {...others}>
            <Basic.PanelHeader>
              <Basic.Row>
                <div className="col-lg-2">
                  {
                    _identity.disabled
                    ?
                    <Basic.Icon type="fa" icon="user-times" className="fa-4x"/>
                    :
                    identityManager.isExterne(_identity)
                    ?
                    <Basic.Icon type="fa" icon="user-secret" className="fa-4x"/>
                    :
                    <Basic.Icon type="fa" icon="user" className="fa-4x"/>
                  }
                </div>
                <div className="col-lg-10">
                  <div><strong>{identityManager.getNiceLabel(_identity)}</strong></div>
                  <div>{_identity.email}</div>
                  <div>{_identity.phone}</div>
                  {
                    identityManager.isExterne(_identity)
                    ?
                    <div>
                      <i>
                        {this.i18n('component.advanced.IdentityInfo.isExterne')}
                      </i>
                    </div>
                    :
                    null
                  }
                  <div><i>{_identity.disabled ? this.i18n('component.advanced.IdentityInfo.disabledInfo') : null}</i></div>
                </div>
              </Basic.Row>
            </Basic.PanelHeader>
          </Basic.Panel>
        }
      </div>
    );
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
   * Internal identity loaded by given username
   */
  _identity: PropTypes.object
};
IdentityInfo.defaultProps = {
  ...Basic.AbstractContextComponent.defaultProps,
  identity: null
};

function select(state, component) {
  return {
    _identity: identityManager.getEntity(state, component.username)
  }
}
export default connect(select)(IdentityInfo)
