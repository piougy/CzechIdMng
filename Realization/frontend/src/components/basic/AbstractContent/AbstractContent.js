

import React from 'react';
//
import AbstractContextComponent from '../AbstractContextComponent/AbstractContextComponent';
import { selectNavigationItems, selectNavigationItem, selectSidebarItem } from '../../../redux/Layout/layoutActions';

/**
* Basic content = page representation
* Requires store and router context
*/
export default class AbstractContent extends AbstractContextComponent {

  constructor(props, context) {
    super(props, context);
  }

  /**
   * select items in navigation by content / page
   *
   * @param  {array} selectedNavigationItems Array of selected navigation item. Can contains null values for select specified navigation level
   */
  selectNavigationItems(selectedNavigationItems) {
    this.context.store.dispatch(selectNavigationItems(selectedNavigationItems));
  }

  /**
   * select item in navigation by content / page id
   *
   * @param  {string} selectedNavigationItem id
   */
  selectNavigationItem(selectedNavigationItem) {
    this.context.store.dispatch(selectNavigationItem(selectedNavigationItem));
  }

  /**
   * @deprecated - use #selectNavigationItems
   * select item in sidebar by content / page
   *
   * @param  {string} selectedSidebarItem
   */
  selectSidebarItem(selectedSidebarItem) {
    this.context.store.dispatch(selectNavigationItems([null, selectedSidebarItem]));
  }

  /**
   * Return content identifier, with can be used in localization etc.
   *
   * @return {string} content identifier
   */
  getContentKey() {
    return null;
  }

  /**
   * Automatically prepend page prefix to localization key
   * If overridened key isn't found in localization, then previous key is used
   *
   * @param  {string} key     localization key
   * @param  {object} options parameters
   * @return {string}         localized message
   */
  i18n(key, options) {
    const contentKey = this.getContentKey();
    //
    const resultKeyWithModule = (key.indexOf(':') > -1 || !contentKey) ? key : `${contentKey}.${key}`;
    const resultKeyWithoutModule = (resultKeyWithModule.indexOf(':') > -1) ? resultKeyWithModule.split(':')[1] : resultKeyWithModule;
    const i18nValue = super.i18n(resultKeyWithModule, options);
    if (i18nValue === resultKeyWithModule || i18nValue === resultKeyWithoutModule) {
      return super.i18n(key, options);
    }
    return i18nValue;
  }
}

AbstractContent.propTypes = {
  ...AbstractContextComponent.propTypes
}

AbstractContent.defaultProps = {
  ...AbstractContextComponent.defaultProps
}

AbstractContent.contextTypes = {
  ...AbstractContextComponent.contextTypes,
  router:  React.PropTypes.object//.isRequired
}
