import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
//
import { Basic, Advanced, Domain } from 'czechidm-core';
import { RoleSystemManager } from '../../redux';

const manager = new RoleSystemManager();

class RoleSystems extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  getContentKey() {
    return 'acc:content.role.systems';
  }

  componentDidMount() {
    this.selectNavigationItems(['roles', 'role-systems']);
  }

  render() {
    const { entityId } = this.props.params;
    const forceSearchParameters = new Domain.SearchParameters().setFilter('roleId', entityId);

    return (
      <div>
        <Helmet title={this.i18n('title')} />
        <Advanced.Table
          ref="table"
          uiKey="role-system-table"
          manager={manager}
          forceSearchParameters={forceSearchParameters}>
          <Advanced.Column property="_embedded.system.name" header={this.i18n('acc:entity.System.name')} sort face="text" />
        </Advanced.Table>
      </div>
    );
  }
}

RoleSystems.propTypes = {
};
RoleSystems.defaultProps = {
};

function select(state, component) {
  return {
  };
}

export default connect(select)(RoleSystems);
