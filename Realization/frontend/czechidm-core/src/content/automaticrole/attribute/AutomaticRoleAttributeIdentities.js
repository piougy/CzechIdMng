import React from 'react';
import { connect } from 'react-redux';
//
import * as Basic from '../../../components/basic';
import SearchParameters from '../../../domain/SearchParameters';
import * as Advanced from '../../../components/advanced';
import IdentityStateEnum from '../../../enums/IdentityStateEnum';
import { IdentityManager } from '../../../redux';
import _ from 'lodash';
import filterHelp from '../../../components/advanced/Filter/README_cs.md';
import IdentityTable from '../../identity/IdentityTable';
import Helmet from 'react-helmet';

class AutomaticRoleAttributeIdentities extends Advanced.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      filterOpened: props.filterOpened
    };
    this.identityManager = new IdentityManager();
  }

  getManager() {
    return this.identityManager;
  }

  getContentKey() {
    return 'content.automaticRoles.identities';
  }

  componentDidMount() {
    this.selectNavigationItems(['system', 'automatic-role-attribute-identities']);
  }
  render() {
    const {
  //    uiKey,
      identityManager,
      columns,
      rendered,
      treeType,
    } = this.props;
    const { entityId } = this.props.params;
    //
    if (!rendered) {
    //  return null;
    }
    //
//    const forceSearchParameters = new SearchParameters().setFilter('automaticRoleAttributeId', this.props.params.attributeId);
    if (entityId) {
      //
    }
    const forceSearchParameters = new SearchParameters().setFilter('automaticRoleAttributeId', entityId);
    console.log('id automaticke role' + entityId);
    return (
      <div>
        ###AutomaticRoleAttributeIdentities {entityId}
        <Basic.Panel>
        <Basic.PanelHeader text={this.i18n('content.automaticRoles.attribute.identities.title')} help="#kotva"/>
          <Advanced.Table
          ref="table"
          uiKey="table"
          manager={this.getManager()}
          pagination={false}
          forceSearchParameters={forceSearchParameters}>
          <Advanced.Column property="_links.self.href" face="text" rendered={false}/>
            <Advanced.ColumnLink to="identity/:username/profile" property="username" width="20%" header={this.i18n('entity.Identity.username')} sort face="text"/>
            <Advanced.Column property="lastName" header={this.i18n('entity.Identity.lastName')} sort face="text"/>
            <Advanced.Column property="firstName" header={this.i18n('entity.Identity.firstName')} width="10%" face="text"/>
            <Advanced.Column property="email" width="15%" header={this.i18n('entity.Identity.email')} face="text"/>
            <Advanced.Column property="disabled" face="bool" header={this.i18n('entity.Identity.disabled')} sort width="100px"/>
            <Advanced.Column property="state" face="enum" header={this.i18n('entity.Identity.state.label')} enumClass={ IdentityStateEnum } sort width="100px"/>
            <Advanced.Column property="description" header={this.i18n('entity.Identity.description')} face="text"/>
          </Advanced.Table>
        </Basic.Panel>
      </div>
    );
  }

}

AutomaticRoleAttributeIdentities.propTypes = {
};
AutomaticRoleAttributeIdentities.defaultProps = {
};

function select() {
  return {
  };
}


export default connect(select)(AutomaticRoleAttributeIdentities);
