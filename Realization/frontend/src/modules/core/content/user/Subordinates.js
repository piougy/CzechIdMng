import React from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
import * as Basic from '../../../../components/basic';
import { IdentitySubordinateManager } from '../../../../redux/data';
import ConnectedUserTable, { UserTable } from './UserTable';

class Subordinates extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      userID: null
    };
  }

  componentDidMount() {
    this._selectNavigationItem();
  }

  componentDidUpdate() {
    const { userID } = this.props.params;
    if (userID && (!this.state.userID || this.state.userID !== userID)) {
      this._refreshTable(userID);
    }
  }

  _refreshTable(userID) {
    this.setState(
      {
        userID
      },
      () => { this.refs.table.getWrappedInstance().cancelFilter(); }
    );
  }

  _selectNavigationItem() {
    this.selectNavigationItems(['user-subordinates', 'profile-subordinates']);
  }

  render() {
    const { userID } = this.props.params;
    const identitySubordinateManager = new IdentitySubordinateManager(userID);

    return (
      <div>
        <Helmet title={this.i18n('navigation.menu.subordinates.label')} />

        <Basic.ContentHeader style={{ marginBottom: 0 }}>
          {this.i18n('navigation.menu.subordinates.label')}
        </Basic.ContentHeader>

        <Basic.Panel className="no-border last">
          <ConnectedUserTable
            ref="table"
            uiKey="subordinate_table"
            identityManager={identitySubordinateManager}
            columns={UserTable.defaultProps.columns.filter(property => { return property !== 'idmManager';})}/>
        </Basic.Panel>
      </div>
    );
  }
}

Subordinates.propTypes = {
};
Subordinates.defaultProps = {
};

function select() {
  return {
  };
}

export default connect(select)(Subordinates);
