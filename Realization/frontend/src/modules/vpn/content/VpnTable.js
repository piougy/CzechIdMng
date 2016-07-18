

import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
import uuid from 'uuid';
//
import * as Basic from '../../../components/basic';
import * as Advanced from '../../../components/advanced';
import { ApprovalTaskManager } from '../../../redux';
import { SecurityManager, IdentityManager } from '../../../modules/core/redux';
import { VpnRecordManager } from '../redux/data';
import VpnActivityStateEnum from '../enums/VpnActivityStateEnum';
import VpnRecordDetail from './VpnRecordDetail';
import VpnRecordProfileDetail from './VpnRecordProfileDetail';

let vpnRecordManager = new VpnRecordManager();

/**
* Table of vpn records
*/
class VpnTable extends Basic.AbstractContent {

  constructor(props) {
    super(props);
    this.state = {showLoading: props.showLoading, showModalEdit: {show:false, readOnly: false}};
  }

  getContentKey() {
    return 'vpn:content.VpnRecordProfileDetail';
  }


  componentDidMount() {
    this.selectNavigationItem('vpns');
  }

  reload(){
    this.refs.vpnRecordsTable.getWrappedInstance().reload();
  }

  _closeModal(){
    this.setState({showModalNew:false, showModalEdit:{show:false, readOnly: false}});
  }

  /**
   * Cell for view detail action (button)
   * @return {[Cell]}
   */
  _viewActionCell({rowIndex, data, property, ...props}){
    return (
      <Advanced.DetailButton
        title={this.i18n('viewBtnTitle')}
        onClick={this._viewVpnRecord.bind(this, data[rowIndex])}/>
    );
  }

  /**
   * Cell with edit and invalidate actions (buttons)
   * @return {[Cell]}
   */
  _actionsCell({rowIndex, data, property, ...props}) {
    let result = [];
    let haveEditRight = SecurityManager.isAdmin(null) || SecurityManager.equalsAuthenticated(data[rowIndex].owner.name);
    let editEnabled = haveEditRight && data[rowIndex]['currentActivity']['state']
    && (data[rowIndex]['currentActivity']['state'] === 'TO_APPROVE' || data[rowIndex]['currentActivity']['state'] === 'NEW_VPN_REQUEST')
    result.push(
      <Basic.Button
        key={`vpn-btn-edit-${data[rowIndex]['id']}`}
        type="button" level="primary" disabled={!editEnabled}
        title={this.i18n('editBtnTitle')}
        titlePlacement="bottom"
        onClick={this._editVpnRecord.bind(this, data[rowIndex])}
        className="btn-xs">
        <Basic.Icon type="fa" icon="edit"/>
      </Basic.Button>
    );

    result.push(' ');
    let invalidateEnabled = haveEditRight && data[rowIndex]['currentActivity']['state']
    && (data[rowIndex]['currentActivity']['state'] === 'APPROVED' || data[rowIndex]['currentActivity']['state'] === 'NEW_VPN_REQUEST')
    result.push(
      <Basic.Button
        key={`vpn-btn-remove-${data[rowIndex]['id']}`}
        type="button" level="danger" disabled={!invalidateEnabled}
        title={this.i18n('invalidateBtnTitle')}
        titlePlacement="bottom"
        onClick={this._invalidateVpnRecord.bind(this, data[rowIndex]['id'])} className="btn-xs">
        <Basic.Icon icon="ban-circle"/>
      </Basic.Button>
    );
    return <span>{result}</span>
  }

  /**
   * Show edit vpn record in modal dialog
   */
  _editVpnRecord(vpnRecord){
    this.setState({editVpnRecord:vpnRecord, showModalEdit:{show:true, readOnly: false}});
  }

  /**
   * Show detail of vpn record in modal dialog
   */
  _viewVpnRecord(vpnRecord){
    this.setState({editVpnRecord:vpnRecord, showModalEdit:{show:true, readOnly: true}});
  }

  /**
   * Save editted vpn record
   */
  _saveVpnRecord(){
    if (!this.refs.recordDetailEdit.isFormValid()) {
      return;
    }
    //get vpn record from VpnRecordDetailEdit component
    const vpnRecord = this.refs.recordDetailEdit.getData();
    this.setState({
      showLoading: true
    });
    let promise = vpnRecordManager.getService().updateById(vpnRecord.id, vpnRecord);
    promises.then((json) => {
      this.setState({
        showLoading: false
      }, () => {
        this.context.store.dispatch(vpnRecordManager.fetchEntity(json.id));
        this.addMessage({ level: 'success', key: 'form-success', message: this.i18n('vpnRequestSaved')});
        this.refs.vpnRecordsTable.getWrappedInstance().reload();
        this._closeModal();
      });
    }).catch(ex => {
      this.setState({
        showLoading: false
      });
      this.addError(ex);
    });
  }

  /**
   * Invalidate selected vpn record
   * @param  {[string]} vpnRecordID [Id of selected vpn record]
   */
  _invalidateVpnRecord(vpnRecordID){
    this.refs.confirm.show(this.i18n('confirmInvalidateVpnRecord'),this.i18n('confirmInvalidateVpnRecordTitle')).then(result => {
      this.setState({
        showLoading: true
      });
      let promises = vpnRecordManager.getService().invalidate(vpnRecordID);
      promises.then(response => {
        this.setState({
          showLoading: false
        });
        return response.json();
      }).then((json) => {
        if (json) {
          if (!json.error) {
            this.context.store.dispatch(vpnRecordManager.fetchEntity(vpnRecordID));
            this.addMessage({ level: 'success', key: 'form-success', message: this.i18n('vpnRequestInvalidated')});
            this.refs.vpnRecordsTable.getWrappedInstance().reload();
          }else {
            this.addError(json.error);
          }
        }
      }).catch(ex => {
        this.addError(ex);
      });
    }, function(err) {
      return;
    });
  }

  /**
   * Method for fire table filter
   */
  _useFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.vpnRecordsTable.getWrappedInstance().useFilter(this.refs.filterForm);
  }

  /**
   * Method for clear filter
   */
  _cancelFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.vpnRecordsTable.getWrappedInstance().cancelFilter(this.refs.filterForm);
  }

  /**
  * Filter for table
  * @return filter definiton
  */
  _getFilterDefinition(){
    const { showColumnOwner} = this.props;
    return(
      <Advanced.Filter onSubmit={this._useFilter.bind(this)}>
        <Basic.AbstractForm ref="filterForm">
          <Basic.Row>
            <div className="col-lg-6">
              <Advanced.Filter.EnumSelectBox
                ref="wfState"
                field="currentActivity.wfState"
                label={this.i18n('filter.wfState.label')}
                placeholder={this.i18n('filter.wfState.placeholder')}
                multiSelect={true}
                enum={VpnActivityStateEnum}/>
            </div>
            <div className="col-lg-6 text-right">
              <Advanced.Filter.FilterButtons cancelFilter={this._cancelFilter.bind(this)}/>
            </div>
          </Basic.Row>
          <Basic.Row>
            <div className="col-lg-6">
              <Advanced.Filter.TextField
                ref="login"
                field="currentActivity.login"
                placeholder={this.i18n('filter.login.placeholder')}
                label={this.i18n('filter.login.label')}/>
            </div>
            <div className="col-lg-6">
              <Advanced.Filter.TextField
                ref="owner"
                rendered={showColumnOwner}
                field="owner.name"
                placeholder={this.i18n('filter.owner.placeholder')}
                label={this.i18n('filter.owner.label')}/>
            </div>
          </Basic.Row>
        </Basic.AbstractForm>
      </Advanced.Filter>
    )
  }

  render() {
    const { forceSearchParameters, uiKey, showColumnOwner, filterOpened} = this.props;
    const { showLoading, newVpnRecord, editVpnRecord, showModalEdit} = this.state;
    let isImplementer = SecurityManager.isAdmin(null) || SecurityManager.hasAuthority(VpnRecordProfileDetail.IMPLEMENTER_ROLE_NAME);
    return (
      <div>
        <Basic.Confirm ref="confirm"/>
        <Advanced.Table
          ref="vpnRecordsTable"
          uiKey={uiKey}
          manager={vpnRecordManager}
          filter={this._getFilterDefinition()}
          filterOpened={filterOpened}
          forceSearchParameters={forceSearchParameters}
          pagination={true}>
          <Advanced.Column
            header={this.i18n('vpn:content.VpnRecordDetail.actions')}
            className="detail-button"
            property="actions"
            cell={this._viewActionCell.bind(this)}
            sort={false}/>
          <Advanced.Column
            header={this.i18n('vpn:content.VpnRecordDetail.owner')}
            width="20%"
            rendered={showColumnOwner}
            property="owner"  sort={true}/>
          <Advanced.Column
            header={this.i18n('vpn:content.VpnRecordDetail.state')}
            width="25%"
            face="enum" enumClass={VpnActivityStateEnum}
            property="currentActivity.wfState"
            sort={true}/>
          <Advanced.Column
            header={this.i18n('vpn:content.VpnRecordDetail.login')}
            width="20%"
            property="currentActivity.login"  sort={true}/>
          <Advanced.Column
            header={this.i18n('vpn:content.VpnRecordDetail.validFrom')}
            width="20%"
            face="date"
            property="currentActivity.validFrom"  sort={true}/>
          <Advanced.Column
            header={this.i18n('vpn:content.VpnRecordDetail.validTill')}
            face="date"
            property="currentActivity.validTill" sort={true}/>
          <Advanced.Column
            header={this.i18n('vpn:content.VpnRecordDetail.actions')}
            className="action"
            property="actions"
            cell={this._actionsCell.bind(this)}
            sort={false}/>
        </Advanced.Table>
        <Basic.Modal show={showModalEdit.show} onHide={this._closeModal.bind(this)} bsSize='lg' backdrop="static" keyboard={!showLoading}>
          <Basic.Modal.Header text={this.i18n('editVpn')}/>
          <Basic.Modal.Body>
            <VpnRecordDetail ref="recordDetailEdit" isImplementer={isImplementer} vpnRecord={editVpnRecord} readOnly={showModalEdit.readOnly} showLoading={showLoading}/>
          </Basic.Modal.Body>
          <Basic.Modal.Footer>
            <Basic.Button level="link" disabled={showLoading} onClick={this._closeModal.bind(this)}>{this.i18n('button.close')}</Basic.Button>
            <Basic.Button rendered={!showModalEdit.readOnly} disabled={showLoading} level="success" onClick={this._saveVpnRecord.bind(this)}>{this.i18n('editVpnSave')}</Basic.Button>
          </Basic.Modal.Footer>
        </Basic.Modal>
      </div>
    );
  }
}

VpnTable.propTypes = {
  forceSearchParameters: PropTypes.object,
  showColumnOwner: PropTypes.bool,
  filterOpened: PropTypes.bool
}

VpnTable.defaultProps = {
  showColumnOwner: false,
  filterOpened: false
}

function select(state) {
  return {
  }
}

export default connect(select, null, null, { withRef: true})(VpnTable)
