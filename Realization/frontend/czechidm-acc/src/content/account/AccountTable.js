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
 */
class AccountTable extends Advanced.AbstractTableContent {

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
      manager.getService().getConnectorObject(entity.id)
      .then(json => {
        this.setState({connectorObject: json});
      })
      .catch(error => {
        this.addError(error);
      });
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
              enum={ SystemEntityTypeEnum }/>
          </Basic.Col>
          <Basic.Col lg={ 4 }>

          </Basic.Col>
        </Basic.Row>
      </Basic.AbstractForm>
    </Advanced.Filter>
   );
  }

  render() {
    const { _showLoading, uiKey, forceSearchParameters, forceSystemEntitySearchParameters, columns, showFilter, _permissions} = this.props;
    const { detail, systemEntity, connectorObject } = this.state;
    let tableFilter = null;
    if (showFilter) {
      tableFilter = this._getTableFilter();
    }
    return (
      <div>
          <Advanced.Table
            ref="table"
            uiKey={uiKey}
            manager={this.getManager()}
            showFilter={showFilter}
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
                  onClick={this.showDetail.bind(this, { accountType: AccountTypeEnum.findKeyBySymbol(AccountTypeEnum.PERSONAL) })}
                  rendered={Managers.SecurityManager.hasAnyAuthority(['SYSTEM_UPDATE'])}>
                  <Basic.Icon type="fa" icon="plus"/>
                  {' '}
                  {this.i18n('button.add')}
                </Basic.Button>
              ]
            }
            filter={
              tableFilter
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
              header={this.i18n('acc:entity.Account.uid')}/>
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
              face="bool" />
            <Advanced.Column
              property="endOfProtection"
              header={this.i18n('acc:entity.Account.endOfProtection')}
              face="datetime" />
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
            <Basic.Modal.Header closeButton={!_showLoading} text={this.i18n('create.header')} rendered={detail.entity.id === undefined}/>
            <Basic.Modal.Header closeButton={!_showLoading} text={this.i18n('edit.header', { name: detail.entity.name })} rendered={detail.entity.id !== undefined}/>
            <Basic.Modal.Body>
              <Basic.AbstractForm
                ref="form"
                showLoading={_showLoading}
                readOnly={ !manager.canSave(detail.entity, _permissions) }>
                <Basic.SelectBox
                  ref="system"
                  manager={systemManager}
                  label={this.i18n('acc:entity.Account.system')}
                  readOnly
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
              </Basic.AbstractForm>
              <Basic.Row>
                <Basic.Col lg={ 12 }>
                  <h3 style={{ margin: '0 0 10px 0', padding: 0, borderBottom: '1px solid #ddd' }}>{this.i18n('acc:entity.SystemEntity.attributes')}</h3>
                  <Basic.Table
                    showLoading = {!connectorObject && !this.state.hasOwnProperty('connectorObject')}
                    data={connectorObject ? connectorObject.attributes : null}
                    noData={this.i18n('component.basic.Table.noData')}
                    className="table-bordered">
                    <Basic.Column property="name" header={this.i18n('label.property')}/>
                    <Basic.Column property="values" header={this.i18n('label.value')}/>
                  </Basic.Table>
                </Basic.Col>
              </Basic.Row>
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
  _showLoading: PropTypes.bool,
  uiKey: PropTypes.string,
  forceSearchParameters: PropTypes.object,
  forceSystemEntitySearchParameters: PropTypes.object
};
AccountTable.defaultProps = {
  columns: ['entityType', 'accountType', 'systemEntity'],
  showFilter: false,
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
