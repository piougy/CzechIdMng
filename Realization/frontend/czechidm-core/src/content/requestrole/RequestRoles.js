import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import { RoleRequestManager, SecurityManager } from '../../redux';
import RoleRequestStateEnum from '../../enums/RoleRequestStateEnum';
import uuid from 'uuid';

const uiKey = 'request-role-table';
const manager = new RoleRequestManager();

class RoleRequests extends Basic.AbstractTableContent {

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
    return 'content.requestRoles';
  }

  componentDidMount() {
    this.selectNavigationItems(['roles-menu', 'role-requests']);
  }

  showDetail(entity, add) {
    const system = entity._embedded && entity._embedded.system ? entity._embedded.system.id : this.props.params.entityId;
    if (add) {
      const uuidId = uuid.v1();
      this.context.router.push(`/system/${system}/object-classes/${uuidId}/new?new=1&systemId=${system}`);
    } else {
      this.context.router.push(`/system/${system}/object-classes/${entity.id}/detail`);
    }
  }

  _generateSchema(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs[`confirm-delete`].show(
      this.i18n(`action.generateSchema.message`),
      this.i18n(`action.generateSchema.header`)
    ).then(() => {
      this.setState({
        showLoading: true
      });
      // const promise = systemManager.getService().generateSchema(entityId);
      // promise.then((json) => {
      //   this.setState({
      //     showLoading: false
      //   });
      //   this.refs.table.getWrappedInstance().reload();
      //   this.addMessage({ message: this.i18n('action.generateSchema.success', { name: json.name }) });
      // }).catch(ex => {
      //   this.setState({
      //     showLoading: false
      //   });
      //   this.addError(ex);
      //   this.refs.table.getWrappedInstance().reload();
      // });
    }, () => {
      // Rejected
    });
  }

  render() {
    const { _showLoading } = this.props;
    const { showLoading } = this.state;
    const innerShowLoading = _showLoading || showLoading;
    return (
      <div>
        <Helmet title={this.i18n('title')} />
        <Basic.Confirm ref="confirm-delete" level="danger"/>
        <Basic.ContentHeader>
          <Basic.Icon type="fa" icon="universal-access"/>
          {' '}
          <span dangerouslySetInnerHTML={{ __html: this.i18n('header') }}/>
        </Basic.ContentHeader>
        <Basic.Panel>
          <Advanced.Table
            ref="table"
            uiKey={uiKey}
            showLoading={innerShowLoading}
            manager={this.getManager()}
            showRowSelection={SecurityManager.hasAnyAuthority(['ROLE_REQUEST_WRITE'])}
            actions={
              [{ value: 'delete', niceLabel: this.i18n('action.delete.action'),
                 action: this.onDelete.bind(this), disabled: false }]
            }
            filter={
              <Advanced.Filter onSubmit={this.useFilter.bind(this)}>
                <Basic.AbstractForm ref="filterForm">
                  <Basic.Row className="last">
                    <div className="col-lg-6">
                      <Advanced.Filter.TextField
                        ref="applicant"
                        placeholder={this.i18n('filter.applicant.placeholder')}/>
                    </div>
                    <div className="col-lg-2"/>
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
                      onClick={this.showDetail.bind(this, data[rowIndex], false)}/>
                  );
                }
              }/>
              <Advanced.Column
                property="applicant"
                header={this.i18n('acc:entity.ProvisioningOperation.entity')}
                face="text"
                cell={
                  ({ rowIndex, data }) => {
                    const entity = data[rowIndex];
                    return (
                      <Advanced.IdentityInfo id={entity.applicant} face="link" />
                    );
                  }
                }/>
            <Advanced.Column
              property="state"
              sort
              face="enum"
              enumClass={RoleRequestStateEnum}/>
            <Advanced.Column
              property="executeImmediately"
              sort
              face="boolean"/>
            <Advanced.Column
              property="created"
              sort
              face="datetime"/>
          </Advanced.Table>
        </Basic.Panel>
      </div>
    );
  }
}

RoleRequests.propTypes = {
  _showLoading: PropTypes.bool
};
RoleRequests.defaultProps = {
  _showLoading: false
};

function select() {
  return {
  };
}

export default connect(select)(RoleRequests);
