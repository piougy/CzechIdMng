import React from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
import * as Basic from '../../../components/basic';
import { TreeTypeManager } from '../../../redux';
import TypeTable from './TypeTable';

/**
* Types list
*/
class Types extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.treeTypeManager = new TreeTypeManager();
  }

  getManager() {
    return this.treeTypeManager;
  }

  getContentKey() {
    return 'content.tree.types';
  }

  componentDidMount() {
    this.selectNavigationItem('tree-types');
  }

  render() {
    return (
      <div>
        <Helmet title={this.i18n('title')} />

        <Basic.PageHeader>
          <Basic.Icon value="fa:server"/>
          {' '}
          {this.i18n('header')}
        </Basic.PageHeader>

        <Basic.Panel>
          <TypeTable treeTypeManager={this.getManager()} />
        </Basic.Panel>
      </div>
    );
  }
}

Types.propTypes = {
};
Types.defaultProps = {
};

export default connect()(Types);
