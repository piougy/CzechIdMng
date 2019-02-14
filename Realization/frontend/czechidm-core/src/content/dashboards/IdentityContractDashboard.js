import React from 'react';
import { connect } from 'react-redux';
import { Link } from 'react-router';
import _ from 'lodash';
//
import * as Basic from '../../components/basic';
import * as Domain from '../../domain';
import { SecurityManager, IdentityManager } from '../../redux';
import ContractTableComponent, { ContractTable } from '../identity/contract/ContractTable';

const identityManager = new IdentityManager();

/**
 * Identity contracts
 *
 * @author Radek Tomiška
 * @since 9.4.0
 */
class IdentityContractDashboard extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  render() {
    const { entityId, permissions, identity } = this.props;
    //
    if (!identity || !SecurityManager.hasAuthority('IDENTITYCONTRACT_READ') ) {
      return null;
    }
    const forceSearchParameters = new Domain.SearchParameters().setFilter('identity', identity.id);
    const columns = _.difference(ContractTable.defaultProps.columns, ['guarantee']);
    //
    return (
      <div>
        <Basic.ContentHeader
          icon="component:contracts"
          text={ this.i18n('content.identity.identityContracts.header') }
          buttons={
            identityManager.canRead(identity, permissions)
            ?
              [
                <Link to={ `/identity/${ encodeURIComponent(entityId) }/contracts` }>
                  <Basic.Icon value="fa:angle-double-right"/>
                  {' '}
                  { this.i18n('component.advanced.IdentityInfo.link.detail.label') }
                </Link>
              ]
            :
            null
          }/>
        <Basic.Panel>
          <ContractTableComponent
            ref="table"
            uiKey={ `identity-contracts-${ identity.id }` }
            columns={ columns }
            forceSearchParameters={ forceSearchParameters }
            showAddButton={ false }/>
        </Basic.Panel>
      </div>
    );
  }
}

function select(state) {
  //
  return {
    i18nReady: state.config.get('i18nReady')
  };
}

export default connect(select)(IdentityContractDashboard);
