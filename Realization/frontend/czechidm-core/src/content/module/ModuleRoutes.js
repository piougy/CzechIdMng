import React from 'react';
import { connect } from 'react-redux';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';

class ModuleRoutes extends Basic.AbstractContent {

  getContentKey() {
    return 'content.system.modules';
  }

  render() {
    return (
      <div>
        <Basic.PageHeader>
          {this.i18n('header')}
        </Basic.PageHeader>

        <Advanced.TabPanel position="top" parentId="modules" params={this.props.params}>
          {this.props.children}
        </Advanced.TabPanel>
      </div>
    );
  }
}

ModuleRoutes.propTypes = {
};
ModuleRoutes.defaultProps = {
};

function select() {
  return {
  };
}

export default connect(select)(ModuleRoutes);
