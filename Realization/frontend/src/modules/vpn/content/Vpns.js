

import React from 'react';
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
import VpnTable from './VpnTable';

let vpnRecordManager = new VpnRecordManager();

/**
* Vpns list
*/
class Vpns extends Basic.AbstractContent {

  constructor(props) {
    super(props);
    this.state = {showLoading: props.showLoading, showModalNew: false, showModalEdit: {show:false, readOnly: false}};
  }

  getContentKey() {
    return 'vpn:content.vpns';
  }

  componentDidMount() {
    this.selectNavigationItem('vpns');
  }

  render() {
    const { showLoading, newVpnRecord, editVpnRecord, showModalEdit} = this.state;
    return (
      <div>
        <Helmet title={this.i18n('title')} />

        <Basic.PageHeader>
          {this.i18n('header')}
        </Basic.PageHeader>

        <Basic.Panel>
          <VpnTable uiKey="vpn-records-table" showColumnOwner={true} filterOpened={true}/>
        </Basic.Panel>

      </div>
    );
  }
}

Vpns.propTypes = {
}

Vpns.defaultProps = {
}

function select(state) {
  return {
  }
}

export default connect(select)(Vpns)
