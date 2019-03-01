import React from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
import * as Basic from '../../components/basic';
import AuditIdentityPasswordChangeTable from '../audit/identity/AuditIdentityPasswordChangeTable';
import { IdentityManager } from '../../redux/data';

const identityManager = new IdentityManager();

class AuditPasswordChange extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  getContentKey() {
    return 'content.audit';
  }

  componentDidMount() {
    const { entityId } = this.props.params;
    this.selectNavigationItems(['identities', 'profile-audit', 'profile-audit-password-change']);
    this.context.store.dispatch(identityManager.fetchEntity(entityId));
  }

  render() {
    const { identity } = this.props;
    return (
      <div>
        <Helmet title={this.i18n('title')} />
        <Basic.Panel className="no-border last">
          {
            !identity
            ||
            <AuditIdentityPasswordChangeTable
              singleUserMod
              id={identity.id}
              uiKey={`identity-password-change-audit-table-${identity.id}`}/>
          }
        </Basic.Panel>
      </div>
    );
  }
}

AuditPasswordChange.propTypes = {
};

AuditPasswordChange.defaultProps = {
};

function select(state, component) {
  const { entityId } = component.params;
  return {
    identity: identityManager.getEntity(state, entityId)
  };
}

export default connect(select)(AuditPasswordChange);
