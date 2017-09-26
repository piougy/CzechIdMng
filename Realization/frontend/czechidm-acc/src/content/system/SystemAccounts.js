import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
//
import { Basic, Advanced, Domain, Managers, Utils } from 'czechidm-core';
import { AccountManager, SystemEntityManager, SystemManager } from '../../redux';
import AccountTypeEnum from '../../domain/AccountTypeEnum';
import SystemEntityTypeEnum from '../../domain/SystemEntityTypeEnum';

const uiKey = 'system-accounts-table';
const manager = new AccountManager();
const systemEntityManager = new SystemEntityManager();
const systemManager = new SystemManager();

/**
 * Linked accounts on target system
 *
 * @author Radek Tomiška
 */
class SystemAccountsContent extends Advanced.AbstractTableContent {

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
    return uiKey;
  }

  getContentKey() {
    return 'acc:content.system.accounts';
  }

  getNavigationKey() {
    return 'system-accounts';
  }

  showDetail(entity) {
    this.setState({
      systemEntity: entity.systemEntity
    }, ()=> {
      super.showDetail(entity, () => {
        this.refs.uid.focus();
      });
    });
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

  render() {
    const { entityId } = this.props.params;
    const { _showLoading } = this.props;
    const { detail, systemEntity } = this.state;
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
              header={ this.i18n('acc:entity.Account.accountType') }
              width={ 75 }
              sort
              face="enum"
              enumClass={ AccountTypeEnum } />
            <Advanced.Column
              property="entityType"
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
              property="inProtection"
              header={this.i18n('acc:entity.Account.inProtection')}
              face="bool" />
            <Advanced.Column
              property="endOfProtection"
              header={this.i18n('acc:entity.Account.endOfProtection')}
              face="datetime" />
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
              <Basic.AbstractForm ref="form" showLoading={_showLoading} >
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
    _searchParameters: Utils.Ui.getSearchParameters(state, uiKey)
  };
}

export default connect(select)(SystemAccountsContent);
