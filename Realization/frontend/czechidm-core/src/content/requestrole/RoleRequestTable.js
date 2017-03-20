import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import _ from 'lodash';
import { Link } from 'react-router';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import {SecurityManager} from '../../redux';
import RoleRequestStateEnum from '../../enums/RoleRequestStateEnum';
import CandicateUsersCell from '../workflow/CandicateUsersCell';

class RoleRequestTable extends Basic.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
  }

  getUiKey() {
    return this.props.uiKey;
  }

  getManager() {
    return this.props.manager;
  }

  getContentKey() {
    return 'content.roleRequests';
  }


  showDetail(entity, add) {
    const {createNewRequestFunc, adminMode} = this.props;
    if (add) {
      createNewRequestFunc(entity);
    } else {
      this.context.router.push({pathname: `/role-requests/${entity.id}/detail`, state: {adminMode}});
    }
  }

  reload() {
    this.refs.table.getWrappedInstance().reload();
  }

  _getCandidatesCell({ rowIndex, data, property}) {
    const entity = data[rowIndex];
    if (!entity || !entity._embedded || !entity._embedded.wfProcessId) {
      return '';
    }
    return (
      <CandicateUsersCell rowIndex={0} data={[entity._embedded.wfProcessId]} property={property} maxEntry={5} />
    );
  }

  _getCurrentActivitiCell({ rowIndex, data}) {
    const entity = data[rowIndex];
    if (!entity || !entity._embedded || !entity._embedded.wfProcessId) {
      return '';
    }
    return (
      entity._embedded.wfProcessId.name
    );
  }

  _getWfProcessCell({ rowIndex, data}) {
    const entity = data[rowIndex];
    if (!entity || !entity.wfProcessId) {
      return '';
    }
    return (
      <Link to={`/workflow/history/processes/${entity.wfProcessId}`}>{entity.wfProcessId}</Link>
    );
  }

  render() {
    const { _showLoading, uiKey, startRequestFunc, createNewRequestFunc, columns, forceSearchParameters, showFilter} = this.props;
    const innerShowLoading = _showLoading;
    return (
      <div>
        <Basic.Confirm ref="confirm-delete" level="danger"/>
        <Advanced.Table
          ref="table"
          uiKey={uiKey}
          showLoading={innerShowLoading}
          forceSearchParameters={forceSearchParameters}
          manager={this.getManager()}
          showRowSelection={SecurityManager.hasAnyAuthority(['ROLE_REQUEST_WRITE'])}
          actions={
            [{ value: 'delete', niceLabel: this.i18n('action.delete.action'),
               action: this.onDelete.bind(this), disabled: false }]
          }
          filter={showFilter ?
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
            : undefined
          }
          buttons={
            [<span>
              <Basic.Button
                level="success"
                key="add_button"
                className="btn-xs"
                onClick={this.showDetail.bind(this, { }, true)}
                rendered={_.includes(columns, 'createNew') && createNewRequestFunc
                  && SecurityManager.hasAnyAuthority(['ROLE_REQUEST_WRITE'])}>
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
            rendered={_.includes(columns, 'detail')}
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
            property="currentActivity"
            rendered={_.includes(columns, 'wf')}
            face="text"
            cell={this._getCurrentActivitiCell}
            />
          <Advanced.Column
            property="candicateUsers"
            rendered={_.includes(columns, 'wf')}
            face="text"
            cell={this._getCandidatesCell}
            />
          <Advanced.Column
            property="state"
            rendered={_.includes(columns, 'state')}
            sort
            face="enum"
            enumClass={RoleRequestStateEnum}/>
          <Advanced.Column
            property="applicant"
            rendered={_.includes(columns, 'applicant')}
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
            rendered={_.includes(columns, 'executeImmediately')}
            sort
            face="boolean"/>
          <Advanced.Column
            property="modified"
            rendered={_.includes(columns, 'modified')}
            sort
            face="datetime"/>
          <Advanced.Column
            property="created"
            rendered={_.includes(columns, 'created')}
            sort
            face="datetime"/>
          <Advanced.Column
            property="wfProcessId"
            rendered={_.includes(columns, 'wf')}
            cell={this._getWfProcessCell}
            sort
            face="text"/>
          <Advanced.Column
            property=""
            header=""
            rendered={_.includes(columns, 'startRequest')}
            width="55px"
            className="detail-button"
            cell={
              ({ rowIndex, data }) => {
                const state = data[rowIndex].state;
                const canBeStart = (state === RoleRequestStateEnum.findKeyBySymbol(RoleRequestStateEnum.CONCEPT))
                || (state === RoleRequestStateEnum.findKeyBySymbol(RoleRequestStateEnum.EXCEPTION))
                || (state === RoleRequestStateEnum.findKeyBySymbol(RoleRequestStateEnum.DUPLICATED));
                return (
                  <span>
                    <Basic.Button
                      ref="startButton"
                      type="button"
                      level="success"
                      rendered={startRequestFunc && SecurityManager.hasAnyAuthority(['ROLE_REQUEST_WRITE']) && canBeStart}
                      style={{marginRight: '2px'}}
                      title={this.i18n('button.start')}
                      titlePlacement="bottom"
                      onClick={startRequestFunc && startRequestFunc.bind(this, [data[rowIndex].id])}
                      className="btn-xs">
                      <Basic.Icon type="fa" icon="play"/>
                    </Basic.Button>
                  </span>
                );
              }
            }/>
        </Advanced.Table>
      </div>
    );
  }
}

RoleRequestTable.propTypes = {
  _showLoading: PropTypes.bool,
  showFilter: PropTypes.bool,
  adminMode: PropTypes.bool,
  forceSearchParameters: PropTypes.object,
  startRequestFunc: PropTypes.func,
  createNewRequestFunc: PropTypes.func,
  columns: PropTypes.arrayOf(PropTypes.string)
};

RoleRequestTable.defaultProps = {
  _showLoading: false,
  showFilter: true,
  adminMode: true,
  columns: ['state', 'created', 'modified', 'wf', 'applicant', 'executeImmediately', 'startRequest', 'createNew', 'detail'],
};

function select() {
  return {
  };
}

export default connect(select, null, null, { withRef: true })(RoleRequestTable);
