import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import uuid from 'uuid';
import _ from 'lodash';
//
import * as Basic from '../../../components/basic';
import * as Advanced from '../../../components/advanced';
import * as Utils from '../../../utils';
import { IdentityContractManager, TreeTypeManager, TreeNodeManager, SecurityManager } from '../../../redux';
import ManagersInfo from '../ManagersInfo';
import ContractStateEnum from '../../../enums/ContractStateEnum';

const manager = new IdentityContractManager(); // default manager

/**
 * Identity contracts
 *
 * @author Radek Tomiška
 * @since 9.4.0
 */
export class ContractTable extends Advanced.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
    this.treeTypeManager = new TreeTypeManager();
    this.treeNodeManager = new TreeNodeManager();
  }

  getManager() {
    return this.props.manager;
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
    const identityIdentifier = this._getIdentityIdentifier();
    if (entity.id === undefined) {
      const uuidId = uuid.v1();
      this.context.router.push(`/identity/${encodeURIComponent(identityIdentifier)}/identity-contract/${uuidId}/new?new=1`);
    } else {
      this.context.router.push(`/identity/${encodeURIComponent(identityIdentifier)}/identity-contract/${entity.id}/detail`);
    }
  }

  showGuarantees(entity, event) {
    if (event) {
      event.preventDefault();
    }
    this.context.router.push(`/identity/${encodeURIComponent(entity.identity)}/identity-contract/${entity.id}/guarantees`);
  }

  reload() {
    this.refs.table.getWrappedInstance().reload();
  }

  _getIdentityIdentifier() {
    const { forceSearchParameters } = this.props;
    if (!forceSearchParameters || !forceSearchParameters.getFilters().has('identity')) {
      return null;
    }
    return forceSearchParameters.getFilters().get('identity');
  }

  render() {
    const {
      columns,
      forceSearchParameters,
      className,
      showAddButton,
      showDetailButton
    } = this.props;
    //
    return (
      <div>
        <Basic.Confirm ref="confirm-delete" level="danger"/>
        <Advanced.Table
          ref="table"
          uiKey={ this.getUiKey() }
          manager={ this.getManager() }
          forceSearchParameters={ forceSearchParameters }
          rowClass={({rowIndex, data}) => { return data[rowIndex].state ? 'disabled' : Utils.Ui.getRowClass(data[rowIndex]); }}
          showRowSelection={ SecurityManager.hasAuthority('IDENTITYCONTRACT_DELETE') }
          actions={
            [
              { value: 'delete', niceLabel: this.i18n('action.delete.action'), action: this.onDelete.bind(this) },
            ]
          }
          buttons={
            [
              <Basic.Button
                level="success"
                className="btn-xs"
                onClick={ this.showDetail.bind(this, {}) }
                rendered={ showAddButton && this._getIdentityIdentifier() && SecurityManager.hasAuthority('IDENTITYCONTRACT_CREATE') }>
                <Basic.Icon value="fa:plus"/>
                {' '}
                {this.i18n('button.add')}
              </Basic.Button>
            ]
          }
          _searchParameters={ this.getSearchParameters() }
          className={ className }>
          <Basic.Column
            className="detail-button"
            rendered={ showDetailButton }
            cell={
              ({rowIndex, data}) => {
                return (
                  <Advanced.DetailButton onClick={ this.showDetail.bind(this, data[rowIndex]) }/>
                );
              }
            }/>
          <Advanced.Column
            property="main"
            header={ <Basic.Icon value="component:main-contract"/> }
            title={ this.i18n('entity.IdentityContract.main.help') }
            face="bool"
            width={ 15 }
            rendered={ _.includes(columns, 'main') }/>
          <Advanced.Column
            property="position"
            header={this.i18n('entity.IdentityContract.position')}
            width={ 200 }
            sort
            rendered={ _.includes(columns, 'position') }/>
          <Basic.Column
            property="workPosition"
            header={this.i18n('entity.IdentityContract.workPosition')}
            width={ 350 }
            rendered={ _.includes(columns, 'workPosition') }
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
          <Basic.Column
            property="guarantee"
            header={ <span title={this.i18n('entity.IdentityContract.managers.title')}>{this.i18n('entity.IdentityContract.managers.label')}</span> }
            rendered={ _.includes(columns, 'guarantee') }
            cell={
              ({ rowIndex, data }) => {
                return (
                  <ManagersInfo
                    managersFor={ data[rowIndex].identity }
                    identityContractId={ data[rowIndex].id }
                    detailLink={ this.showGuarantees.bind(this, data[rowIndex]) }/>
                );
              }
            }
          />
          <Advanced.Column
            property="validFrom"
            header={ this.i18n('entity.IdentityContract.validFrom') }
            face="date"
            sort
            rendered={ _.includes(columns, 'validFrom') }
          />
          <Advanced.Column
            property="validTill"
            header={ this.i18n('entity.IdentityContract.validTill') }
            face="date"
            sort
            rendered={ _.includes(columns, 'validTill') }/>
          <Advanced.Column
            property="state"
            header={ this.i18n('entity.IdentityContract.state.label') }
            face="enum"
            enumClass={ ContractStateEnum }
            width={ 100 }
            sort
            rendered={ _.includes(columns, 'state') }/>
          <Advanced.Column
            property="externe"
            header={ this.i18n('entity.IdentityContract.externe') }
            face="bool"
            width={ 100 }
            sort
            rendered={ _.includes(columns, 'externe') }/>
        </Advanced.Table>
      </div>
    );
  }
}

ContractTable.propTypes = {
  uiKey: PropTypes.string.isRequired,
  /**
   * Identity contract manager
   */
  manager: PropTypes.object,
  /**
   * Rendered columns - see table columns above
   *
   * TODO: move to advanced table and add column sorting
   */
  columns: PropTypes.arrayOf(PropTypes.string),
  /**
   * "Hard filters"
   */
  forceSearchParameters: PropTypes.object,
  /**
   * Button for create user will be shown
   */
  showAddButton: PropTypes.bool,
  /**
   * Button for show entity detail
   */
  showDetailButton: PropTypes.bool
};

ContractTable.defaultProps = {
  manager,
  columns: ['main', 'position', 'workPosition', 'guarantee', 'validFrom', 'validTill', 'state', 'externe'],
  showAddButton: true,
  showDetailButton: true
};

function select(state, component) {
  return {
    _searchParameters: Utils.Ui.getSearchParameters(state, component.uiKey),
    i18nReady: state.config.get('i18nReady')
  };
}

export default connect(select, null, null, { withRef: true })(ContractTable);
