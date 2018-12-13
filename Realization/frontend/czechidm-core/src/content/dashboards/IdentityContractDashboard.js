import React from 'react';
import { connect } from 'react-redux';
import { Link } from 'react-router';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import * as Domain from '../../domain';
import * as Utils from '../../utils';
import { SecurityManager, IdentityManager, IdentityContractManager } from '../../redux';
import ContractStateEnum from '../../enums/ContractStateEnum';

const identityManager = new IdentityManager();
const identityContractManager = new IdentityContractManager();

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
    //
    return (
      <div>
        <Basic.ContentHeader
          icon="fa:building"
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
            : null
          }/>
        <Basic.Panel>
          {/* FIXME: active operations */}
          {/* FIXME: contract table component */}
          <Advanced.Table
            ref="contract-table"
            uiKey={ 'todo-identity-contracts-key' }
            manager={ identityContractManager }
            forceSearchParameters={ new Domain.SearchParameters().setFilter('identity', entityId) }
            rowClass={({rowIndex, data}) => { return data[rowIndex].state ? 'disabled' : Utils.Ui.getRowClass(data[rowIndex]); }}>
            <Advanced.Column
              header={ this.i18n('entity.IdentityContract.main.short') }
              title={ this.i18n('entity.IdentityContract.main.help') }
              property="main"
              face="bool"/>
            <Advanced.Column
              property="position"
              header={this.i18n('entity.IdentityContract.position')}
              width={ 200 }
              sort/>
            <Basic.Column
              property="workPosition"
              header={this.i18n('entity.IdentityContract.workPosition')}
              width={ 350 }
              cell={
                ({ rowIndex, data }) => {
                  return (
                    <span>
                      {
                        data[rowIndex]._embedded && data[rowIndex]._embedded.workPosition
                        ?
                        <Advanced.EntityInfo
                          entity={ data[rowIndex]._embedded.workPosition }
                          entityType="treeNode"
                          entityIdentifier={ data[rowIndex].workPosition }
                          face="popover" />
                        :
                        null
                      }
                    </span>
                  );
                }
              }
            />
            <Advanced.Column
              property="validFrom"
              header={this.i18n('entity.IdentityContract.validFrom')}
              face="date"
              sort
            />
            <Advanced.Column
              property="validTill"
              header={this.i18n('entity.IdentityContract.validTill')}
              face="date"
              sort/>
            <Advanced.Column
              property="state"
              header={this.i18n('entity.IdentityContract.state.label')}
              face="enum"
              enumClass={ ContractStateEnum }
              width={100}
              sort/>
            <Advanced.Column
              property="externe"
              header={this.i18n('entity.IdentityContract.externe')}
              face="bool"
              width={100}
              sort/>
          </Advanced.Table>
        </Basic.Panel>
      </div>
    );
  }
}

function select() {
  //
  return {
  };
}

export default connect(select)(IdentityContractDashboard);
