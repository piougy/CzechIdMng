import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
//
import { Basic } from 'czechidm-core';
import { IdentityAccountManager } from '../../redux';
import PasswordChangeForm from 'czechidm-core/src/content/identity/PasswordChangeForm';
//

const RESOURCE_IDM = '0:CzechIdM';

const identityAccountManager = new IdentityAccountManager();

/**
 * In this component include password change and send props with account options
 */
class PasswordChangeAccounts extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  componentDidMount() {
    const { entityId } = this.props.params;

    const defaultSearchParameters = identityAccountManager.getDefaultSearchParameters()
                                      .setFilter('ownership', true)
                                      .setFilter('identity', entityId);

    this.selectSidebarItem('profile-password');
    this.context.store.dispatch(identityAccountManager.fetchEntities(defaultSearchParameters, `${entityId}-accounts`));
  }

  _getOptions() {
    const { entityId } = this.props.params;
    const { accounts, showLoading } = this.props;

    if (showLoading) {
      return null;
    }

    const options = [
      { value: RESOURCE_IDM, niceLabel: 'CzechIdM (' + entityId + ')'}
    ];

    accounts.forEach(acc => {
      options.push({
        value: acc.id,
        niceLabel: identityAccountManager.getNiceLabelWithSystem(acc._embedded.account._embedded.system.name, acc._embedded.identity.username) });
    });

    return options;
  }

  render() {
    const { passwordChangeType, userContext, requireOldPassword, showLoading } = this.props;
    const { entityId } = this.props.params;
    const options = this._getOptions();
    return (
      <div>
        {
          showLoading
          ||
          <PasswordChangeForm
            userContext={userContext}
            entityId={entityId}
            passwordChangeType={passwordChangeType}
            requireOldPassword={requireOldPassword}
            accountOptions={options}/>
        }
    </div>
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
  const { entityId } = component.params;

  return {
    userContext: state.security.userContext,
    accounts: identityAccountManager.getEntities(state, `${entityId}-accounts`),
    showLoading: identityAccountManager.isShowLoading(state, `${entityId}-accounts`)
  };
}
export default connect(select)(PasswordChangeAccounts);
