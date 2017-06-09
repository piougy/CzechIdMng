import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
//
import { Basic, Domain } from 'czechidm-core';
import { SystemManager } from '../../redux';
import SystemTable from '../system/SystemTable';

/**
* Table with connected systems to this password policy
*/

class PasswordPolicySystems extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.systemManager = new SystemManager();
  }

  componentDidMount() {
    this.selectNavigationItems(['system', 'password-policies', 'password-policies-systems']);
  }

  componentWillReceiveProps(nextProps) {
    if (!this.props.entity || nextProps.entity.id !== this.props.entity.id || nextProps.entity.id !== this.refs.form.getData().id) {
      this._initForm(nextProps.entity);
    }
  }
  render() {
    const { entityId } = this.props.params;
    return (
      <div>
        <Basic.ContentHeader text={this.i18n('acc:content.passwordPolicy.system.title')} style={{ marginBottom: 0 }}/>

        <Basic.Panel className="no-border last">
          <SystemTable uiKey="password_policy_system_table"
            columns={['name', 'description', 'disabled', 'readonly']}
            manager={this.systemManager}
            forceSearchParameters={new Domain.SearchParameters().setFilter('passwordPolicyId', entityId)}
            showRowSelection={false}
            showAddButton={false}
            filterOpened={false}/>
        </Basic.Panel>
      </div>
    );
  }
}

PasswordPolicySystems.propTypes = {
  entity: PropTypes.object,
  uiKey: PropTypes.string.isRequired,
  isNew: PropTypes.bool
};
PasswordPolicySystems.defaultProps = {
};

function select() {
  return {
  };
}

export default connect(select)(PasswordPolicySystems);
