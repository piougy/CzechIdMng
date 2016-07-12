'use strict';

import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
//
import * as Basic from '../../../components/basic';
import { ApprovalTaskManager } from '../../../redux';
import { SecurityManager, IdentityManager } from '../../../modules/core/redux';
import { VpnApprovalTaskManager, VpnRecordManager} from '../redux/data';
import * as Advanced from '../../../components/advanced';
import ApiOperationTypeEnum from '../../../modules/core/enums/ApiOperationTypeEnum';
import TaskStateEnum from '../../core/enums/TaskStateEnum';
import VpnActivityStateEnum from '../enums/VpnActivityStateEnum';
import VpnTable from './VpnTable';
import VpnRecordDetail from './VpnRecordDetail';
import moment from 'moment';


const identityManager = new IdentityManager();
let vpnRecordManager = new VpnRecordManager();

/**
 * Component for show user vpn records and allows create vpn request
 */
class VpnRecordProfileDetail extends Basic.AbstractContent {

  constructor(props) {
    super(props);
    this.state = {showLoading: props.showLoading, showModalNew: false};
  }

  componentDidMount() {
    const { userID } = this.props.params;
    let { query } = this.props.location;

    this.context.store.dispatch(identityManager.fetchEntityIfNeeded(userID));
    this.selectSidebarItem('vpn-info');
    if (query && query.open === '1') {
      this._addVpn();
    }
  }

  getContentKey() {
    return 'vpn:content.VpnRecordProfileDetail';
  }

  /**
   * Show modal dialog with new vpn request
   */
  _addVpn(){
    const { userID } = this.props.params;
    this.setState({showModalNew:true, newVpnRecord:{owner:userID, currentActivity:{wfState: VpnActivityStateEnum.findKeyBySymbol(VpnActivityStateEnum.NEW_REQUEST),
       validFrom: new Date()}}});

  }

  /**
   * Create new vpn request (save to datebase)
   */
  _requestNewVpn(){
    if (!this.refs.recordDetailNew.isFormValid()) {
      return;
    }
    //get vpn record from VpnRecordDetailNew component
    const vpnRecord = this.refs.recordDetailNew.getData();
    this.setState({
      showLoading: true
    });
    let promises = vpnRecordManager.getService().create(vpnRecord);
    promises.then((json) => {
      this.setState({
        showLoading: false
      }, () => {
        this.context.store.dispatch(vpnRecordManager.fetchEntity(json.id));
        this.addMessage({ level: 'success', key: 'form-success', message: this.i18n('vpnRequestCreated')});
        this.refs.vpnRecordsTable.getWrappedInstance().reload();
        this._closeModal();
      });
    }).catch(ex => {
      this.addError(ex);
    });
  }

  /**
   * Close modal dialog with new vpn request
   * @return {[type]} [description]
   */
  _closeModal(){
    this.setState({showModalNew:false});
  }

  render() {
    const { identity} = this.props;
    const { userID} = this.props.params;
    const { showLoading, newVpnRecord, showModalEdit} = this.state;
    const forceFilters = [{
      field: 'owner.name',
      value: userID,
    }];

    let isImplementer = SecurityManager.isAdmin(null) || SecurityManager.hasAuthority(VpnRecordProfileDetail.IMPLEMENTER_ROLE_NAME);

    return (
      <div>
        <Basic.Confirm ref="confirm"/>
        <Basic.ContentHeader >
          {this.i18n('title')}
        </Basic.ContentHeader>
        <Basic.Row>
          <div className="col-sm-6">
            <Advanced.IdentityInfo identity={identity} showLoading={!identity} className="no-margin"/>
          </div>
          <div className="col-sm-6" style={{marginTop:'20px'}}>
            <div style={{margin: 'auto auto', width:'200px'}}>
              <Basic.Button
                className="btn"
                style={{display:'block'}}
                level="success"
                onClick={this._addVpn.bind(this)}>
                <Basic.Icon type="fa" icon="key"/>
                {' '+this.i18n('addVpnlabel')}
              </Basic.Button>
            </div>
          </div>
        </Basic.Row>
        <Basic.Panel style={{marginTop:'10px'}}>
          <Basic.PanelHeader text={SecurityManager.equalsAuthenticated(userID) ? this.i18n('yourVpn') : this.i18n('vpnsYoursSubordinate')}/>
          <VpnTable ref="vpnRecordsTable" uiKey="vpn-records-profile-table" forceSearchParameters={{filter:{filters: forceFilters}}} showColumnOwner={false}/>
        </Basic.Panel>
        <Basic.Modal show={this.state.showModalNew} onHide={this._closeModal.bind(this)} bsSize='lg' backdrop="static" keyboard={!showLoading}>
          <Basic.Modal.Header text={this.i18n('addVpn')}/>
          <Basic.Modal.Body>
            <VpnRecordDetail ref="recordDetailNew" isImplementer={isImplementer} vpnRecord={newVpnRecord} showLoading={showLoading}/>
          </Basic.Modal.Body>
          <Basic.Modal.Footer>
            <Basic.Button level="link" disabled={showLoading} onClick={this._closeModal.bind(this)}>{this.i18n('button.close')}</Basic.Button>
            <Basic.Button disabled={showLoading} level="success" onClick={this._requestNewVpn.bind(this)}>{this.i18n('requestNewVpn')}</Basic.Button>
          </Basic.Modal.Footer>
        </Basic.Modal>
      </div>
    )
  }
}

VpnRecordProfileDetail.propTypes = {
  readOnly: PropTypes.bool
}
VpnRecordProfileDetail.defaultProps = {
  readOnly: false
}

function select(state, component) {
  const { userID } = component.params;
  return {identity: identityManager.getEntity(state, userID)};
}

VpnRecordProfileDetail.IMPLEMENTER_ROLE_NAME = 'vpnAdmin';

export default connect(select)(VpnRecordProfileDetail);
