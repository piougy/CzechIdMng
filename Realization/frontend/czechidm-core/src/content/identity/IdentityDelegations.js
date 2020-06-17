import React from 'react';
import { connect } from 'react-redux';
//
import * as Basic from '../../components/basic';
import { IdentityManager, DelegationDefinitionManager } from '../../redux';
import SearchParameters from '../../domain/SearchParameters';
import DelegationDefinitionTable from '../delegation/DelegationDefinitionTable';

const identityManager = new IdentityManager();
const delegationDefinitionManager = new DelegationDefinitionManager();

/**
 * Identity's delegations
 *
 * @author Vít Švanda
 */
class IdentityDelegations extends Basic.AbstractContent {

  getManager() {
    return identityManager;
  }

  getContentKey() {
    return 'content.delegation-definitions';
  }

  getNavigationKey() {
    return 'profile-delegations';
  }

  render() {
    const {identity} = this.props;
    if (!identity) {
      return null;
    }
    const forceSearchParameters = new SearchParameters()
      .setFilter('delegatorId', identity.id);
    return (
      <div className="tab-pane-table-body">
        <Basic.Row rendered={delegationDefinitionManager.canSave()} style={{paddingTop: 15}}>
          <Basic.Col lg={ 6 }>
            <Basic.Alert
              level="success"
              title={ this.i18n('button.add-delegation.header') }
              text={ this.i18n('button.add-delegation.text') }
              className="no-margin"
              buttons={[
                <Basic.Button
                  icon="fa:plus"
                  level="success"
                  onClick={ () => this.refs.delegatorTable.showDetail({ }) }
                  title={ this.i18n('button.add-delegation.title.tooltip') }>
                  { this.i18n('button.add-delegation.label') }
                </Basic.Button>
              ]}/>
          </Basic.Col>
        </Basic.Row>
        <Basic.Panel className="no-border">
          <Basic.ContentHeader
            icon="fa:dolly"
            text={ this.i18n('identity-is-delegator') }
            style={{ marginBottom: 15, paddingRight: 15, paddingLeft: 15, paddingTop: 15 }}/>
          <DelegationDefinitionTable
            ref="delegatorTable"
            uiKey="delegation-definition-delegator-identity-table"
            delegator={identity}
            columns={['delegate', 'type', 'validFrom', 'validTill', 'description']}
            filterOpened={false}
            forceSearchParameters={ forceSearchParameters }
            className="no-margin"/>
        </Basic.Panel>
        <Basic.Panel className="no-border">
          <Basic.ContentHeader
            icon="fa:dolly"
            text={ this.i18n('identity-is-delegate') }
            style={{ marginBottom: 15, paddingRight: 15, paddingLeft: 15, paddingTop: 15 }}/>
          <DelegationDefinitionTable
            uiKey="delegation-definition-delegate-identity-table"
            delegator={identity}
            readOnly
            columns={['delegator', 'type', 'validFrom', 'validTill', 'description']}
            filterOpened={false}
            forceSearchParameters={ new SearchParameters()
              .setFilter('delegateId', identity.id) }
            className="no-margin"/>
        </Basic.Panel>
      </div>
    );
  }
}

function select(state, component) {
  const { entityId } = component.match.params;
  return {
    identity: identityManager.getEntity(state, entityId)
  };
}

export default connect(select)(IdentityDelegations);
