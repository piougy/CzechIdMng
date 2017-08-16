import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
//
import { Basic, Domain, Enums, Managers } from 'czechidm-core';
import { SystemManager } from '../../redux';
import SystemTable from '../system/SystemTable';

/**
* Table with connected systems to this password policy
*/

const passwordPolicyManager = new Managers.PasswordPolicyManager();

class PasswordPolicySystems extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.systemManager = new SystemManager();
  }

  componentDidMount() {
    const { entityId } = this.props.params;
    this.selectNavigationItems(['system', 'password-policies', 'password-policies-systems']);
    this.context.store.dispatch(passwordPolicyManager.fetchEntity(entityId));
  }

  render() {
    const { entityId, entity, showLoading } = this.props.params;

    const forceSearchparameters = new Domain.SearchParameters();

    const validateType = entity && Enums.PasswordPolicyTypeEnum.findSymbolByKey(entity.type) === Enums.PasswordPolicyTypeEnum.VALIDATE;
    if (validateType) {
      forceSearchparameters.setFilter('passwordPolicyValidationId', entityId);
    } else {
      forceSearchparameters.setFilter('passwordPolicyGenerationId', entityId);
    }
console.log(showLoading, entity, validateType);
    return (
      <div>
        <Basic.ContentHeader text={this.i18n('acc:content.passwordPolicy.system.title')} style={{ marginBottom: 0 }}/>

        <Basic.Panel className="no-border last" showLoading={showLoading}>
          <SystemTable uiKey="password_policy_system_table"
            columns={['name', 'description', 'disabled', 'readonly']}
            manager={this.systemManager}
            forceSearchParameters={forceSearchparameters}
            showRowSelection={false}
            showAddButton={false}
            filterOpened={false}/>
        </Basic.Panel>
      </div>
    );
  }
}

PasswordPolicySystems.propTypes = {
  entity: PropTypes.object
};
PasswordPolicySystems.defaultProps = {
};

function select(state, component) {
  const { entityId } = component.params;
  //
  return {
    entity: passwordPolicyManager.getEntity(state, entityId),
    showLoading: passwordPolicyManager.isShowLoading(state, null, entityId)
  };
}

export default connect(select)(PasswordPolicySystems);
