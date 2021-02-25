import React from 'react';
import { Link } from 'react-router-dom';
import pathToRegexp from 'path-to-regexp';
import _ from 'lodash';
//
import { SecurityManager } from '../../../redux';
import DefaultCell from './DefaultCell';
import Popover from '../Popover/Popover';
import Button from '../Button/Button';
// TODO: Localization service could not be accessed directly (advadced component)
import { LocalizationService } from '../../../services';

const TARGET_PARAMETER = '_target';

/**
 * Fills href parameter values from ginen rowData / entity object
 *
 * @param  {string} to href
 * @param  {object} rowData entity
 * @return {string} formated href
 *
 * @author Radek Tomiška
 */
function _resolveToWithParameters(to, rowData, target) {
  const parameterNames = pathToRegexp.parse(to);
  parameterNames.forEach(parameter => {
    if (parameter && parameter.name === TARGET_PARAMETER && target) {
      const targetValue = DefaultCell.getPropertyValue(rowData, target);
      if (targetValue) {
        to = to.replace(`:${TARGET_PARAMETER}`, targetValue);
      }
    }
  });
  const thingPath = pathToRegexp.compile(to);
  return thingPath(rowData);
}

function _linkFunction(to, rowIndex, data, event) {
  if (event) {
    event.preventDefault();
  }
  if (to) {
    to({ rowIndex, data, event });
  }
}

/**
 * Renders cell with link and text content.
 * Parametrs are automatically propagated from table / row / column

 * @param number rowIndex
 * @param array[json] input data
 * @param property column key
 * @param to - router link
 * @param className className
 * @param title - html title
 * @param target - optional entity property could be used as `_target` property in `to` property.
 * @param access - link could be accessed, if current user has access to target agenda. Otherwise propertyValue without link is rendered.
 * @param props other optional properties
 *
 * @author Radek Tomiška
 */
const LinkCell = ({ rowIndex, data, property, to, href, className, title, target, access, ...props }) => {
  const propertyValue = DefaultCell.getPropertyValue(data[rowIndex], property);
  const accessItems = (access && !Array.isArray(access)) ? [access] : access;
  // when is property and accessItems null, then return only default cell
  if (!propertyValue) {
    return <DefaultCell {...props}/>;
  }
  // construct html link href
  let _href = '#';
  if (_.isFunction(to) && href) {
    if (_.isFunction(href)) {
      _href = href({ data, rowIndex, property});
    } else {
      _href = href;
    }
  }
  //
  return (
    <DefaultCell {...props}>
      {
        (accessItems && !SecurityManager.hasAccess(accessItems))
        ?
        <span>
          {
            SecurityManager.isDenyAll(accessItems)
            ?
            propertyValue
            :
            <Popover
              level="warning"
              title={LocalizationService.i18n('security.access.link.denied')}
              value={
                <span>
                  {
                    [...accessItems.map((accessItem) => {
                      if (SecurityManager.hasAccess(accessItem)) {
                        return null;
                      }
                      return (
                        <div>
                          {/* TODO: make appropriate enum and refactor security service etc. */}
                          <strong>{ LocalizationService.i18n(`enums.AccessTypeEnum.${accessItem.type}`) }</strong>
                          {
                            !accessItem.authorities
                            ||
                            <span>
                              : <div>{ accessItem.authorities.join(', ') }</div>
                            </span>
                          }
                        </div>
                      );
                    }).values()]
                  }
                </span>
              }>
              {
                <Button level="link" style={{ padding: 0 }}>{ propertyValue }</Button>
              }
            </Popover>
          }
        </span>
        :
        <span>
          {
            _.isFunction(to)
            ?
            <Link
              to={ _href }
              onClick={ _linkFunction.bind(this, to, rowIndex, data) }
              title={ title }>
              { propertyValue }
            </Link>
            :
            <Link to={ _resolveToWithParameters(to, data[rowIndex], target) } title={ title } className={ className }>
              { propertyValue }
            </Link>
          }
        </span>
      }
    </DefaultCell>
  );
};

export default LinkCell;
