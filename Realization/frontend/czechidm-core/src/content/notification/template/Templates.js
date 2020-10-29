import React from 'react';
import { connect } from 'react-redux';
import * as Basic from '../../../components/basic';
import { NotificationTemplateManager } from '../../../redux';
import TemplateTable from './TemplateTable';

const TABLE_UIKEY = 'NotificationTemplateTableUIKEY';

/**
* Content with notification templates.
*
* @author Ondřej Kopr
* @author Radek Tomiška
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
      <Basic.Div>
        { this.renderPageHeader() }

        <Basic.Panel>
          <TemplateTable manager={ this.getManager() } uiKey={ TABLE_UIKEY }/>
        </Basic.Panel>
      </Basic.Div>
    );
  }
}

Templates.propTypes = {
};
Templates.defaultProps = {
};

export default connect()(Templates);
