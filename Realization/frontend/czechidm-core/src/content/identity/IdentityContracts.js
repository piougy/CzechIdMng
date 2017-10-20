import React from 'react';
import uuid from 'uuid';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import * as Utils from '../../utils';
import { IdentityContractManager, TreeTypeManager, TreeNodeManager, SecurityManager } from '../../redux';
import SearchParameters from '../../domain/SearchParameters';
import ManagersInfo from './ManagersInfo';
import ContractStateEnum from '../../enums/ContractStateEnum';

const uiKey = 'identity-contracts';

/**
 * Identity's work positions - reference to tree structures and garants
 *
 * @author Radek Tomiška
 */
export default class IdentityContracts extends Advanced.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
    this.identityContractManager = new IdentityContractManager();
    this.treeTypeManager = new TreeTypeManager();
    this.treeNodeManager = new TreeNodeManager();
  }

  getUiKey() {
    const { entityId } = this.props.params;
    //
    return `${uiKey}-${entityId}`;
  }

  getManager() {
    return this.identityContractManager;
  }

  getContentKey() {
    return 'content.identity.identityContracts';
  }

  getNavigationKey() {
    return 'profile-contracts';
  }

  showDetail(entity, event) {
    if (event) {
      event.preventDefault();
    }
    //
    const { entityId } = this.props.params;
    if (entity.id === undefined) {
      const uuidId = uuid.v1();
      this.context.router.push(`/identity/${encodeURIComponent(entityId)}/identity-contract/${uuidId}/new?new=1`);
    } else {
      this.context.router.push(`/identity/${encodeURIComponent(entityId)}/identity-contract/${entity.id}/detail`);
    }
  }

  render() {
    const { entityId } = this.props.params;
    //
    return (
      <div>
        <Basic.Confirm ref="confirm-delete" level="danger"/>

        { this.renderContentHeader({ style: { marginBottom: 0 } }) }

        <Basic.Panel className="no-border">
          <Advanced.Table
            ref="table"
            uiKey={ this.getUiKey() }
            manager={ this.identityContractManager }
            forceSearchParameters={ new SearchParameters().setFilter('identity', entityId) }
            rowClass={({rowIndex, data}) => { return data[rowIndex].state ? 'disabled' : Utils.Ui.getRowClass(data[rowIndex]); }}
            showRowSelection={ SecurityManager.hasAuthority('IDENTITYCONTRACT_DELETE') }
            actions={
              [
                { value: 'delete', niceLabel: this.i18n('action.delete.action'), action: this.onDelete.bind(this) },
              ]
            }
            buttons={
              [
                <Basic.Button level="success" className="btn-xs" onClick={this.showDetail.bind(this, {})} rendered={ SecurityManager.hasAuthority('IDENTITYCONTRACT_CREATE') }>
                  <Basic.Icon value="fa:plus"/>
                  {' '}
                  {this.i18n('button.add')}
                </Basic.Button>
              ]
            }>
            <Basic.Column
              className="detail-button"
              cell={
                ({rowIndex, data}) => {
                  return (
                    <Advanced.DetailButton onClick={this.showDetail.bind(this, data[rowIndex])}/>
                  );
                }
              }/>
            <Advanced.Column
              property="main"
              header={this.i18n('entity.IdentityContract.main.label')}
              face="bool"
              width={75}
              sort/>
            <Basic.Column
              property="workPosition"
              header={this.i18n('entity.IdentityContract.workPosition')}
              width="175px"
              cell={
                ({ rowIndex, data }) => {
                  return (
                    <span>
                      {
                        data[rowIndex]._embedded && data[rowIndex]._embedded.workPosition
                        ?
                        this.treeNodeManager.getNiceLabel(data[rowIndex]._embedded.workPosition)
                        :
                        data[rowIndex].position
                      }
                    </span>
                  );
                }
              }
            />
            <Basic.Column
              property="treeType"
              header={this.i18n('entity.IdentityContract.treeType')}
              width="175px"
              cell={
                ({ rowIndex, data }) => {
                  return (
                    <span>
                      {
                        !data[rowIndex]._embedded || !data[rowIndex]._embedded.workPosition || !data[rowIndex]._embedded.workPosition._embedded
                        ||
                        this.treeTypeManager.getNiceLabel(data[rowIndex]._embedded.workPosition._embedded.treeType)
                      }
                    </span>
                  );
                }
              }
            />
            <Basic.Column
              property="guarantee"
              header={<span title={this.i18n('entity.IdentityContract.managers.title')}>{this.i18n('entity.IdentityContract.managers.label')}</span>}
              cell={
                ({ rowIndex, data }) => {
                  return (
                    <ManagersInfo managersFor={entityId} identityContractId={data[rowIndex].id}/>
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
              property="disabled"
              header={this.i18n('entity.IdentityContract.disabled.label')}
              face="bool"
              width={100}
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
