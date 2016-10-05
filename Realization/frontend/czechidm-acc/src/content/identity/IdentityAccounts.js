import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
import _ from 'lodash';
//
import { Basic, Advanced, Domain, Managers, Utils } from 'czechidm-core';
import { IdentityAccountManager, AccountManager } from '../../redux';
import AccountTypeEnum from '../../domain/AccountTypeEnum';

const uiKey = 'identity-accounts-table';
const manager = new IdentityAccountManager();
const accountManager = new AccountManager();
const identityManager = new Managers.IdentityManager();

class IdentityAccountsContent extends Basic.AbstractTableContent {

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
    return 'acc:content.identity.accounts';
  }

  componentDidMount() {
    this.selectSidebarItem('identity-accounts');
  }

  showDetail(entity) {
    const entityFormData = _.merge({}, entity, {
      identity: entity._embedded && entity._embedded.identity ? entity._embedded.identity.id : this.props.params.entityId,
      account: entity.account ? entity.account.id : null
    });
    //
    super.showDetail(entityFormData, () => {
      this.refs.account.focus();
    });
  }

  save(entity, event) {
    const formEntity = this.refs.form.getData();
    formEntity.identity = identityManager.getSelfLink(formEntity.identity);
    formEntity.account = {
      id: formEntity.account
    };
    //
    super.save(formEntity, event);
  }

  afterSave(entity, error) {
    if (!error) {
      this.addMessage({ message: this.i18n('save.success', { name: entity.account.uid }) });
    }
    //
    super.afterSave(entity, error);
  }

  render() {
    const { entityId } = this.props.params;
    const { _showLoading } = this.props;
    const { detail } = this.state;
    const forceSearchParameters = new Domain.SearchParameters().setFilter('identity', entityId);

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
                  onClick={this.showDetail.bind(this, { type: AccountTypeEnum.findKeyBySymbol(AccountTypeEnum.PERSONAL) })}
                  rendered={Managers.SecurityManager.hasAnyAuthority(['ROLE_WRITE'])}>
                  <Basic.Icon type="fa" icon="plus"/>
                  {' '}
                  {this.i18n('button.add')}
                </Basic.Button>
              ]
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
            <Advanced.Column property="account._embedded.system.name" header={this.i18n('acc:entity.System.name')} face="text" />
            <Advanced.Column property="account.type" header={this.i18n('acc:entity.Account.type')} sort face="enum" enumClass={AccountTypeEnum} />
            <Advanced.Column property="account.uid" header={this.i18n('acc:entity.Account.uid')} face="text" />
            <Advanced.Column property="identityRole._embedded.role.name" header={this.i18n('acc:entity.IdentityAccount.role')} face="text" />
            <Advanced.Column property="ownership" header={this.i18n('acc:entity.IdentityAccount.ownership')} sort face="bool" />
          </Advanced.Table>
        </Basic.Panel>

        <Basic.Modal
          bsSize="large"
          show={detail.show}
          onHide={this.closeDetail.bind(this)}
          backdrop="static"
          keyboard={!_showLoading}>

          <form onSubmit={this.save.bind(this, {})}>
            <Basic.Modal.Header closeButton={!_showLoading} text={this.i18n('create.header')} rendered={Utils.Entity.isNew(detail.entity)}/>
            <Basic.Modal.Header closeButton={!_showLoading} text={this.i18n('edit.header', { name: detail.entity.name })} rendered={!Utils.Entity.isNew(detail.entity)}/>
            <Basic.Modal.Body>
              <Basic.AbstractForm ref="form" showLoading={_showLoading} className="form-horizontal">
                <Basic.SelectBox
                  ref="account"
                  manager={accountManager}
                  label={this.i18n('acc:entity.Account._type')}
                  readOnly={!Utils.Entity.isNew(detail.entity)}
                  required/>
                <Basic.Checkbox
                  ref="ownership"
                  label={this.i18n('acc:entity.IdentityAccount.ownership')}/>
              </Basic.AbstractForm>
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

IdentityAccountsContent.propTypes = {
  _showLoading: PropTypes.bool,
};
IdentityAccountsContent.defaultProps = {
  _showLoading: false,
};

function select(state) {
  return {
    _showLoading: Utils.Ui.isShowLoading(state, `${uiKey}-detail`),
  };
}

export default connect(select)(IdentityAccountsContent);
