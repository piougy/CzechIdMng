import React from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
import * as Basic from '../../components/basic';
import AuditIdentityLoginTable from '../audit/identity/AuditIdentityLoginTable';
import { IdentityManager } from '../../redux/data';

const identityManager = new IdentityManager();

class AuditLogin extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  getContentKey() {
    return 'content.audit';
  }

  componentDidMount() {
    const { entityId } = this.props.params;
    this.selectNavigationItems(['identities', 'profile-audit', 'profile-audit-login']);
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
            <AuditIdentityLoginTable
              singleUserMod
              id={identity.id}
              uiKey={`identity-login-audit-table-${identity.id}`}/>
          }
        </Basic.Panel>
      </div>
    );
  }
}

AuditLogin.propTypes = {
};

AuditLogin.defaultProps = {
};

function select(state, component) {
  const { entityId } = component.params;
  return {
    identity: identityManager.getEntity(state, entityId)
  };
}

export default connect(select)(AuditLogin);
