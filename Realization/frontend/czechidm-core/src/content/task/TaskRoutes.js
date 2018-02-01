import React from 'react';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';

/**
 * Default content (routes) for tasks
 *
 * @author Ondřej Kopr
 */
export default class TaskRoutes extends Basic.AbstractContent {

  getContentKey() {
    return 'content.taks';
  }

  componentDidMount() {
  }

  render() {
    return (
      <div>
        <Basic.PageHeader>
          <Basic.Icon value="tasks"/>
          {' '}
          {this.i18n('navigation.menu.tasks.label')}
        </Basic.PageHeader>

        <Advanced.TabPanel position="top" parentId="tasks" params={this.props.params}>
          {this.props.children}
        </Advanced.TabPanel>
      </div>
    );
  }
}

TaskRoutes.propTypes = {
};
TaskRoutes.defaultProps = {
};
