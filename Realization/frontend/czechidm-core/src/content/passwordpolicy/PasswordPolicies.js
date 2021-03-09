import React from 'react';
import { connect } from 'react-redux';
import * as Basic from '../../components/basic';
import { PasswordPolicyManager } from '../../redux';
import PasswordPolicyTable from './PasswordPolicyTable';

const PASSWORD_POLICY_TABLE_UIKEY = 'passwordPoliciesTableUikey';

/**
* Content with all password policy.
*
* @author Ondřej Kopr
*/
class PasswordPolicies extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.passwordPolicyManager = new PasswordPolicyManager();
  }

  getManager() {
    return this.passwordPolicyManager;
  }

  getContentKey() {
    return 'content.passwordPolicies';
  }

  getNavigationKey() {
    return 'password-policies';
  }

  render() {
    return (
      <Basic.Div>
        { this.renderPageHeader() }

        <Basic.Panel>
          <PasswordPolicyTable passwordPolicyManager={ this.passwordPolicyManager } uiKey={ PASSWORD_POLICY_TABLE_UIKEY }/>
        </Basic.Panel>
      </Basic.Div>
    );
  }
}

PasswordPolicies.propTypes = {
};
PasswordPolicies.defaultProps = {
};

export default connect()(PasswordPolicies);
