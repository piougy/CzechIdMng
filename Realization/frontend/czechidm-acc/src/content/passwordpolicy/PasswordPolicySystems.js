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
      <div className="tab-pane-panel-body">
          <Basic.PanelHeader>
            <h2>
              <span>{this.i18n('acc:content.passwordPolicy.system.title')}</span>
            </h2>
            <div className="clearfix"></div>
          </Basic.PanelHeader>
          <SystemTable uiKey="password_policy_system_table"
            columns={['name', 'description', 'disabled', 'readonly']}
            manager={this.systemManager}
            forceSearchParameters={new Domain.SearchParameters().setFilter('passwordPolicyId', entityId)}
            showAddButton={false}
            filterOpened={false}/>
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
