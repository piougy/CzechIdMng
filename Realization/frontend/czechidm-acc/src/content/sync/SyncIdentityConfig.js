import React from 'react';
//
import { Basic, Managers } from 'czechidm-core';

const roleManager = new Managers.RoleManager();

/**
 * Identity's specific sync configuration
 *
 * @author Vít Švanda
 */
class SyncIdentityConfig extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  getContentKey() {
    return 'acc:content.system.systemSynchronizationConfigDetail';
  }

  getData(allData) {
    if (this.refs.formSpecific) {
      return this.refs.formSpecific.getData(allData);
    }
  }

  isFormValid() {
    if (this.refs.formSpecific) {
      return this.refs.formSpecific.isFormValid();
    }
  }

  render() {
    const {synchronizationConfig, showLoading} = this.props;
    return (
      <Basic.AbstractForm ref="formSpecific" data={synchronizationConfig} showLoading={showLoading} className="panel-body">
        <Basic.SelectBox
          ref="defaultRole"
          manager={roleManager}
          label={this.i18n('identityConfigDetail.defaultRole.label')}
          helpBlock={this.i18n('identityConfigDetail.defaultRole.helpBlock')}/>
        <Basic.Checkbox
          ref="startAutoRoleRec"
          label={this.i18n('identityConfigDetail.startAutoRoleRec.label')}
          helpBlock={this.i18n('identityConfigDetail.startAutoRoleRec.helpBlock')}/>
      </Basic.AbstractForm>
    );
  }
}

export default SyncIdentityConfig;
