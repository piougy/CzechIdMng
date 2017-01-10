import React from 'react';
//
import DefaultCell from './DefaultCell';
import Icon from '../Icon/Icon';

/**
 * Renders cell with link and text content.
 * Parametrs are automatically propagated from table / row / column

 * @param number rowIndex
 * @param array[json] input data
 * @param property column key
 * @param to - router link
 * @param className className
 * @param title - html title
 * @param props other optional properties
 */
const EnumCell = ({rowIndex, data, property, enumClass, ...props}) => {
  const propertyValue = DefaultCell.getPropertyValue(data[rowIndex], property);
  //
  let content = propertyValue;
  if (propertyValue && enumClass) {
    content = enumClass.getNiceLabel(propertyValue);
    //
    const icon = enumClass.getIcon(propertyValue);
    if (icon) {
      content = (
        <span>
          <Icon value={icon} style={{ marginRight: 3 }}/>
          { content }
        </span>
      );
    }
    const level = enumClass.getLevel(propertyValue);
    if (level) {
      content = (
        <span className={'label label-' + level}>
          { content }
        </span>
      );
    }
  }
  //
  return (
    <DefaultCell {...props}>
      { content }
    </DefaultCell>
  );
};

export default EnumCell;
