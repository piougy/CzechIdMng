import React from 'react';
import PropTypes from 'prop-types';
import classNames from 'classnames';
//
import AbstractFormComponent from '../AbstractFormComponent/AbstractFormComponent';
import EnumValue from '../EnumValue/EnumValue';

/**
 * Renders localized enum label -
 *
 * Look out: usable in forms - it's form component. Use EnumValue otherwise.
 *
 * @author Vít Švanda
 * @author Radek Tomiška
 */
class EnumLabel extends AbstractFormComponent {

  getBody() {
    const { labelSpan, label, componentSpan } = this.props;
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
            <EnumValue
              level={enumeration.getLevel(value)}
              value={value}
              enum={ enumeration }
              label={ enumeration.getNiceLabel(value) }/>
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
