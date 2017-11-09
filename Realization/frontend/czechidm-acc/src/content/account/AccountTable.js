import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import _ from 'lodash';
//
import { Basic, Advanced, Managers, Utils } from 'czechidm-core';
import { AccountManager, SystemEntityManager, SystemManager } from '../../redux';
import AccountTypeEnum from '../../domain/AccountTypeEnum';
import SystemEntityTypeEnum from '../../domain/SystemEntityTypeEnum';

const manager = new AccountManager();
const systemEntityManager = new SystemEntityManager();
const systemManager = new SystemManager();

/**
 * Accounts on target system
 *
 * @author Vít Švanda
 * @author Radek Tomiška
 */
export class AccountTable extends Advanced.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      ...this.state,
      systemEntity: null
    };
  }

  getManager() {
    return manager;
  }

  getUiKey() {
    return this.props.uiKey;
  }

  getContentKey() {
    return 'acc:content.system.accounts';
  }

  getNavigationKey() {
    return 'system-accounts';
  }

  showDetail(entity) {
    if (!Utils.Entity.isNew(entity)) {
      this.context.store.dispatch(this.getManager().fetchPermissions(entity.id, `${this.getUiKey()}-detail`));
    }
    this.setState({
      systemEntity: entity.systemEntity
    }, ()=> {
      if (!Utils.Entity.isNew(entity)) {
        manager.getService().getConnectorObject(entity.id)
        .then(json => {
          this.setState({connectorObject: json});
        })
        .catch(error => {
          this.addError(error);
        });
      }
      super.showDetail(entity, () => {
        this.refs.uid.focus();
      });
    });
  }

  closeDetail() {
    super.closeDetail();
    delete this.state.connectorObject;
  }

  save(entity, event) {
    const formEntity = this.refs.form.getData();
    //
    super.save(formEntity, event);
  }

  afterSave(entity, error) {
    if (!error) {
      this.addMessage({ message: this.i18n('save.success', { name: entity.uid }) });
    }
    super.afterSave(entity, error);
  }

  onChangeSystemEntity(systemEntity) {
    this.setState({
      systemEntity
    });
  }

  _getTableFilter() {
    const { forceSearchParameters } = this.props;
    //
    return (
      <Advanced.Filter onSubmit={this.useFilter.bind(this)}>
      <Basic.AbstractForm ref="filterForm">
        <Basic.Row>
          <Basic.Col lg={ 8 }>
            <Advanced.Filter.TextField
              ref="text"
              placeholder={this.i18n('filter.text.placeholder')}/>
          </Basic.Col>
          <Basic.Col lg={ 4 } className="text-right">
            <Advanced.Filter.FilterButtons cancelFilter={this.cancelFilter.bind(this)}/>
          </Basic.Col>
        </Basic.Row>
        <Basic.Row className="last">
          <Basic.Col lg={ 4 }>
            <Advanced.Filter.EnumSelectBox
              ref="accountType"
              placeholder={ this.i18n('acc:entity.Account.accountType') }
              enum={ AccountTypeEnum }/>
          </Basic.Col>
          <Basic.Col lg={ 4 }>
            <Advanced.Filter.EnumSelectBox
              ref="entityType"
              placeholder={this.i18n('acc:entity.SystemEntity.entityType')}
              enum={ SystemEntityTypeEnum }
              rendered={ !forceSearchParameters.getFilters().has('entityType') }/>
          </Basic.Col>
          <Basic.Col lg={ 4 }>

          </Basic.Col>
        </Basic.Row>
      </Basic.AbstractForm>
    </Advanced.Filter>
   );
  }

  render() {
    const {
      _showLoading,
      uiKey,
      forceSearchParameters,
      forceSystemEntitySearchParameters,
      columns,
      _permissions,
      showAddButton
    } = this.props;
    const { detail, systemEntity, connectorObject } = this.state;
    //
    let systemId = null;
    if (forceSearchParameters.getFilters().has('systemId')) {
      systemId = forceSearchParameters.getFilters().get('systemId');
    }
    //
    return (
      <div>
        <Basic.Confirm ref="confirm-delete" level="danger"/>
        <Advanced.Table
          ref="table"
          uiKey={uiKey}
          manager={this.getManager()}
          forceSearchParameters={forceSearchParameters}
          showRowSelection={Managers.SecurityManager.hasAnyAuthority(['SYSTEM_UPDATE'])}
          rowClass={({rowIndex, data}) => { return (data[rowIndex].inProtection) ? 'disabled' : ''; }}
          actions={
            Managers.SecurityManager.hasAnyAuthority(['SYSTEM_UPDATE'])
            ?
            [{ value: 'delete', niceLabel: this.i18n('action.delete.action'), action: this.onDelete.bind(this), disabled: false }]
            :
            null
          }
          buttons={
            [
              <Basic.Button
                level="success"
                key="add_button"
                className="btn-xs"
                onClick={ this.showDetail.bind(this, { system: systemId, accountType: AccountTypeEnum.findKeyBySymbol(AccountTypeEnum.PERSONAL) })}
                rendered={ showAddButton && Managers.SecurityManager.hasAnyAuthority(['SYSTEM_UPDATE']) }>
                <Basic.Icon type="fa" icon="plus"/>
                {' '}
                {this.i18n('button.add')}
              </Basic.Button>
            ]
          }
          filter={
             this._getTableFilter()
          }
          _searchParameters={ this.getSearchParameters() }>
          <Advanced.Column
            property=""
            header=""
            className="detail-button"
            cell={
              ({ rowIndex, data }) => {
                return (
                  <Advanced.DetailButton
                    title={this.i18n('button.detail')}
                    onClick={this.showDetail.bind(this, data[rowIndex])}/>
                );
              }
            }/>
          <Advanced.Column
            property="accountType"
            rendered={_.includes(columns, 'accountType')}
            header={ this.i18n('acc:entity.Account.accountType') }
            width={ 75 }
            sort
            face="enum"
            enumClass={ AccountTypeEnum } />
          <Advanced.Column
            property="entityType"
            rendered={_.includes(columns, 'entityType')}
            header={ this.i18n('acc:entity.SystemEntity.entityType') }
            width={ 75 }
            sort
            face="enum"
            enumClass={ SystemEntityTypeEnum }/>
          <Advanced.ColumnLink
            to={
              ({ rowIndex, data }) => {
                this.showDetail(data[rowIndex]);
              }
            }
            property="uid"
            header={this.i18n('acc:entity.Account.uid')}
            rendered={_.includes(columns, 'uid')}/>
          <Advanced.Column
            header={this.i18n('acc:entity.System.name')}
            rendered={_.includes(columns, 'system')}
            cell={
              /* eslint-disable react/no-multi-comp */
              ({rowIndex, data}) => {
                return (
                  <Advanced.EntityInfo
                    entityType="system"
                    entityIdentifier={ data[rowIndex]._embedded.systemEntity.system }
                    face="popover" />
                );
              }
            }/>
          <Advanced.Column
            property="inProtection"
            header={this.i18n('acc:entity.Account.inProtection')}
            face="bool"
            rendered={_.includes(columns, 'inProtection')}/>
          <Advanced.Column
            property="endOfProtection"
            header={this.i18n('acc:entity.Account.endOfProtection')}
            face="datetime"
            rendered={_.includes(columns, 'endOfProtection')}/>
          <Advanced.Column property="_embedded.systemEntity.uid"
            rendered={_.includes(columns, 'systemEntity')}
            header={this.i18n('acc:entity.Account.systemEntity')}
            face="text" />
        </Advanced.Table>

        <Basic.Modal
          bsSize="large"
          show={detail.show}
          onHide={this.closeDetail.bind(this)}
          backdrop="static"
          keyboard={!_showLoading}>

          <form onSubmit={this.save.bind(this, {})}>
            <Basic.Modal.Header closeButton={!_showLoading} text={this.i18n('create.header')} rendered={ Utils.Entity.isNew(detail.entity) }/>
            <Basic.Modal.Header closeButton={!_showLoading} text={this.i18n('edit.header', { name: detail.entity.name })} rendered={ !Utils.Entity.isNew(detail.entity) }/>
            <Basic.Modal.Body>
              <Basic.AbstractForm
                ref="form"
                showLoading={_showLoading}
                readOnly={ !manager.canSave(detail.entity, _permissions) }>
                <Basic.SelectBox
                  ref="system"
                  manager={systemManager}
                  label={this.i18n('acc:entity.Account.system')}
                  readOnly={ !Utils.Entity.isNew(detail.entity) || systemId }
                  required/>
                <Basic.TextField
                  ref="uid"
                  label={this.i18n('acc:entity.Account.uid')}
                  required
                  max={1000}/>
                <Basic.SelectBox
                  ref="systemEntity"
                  manager={systemEntityManager}
                  label={this.i18n('acc:entity.Account.systemEntity')}
                  forceSearchParameters={forceSystemEntitySearchParameters}
                  onChange={ this.onChangeSystemEntity.bind(this) }/>
                <Basic.EnumSelectBox
                  ref="accountType"
                  enum={AccountTypeEnum}
                  label={this.i18n('acc:entity.Account.accountType')}
                  required/>
                <Basic.EnumSelectBox
                  ref="entityType"
                  enum={ SystemEntityTypeEnum }
                  label={ this.i18n('acc:entity.SystemEntity.entityType') }
                  hidden={ systemEntity }/>
                <Basic.Checkbox
                  ref="inProtection"
                  label={ this.i18n('acc:entity.Account.inProtection') }>
                </Basic.Checkbox>
                <Basic.DateTimePicker
                  mode="datetime"
                  ref="endOfProtection"
                  label={ this.i18n('acc:entity.Account.endOfProtection') }/>

              </Basic.AbstractForm>

              <Basic.ContentHeader text={ this.i18n('acc:entity.SystemEntity.attributes') } rendered={ !Utils.Entity.isNew(detail.entity) }/>

              <Basic.Table
                showLoading = {!connectorObject && !this.state.hasOwnProperty('connectorObject')}
                data={connectorObject ? connectorObject.attributes : null}
                noData={this.i18n('component.basic.Table.noData')}
                className="table-bordered"
                rendered={ !Utils.Entity.isNew(detail.entity) }>
                <Basic.Column property="name" header={this.i18n('label.property')}/>
                <Basic.Column property="values" header={this.i18n('label.value')}/>
              </Basic.Table>

            </Basic.Modal.Body>

            <Basic.Modal.Footer>
              <Basic.Button
                level="link"
                onClick={this.closeDetail.bind(this)}
                showLoading={_showLoading}>
                {this.i18n('button.close')}
              </Basic.Button>
              <Basic.Button
                type="submit"
                level="success"
                rendered={manager.canSave(detail.entity, _permissions)}
                showLoading={_showLoading}
                showLoadingIcon
                showLoadingText={this.i18n('button.saving')}>
                {this.i18n('button.save')}
              </Basic.Button>
            </Basic.Modal.Footer>
          </form>
        </Basic.Modal>
      </div>
    );
  }
}

AccountTable.propTypes = {
  uiKey: PropTypes.string.isRequired,
  forceSearchParameters: PropTypes.object.isRequired,
  forceSystemEntitySearchParameters: PropTypes.object,
  /**
   * Button for create user will be shown
   */
  showAddButton: PropTypes.bool,
  //
  _showLoading: PropTypes.bool
};
AccountTable.defaultProps = {
  columns: ['accountType', 'entityType', 'uid', 'system', 'inProtection', 'endOfProtection', 'systemEntity'],
  showAddButton: true,
  _showLoading: false,
};

function select(state, component) {
  const {uiKey} = component;
  return {
    _showLoading: Utils.Ui.isShowLoading(state, `${uiKey}-detail`),
    _searchParameters: Utils.Ui.getSearchParameters(state, uiKey),
    _permissions: Utils.Permission.getPermissions(state, `${uiKey}-detail`)
  };
}

export default connect(select)(AccountTable);
