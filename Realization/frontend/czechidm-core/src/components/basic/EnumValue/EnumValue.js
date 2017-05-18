import React, { PropTypes } from 'react';
//
import AbstractComponent from '../AbstractComponent/AbstractComponent';
import Label from '../Label/Label';
import Icon from '../Icon/Icon';

/**
 * Simple enum formatter
 */
class EnumValue extends AbstractComponent {

  constructor(props) {
    super(props);
  }

  render() {
    const { rendered, value, style, ...others } = this.props;
    const enumClass = this.props.enum;
    //
    if (!rendered || !value) {
      return null;
    }

    let content = value;
    if (value && enumClass) {
      content = enumClass.getNiceLabel(value);
      //
      const icon = enumClass.getIcon(value);
      if (icon) {
        content = (
          <span>
            <Icon value={icon} style={{ marginRight: 3 }}/>
            { content }
          </span>
        );
      }
      const level = enumClass.getLevel(value);
      if (level) {
        content = (
          <Label style={style} level={level} text={content}/>
        );
      }
    }
    return content;
  }
}

EnumValue.propTypes = {
  /**
   * If component is rendered on page
   */
  rendered: PropTypes.bool,
  /**
   * enum value
   */
  value: PropTypes.string,
  enum: PropTypes.func.isRequired
};

EnumValue.defaultProps = {
  rendered: true
};


export default EnumValue;
