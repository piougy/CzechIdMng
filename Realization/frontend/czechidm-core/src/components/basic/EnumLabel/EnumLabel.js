import React, { PropTypes } from 'react';
import classNames from 'classnames';
//
import AbstractFormComponent from '../AbstractFormComponent/AbstractFormComponent';
import Label from '../Label/Label';

class EnumLabel extends AbstractFormComponent {

  constructor(props) {
    super(props);
  }

  getBody() {
    const { labelSpan, label, componentSpan, style } = this.props;
    const enumeration = this.props.enum;
    const { value } = this.state;
    //
    const labelClassName = classNames(labelSpan, 'control-label');

    return (
      <div>
        {
          !label
          ||
          <label
            className={labelClassName}>
            {label}
          </label>
        }
        <div className={componentSpan} style={{ whiteSpace: 'nowrap' }}>
          <div style={{marginTop: '5px'}}>
            <Label style={style} level={enumeration.getLevel(value)} text = {enumeration.getNiceLabel(value)} className="label-form"/>
          </div>
        </div>
      </div>
    );
  }
}

EnumLabel.propTypes = {
  ...AbstractFormComponent.propTypes,
  enum: PropTypes.func.isRequired
};

EnumLabel.defaultProps = {
  ...AbstractFormComponent.defaultProps,
  readOnly: true
};


export default EnumLabel;
