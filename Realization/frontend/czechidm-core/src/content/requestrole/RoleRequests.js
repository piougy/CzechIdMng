import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import { RoleRequestManager, SecurityManager, IdentityManager} from '../../redux';
import RoleRequestStateEnum from '../../enums/RoleRequestStateEnum';
import uuid from 'uuid';

const uiKey = 'role-request-table';
const manager = new RoleRequestManager();
const identityManager = new IdentityManager();

class RoleRequests extends Basic.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      detail: {
        show: false,
        entity: {}
      }
    };
  }

  getManager() {
    return manager;
  }

  getUiKey() {
    return uiKey;
  }

  getContentKey() {
    return 'content.roleRequests';
  }

  componentDidMount() {
    this.selectNavigationItems(['roles-menu', 'role-requests']);
  }

  showDetail(entity, add) {
    if (add) {
      this.setState({
        detail: {
          ... this.state.detail,
          show: true
        }
      });
    } else {
      this.context.router.push(`/role-requests/${entity.id}/detail`);
    }
  }

  _createNewRequest(event) {
    if (event) {
      event.preventDefault();
    }
    if (!this.refs.modalForm.isFormValid()) {
      return;
    }
    const applicantId = this.refs.applicant.getValue();
    const uuidId = uuid.v1();
    this.context.router.push(`/role-requests/${uuidId}/new?new=1&applicantId=${applicantId}`);
  }

  _startRequest(idRequest, event) {
    if (event) {
      event.preventDefault();
    }
    this.refs[`confirm-delete`].show(
      this.i18n(`action.startRequest.message`),
      this.i18n(`action.startRequest.header`)
    ).then(() => {
      this.setState({
        showLoading: true
      });
      const promise = this.getManager().getService().startRequest(idRequest);
      promise.then((json) => {
        this.setState({
          showLoading: false
        });
        if (this.refs.table) {
          this.refs.table.getWrappedInstance().reload();
        }
        this.addMessage({ message: this.i18n('action.startRequest.started', { name: json.name }) });
      }).catch(ex => {
        this.setState({
          showLoading: false
        });
        this.addError(ex);
        if (this.refs.table) {
          this.refs.table.getWrappedInstance().reload();
        }
      });
    }, () => {
      // Rejected
    });
    return;
  }

  /**
   * Close modal dialog
   */
  _closeDetail() {
    this.setState({
      detail: {
        ... this.state.detail,
        show: false
      }
    });
  }

  render() {
    const { _showLoading } = this.props;
    const { showLoading, detail } = this.state;
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
            }
            buttons={
              [<span>
                <Basic.Button
                  level="success"
                  key="add_button"
                  className="btn-xs"
                  onClick={this.showDetail.bind(this, { }, true)}
                  rendered={SecurityManager.hasAnyAuthority(['ROLE_REQUEST_WRITE'])}>
                  <Basic.Icon type="fa" icon="plus"/>
                  {' '}
                  {this.i18n('button.add')}
                </Basic.Button>
              </span>
              ]
            }
            >
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
              property="state"
              sort
              face="enum"
              enumClass={RoleRequestStateEnum}/>
            <Advanced.Column
              property="applicant"
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
              property="executeImmediately"
              sort
              face="boolean"/>
            <Advanced.Column
              property="created"
              sort
              face="datetime"/>
            <Advanced.Column
              property=""
              header=""
              width="55px"
              className="detail-button"
              cell={
                ({ rowIndex, data }) => {
                  const state = data[rowIndex].state;
                  const canBeStart = (state === RoleRequestStateEnum.findKeyBySymbol(RoleRequestStateEnum.CONCEPT))
                  || (state === RoleRequestStateEnum.findKeyBySymbol(RoleRequestStateEnum.EXCEPTION));
                  return (
                    <span>
                      <Basic.Button
                        ref="startButton"
                        type="button"
                        level="success"
                        rendered={SecurityManager.hasAnyAuthority(['ROLE_REQUEST_WRITE']) && canBeStart}
                        style={{marginRight: '2px'}}
                        title={this.i18n('button.start')}
                        titlePlacement="bottom"
                        onClick={this._startRequest.bind(this, [data[rowIndex].id])}
                        className="btn-xs">
                        <Basic.Icon type="fa" icon="play"/>
                      </Basic.Button>
                    </span>
                  );
                }
              }/>
          </Advanced.Table>
        </Basic.Panel>
        <Basic.Modal
          bsSize="default"
          show={detail.show}
          onHide={this._closeDetail.bind(this)}
          backdrop="static"
          keyboard={!innerShowLoading}>

          <form onSubmit={this._createNewRequest.bind(this)}>
            <Basic.Modal.Header closeButton={!_showLoading} text={this.i18n('create.header')}/>
            <Basic.Modal.Body>
              <Basic.AbstractForm ref="modalForm" showLoading={_showLoading}>
                <Basic.SelectBox
                  ref="applicant"
                  manager={ identityManager }
                  label={ this.i18n('applicant') }
                  required/>

              </Basic.AbstractForm>
            </Basic.Modal.Body>
            <Basic.Modal.Footer>
              <Basic.Button
                level="link"
                onClick={this._closeDetail.bind(this)}
                showLoading={_showLoading}>
                {this.i18n('button.close')}
              </Basic.Button>
              <Basic.Button
                type="submit"
                level="success"
                showLoading={_showLoading}
                showLoadingIcon>
                {this.i18n('button.createRequest')}
              </Basic.Button>
            </Basic.Modal.Footer>
          </form>
        </Basic.Modal>
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
