import React, { PropTypes } from 'react';
import { Link } from 'react-router';
//
import * as Basic from '../../basic';
import OperationStateEnum from '../../../enums/OperationStateEnum';
import { AttachmentService } from '../../../services';

/**
* Operation result component - shows enum value and result code with flash message
*
* TODO: less file - move inner styles there
*
* @author Patrik Stloukal
* @author Radek Tomiška
*
*/

const PARTIAL_CONTENT_STATUS = 206;

export default class OperationResult extends Basic.AbstractContextComponent {

  constructor(props, context) {
    super(props, context);
    this.attachmentService = new AttachmentService();
  }

  getComponentKey() {
    return 'component.advanced.OperationResult';
  }

  /**
   * Renders popover info card
   */
  _renderPopover() {
    const { value, stateLabel } = this.props;
    const message = this.getFlashManager().convertFromResultModel(value.model);
    //
    return (
      <div style={{ whiteSpace: 'nowrap' }}>
        <Basic.EnumValue
          level={ message ? message.level : null }
          value={ value.state }
          enum={ OperationStateEnum }
          label={ stateLabel } />
        <Basic.Popover
          ref="popover"
          trigger={['click']}
          value={ this._getPopoverContent() }
          className="abstract-entity-info-popover"
          rendered={ value.code !== null && value.code !== undefined }>
          {
            <span>
              <Basic.Button
                level="link"
                style={{ padding: 0, marginLeft: 3 }}
                title={ this.i18n('link.popover.title') }
                icon="fa:info-circle"/>
            </span>
          }
        </Basic.Popover>
      </div>
    );
  }

  /**
   * Returns popover info content
   *
   * @param  {object} result
   */
  _getPopoverContent() {
    const { value, detailLink } = this.props;
    //
    const linkLabel = (
      <span>
        <Basic.Icon value="fa:angle-double-right"/>
        {' '}
        {this.i18n('link.detail.label')}
      </span>
    );
    //
    return (
      <Basic.Panel>
        <Basic.PanelHeader rendered={ value.model !== null }>
          { this.i18n('result.code') }
          {': '}
          { value.code }
        </Basic.PanelHeader>
        <Basic.PanelBody style={{ padding: 2 }}>
          <Basic.FlashMessage
            message={ this.getFlashManager().convertFromResultModel(value.model) }
            style={{ wordWrap: 'break-word', margin: 0 }}/>
        </Basic.PanelBody>
        {
          !detailLink
          ||
          <Basic.PanelFooter>
            {
              typeof detailLink === 'function'
              ?
              <a href="#" onClick={ this._showDetail.bind(this) }>
                { linkLabel }
              </a>
              :
              <Link to={ detailLink }>
                { linkLabel }
              </Link>
            }
          </Basic.PanelFooter>
        }
      </Basic.Panel>
    );
  }

  _getDownloadComponent(value) {
    const { downloadLinkPrefix, downloadLinkSuffix } = this.props;
    const model = value.model;

    if (model && model.statusCode === PARTIAL_CONTENT_STATUS) {
      const parameters = model.parameters;
      let attachmentId = null;
      // attachmentId must exists
      if (parameters && parameters.attachmentId) {
        attachmentId = parameters.attachmentId;
      } else {
        return null;
      }

      const downloadUrl = this.attachmentService.getDownloadUrl(attachmentId, downloadLinkPrefix, downloadLinkSuffix);
      return (
        <a
          key={ `attachment-download-${attachmentId}` }
          href={ downloadUrl }
          title={ this.i18n('button.download')}
          className="btn btn-xs btn-primary">
          <Basic.Icon value="fa:download" />
          {' '}
          { this.i18n('button.download') }
        </a>
      );
    }
    return null;
  }

  _showDetail(event) {
    if (event) {
      event.preventDefault();
    }
    //
    // close popover
    this.refs.popover.close();
    // use link function
    this.props.detailLink();
  }

  /**
   * Renders full info card (with exception stacktrace etc.)
   */
  _renderFull() {
    const { value, stateLabel, header } = this.props;
    const message = this.getFlashManager().convertFromResultModel(value.model);
    const downloadComponent = this._getDownloadComponent(value);
    //
    return (
      <div>
        <Basic.ContentHeader text={ header === null ? this.i18n('result.header') : header }/>

        <div style={{ marginBottom: 15 }}>
          { downloadComponent }
          <Basic.EnumValue
            level={ message ? message.level : null }
            value={value.state}
            style={{ marginLeft: downloadComponent ? 15 : 0 }}
            enum={ OperationStateEnum }
            label={ stateLabel }/>
          {
            (!value || !value.code)
            ||
            <span style={{ marginLeft: value.state ? 15 : 0 }}>
              { this.i18n('result.code') }: { value.code }
            </span>
          }
          <Basic.FlashMessage message={ message } style={{ margin: '15px 0 0 0' }}/>
        </div>
        {
          (!value || !value.stackTrace)
          ||
          <div>
            <textArea
              rows="10"
              value={value.stackTrace}
              readOnly
              style={{ width: '100%', marginBottom: 15 }}/>
          </div>
        }
      </div>
    );
  }

  render() {
    const { value, face, rendered, showLoading } = this.props;
    if (!rendered || !value) {
      return null;
    }
    if (showLoading) {
      return (
        <Basic.Icon value="refresh" showLoading />
      );
    }
    //
    switch (face) {
      case 'popover': {
        return this._renderPopover();
      }
      default: {
        return this._renderFull();
      }
    }
  }

}

OperationResult.propTypes = {
  ...Basic.AbstractContextComponent.propTypes,
  /**
   * OperationResult object
   * @type {OperationResult}
   */
  value: PropTypes.object,
  /**
   * Decorator
   */
  face: PropTypes.oneOf(['popover', 'full']),
  /**
   * Custom label
   */
  stateLabel: PropTypes.string,
  /**
   * link to detail
   *
   * @type {[type]}
   */
  detailLink: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.func
  ]),
  /**
   * Header text
   * @type {[type]}
   */
  header: PropTypes.string,
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
  downloadLinkSuffix: PropTypes.string
};
OperationResult.defaultProps = {
  ...Basic.AbstractContextComponent.defaultProps,
  value: null,
  face: 'popover',
  stateLabel: null,
  rendered: true,
  detailLink: null,
  header: null, // default text from component locale will be used
  downloadLinkPrefix: null,
  downloadLinkSuffix: null
};
