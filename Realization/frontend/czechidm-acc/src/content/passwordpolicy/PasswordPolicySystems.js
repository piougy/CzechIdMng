import PropTypes from 'prop-types';
import React from 'react';
import { connect } from 'react-redux';
//
import { Basic, Domain, Enums, Managers } from 'czechidm-core';
import { SystemManager } from '../../redux';
import SystemTable from '../system/SystemTable';

const passwordPolicyManager = new Managers.PasswordPolicyManager();

/**
* Table with connected systems to this password policy.
*
* @author Ondrej Kopr
* @author Radek Tomiška
*/
class PasswordPolicySystems extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.systemManager = new SystemManager();
  }

  getNavigationKey() {
    'password-policies-systems';
  }

  componentDidMount() {
    super.componentDidMount();
    const { entityId } = this.props.match.params;
    //
    this.selectNavigationItems(['system', 'password-policies', 'password-policies-systems']);
    this.context.store.dispatch(passwordPolicyManager.fetchEntity(entityId));
  }

  render() {
    const { entityId } = this.props.match.params;
    const { entity, showLoading } = this.props;

    let forceSearchParameters = null;

    const validateType = entity && Enums.PasswordPolicyTypeEnum.findSymbolByKey(entity.type) === Enums.PasswordPolicyTypeEnum.VALIDATE;
    if (validateType === undefined || validateType === null) {
      forceSearchParameters = null;
    } else if (validateType) {
      forceSearchParameters = (new Domain.SearchParameters()).setFilter('passwordPolicyValidationId', entityId);
    } else {
      forceSearchParameters = (new Domain.SearchParameters()).setFilter('passwordPolicyGenerationId', entityId);
    }

    return (
      <Basic.Div>
        <Basic.ContentHeader
          text={ this.i18n('acc:content.passwordPolicy.system.title') }
          icon="component:systems"
          style={{ marginBottom: 0 }}/>

        <Basic.Panel
          className="no-border last"
          rendered={ forceSearchParameters !== null }
          showLoading={ showLoading }>
          <SystemTable
            uiKey="password_policy_system_table"
            columns={[ 'name', 'description', 'disabled', 'readonly' ]}
            manager={ this.systemManager }
            forceSearchParameters={ forceSearchParameters }
            showRowSelection={ false }
            showAddButton={ false }
            filterOpened={ false }
            className="no-margin"/>
        </Basic.Panel>
      </Basic.Div>
    );
  }
}

PasswordPolicySystems.propTypes = {
  entity: PropTypes.object
};
PasswordPolicySystems.defaultProps = {
};

function select(state, component) {
  const { entityId } = component.match.params;
  //
  return {
    entity: passwordPolicyManager.getEntity(state, entityId),
    showLoading: passwordPolicyManager.isShowLoading(state, null, entityId)
  };
}

export default connect(select)(PasswordPolicySystems);
