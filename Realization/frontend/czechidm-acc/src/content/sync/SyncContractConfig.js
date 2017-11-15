import React from 'react';
//
import { Basic, Managers, Domain } from 'czechidm-core';

const treeNodeManager = new Managers.TreeNodeManager();
const treeTypeManager = new Managers.TreeTypeManager();
const identityManager = new Managers.IdentityManager();

/**
 * Contract's specific sync configuration
 *
 * @author Vít Švanda
 */
class SyncContractConfig extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      ...this.state,
      defaultTreeType: null,
      treeNodeSearchParameters: new Domain.SearchParameters().setFilter('treeTypeId', Domain.SearchParameters.BLANK_UUID)
    };
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

  _onChangeTreeType(treeType) {
    const treeTypeId = treeType ? treeType.id : null;
    this.setState({
      treeTypeId,
      treeNodeSearchParameters: this.state.treeNodeSearchParameters.setFilter('treeTypeId', treeTypeId || Domain.SearchParameters.BLANK_UUID)
    }, () => {
      this.refs.defaultTreeNode.setValue(null);
    });
  }

  render() {
    const {treeNodeSearchParameters, treeTypeId} = this.state;
    const {synchronizationConfig, showLoading, isNew} = this.props;
    let resutlTreeTypeId = treeTypeId;
    if (!resutlTreeTypeId && synchronizationConfig) {
      resutlTreeTypeId = isNew ? treeTypeId : synchronizationConfig.defaultTreeType;
    }
    return (
      <Basic.AbstractForm ref="formSpecific" data={synchronizationConfig} showLoading={showLoading} className="panel-body">
        <Basic.SelectBox
          ref="defaultTreeType"
          required
          manager={treeTypeManager}
          label={this.i18n('contractConfigDetail.defaultTreeType.label')}
          helpBlock={this.i18n('contractConfigDetail.defaultTreeType.helpBlock')}
          onChange={this._onChangeTreeType.bind(this)}/>
        <Basic.SelectBox
          ref="defaultTreeNode"
          manager={treeNodeManager}
          label={this.i18n('contractConfigDetail.defaultTreeNode.label')}
          helpBlock={this.i18n('contractConfigDetail.defaultTreeNode.helpBlock')}
          forceSearchParameters={treeNodeSearchParameters}
          hidden={!resutlTreeTypeId}/>
        <Basic.SelectBox
          ref="defaultLeader"
          manager={identityManager}
          label={this.i18n('contractConfigDetail.defaultLeader.label')}
          helpBlock={this.i18n('contractConfigDetail.defaultLeader.helpBlock')}/>
      </Basic.AbstractForm>
    );
  }
}

export default SyncContractConfig;
