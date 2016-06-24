'use strict';

import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
//
import * as Basic from '../../../components/basic';
import { SecurityManager, IdentityManager } from '../../../modules/core/redux';
import { VpnActivityManager, VpnRecordManager} from '../redux/data';
import * as Advanced from '../../../components/advanced';
import ApiOperationTypeEnum from '../../../modules/core/enums/ApiOperationTypeEnum';
import VpnActivityStateEnum from '../enums/VpnActivityStateEnum';
import _ from 'lodash';


const identityManager = new IdentityManager();
const vpnRecordManager = new VpnRecordManager();

class VpnRecordDetail extends Basic.AbstractContent {

  constructor(props) {
    super(props);
    this.state = {showLoading: props.showLoading};
  }

  componentDidMount() {
    const { vpnRecord } = this.props;
    if (vpnRecord){
      this.refs.formRecord.setData(vpnRecord);
      this.refs.formActivity.setData(vpnRecord.currentActivity);
    }
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps.vpnRecord != null && nextProps.vpnRecord.id) {
      if (this.props.vpnRecord !== nextProps.vpnRecord){
        this.refs.formRecord.setData(nextProps.vpnRecord);
        this.refs.formActivity.setData(nextProps.vpnRecord.currentActivity);
      }
    }
  }

  isFormValid(){
    return this.refs.formRecord.isFormValid() && this.refs.formActivity.isFormValid();
  }

  getData(){
    let record = this.refs.formRecord.getData();
    let acitivity = this.refs.formActivity.getData();
    record.currentActivity = acitivity;
    return record;
  }

  getContentKey() {
    return 'vpn:content.VpnRecordDetail';
  }

  render() {
    const { vpnRecord, readOnly, showLoading, isImplementer} = this.props;
    let showLoadingInternal = vpnRecord ? showLoading : true;
    let readOnlyInternal = readOnly;
    let readOnlyRecord = vpnRecord && !vpnRecord.id ? readOnly : true;

    return (
      <div>
        <Basic.Loading showLoading={showLoadingInternal} r>
          <Basic.AbstractForm className="inner-form" ref="formActivity" readOnly={readOnlyInternal}>
            <Basic.EnumLabel ref="wfState" enum={VpnActivityStateEnum} label={this.i18n('wfState')}/>
            <Basic.AbstractForm className="inner-form" ref="formRecord" readOnly={readOnlyRecord}>
              <Basic.TextField hidden={true} ref="owner" label={this.i18n('owner')}/>
              <Basic.TextField ref="company" label={this.i18n('company')}/>
              <Basic.TextField ref="contract" label={this.i18n('contract')}/>
              <Basic.TextField ref="scopeOfActivities" label={this.i18n('scopeOfActivities')}/>
            </Basic.AbstractForm>
            <Basic.TextArea ref="definitionOfAccess" label={this.i18n('definitionOfAccess')} placeholder={this.i18n('definitionOfAccess_placeholder')} required/>
            <Basic.TextArea ref="privateNote" label={this.i18n('privateNote')} rendered={isImplementer} placeholder={this.i18n('privateNote_placeholder')}/>
            <Basic.TextArea ref="publicNote" label={this.i18n('publicNote')} placeholder={this.i18n('publicNote_placeholder')}/>
            <Basic.DateTimePicker ref="validFrom" mode="date" componentSpan="col-sm-3" required label={this.i18n('validFrom')}/>
            <Basic.DateTimePicker ref="validTill" mode="date" componentSpan="col-sm-3" label={this.i18n('validTill')}/>
            <Basic.TextField ref="login" componentSpan="col-sm-3" label={this.i18n('login')} rendered={isImplementer} placeholder={this.i18n('login_placeholder')}/>
            <Basic.TextField ref="password" type="password" componentSpan="col-sm-3" label={this.i18n('password')} rendered={isImplementer}/>
          </Basic.AbstractForm>
        </Basic.Loading>
      </div>
    )
  }
}

VpnRecordDetail.propTypes = {
  vpnRecord: PropTypes.object.isRequired,
  readOnly: PropTypes.bool,
  isImplementer: PropTypes.bool,
  showLoading: PropTypes.bool
}
VpnRecordDetail.defaultProps = {
  isImplementer: false
}

export default VpnRecordDetail;
