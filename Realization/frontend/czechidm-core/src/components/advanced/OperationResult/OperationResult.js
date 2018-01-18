import React, { PropTypes } from 'react';
//
import * as Basic from '../../basic';
import OperationStateEnum from '../../../enums/OperationStateEnum';
import AbstractEntityInfo from '../EntityInfo/AbstractEntityInfo';

/**
* Operation result component - shows enum value and result code with flash message
*
* @author Patrik Stloukal
*
*/
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

  /**
   * Returns enum value
   *
   */
  getEnumValue( enumLabel, state) {
    return (
      <Basic.EnumValue
        value={ state }
        enum={ OperationStateEnum }
        label={ enumLabel } />
    );
  }

  render() {
    const { style, result, rendered, enumLabel } = this.props;
    if (!rendered || result === null) {
      return null;
    }
    if (result.code === null) {
      return this.getEnumValue( enumLabel, result.state);
    }
    //
    return (
      <div>
      <span style={{display: 'block'}}>
        { this.getEnumValue( enumLabel, result.state) }
      </span>
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
    </div>
    );
  }

}

OperationResult.propTypes = {
  ...AbstractEntityInfo.propTypes,
  result: PropTypes.object,
  enumLabel: PropTypes.object,
  rendered: PropTypes.bool
};
OperationResult.defaultProps = {
  ...AbstractEntityInfo.defaultProps,
  result: null,
  enumLabel: null,
  rendered: true
};

export default (OperationResult);
