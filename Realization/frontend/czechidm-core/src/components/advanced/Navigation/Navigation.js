import React, { PropTypes } from 'react';
import { Link } from 'react-router';
import { connect } from 'react-redux';
import classnames from 'classnames';
//
import * as Basic from '../../basic';
import {ConfigurationManager} from '../../../redux/data';
import {SecurityManager} from '../../../redux';
import { getNavigationItems, resolveNavigationParameters } from '../../../redux/layout/layoutActions';
import NavigationItem from './NavigationItem';
import NavigationSeparator from './NavigationSeparator';

/**
 * Top navigation
 */
export class Navigation extends Basic.AbstractContextComponent {

  constructor(props, context) {
    super(props, context);
  }

  componentDidMount() {
    this.initSideMenu();
  }

  componentDidUpdate() {
    // this is needed - menu is updated after login
    this.initSideMenu();
  }

  initSideMenu() {
    if (typeof $ === undefined) {
      return;
    }
    const sideMenu = $('#side-menu');
    if (!sideMenu || sideMenu === undefined) {
      return;
    }

    sideMenu.metisMenu({
      toggle: true
    });

    $(window).bind('load resize', function sidebarResize() {
      let topOffset = 50;
      const width = (this.window.innerWidth > 0) ? this.window.innerWidth : this.screen.width;
      if (width < 768) {
        $('div.navbar-collapse').addClass('collapse');
        topOffset = 100; // 2-row-menu
      } else {
        $('div.navbar-collapse').removeClass('collapse');
      }

      let height = ((this.window.innerHeight > 0) ? this.window.innerHeight : this.screen.height) - 1;
      height = height - topOffset;
      if (height < 1) height = 1;
      if (height > topOffset) {
        $('#content-wrapper').css('min-height', (height) + 'px');
      }
    });
    /*
    TODO: default menu collapse by url? Now is setted by react
    var url = window.location;
    var element = $('ul.nav a').filter(function() {
        return this.href === url || url.href.indexOf(this.href) === 0;
    }).addClass('active').parent().parent().addClass('in').parent();
    if (element.is('li')) {
        element.addClass('active');
    }*/
  }

  renderNavigationItems(section = 'main') {
    const { navigation, userContext, selectedNavigationItems } = this.props;
    const items = getNavigationItems(navigation, null, section, userContext);
    return this._renderNavigationItems(items, userContext, selectedNavigationItems);
  }

  _renderNavigationItems(items, userContext, selectedNavigationItems) {
    if (!items) {
      return null;
    }
    const renderedItems = [];
    for (const item of items) {
      const renderedItem = this.renderNavigationItem(item, userContext, selectedNavigationItems[0]);
      if (renderedItem) { // can be null
        renderedItems.push(renderedItem);
      }
    }
    return renderedItems;
  }

  _resolveNavigationItemText(item, userContext) {
    const labelParams = resolveNavigationParameters(userContext);
    labelParams.defaultValue = item.label;

    if (item.labelKey) {
      return (
          <span>{this.i18n(item.labelKey, labelParams)}</span>
      );
    }
    if (item.label) {
      return (
          <span>{item.label}</span>
      );
    }
    return (
      <span className="visible-xs-inline"> {this.i18n(item.titleKey, { defaultValue: item.title })}</span>
    );
  }

  renderNavigationItem(item, userContext, activeItem, titlePlacement = 'bottom') {
    switch (item.type) {
      case 'DYNAMIC': {
        return (
          <NavigationItem
            id={`nav-item-${item.id}`}
            key={`nav-item-${item.id}`}
            to={item.to}
            title={this.i18n(item.titleKey, { defaultValue: item.title })}
            titlePlacement={titlePlacement}
            icon={item.icon}
            iconColor={item.iconColor}
            active={activeItem === item.id}
            text={this._resolveNavigationItemText(item, userContext)}/>
        );
      }
      case 'TAB': {
        // tab is not visible in menu
        return null;
      }
      case 'SEPARATOR': {
        return <NavigationSeparator text={this._resolveNavigationItemText(item, userContext)} />;
      }
      default: {
        this.getLogger().warn('[Advanced.Navigation] ' + item.type + ' type not implemeted for item id [' + item.id + ']');
        return null;
      }
    }
  }

  // TODO: refator all sibedars (react component, drop original sibebar = detailTabs)
  renderSidebarItems(parentId = null, level = 0) {
    const { navigation, userContext, selectedNavigationItems } = this.props;
    level = level + 1;
    const levelItems = getNavigationItems(navigation, parentId, 'main', userContext);
    if (!levelItems || levelItems.length === 0) {
      return null;
    }

    const items = [];
    for (const levelItem of levelItems) {
      if (levelItem.type !== 'DYNAMIC' && levelItem.type !== 'SEPARATOR') {
        continue;
      }
      const children = this.renderSidebarItems(levelItem.id, level);
      const isActive = selectedNavigationItems.length >= level && selectedNavigationItems[level - 1] === levelItem.id;
      if (children) {
        items.push(
          <li key={`nav-item-${levelItem.id}`} className={isActive ? 'has-children active' : 'has-children'}>
            <a href="#">
              <Basic.Icon icon={levelItem.icon} color={levelItem.iconColor}/>
              {this._resolveNavigationItemText(levelItem, userContext)}
              <span className="fa arrow"></span>
            </a>
            {children}
          </li>
        );
      } else {
        const item = this.renderNavigationItem(levelItem, userContext, selectedNavigationItems.length >= level ? selectedNavigationItems[level - 1] : null, 'right');
        if (item) {
          items.push(item);
        }
      }
    }

    if (items.length === 0) {
      return null;
    }

    const classNames = classnames(
      'nav',
      { 'metismenu': level === 1 },
      { 'nav-second-level': level === 2 },
      { 'nav-third-level': level === 3 },
      { 'hidden': level > 3 }, // only three levels are supported
      { 'in': (selectedNavigationItems.length > level - 1 && selectedNavigationItems[level - 2]) === parentId },
      { 'collapse': parentId && !this._isSelected(selectedNavigationItems, parentId) && (selectedNavigationItems.length > level - 1 && selectedNavigationItems[level - 2]) !== parentId }
    );
    return (
      <ul
        id={level === 1 ? 'side-menu' : 'side-menu-' + level}
        className={classNames}>
        {items}
      </ul>
    );
  }

  _isSelected(selectedNavigationItems, parentId) {
    if (!parentId || !selectedNavigationItems) {
      return false;
    }
    for (const selectedNavigationItem of selectedNavigationItems) {
      if (parentId === selectedNavigationItem) {
        return true;
      }
    }
    return false;
  }

  render() {
    const { environment, userContext } = this.props;

    let environmentLabel = null;
    if (environment) {
      const environmentClassName = classnames(
        'label',
        {'label-success': environment === 'development'},
        {'label-warning': environment !== 'development'},
        {'hidden': environment === 'production'}
      );
      environmentLabel = (
        <p className="navbar-text hidden-xs" title={this.i18n('environment.' + environment + '.title', { defaultValue: environment })}>
          <span className={environmentClassName}>
            <span className="hidden-sm">{this.i18n('environment.' + environment + '.label', { defaultValue: environment })}</span>
            <span className="visible-sm-inline">{this.i18n('environment.' + environment + '.short', { defaultValue: environment })}</span>
          </span>
        </p>
      );
    }

    const mainItems = this.renderNavigationItems('main');
    const systemItems = this.renderNavigationItems('system');
    const sidebarItems = this.renderSidebarItems();

    return (
      <div>
        <header>
          <nav className="navbar navbar-default navbar-static-top" style={{ marginBottom: 0 }}>
            <div className="navbar-header">
              <button type="button" className="navbar-toggle collapsed" data-toggle="collapse" data-target=".navbar-collapse">
                <span className="sr-only">{this.i18n('navigation.toogle')}</span>
                <span className="icon-bar"></span>
                <span className="icon-bar"></span>
                <span className="icon-bar"></span>
              </button>
              <Link to="/" title="Úvodní stránka" className="home">
                {' '}
              </Link>
            </div>
            <div id="navbar" className="navbar-collapse">
              {
                !SecurityManager.isAuthenticated(userContext)
                ?
                <ul className="nav navbar-nav">
                  {mainItems}
                </ul>
                :
                null
              }
              <ul className="nav navbar-nav navbar-right">
                {environmentLabel}
                {systemItems}
              </ul>
            </div>
            {
              SecurityManager.isAuthenticated(userContext)
              ?
              <div className="navbar-default sidebar" role="navigation">
                <div className="sidebar-nav navbar-collapse">
                  {sidebarItems}
                </div>
              </div>
              :
              null
            }
          </nav>
        </header>
      </div>
    );
  }
}

Navigation.propTypes = {
  navigation: PropTypes.object,
  selectedNavigationItems: PropTypes.array,
  environment: PropTypes.string,
  userContext: PropTypes.object
};

Navigation.defaultProps = {
  navigation: null,
  selectedNavigationItems: null,
  environment: null,
  userContext: null
};

Navigation.contextTypes = {
  ...Basic.AbstractContextComponent.contextTypes,
  router: PropTypes.object.isRequired
};

function select(state) {
  let environment = ConfigurationManager.getPublicValue(state, 'idm.pub.app.stage');
  if (environment) {
    environment = environment.toLowerCase();
  }
  return {
    navigation: state.layout.get('navigation'),
    selectedNavigationItems: state.layout.get('selectedNavigationItems'),
    environment,
    userContext: state.security.userContext
  };
}

export default connect(select)(Navigation);
