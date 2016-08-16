

import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import _ from 'lodash';
//
import * as Basic from '../../../../components/basic';
import * as Advanced from '../../../../components/advanced';
/**
* Table of user roles
*/
export class UserRoleTable extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      filterOpened: this.props.filterOpened,
      detail: {
        show: false,
        entity: {}
      }
    };
  }

  getContentKey() {
    return 'content.user.roles';
  }

  componentDidMount() {
  }

  componentDidUpdate() {
  }

  useFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.table.getWrappedInstance().useFilterForm(this.refs.filterForm);
  }

  cancelFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.table.getWrappedInstance().cancelFilter(this.refs.filterForm);
  }

  closeDetail() {
    this.setState({
      detail: {
        show: false,
        entity: {}
      }
    });
  }

  _useFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.table.getWrappedInstance().useFilterForm(this.refs.filterForm);
  }

  _cancelFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.table.getWrappedInstance().cancelFilter(this.refs.filterForm);
  }

  render() {
    const { uiKey, identityRoleManager, columns, _showLoading, forceSearchParameters } = this.props;
    const { filterOpened } = this.state;

    return (
      <div>
        <Advanced.Table
          ref="table"
          uiKey={uiKey}
          showLoading={_showLoading}
          forceSearchParameters={forceSearchParameters}
          manager={identityRoleManager}
          showRowSelection={false}
          filterOpened={filterOpened}>

          <Advanced.Column
            property="_embedded.role.name"
            sort={false} face="text"
            header={this.i18n('entity.IdentityRole.role')}
            rendered={_.includes(columns, 'name')}/>
          <Advanced.Column
            property="_embedded.identity.username"
            sort={false}
            face="text"
            header={this.i18n('entity.IdentityRole.identity')}
            rendered={_.includes(columns, 'username')}/>
          <Advanced.Column
            property="validFrom"
            sort
            header={this.i18n('label.validFrom')}
            face="date"
            rendered={_.includes(columns, 'validFrom')}/>
          <Advanced.Column
            property="validTill"
            sort
            header={this.i18n('label.validTill')}
            face="date"
            rendered={_.includes(columns, 'validTill')}/>
          <Advanced.Column
            property="id"
            sort={false}
            face="text"
            rendered={_.includes(columns, 'id')}/>
        </Advanced.Table>
      </div>
    );
  }
}

UserRoleTable.propTypes = {
  uiKey: PropTypes.string.isRequired,
  identityRoleManager: PropTypes.object.isRequired,
  columns: PropTypes.arrayOf(PropTypes.string),
  filterOpened: PropTypes.bool,
  forceSearchParameters: PropTypes.object
};

UserRoleTable.defaultProps = {
  columns: ['name', 'validTill', 'validFrom'],
  filterOpened: false,
  _showLoading: false
};

function select(state, component) {
  return {
    _searchParameters: state.data.ui[component.uiKey] ? state.data.ui[component.uiKey].searchParameters : {}
  //  _showLoading: component.identityRoleManager.isShowLoading(state, `${component.uiKey}-detail`)
  };
}

export default connect(select, null, null, { withRef: true })(UserRoleTable);
