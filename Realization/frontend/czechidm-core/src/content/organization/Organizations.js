import React from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
import * as Basic from '../../components/basic';
import { OrganizationManager } from '../../redux';
import * as Utils from '../../utils';
import OrganizationTable from './OrganizationTable';

const uiKey = 'organization_table';

/**
* Organizations list
*/
class Organizations extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      filterOpened: true,
      detail: {
        show: false,
        entity: {}
      }
    };
    this.organizationManager = new OrganizationManager();
  }

  getManager() {
    return this.organizationManager;
  }

  getContentKey() {
    return 'content.organizations';
  }

  componentDidMount() {
    this.selectNavigationItem('organizations');
  }

  render() {
    return (
      <div>
        <Helmet title={this.i18n('title')} />
        <Basic.Confirm ref="confirm-delete" level="danger"/>

        <Basic.PageHeader>
          <Basic.Icon value="fa:building"/>
          {' '}
          {this.i18n('header')}
        </Basic.PageHeader>

        <Basic.Panel>
          <OrganizationTable uiKey={this.uiKey} organizationManager={this.getManager()} organizationTree={this.refs.organizationTree} />
        </Basic.Panel>
      </div>
    );
  }
}

Organizations.propTypes = {
};
Organizations.defaultProps = {
  _showLoading: false
};

function select(state, component) {
  return {
    _searchParameters: Utils.Ui.getSearchParameters(state, component.uiKey),
    _showLoading: Utils.Ui.isShowLoading(state, `${uiKey}-detail`)
  };
}

export default connect(select, null, null, { withRef: true})(Organizations);
