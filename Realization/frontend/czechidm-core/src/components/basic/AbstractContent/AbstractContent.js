import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
//
import AbstractContextComponent from '../AbstractContextComponent/AbstractContextComponent';
import PageHeader from '../PageHeader/PageHeader';
import Icon from '../Icon/Icon';
import { selectNavigationItems, selectNavigationItem, getNavigationItem } from '../../../redux/layout/layoutActions';

/**
* Basic content = page representation
* Requires store and router context
*/
export default class AbstractContent extends AbstractContextComponent {

  constructor(props, context) {
    super(props, context);
  }

  componentDidMount() {
    if (this.getNavigationKey()) {
      this.selectNavigationItem(this.getNavigationKey());
    }
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
   * Returns navigation for given id. If no navigationId is given, then returns navigation item by defined navigation key.
   *
   * @param  {string} navigationId
   * @return {object} navigationItem
   */
  getNavigationItem(navigationId = null) {
    if (!navigationId) {
      navigationId = this.getNavigationKey();
    }
    if (!navigationId) {
      return null;
    }
    return this.context.store.dispatch(getNavigationItem(this.context.store.getState().layout, navigationId));
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
   * Return component identifier, with can be used in localization etc.
   *
   * @return {string} component identifier
   */
  getComponentKey() {
    return this.getContentKey() || super.getComponentKey();
  }

  /**
   * Return navigation identifier, with can be used to show content header, title, icon ...
   *
   * @return {string} navigation item identifier
   */
  getNavigationKey() {
    return null;
  }

  /**
   * Default Page header with page title based on navigation item
   *
   * @return {element} react element
   */
  renderPageHeader() {
    const navigationItem = this.getNavigationItem() || {};
    //
    return (
      <PageHeader>
        <Helmet title={this.i18n('title')} />
        <Icon value={navigationItem.icon}/>
        {' '}
        {this.i18n('header')}
      </PageHeader>
    );
  }
}

AbstractContent.propTypes = {
  ...AbstractContextComponent.propTypes
};

AbstractContent.defaultProps = {
  ...AbstractContextComponent.defaultProps
};

AbstractContent.contextTypes = {
  ...AbstractContextComponent.contextTypes,
  router: PropTypes.object // .isRequired
};
