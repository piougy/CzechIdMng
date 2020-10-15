import PropTypes from 'prop-types';
import React from 'react';
import { connect } from 'react-redux';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import * as Utils from '../../utils';
import { NotificationAttachmentManager } from '../../redux';

const notificationAttachmentManager = new NotificationAttachmentManager(); // default manager

/**
* Notification attachments.
*
* @author Radek Tomiška
* @since 10.6.0
*/
export class NotificationAttachmentTable extends Advanced.AbstractTableContent {

  getContentKey() {
    return 'content.notification';
  }

  getManager() {
    return this.props.manager;
  }

  render() {
    const { uiKey, manager, forceSearchParameters, rendered } = this.props;
    if (!rendered) {
      return null;
    }
    //
    return (
      <Basic.Div>
        <Advanced.Table
          ref="table"
          uiKey={ uiKey }
          manager={ manager }
          forceSearchParameters={ forceSearchParameters }
          _searchParameters={ this.getSearchParameters() }
          header={ this.i18n('attachments.header') }>

          <Advanced.Column property="name" header={ this.i18n('entity.Attachment.name') } width="60%"/>
          <Advanced.Column
            header={ this.i18n('entity.Attachment.filesize') }
            cell={
              ({ rowIndex, data }) => {
                const entity = data[rowIndex];
                if (!entity._embedded || !entity._embedded.attachment) {
                  // attachment could be purged from storage
                  return null;
                }
                return (
                  <span>{ entity._embedded.attachment.filesize }</span>
                );
              }
            }/>
          <Advanced.Column
            header={ this.i18n('label.download') }
            cell={
              ({ rowIndex, data }) => {
                const entity = data[rowIndex];
                if (!entity._embedded || !entity._embedded.attachment) {
                  // attachment could be purged from storage
                  return null;
                }
                return (
                  <a
                    key={ `attachment-download-${entity.id}` }
                    href={ this.getManager().getService().getDownloadUrl(entity.id) }
                    title={ this.i18n('button.download') }
                    className="btn btn-primary btn-xs"
                    style={{ color: 'white' }}>
                    <Basic.Icon value="fa:download" />
                    {' '}
                    { this.i18n('button.download') }
                  </a>
                );
              }
            }/>

        </Advanced.Table>
      </Basic.Div>
    );
  }
}

NotificationAttachmentTable.propTypes = {
  uiKey: PropTypes.string.isRequired,
  manager: PropTypes.object,
  forceSearchParameters: PropTypes.object
};

NotificationAttachmentTable.defaultProps = {
  manager: notificationAttachmentManager,
  rendered: true
};

function select(state, component) {
  return {
    _searchParameters: Utils.Ui.getSearchParameters(state, component.uiKey)
  };
}

export default connect(select, null, null, { forwardRef: true })(NotificationAttachmentTable);
