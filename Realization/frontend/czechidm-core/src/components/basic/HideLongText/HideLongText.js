import React, { PropTypes } from 'react';

import UiUtils from '../../../utils/UiUtils';
import AbstractComponent from '../AbstractComponent/AbstractComponent';

/**
* Simple component to make text shorter
 */
class HideLongText extends AbstractComponent {

  constructor(props) {
    super(props);
  }

  render() {
    const { text, maxLen, cutPointEnd, cutChar, className } = this.props;
    let shortText = '';
    if (text.length > maxLen) {
      if (cutPointEnd) {
        shortText = UiUtils.substringBegin(text, maxLen, cutChar);
        shortText = shortText + '...';
      } else {
        shortText = UiUtils.substringEnd(text, maxLen, cutChar);
        shortText = '...' + shortText;
      }
    } else {
      shortText = text;
    }
    return <span className={className}>{shortText}</span>;
  }
}

HideLongText.propTypes = {
  /**
   * String to be shorten
   */
  text: PropTypes.string,
  /**
   * Number of lenght for string to be shorten to
   */
  maxLen: PropTypes.number,
  /**
   * Character acording which you cut
   */
  cutChar: PropTypes.string,
  /**
   * Boolean that indicates if you cut from begining or ending
   */
  cutPointEnd: PropTypes.bool
};

HideLongText.defaultProps = {
  /**
   * default shorten value of maxLen
   */
  maxLen: 20,
  /**
   * default character for cutting
   */
  cutChar: ' ',
  /**
   * default point of cutting
   */
  cutPointEnd: true
};

export default HideLongText;
