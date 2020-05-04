import React from 'react';
import PropTypes from 'prop-types';
//
import * as Basic from '../../basic';
import { AttachmentService } from '../../../services';

const attachmentService = new AttachmentService();

/**
* Download button component for operation result.
*
* @author Vít Švanda
*
*/
export default class OperationResultDownloadButton extends Basic.AbstractContextComponent {

  render() {
    const {operationResult, downloadLinkPrefix, downloadLinkSuffix, btnSize, style} = this.props;

    if (!operationResult) {
      return null;
    }
    const model = operationResult.model;

    if (!model || model.statusCode !== OperationResultDownloadButton.PARTIAL_CONTENT_STATUS) {
      return null;
    }
    let completeDownloadUrl = null;
    const parameters = model.parameters;
    let attachmentId = null;
    // attachmentId must exists
    if (parameters && parameters.attachmentId) {
      attachmentId = parameters.attachmentId;
      completeDownloadUrl = attachmentService.getDownloadUrl(attachmentId, downloadLinkPrefix, downloadLinkSuffix);
    }

    if (!completeDownloadUrl) {
      // downloadUrl must exists
      if (parameters && parameters.downloadUrl) {
        completeDownloadUrl = attachmentService.getDownloadUrl(null, parameters.downloadUrl);
      }
    }

    if (!completeDownloadUrl) {
      return null;
    }

    return (
      <a
        key={ `attachment-download-${attachmentId}` }
        href={ completeDownloadUrl }
        style={style}
        title={ this.i18n('button.download')}
        className={`btn ${btnSize} btn-primary`}>
        <Basic.Icon value="fa:download" />
        {' '}
        { this.i18n('button.download') }
      </a>
    );
  }
}

OperationResultDownloadButton.propTypes = {
  ...Basic.AbstractContextComponent.propTypes,
  /**
   * OperationResult object
   * @type {OperationResult}
   */
  operationResult: PropTypes.object,
  /**
   * Download link prefix for specific download url.
   * When is download link prefix null classic download url from attachment controller
   * will be used. Eq.: /attachments/{$attachmentId}/download
   *
   * @type {String}
   */
  downloadLinkPrefix: PropTypes.string,
  /**
   * Download link prefix for specific download url. Suffix can be used only with
   * prefix. Cant be used itself.
   *
   * @type {String}
   */
  downloadLinkSuffix: PropTypes.string,

  /**
   * Size of the download button.
   *
   * @type {String}
   */
  btnSize: PropTypes.string
};
OperationResultDownloadButton.defaultProps = {
  ...Basic.AbstractContextComponent.defaultProps,
  operationResult: null,
  downloadLinkPrefix: null,
  downloadLinkSuffix: null,
  btnSize: 'btn-xs'
};

OperationResultDownloadButton.PARTIAL_CONTENT_STATUS = 206;
