import React from 'react';
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

  getNavigationKey() {
    return 'tree-types';
  }

  render() {
    return (
      <div>
        {this.renderPageHeader()}

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
