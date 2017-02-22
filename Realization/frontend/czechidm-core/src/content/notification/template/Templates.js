import React from 'react';
import { connect } from 'react-redux';
import * as Basic from '../../../components/basic';
import { NotificationTemplateManager } from '../../../redux';
import TemplateTable from './TemplateTable';

const TABLE_UIKEY = 'NotificationTemplateTableUIKEY';

/**
* Content with notification templates
*/
class Templates extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.notificationTemplateManager = new NotificationTemplateManager();
  }

  getManager() {
    return this.notificationTemplateManager;
  }

  getContentKey() {
    return 'content.notificationTemplate';
  }

  getNavigationKey() {
    return 'notification-templates';
  }

  render() {
    return (
      <div>
        {this.renderPageHeader()}

        <Basic.Panel>
          <TemplateTable manager={this.notificationTemplateManager} uiKey={TABLE_UIKEY}/>
        </Basic.Panel>
      </div>
    );
  }
}

Templates.propTypes = {
};
Templates.defaultProps = {
};

export default connect()(Templates);
