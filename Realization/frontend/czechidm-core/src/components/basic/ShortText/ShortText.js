import React, { PropTypes } from 'react';

import UiUtils from '../../../utils/UiUtils';
import AbstractComponent from '../AbstractComponent/AbstractComponent';
import Tooltip from '../Tooltip/Tooltip';

/**
* Simple component to make text shorter
*
* @author Marek Klement
 */
class ShortText extends AbstractComponent {

  constructor(props) {
    super(props);
  }

  render() {
    const { text, maxLength, cutPointEnd, cutChar, className, rendered } = this.props;
    if (!rendered) return null;
    let shortText = '';
    if (text.length > maxLength) {
      if (cutPointEnd) {
        shortText = UiUtils.substringBegin(text, maxLength, cutChar);
        shortText = shortText + '...';
      } else {
        shortText = UiUtils.substringEnd(text, maxLength, cutChar);
        shortText = '...' + shortText;
      }
    } else {
      shortText = text;
    }
    return (
      <Tooltip value={ text }>
        <span className={ className }>
          { shortText }
        </span>
      </Tooltip>
    );
  }
}

ShortText.propTypes = {
  /**
   * String to be shorten
   */
  text: PropTypes.string,
  /**
   * Number of lenght for string to be shorten to
   */
  maxLength: PropTypes.number,
  /**
   * Character acording which you cut
   */
  cutChar: PropTypes.string,
  /**
   * Boolean that indicates if you cut from begining or ending
   */
  cutPointEnd: PropTypes.bool
};

ShortText.defaultProps = {
  ...AbstractComponent.defaultProps,
  /**
   * default shorten value of maxLength
   */
  maxLength: 20,
  /**
   * default character for cutting
   */
  cutChar: ' ',
  /**
   * default point of cutting
   */
  cutPointEnd: true
};

export default ShortText;
