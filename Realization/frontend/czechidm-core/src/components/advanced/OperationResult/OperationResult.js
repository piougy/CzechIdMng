import React, { PropTypes } from 'react';
//
import * as Basic from '../../basic';
import AbstractEntityInfo from '../EntityInfo/AbstractEntityInfo';

class OperationResult extends Basic.AbstractContextComponent {

  constructor(props, context) {
    super(props, context);
  }

  /**
   * Returns popover info content
   *
   * @param  {object} result
   */
  getPopoverContent(result) {
    const code = this.i18n('content.scheduler.all-tasks.detail.resultCode') + ': ';
    return (
    <Basic.Panel>
      <Basic.PanelHeader rendered={result.model !== null}>
        <Basic.FlashMessage message={this.getFlashManager().convertFromResultModel(result.model)} style={{ margin: -15, marginBottom: -10, marginTop: -10, borderRadius: 2, borderBottomLeftRadius: 0, borderBottomRightRadius: 0, overflowWrap: 'break-word' }}/>
      </Basic.PanelHeader>
      <Basic.Panel style={{ padding: 10, border: 0 }}>
          { code }
          { result.code }
      </Basic.Panel>
    </Basic.Panel>
    );
  }

  /**
   * Returns popovers title
   *
   */
  getPopoverTitle() {
    const code = this.i18n('content.scheduler.all-tasks.detail.resultCode') + ': ';
    return (
      <div>
        { code }
        <Basic.Icon value="fa:info-circle" disabled/>
      </div>
    );
  }

  render() {
    const { style, result, rendered } = this.props;
    if (!rendered || result === null) {
      return null;
    }
    if (result.code === null) {
      return null;
    }
    //
    return (
      <Basic.Popover
        trigger={['click']}
        value={ this.getPopoverContent(result) }
        className="abstract-entity-info-popover"
        >
        {
          <span
            style={ style }>
            <Basic.Button
              level="link"
              style={{ padding: 0 }}
              title={ this.i18n('component.advanced.EntityInfo.link.popover.title') }>
              { this.getPopoverTitle() }
            </Basic.Button>
          </span>
        }
      </Basic.Popover>
    );
  }

}

OperationResult.propTypes = {
  ...AbstractEntityInfo.propTypes,
  result: PropTypes.object,
  rendered: PropTypes.bool
};
OperationResult.defaultProps = {
  ...AbstractEntityInfo.defaultProps,
  result: null,
  rendered: true
};

export default (OperationResult);
