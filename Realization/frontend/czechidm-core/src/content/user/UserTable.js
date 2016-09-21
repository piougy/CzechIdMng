import React, { PropTypes } from 'react';
import uuid from 'uuid';
import { connect } from 'react-redux';
import _ from 'lodash';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import * as Utils from '../../utils';
import { DataManager, OrganizationManager, SecurityManager } from '../../redux';
// TODO: LocalizationService.getCurrentLanguage()
import filterHelp from '../../components/advanced/Filter/README_cs.md';

/**
* Table of users
*/
export class UserTable extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      filterOpened: this.props.filterOpened
    };
    this.dataManager = new DataManager();
    this.organizationManager = new OrganizationManager();
  }

  componentDidMount() {
  }

  componentDidUpdate() {
  }

  onRowDoubleClick() {
    // redirect to profile
    /*
    const username = data[rowIndex]['name'];
    this.context.router.push('/user/' + username + '/profile');*/
  }

  /**
  * Redirec to new user form
  */
  addUser() {
    const uuidId = uuid.v1();
    this.context.router.push(`user/new?id=${uuidId}`);
  }

  useFilter(event) {
    if (event) {
      event.preventDefault();
    }
    /*
    if (!this.refs.filterName.getValue() && !selectedOrganization) {
      this.cancelFilter();
      return;
    }
    let homeOrganisationFilter = {
      field: 'homeOrganisation',
      operation: 'AND',
      filters: [
        {
          field: 'homeOrganisation.fullName',
          value: (selectedOrganization ? selectedOrganization : '') + '%'
        }
      ]
    }*/
    /*
    if (selectedOrganization){
      userSearchParameters.filter.filters.push(homeOrganisationFilter);
    }*/
    this.refs.table.getWrappedInstance().useFilterData({ text: this.refs.filterName.getValue() || '' });
  }

  cancelFilter() {
    const { identityManager, _searchParameters } = this.props;
    this.refs.filterName.setState({ value: null });
    //
    // prevent sort and pagination
    let userSearchParameters = _searchParameters.setFilters(identityManager.getDefaultSearchParameters().getFilters());
    userSearchParameters = userSearchParameters.setPage(0);
    //
    this.refs.table.getWrappedInstance().fetchEntities(userSearchParameters);
    // this.refs.orgTree.getWrappedInstance().collapse();
  }

  onActivate(bulkActionValue, usernames) {
    const { identityManager } = this.props;
    //
    this.refs['confirm-' + bulkActionValue].show(
      this.i18n(`content.users.action.${bulkActionValue}.message`, { count: usernames.length, username: identityManager.getEntity(this.context.store.getState(), usernames[0]).username }),
      this.i18n(`content.users.action.${bulkActionValue}.header`, { count: usernames.length})
    ).then(() => {
      this.context.store.dispatch(identityManager.setUsersActivity(usernames, bulkActionValue));
    }, () => {
      // nothing
    });
  }

  onReset(bulkActionValue, usernames) {
    // push state to redux
    this.context.store.dispatch(this.dataManager.storeData('selected-usernames', usernames));
    // redirect to reset page
    this.context.router.push(`/users/password/reset`);
  }

  onRemove(selectedRows) {
    this.refs.confirm.show('Are you sure ' + selectedRows + '?', 'Title').then(() => {
      // alert('onRemove');
    }, () => {
      // Rejected
    });
  }

  _homeOrganizationFilter(node, event) {
    event.stopPropagation();
    this.setState({selectedOrganization: node ? node.id : null}, ()=>{this.useFilter();});
  }

  _orgTreeHeaderDecorator(props) {
    const style = props.style;
    const iconType = props.node.isLeaf ? 'group' : 'building';
    const iconClass = `fa fa-${iconType}`;
    const iconStyle = { marginRight: '5px' };
    return (
      <div style={style.base}>
        <div style={style.title}>
          <i className={iconClass} style={iconStyle}/>
          <Basic.Button level="link" style={{padding: '0px 0px 0px 0px'}} onClick={this._homeOrganizationFilter.bind(this, props.node)}>
            { props.node.shortName }
          </Basic.Button>
        </div>
      </div>
    );
  }

  render() {
    const { uiKey, identityManager, columns } = this.props;
    const { filterOpened } = this.state;

    return (
      <div>
        <Basic.Confirm ref="confirm-deactivate" level="danger"/>
        <Basic.Confirm ref="confirm-activate"/>

        <Advanced.Table
          ref="table"
          uiKey={uiKey}
          manager={identityManager}
          onRowDoubleClick={this.onRowDoubleClick.bind(this)}
          showRowSelection={SecurityManager.hasAuthority('IDENTITY_WRITE')}
          rowClass={({rowIndex, data}) => { return Utils.Ui.getRowClass(data[rowIndex]); }}
          filter={
            <Advanced.Filter onSubmit={this.useFilter.bind(this)}>
              <Basic.AbstractForm ref="filterForm" className="form-horizontal">
                <Basic.Row className="last">
                  <div className="col-lg-8">
                    <Advanced.Filter.TextField
                      ref="filterName"
                      placeholder={this.i18n('content.users.filter.name.placeholder')}
                      labelSpan=""
                      componentSpan="col-sm-12"
                      help={filterHelp}/>
                  </div>
                  <div className="col-lg-4 text-right">
                    <Advanced.Filter.FilterButtons cancelFilter={this.cancelFilter.bind(this)}/>
                  </div>
                </Basic.Row>
                {/*
                <Basic.Row>
                  <div className="col-lg-8">
                    <Basic.LabelWrapper readOnly ref="homeOrgTree" componentSpan="col-sm-12">
                      <Basic.Panel className="no-margin">
                        <Advanced.Tree
                          ref="orgTree"
                          rootNode={{id: 'top', name: 'top', toggled: false, shortName: this.i18n(`content.users.filter.orgStructure`), children: []}}
                          propertyId="name"
                          propertyParent="parentId"
                          propertyName="shortName"
                          headerDecorator={this._orgTreeHeaderDecorator.bind(this)}
                          uiKey="user-table-org-tree"
                          manager={this.organizationManager}
                          />
                      </Basic.Panel>
                    </Basic.LabelWrapper>
                  </div>
                  <div className="col-lg-4">
                  </div>
                </Basic.Row>
                */}
              </Basic.AbstractForm>
            </Advanced.Filter>
          }
          filterOpened={filterOpened}
          actions={
            [
              { value: 'remove', niceLabel: this.i18n('content.users.action.remove.action'), action: this.onRemove.bind(this), disabled: true },
              { value: 'activate', niceLabel: this.i18n('content.users.action.activate.action'), action: this.onActivate.bind(this) },
              { value: 'deactivate', niceLabel: this.i18n('content.users.action.deactivate.action'), action: this.onActivate.bind(this) },
              { value: 'password-reset', niceLabel: this.i18n('content.users.action.reset.action'), action: this.onReset.bind(this), disabled: true }
            ]
          }
          buttons={
            [
              <Basic.Button
                level="success"
                key="add_button"
                type="submit"
                className="btn-xs"
                onClick={this.addUser.bind(this)}
                rendered={SecurityManager.hasAuthority('IDENTITY_WRITE')}>
                <Basic.Icon type="fa" icon="user-plus"/>
                {this.i18n('content.user.create.button.add')}
              </Basic.Button>
            ]
          }>
          <Advanced.Column property="_links.self.href" face="text" rendered={false}/>
          <Advanced.ColumnLink to="user/:username/profile" property="username" width="20%" sort face="text" rendered={_.includes(columns, 'username')}/>
          <Advanced.Column property="lastName" sort face="text" rendered={_.includes(columns, 'lastName')}/>
          <Advanced.Column property="firstName" width="10%" face="text" rendered={_.includes(columns, 'firstName')}/>
          <Advanced.Column property="email" width="15%" face="text" sort rendered={_.includes(columns, 'email')}/>
          <Advanced.Column property="disabled" face="bool" sort width="100px" rendered={_.includes(columns, 'disabled')}/>
          <Basic.Column
            header={this.i18n('entity.Identity.description')}
            cell={<Basic.TextCell property="description" />}
            rendered={_.includes(columns, 'description')}/>
        </Advanced.Table>
      </div>
    );
  }
}

UserTable.propTypes = {
  uiKey: PropTypes.string.isRequired,
  identityManager: PropTypes.object.isRequired,
  columns: PropTypes.arrayOf(PropTypes.string),
  filterOpened: PropTypes.bool
};

UserTable.defaultProps = {
  columns: ['username', 'lastName', 'firstName', 'email', 'disabled', 'description'],
  filterOpened: false
};

function select(state, component) {
  return {
    _searchParameters: state.data.ui[component.uiKey] ? state.data.ui[component.uiKey].searchParameters : {}
  };
}

export default connect(select, null, null, { withRef: true })(UserTable);
