'use strict';

import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import { Link }  from 'react-router';
import { connect } from 'react-redux';
//
import * as Basic from '../../../../components/basic';
import { IdentityAccountManager } from '../../../../redux/data';

const uiKey = 'user-accounts';

class Accounts extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.identityAccountManager = new IdentityAccountManager();
  }

  componentDidMount() {
    this.selectSidebarItem('profile-accounts');
    const { userID } = this.props.params;
    this.identityAccountManager.setUsername(userID);
    this.context.store.dispatch(this.identityAccountManager.getAccounts(uiKey));
  }

  render() {
    const { _entities, _showLoading} = this.props;

    let accountPanels = [];
    if (_entities) {
      for (let i = 0; i < _entities.length; i++) {
        let account = _entities[i];
        let key = 'account_panel_' + i;
        accountPanels.push(
          <div key={key} className="col-sm-6">
            <Basic.Panel showLoading={_showLoading} className="second">
              <Basic.PanelHeader text={this.identityAccountManager.getNiceLabel(account)}/>
              <Basic.Table data={account.attributes}>
                <Basic.Column property="name" header={this.i18n('entity.IdentityAccount.attribute.name')} width="50%"/>
                <Basic.Column property="value" header={this.i18n('entity.IdentityAccount.attribute.value')} width="50%"/>
              </Basic.Table>
            </Basic.Panel>
          </div>
        );
      }
    }

    return (
      <div>
        <Helmet title={this.i18n('content.user.accounts.title')} />
        <Basic.ContentHeader>
          {this.i18n('content.user.accounts.header')}
        </Basic.ContentHeader>
        {
          _showLoading
          ?
          <Basic.Loading showLoading={true} className="static"/>
          :
          accountPanels.length === 0
          ?
          <Basic.Alert text={this.i18n('content.user.accounts.empty')}/>
          :
          null
        }
        <div className="row">
          {
            _showLoading
            ||
            accountPanels
          }
        </div>
      </div>
    );
  }
}

Accounts.propTypes = {
  _showLoading: PropTypes.bool,
  _entities: PropTypes.arrayOf(React.PropTypes.object)
};
Accounts.defaultProps = {
  _showLoading: true,
  _entities: []
};

function select(state) {
  const identityAccountManager = new IdentityAccountManager('n/a'); // TODO: just for gain entities from store - move function to common manager
  return {
    _showLoading: identityAccountManager.isShowLoading(state, uiKey),
    _entities: identityAccountManager.getEntities(state, uiKey)
  }
}

export default connect(select)(Accounts);
