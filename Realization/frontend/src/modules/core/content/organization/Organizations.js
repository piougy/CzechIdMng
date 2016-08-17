import React from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
import * as Basic from 'app/components/basic';
import * as Advanced from 'app/components/advanced';
import { OrganizationManager } from 'core/redux';
import * as Utils from 'core/utils';
import OrganizationTable from './OrganizationTable';

const uiKey = 'organization_table';

const rootKey = 'tree_root';

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
    const searchParameters = this.getManager().getService().getTreeSearchParameters();
    this.context.store.dispatch(this.getManager().fetchEntities(searchParameters, rootKey));
    // this.context.store.dispatch(this.getManager().fetchEntities(searchParameters, rootKey));
    this.selectNavigationItem('organizations');
  }

  render() {
    const { _showLoading, _root } = this.props;

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
          {
        <Basic.Panel>
          {
            !_root
            ||
          <Advanced.Tree
            ref="organizationTree"
            rootNode={{name: _root.name, shortName: 'Organizace', toggled: true, id: _root.id}}
            propertyId="id"
            propertyParent="parent"
            propertyName="name"
            showLoading={_showLoading}
            uiKey="orgTree"
            manager={this.getManager()}
            />
          }
        </Basic.Panel>
      }
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
  // console.log(Utils.Ui.getUiState(state, rootKey));
  return {
    _searchParameters: Utils.Ui.getSearchParameters(state, component.uiKey),
    _showLoading: Utils.Ui.isShowLoading(state, `${uiKey}-detail`),
    _root: Utils.Ui.getEntities(state, rootKey)[0]
  };
}

export default connect(select, null, null, { withRef: true})(Organizations);
