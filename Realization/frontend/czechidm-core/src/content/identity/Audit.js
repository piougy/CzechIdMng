import React from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
import * as Basic from '../../components/basic';
import AuditIdentityTable from '../audit/identity/AuditIdentityTable';
import { IdentityManager } from '../../redux/data';

const identityManager = new IdentityManager();

/**
 *
 * @author Ondřej Kopr
 */
class Audit extends Basic.AbstractContent {

  getContentKey() {
    return 'content.audit';
  }

  showDetail(revId, entityIde) {
    // TODO: this.context.router.push set only rev id, fetchEntity isn't necessary. this.props.params not working.
    this.context.store.dispatch(identityManager.fetchEntity(entityIde, null, (identity) => {
      this.context.router.push('/identity/' + identity.username + '/revision/' + revId);
    }));
  }

  componentDidMount() {
    const { entityId } = this.props.params;
    this.selectNavigationItems(['identities', 'profile-audit', 'profile-audit-profile']);
    this.context.store.dispatch(identityManager.fetchEntity(entityId));
  }

  render() {
    const { identity } = this.props;
    return (
      <div>
        <Helmet title={this.i18n('title')} />
        <Basic.Confirm ref="confirm-delete" level="danger"/>
        {
          !identity
          ||
          <AuditIdentityTable
            singleUserMod
            id={identity.id}
            uiKey="identity-audit-table"/>
        }
      </div>
    );
  }
}

Audit.propTypes = {
};

Audit.defaultProps = {
};

function select(state, component) {
  const { entityId } = component.params;
  return {
    identity: identityManager.getEntity(state, entityId)
  };
}

export default connect(select)(Audit);
