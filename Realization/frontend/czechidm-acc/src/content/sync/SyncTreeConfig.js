import React from 'react';
//
import { Basic } from 'czechidm-core';

/**
 * Tree specific sync configuration.
 *
 * @author Radek Tomiška
 * @since 10.4.0
 */
export default class SyncTreeConfig extends Basic.AbstractContent {

  getContentKey() {
    return 'acc:content.system.systemSynchronizationConfigDetail';
  }

  getData(allData) {
    if (this.refs.formSpecific) {
      return this.refs.formSpecific.getData(allData);
    }
    //
    return null;
  }

  isFormValid() {
    if (this.refs.formSpecific) {
      return this.refs.formSpecific.isFormValid();
    }
    //
    return true;
  }

  render() {
    const { synchronizationConfig, showLoading } = this.props;
    //
    return (
      <Basic.AbstractForm ref="formSpecific" data={ synchronizationConfig } showLoading={ showLoading } className="panel-body">
        <Basic.Checkbox
          ref="startAutoRoleRec"
          label={ this.i18n('treeConfigDetail.startAutoRoleRec.label') }
          helpBlock={ this.i18n('treeConfigDetail.startAutoRoleRec.helpBlock') }/>
      </Basic.AbstractForm>
    );
  }
}
