import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
import _ from 'lodash';
//
import { Basic, Advanced, Domain, Managers, Utils } from 'czechidm-core';
import { AccountManager, SystemEntityManager, SystemManager } from '../../redux';
import AccountTypeEnum from '../../domain/AccountTypeEnum';
import SystemEntityTypeEnum from '../../domain/SystemEntityTypeEnum';

const uiKey = 'system-accounts-table';
const manager = new AccountManager();
const systemEntityManager = new SystemEntityManager();
const systemManager = new SystemManager();

class SystemAccountsContent extends Basic.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
  }

  getManager() {
    return manager;
  }

  getUiKey() {
    return uiKey;
  }

  getContentKey() {
    return 'acc:content.system.accounts';
  }

  componentDidMount() {
    this.selectNavigationItems(['sys-systems', 'system-accounts']);
  }

  showDetail(entity) {
    const entityFormData = _.merge({}, entity, {
      system: entity._embedded && entity._embedded.system ? entity._embedded.system.id : this.props.params.entityId,
      systemEntity: entity._embedded && entity._embedded.systemEntity ? entity._embedded.systemEntity.id : null
    });
    super.showDetail(entityFormData, () => {
      this.refs.uid.focus();
    });
  }

  save(entity, event) {
    const formEntity = this.refs.form.getData();
    formEntity.system = systemManager.getSelfLink(formEntity.system);
    formEntity.systemEntity = systemEntityManager.getSelfLink(formEntity.systemEntity);
    //
    super.save(formEntity, event);
  }

  afterSave(entity, error) {
    if (!error) {
      this.addMessage({ message: this.i18n('save.success', { name: entity.uid }) });
    }
    super.afterSave(entity, error);
  }

  render() {
    const { entityId } = this.props.params;
    const { _showLoading } = this.props;
    const { detail } = this.state;
    const forceSearchParameters = new Domain.SearchParameters().setFilter('systemId', entityId);
    const forceSystemEntitySearchParameters = new Domain.SearchParameters().setFilter('systemId', entityId).setFilter('entityType', SystemEntityTypeEnum.findKeyBySymbol(SystemEntityTypeEnum.IDENTITY));

    return (
      <div>
        <Helmet title={this.i18n('title')} />
        <Basic.Confirm ref="confirm-delete" level="danger"/>

        <Basic.ContentHeader style={{ marginBottom: 0 }}>
          <span dangerouslySetInnerHTML={{ __html: this.i18n('header') }}/>
        </Basic.ContentHeader>

        <Basic.Panel className="no-border last">
          <Advanced.Table
            ref="table"
            uiKey={uiKey}
            manager={this.getManager()}
            forceSearchParameters={forceSearchParameters}
            showRowSelection={Managers.SecurityManager.hasAnyAuthority(['SYSTEM_WRITE'])}
            actions={
              Managers.SecurityManager.hasAnyAuthority(['SYSTEM_WRITE'])
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
                  rendered={Managers.SecurityManager.hasAnyAuthority(['ROLE_WRITE'])}>
                  <Basic.Icon type="fa" icon="plus"/>
                  {' '}
                  {this.i18n('button.add')}
                </Basic.Button>
              ]
            }
            filter={
              <Advanced.Filter onSubmit={this.useFilter.bind(this)}>
                <Basic.AbstractForm ref="filterForm" className="form-horizontal">
                  <Basic.Row className="last">
                    <div className="col-lg-4">
                      <Advanced.Filter.EnumSelectBox
                        ref="accountType"
                        label={this.i18n('acc:entity.Account.accountType')}
                        placeholder={this.i18n('acc:entity.Account.accountType')}
                        enum={AccountTypeEnum}/>
                    </div>
                    <div className="col-lg-4">
                      <Advanced.Filter.TextField
                        ref="uid"
                        label={this.i18n('filter.uid.label')}
                        placeholder={this.i18n('filter.uid.placeholder')}/>
                    </div>
                    <div className="col-lg-4 text-right">
                      <Advanced.Filter.FilterButtons cancelFilter={this.cancelFilter.bind(this)}/>
                    </div>
                  </Basic.Row>
                </Basic.AbstractForm>
              </Advanced.Filter>
            }>
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
            <Advanced.Column property="accountType" header={this.i18n('acc:entity.Account.accountType')} width="75px" sort face="enum" enumClass={AccountTypeEnum} />
            <Advanced.ColumnLink
              to={
                ({ rowIndex, data }) => {
                  this.showDetail(data[rowIndex]);
                }
              }
              property="uid"
              header={this.i18n('acc:entity.Account.uid')}/>
            <Advanced.Column property="_embedded.systemEntity.uid" header={this.i18n('acc:entity.Account.systemEntity')} face="text" />
          </Advanced.Table>
        </Basic.Panel>

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
              <Basic.AbstractForm ref="form" showLoading={_showLoading} className="form-horizontal">
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
                  forceSearchParameters={forceSystemEntitySearchParameters}/>
                <Basic.EnumSelectBox
                  ref="accountType"
                  enum={AccountTypeEnum}
                  label={this.i18n('acc:entity.Account.accountType')}
                  required/>
              </Basic.AbstractForm>

              {/*
              <Basic.ContentHeader>
                Identity nalinkované na účet
              </Basic.ContentHeader>
              TODO: accounts*/}
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

SystemAccountsContent.propTypes = {
  _showLoading: PropTypes.bool,
};
SystemAccountsContent.defaultProps = {
  _showLoading: false,
};

function select(state) {
  return {
    _showLoading: Utils.Ui.isShowLoading(state, `${uiKey}-detail`),
  };
}

export default connect(select)(SystemAccountsContent);
