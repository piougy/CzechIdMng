import React from 'react';
import { connect } from 'react-redux';
//
import * as Basic from '../../../components/basic';
import SearchParameters from '../../../domain/SearchParameters';
import * as Advanced from '../../../components/advanced';
import IdentityStateEnum from '../../../enums/IdentityStateEnum';
import { IdentityManager } from '../../../redux';


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
    const { entityId } = this.props.params;
    const forceSearchParameters = new SearchParameters().setFilter('automaticRoleAttributeId', entityId);
    const { filterOpened } = this.state;
    return (
      <div>
          <Basic.ContentHeader style={{ marginBottom: 0 }}>
            {this.i18n('content.automaticRoles.attribute.identities.header')}
          </Basic.ContentHeader>
          <Advanced.Table
          ref="table"
          uiKey="table"
          manager={this.getManager()}
          pagination={false}
          forceSearchParameters={forceSearchParameters}
          filter={
            <Advanced.Filter onSubmit={this.useFilter.bind(this)}>
              <Basic.AbstractForm ref="filterForm">
                <Basic.Row>
                  <div className="col-lg-6">
                    <Advanced.Filter.TextField
                      ref="text"
                      placeholder={this.i18n('filter.text')}/>
                  </div>
                  <div className="col-lg-6 text-right">
                    <Advanced.Filter.FilterButtons cancelFilter={this.cancelFilter.bind(this)}/>
                  </div>
                </Basic.Row>
              </Basic.AbstractForm>
            </Advanced.Filter>
          }
          filterOpened={filterOpened}
          _searchParameters={ this.getSearchParameters() }
          >
          <Advanced.Column property="_links.self.href" face="text" rendered={false}/>
            <Advanced.ColumnLink to="identity/:username/profile" property="username" width="20%" header={this.i18n('entity.Identity.username')} sort face="text"/>
            <Advanced.Column property="lastName" header={this.i18n('entity.Identity.lastName')} sort face="text"/>
            <Advanced.Column property="firstName" header={this.i18n('entity.Identity.firstName')} width="10%" face="text"/>
            <Advanced.Column property="email" width="15%" header={this.i18n('entity.Identity.email')} face="text"/>
            <Advanced.Column property="disabled" face="bool" header={this.i18n('entity.Identity.disabled')} sort width="100px"/>
            <Advanced.Column property="state" face="enum" header={this.i18n('entity.Identity.state.label')} enumClass={ IdentityStateEnum } sort width="100px"/>
            <Advanced.Column property="description" header={this.i18n('entity.Identity.description')} face="text"/>
          </Advanced.Table>
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
