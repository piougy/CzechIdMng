

import React, { PropTypes } from 'react';
import classNames from 'classnames';
import Joi from 'joi';
//
import AbstractFormComponent from '../AbstractFormComponent/AbstractFormComponent';
import Label from '../Label/Label';

class EnumLabel extends AbstractFormComponent {

  constructor(props) {
    super(props);
  }

  getBody(feedback) {
    const {ref, labelSpan, label, componentSpan, placeholder, style, required, help } = this.props;
    const enumeration = this.props.enum;
    const {value, disabled, readOnly} = this.state;
    //
    const className = classNames('form-control');
    const labelClassName = classNames(labelSpan, 'control-label');

    let render =  (
      <div>
        {
          !label
          ||
          <label
            for={ref}
            className={labelClassName}>
            {label}
          </label>
        }
        <div className={componentSpan} style={{ whiteSpace: 'nowrap' }}>
          <div style={{marginTop: '5px'}}>
            <Label id={ref} style={style} level={enumeration.getLevel(value)} text = {enumeration.getNiceLabel(value)} className="label-form"/>
          </div>
        </div>
      </div>
    );
    return render;
  }
}

EnumLabel.propTypes = {
  ...AbstractFormComponent.propTypes,
  enum: React.PropTypes.object.isRequired
}

EnumLabel.defaultProps = {
  ...AbstractFormComponent.defaultProps,
  readOnly:true
}


export default EnumLabel;
