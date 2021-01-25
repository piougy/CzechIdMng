import React from 'react';
import { connect } from 'react-redux';
//
import * as Basic from '../../../components/basic';
import * as Domain from '../../../domain';
import TokenTable from '../../token/TokenTable';
import { IdentityManager } from '../../../redux';

const identityManager = new IdentityManager();

/**
 * Token agenda.
 *
 * @author Radek Tomiška
 * @since 10.8.0
 */
class IdentityTokens extends Basic.AbstractContent {

  getContentKey() {
    return 'content.tokens';
  }

  getNavigationKey() {
    return 'profile-tokens';
  }

  render() {
    const { identity } = this.props;
    //
    if (!identity) {
      return (
        <Basic.Loading isStatic show/>
      );
    }
    //
    const forceSearchParameters = new Domain.SearchParameters()
      .setFilter('ownerType', 'eu.bcvsolutions.idm.core.model.entity.IdmIdentity')
      .setFilter('ownerId', identity.id);
    //
    return (
      <Basic.Div>
        { this.renderContentHeader({ style: { marginBottom: 0, paddingRight: 15, paddingLeft: 15, paddingTop: 15 } }) }

        <Basic.Alert level="info" text={ this.i18n('content.tokens.help', { escape: false }) } style={{ marginBottom: 0 }}/>

        <TokenTable
          uiKey={ `identity-token-table-${ identity.id }` }
          forceSearchParameters={ forceSearchParameters }
          match={ this.props.match }
          filterOpened={ false }/>

      </Basic.Div>
    );
  }
}

function select(state, component) {
  return {
    identity: identityManager.getEntity(state, component.match.params.entityId)
  };
}

export default connect(select)(IdentityTokens);
