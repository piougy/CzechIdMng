import React from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
//
import * as Basic from '../../components/basic';
import { IdentityManager } from '../../redux';
import SearchParameters from '../../domain/SearchParameters';
import IdentityTable from './IdentityTable';

class IdentitySubordinates extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.identityManager = new IdentityManager();
  }

  getManager() {
    return this.identityManager;
  }

  getContentKey() {
    return 'content.identity.subordinates';
  }

  componentDidMount() {
    this.selectSidebarItem('profile-subordinates');
  }

  render() {
    const forceSearchParameters = new SearchParameters().setFilter('subordinatesFor', this.props.params.entityId);
    return (
      <div className="tab-pane-table-body">
        <Helmet title={this.i18n('title')} />

        <Basic.ContentHeader style={{ marginBottom: 0 }}>
          {this.i18n('header')}
        </Basic.ContentHeader>

        <Basic.Panel className="no-border last">
          <IdentityTable
            uiKey="subordinates_table"
            identityManager={this.getManager()}
            filterOpened={false}
            forceSearchParameters={forceSearchParameters} />
        </Basic.Panel>
      </div>
    );
  }
}

IdentitySubordinates.propTypes = {
};
IdentitySubordinates.defaultProps = {
};

function select() {
  return {
  };
}

export default connect(select)(IdentitySubordinates);
