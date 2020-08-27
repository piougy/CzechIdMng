import PropTypes from 'prop-types';
import React from 'react';
import { connect } from 'react-redux';
//
import { Basic, Utils, Managers } from 'czechidm-core';
import PasswordChangeForm from 'czechidm-core/src/content/identity/PasswordChangeForm';
import { UniformPasswordManager } from '../../redux';
//
const IDM_NAME = Utils.Config.getConfig('app.name', 'CzechIdM');
const RESOURCE_IDM = `0:${IDM_NAME}`;
//
const uniformPasswordManager = new UniformPasswordManager();
const identityManager = new Managers.IdentityManager();

/**
 * In this component include password change and send props with account options
 *
 * @author Ondřej Kopr
 */
class PasswordChangeAccounts extends Basic.AbstractContent {

  componentDidMount() {
    super.componentDidMount();
    //
    const { entityId } = this.props.match.params;

    this.context.store.dispatch(uniformPasswordManager.fetchPasswordChangeOptions(entityId, `${ entityId }-account-options`, (accounts, error) => {
      // Prevent to show error, when logged identity cannot read identity accounts => password change for IdM only.
      if (error && error.statusCode !== 403) {
        this.addError(error);
      }
    }));
  }

  _getOptions() {
    const { entityId } = this.props.match.params;
    const { accounts, showLoading } = this.props;

    if (showLoading || accounts == null) {
      return null;
    }

    const identity = identityManager.getEntity(this.context.store.getState(), entityId);
    const options = [ ];

    let changeInIdm = false;
    accounts.forEach(acc => {
      if (acc.changeInIdm) {
        changeInIdm = true;
      }
      options.push({
        value: acc.id, // Id is there only as unique key, ID can be id of unifrom password or account id
        accounts: acc.accounts,
        idm: acc.changeInIdm,
        niceLabel: acc.niceLabel
      });
    });
    if (changeInIdm === false) {
      options.push({
        value: RESOURCE_IDM, accounts: RESOURCE_IDM, idm: true, niceLabel: `${ IDM_NAME }${ identity ? ` (${ identity.username })` : '' }`
      });
    }

    return options;
  }

  render() {
    const { passwordChangeType, userContext, requireOldPassword, showLoading } = this.props;
    const { entityId } = this.props.match.params;
    const options = this._getOptions();

    //
    return (
      <Basic.Div>
        {
          showLoading || !options
          ?
          <Basic.Loading isStatic show/>
          :
          <PasswordChangeForm
            userContext={ userContext }
            entityId={ entityId }
            passwordChangeType={ passwordChangeType }
            requireOldPassword={ requireOldPassword }
            accountOptions={ options }/>
        }
      </Basic.Div>
    );
  }

}

PasswordChangeAccounts.propTypes = {
  showLoading: PropTypes.bool,
  userContext: PropTypes.object,
  accounts: PropTypes.object
};
PasswordChangeAccounts.defaultProps = {
  userContext: null,
  showLoading: true,
  accounts: null
};

function select(state, component) {
  const { entityId } = component.match.params;
  return {
    userContext: state.security.userContext,
    accounts: uniformPasswordManager.getPasswordChangeOptions(state, `${entityId}-account-options`),
    showLoading: uniformPasswordManager.isShowLoading(state, `${entityId}-account-options`)
  };
}
export default connect(select)(PasswordChangeAccounts);
