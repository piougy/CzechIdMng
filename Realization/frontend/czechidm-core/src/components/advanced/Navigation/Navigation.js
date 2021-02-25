import React from 'react';
import PropTypes from 'prop-types';
import { Link } from 'react-router-dom';
import { connect } from 'react-redux';
import classnames from 'classnames';
import Immutable from 'immutable';
//
import * as Basic from '../../basic';
import * as Utils from '../../../utils';
import ComponentService from '../../../services/ComponentService';
import ConfigLoader from '../../../utils/ConfigLoader';
import { LocalizationService } from '../../../services';
import {
  ConfigurationManager,
  SecurityManager,
  IdentityManager,
  DataManager
} from '../../../redux';
import {
  getNavigationItems,
  resolveNavigationParameters,
  collapseNavigation,
  i18nChange,
  selectNavigationItems
} from '../../../redux/config/actions';
import NavigationItem from './NavigationItem';
import NavigationSeparator from './NavigationSeparator';
import NavigationSearch from './NavigationSearch';

const componentService = new ComponentService();
const identityManager = new IdentityManager();
const securityManager = new SecurityManager();

/**
 * Top navigation
 *
 * @author Radek Tomiška
 */
export class Navigation extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    //
    this.state = {
      identityMenuShowLoading: false,
      modals: new Immutable.Map({}), // opened modal windows
      collapsed: true
    };
  }

  renderNavigationItems(section = 'main', dynamicOnly = true) {
    const { navigation, userContext, selectedNavigationItems } = this.props;
    //
    const items = getNavigationItems(navigation, null, section, userContext, null, dynamicOnly);
    //
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
      <span className="visible-xs-inline">
        { this.i18n(item.titleKey, { defaultValue: item.title }) }
      </span>
    );
  }

  renderNavigationItem(item, userContext, activeItem, titlePlacement = 'bottom') {
    switch (item.type) {
      case 'DYNAMIC': {
        const { modals} = this.state;
        //
        let ModalComponent = null;
        let onClick = null;
        if (item.modal) {
          // resolve modal component
          ModalComponent = componentService.getComponent(item.modal);
          onClick = (event) => {
            if (event) {
              event.preventDefault();
            }
            this.setState({
              modals: modals.set(item.modal, { show: true })
            });
          };
        }
        //
        return (
          <NavigationItem
            id={ `nav-item-${item.id}` }
            key={ `nav-item-${item.id}` }
            to={ item.to }
            title={ this.i18n(item.titleKey, { defaultValue: item.title }) }
            titlePlacement={ titlePlacement }
            icon={ item.icon }
            iconColor={ item.iconColor }
            active={ activeItem === item.id }
            text={ this._resolveNavigationItemText(item, userContext) }
            onClick={ onClick }>
            {
              !ModalComponent
              ||
              <ModalComponent
                show={ modals.has(item.modal) ? modals.get(item.modal).show : false }
                onHide={ () => { this.setState({ modals: modals.set(item.modal, { show: false }) }); } }/>
            }
          </NavigationItem>
        );
      }
      case 'TAB': {
        // tab is not visible in menu
        return null;
      }
      case 'SEPARATOR': {
        return (
          <NavigationSeparator
            id={ `nav-item-${item.id}` }
            key={ `nav-item-${item.id}` }
            text={ this._resolveNavigationItemText(item, userContext) } />
        );
      }
      default: {
        this.getLogger().warn(`[Advanced.Navigation] - [${ item.type }] type not implemeted for item id [${ item.id }]`);
        return null;
      }
    }
  }

  toogleNavigationCollapse(navigationCollapsed, event) {
    if (event) {
      event.preventDefault();
    }
    // FE change
    this.context.store.dispatch(collapseNavigation(!navigationCollapsed));
    // BE save navigation is collapsed
    const { userContext } = this.props;
    if (SecurityManager.isAuthenticated(userContext)) {
      this.context.store.dispatch(identityManager.saveCurrentProfile(userContext.id, {
        navigationCollapsed: !navigationCollapsed
      }));
    }
  }

  toogleNavigationItem(item, level, isActive, redirect = true, event) {
    if (event) {
      event.preventDefault();
    }
    if (!redirect) {
      // prevent to redirect on click on arrow - toogle navigation only
      event.stopPropagation();
    } else if (item.to) {
      this.context.history.push(item.to);
    }
    const { selectedNavigationItems } = this.props;
    const newNavigationState = level > 0 ? selectedNavigationItems.slice(0, level - 1) : [];
    if (!isActive) {
      // show another level
      newNavigationState.push(item.id);
    }
    //
    this.context.store.dispatch(selectNavigationItems(newNavigationState));
    // prevent default link
    return false;
  }

  renderSidebarItems(parentId = null, level = 0) {
    const { navigation, navigationCollapsed, userContext, selectedNavigationItems } = this.props;
    level += 1;
    const levelItems = getNavigationItems(navigation, parentId, 'main', userContext, null, true);
    if (!levelItems || levelItems.length === 0) {
      return null;
    }

    const items = [];
    for (const levelItem of levelItems) {
      if (levelItem.type !== 'DYNAMIC' && levelItem.type !== 'SEPARATOR') {
        continue;
      }
      const childrenItems = getNavigationItems(navigation, levelItem.id, 'main', userContext, null, true);
      //
      if (childrenItems.length === 1 && childrenItems[0].path === levelItem.path) {
        // if menu contains only one subitem, which leeds to the same path - sub menu is truncated
        const item = this.renderNavigationItem(
          levelItem,
          userContext,
          selectedNavigationItems.length >= level ? selectedNavigationItems[level - 1] : null,
          'right'
        );
        if (item) {
          items.push(item);
        }
      } else {
        const children = this.renderSidebarItems(levelItem.id, level);
        let isActive = selectedNavigationItems.length >= level && selectedNavigationItems[level - 1] === levelItem.id;
        const isExpanded = isActive;
        // last active child exists => not active
        if (isActive && childrenItems && selectedNavigationItems.length > level) {
          const nextSelectedItemId = selectedNavigationItems[level];
          const child = childrenItems.find(c => c.id === nextSelectedItemId);
          if (child) {
            isActive = false;
          }
        }
        let parentRedirect = false; // show content, when menu is expanded
        if (navigation.get(ConfigLoader.NAVIGATION_BY_PARENT).has(levelItem.id)) {
          const childAlias = navigation.get(ConfigLoader.NAVIGATION_BY_PARENT).get(levelItem.id).toArray()
            .find(c => c.path === levelItem.path); // childer by the same path
          //
          if (childAlias && childAlias.type === 'MAIN-MENU') {
            parentRedirect = !childAlias.access || SecurityManager.hasAccess(childAlias.access);
          }
        }
        //
        if (children && !navigationCollapsed) {
          items.push(
            <li
              key={ `nav-item-${ levelItem.id }` }
              className={ isExpanded ? 'has-children expanded' : 'has-children'}>
              <Basic.Tooltip
                id={ `${ levelItem.id }-tooltip` }
                placement="right"
                value={ this.i18n(levelItem.titleKey, { defaultValue: levelItem.title }) }>
                <Link
                  to={ levelItem.to || '#' }
                  onClick={
                    this.toogleNavigationItem.bind(
                      this,
                      levelItem,
                      level,
                      isActive,
                      parentRedirect
                    )
                  }
                  className={ isActive && parentRedirect ? 'active' : '' }>
                  <Basic.Icon icon={ levelItem.icon } color={ levelItem.iconColor }/>
                  {
                    navigationCollapsed
                    ?
                    null
                    :
                    <span>
                      { this._resolveNavigationItemText(levelItem, userContext) }
                      <Basic.Icon
                        onClick={ this.toogleNavigationItem.bind(this, levelItem, level, isActive, false) }
                        value={ `fa:angle-${ isActive ? 'down' : 'left' }` }
                        className="arrow-icon" />
                    </span>
                  }
                </Link>
              </Basic.Tooltip>
              { children }
            </li>
          );
        } else {
          const item = this.renderNavigationItem(
            levelItem,
            userContext,
            selectedNavigationItems.length >= level ? selectedNavigationItems[level - 1] : null,
            'right'
          );
          if (item) {
            items.push(item);
          }
        }
      }
    }

    if (items.length === 0) {
      return null;
    }

    if (level === 1) { // collapse menu
      items.push(
        <li key="navigation-collapse">
          <Basic.Tooltip
            id="navigation-collapse-tooltip"
            placement="right"
            value={ navigationCollapsed ? this.i18n('navigation.expand.label') : this.i18n('navigation.collapse.label') }>
            <a href="#" onClick={ this.toogleNavigationCollapse.bind(this, navigationCollapsed) }>
              <Basic.Icon value={ `arrow-${navigationCollapsed ? 'right' : 'left'}` }/>
              <span className="item-text" style={{ color: '#bbb' }}>
                {
                  navigationCollapsed
                  ?
                  <span>{ this.i18n('navigation.expand.label') }</span>
                  :
                  <span>{ this.i18n('navigation.collapse.label') }</span>
                }
              </span>
            </a>
          </Basic.Tooltip>
        </li>
      );
    }

    const classNames = classnames(
      'nav',
      { 'nav-second-level': level === 2 },
      { 'nav-third-level': level === 3 },
      { hidden: (level > 3 || (navigationCollapsed && level > 1)) }, // only three levels are supported
      { in: (selectedNavigationItems.length > level - 1 && selectedNavigationItems[level - 2]) === parentId },
      { collapse: parentId
        && !this._isSelected(selectedNavigationItems, parentId)
        && (selectedNavigationItems.length > level - 1
        && selectedNavigationItems[level - 2]) !== parentId }
    );
    return (
      <ul
        id={ level === 1 ? 'side-menu' : `side-menu-${ level }` }
        className={ classNames }>
        { items }
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

  _i18nChange(lng, event) {
    if (event) {
      event.preventDefault();
    }
    //
    this.context.store.dispatch(i18nChange(lng, () => {
      // RT: reload is not needed anymore, most of component was refectored to listen redux state.
      // RT: filled form values are not rerendered (e.g. filled filters), when locale is changed, but i think is trivial issue
      // window.location.reload();
      //
      const { userContext } = this.props;
      if (SecurityManager.isAuthenticated(userContext)) {
        this.context.store.dispatch(identityManager.saveCurrentProfile(userContext.id, {
          preferredLanguage: lng
        }));
      }
    }));
  }

  _onSwitchUserLogout(event) {
    if (event) {
      event.preventDefault();
    }
    const username = this.props.userContext.originalUsername;
    //
    this.context.store.dispatch(securityManager.switchUserLogout((result) => {
      if (result) {
        this.addMessage({
          level: 'success',
          key: 'core-switch-user-success',
          message: this.i18n('content.identity.switch-user.message.success', { username })
        });
        this.context.history.replace(`/`);
      }
    }));
  }

  render() {
    const { environment, userContext, navigationCollapsed, rendered, i18nReady } = this.props;
    const { collapsed } = this.state;
    //
    if (!rendered) {
      return false;
    }

    let environmentLabel = null;
    if (environment) {
      const environmentClassName = classnames(
        'label',
        { 'label-success': environment === 'development' },
        { 'label-warning': environment !== 'development' },
        { hidden: environment === 'production'}
      );
      environmentLabel = (
        <Basic.Div className="navbar-text hidden-xs" title={ this.i18n(`environment.${ environment }.title`, { defaultValue: environment }) }>
          <span className={ environmentClassName }>
            <span className="hidden-sm">{ this.i18n(`environment.${ environment }.label`, { defaultValue: environment }) }</span>
            <span className="visible-sm-inline">{ this.i18n(`environment.${ environment }.short`, { defaultValue: environment }) }</span>
          </span>
        </Basic.Div>
      );
    }

    const supportedLanguages = LocalizationService.getSupportedLanguages();
    let flags = null;
    if (supportedLanguages && supportedLanguages.length > 1) {
      flags = (
        <Basic.Div className="navbar-text hidden-xs">
          <Basic.Div className="flags-container">
            <Basic.Div className="flags">
              {
                [...supportedLanguages.map((lng, i) => {
                  const lgnClassName = classnames(
                    'flag',
                    lng,
                    { active: i18nReady === lng },
                    { last: i === supportedLanguages.length - 1 }
                  );
                  return (
                    <span
                      key={ `locale-${ lng }` }
                      className={ lgnClassName }
                      onClick={ this._i18nChange.bind(this, lng) }
                      role="button"
                      tabIndex={ 0 }
                      onKeyPress={ null }/>
                  );
                }).values()]
              }
            </Basic.Div>
          </Basic.Div>
        </Basic.Div>
      );
    }
    //
    let identityMenu = null;
    let isSwitchedUser = false;
    if (!userContext.isExpired && SecurityManager.isAuthenticated(userContext)) {
      const { identityMenuShowLoading } = this.state;
      // rename => move to modal component
      const { _imageUrl, identity } = this.props;
      //
      const identityItems = this.renderNavigationItems('identity-menu', false);
      isSwitchedUser = userContext.originalUsername && userContext.originalUsername !== userContext.username;
      //
      identityMenu = (
        <li>
          <a
            href="#"
            className="dropdown-toggle"
            data-toggle="dropdown"
            role="button"
            aria-haspopup="true"
            aria-expanded="false"
            onClick={ () => {
              // load identity ... and icon
              this.setState({
                identityMenuShowLoading: true
              }, () => {
                this.context.store.dispatch(identityManager.downloadProfileImage(userContext.username));
                this.context.store.dispatch(identityManager.fetchEntityIfNeeded(userContext.username, null, () => {
                  this.setState({
                    identityMenuShowLoading: false
                  });
                }));
              });
            }}>
            <span>
              <Basic.Icon value={ isSwitchedUser ? 'component:switch-user' : 'component:identity' }/>
              <Basic.ShortText value={ userContext.username } cutChar="" maxLength="30"/>
              <span className="caret"/>
            </span>
          </a>
          {
            identityMenuShowLoading
            ?
            <ul className="dropdown-menu">
              <li>
                <Basic.Loading isStatic show />
              </li>
            </ul>
            :
            <ul className="dropdown-menu">
              <li className="identity-image">
                <Basic.Div style={{ display: 'flex', alignItems: 'center' }}>
                  <Basic.Div>
                    {
                      _imageUrl
                      ?
                      <img src={ _imageUrl } alt="profile" className="img-thumbnail" style={{ height: 50, padding: 0 }} />
                      :
                      <Basic.Icon
                        value="component:identity"
                        identity={ identity }
                        className="text-center img-thumbnail profile-default-icon"
                        style={{
                          backgroundColor: Utils.Entity.isDisabled(identity) ? '#FCF8E3' : '#DFF0D8',
                          width: 50,
                          fontSize: '20px',
                          lineHeight: '35px'
                        }}
                        color="#FFFFFF" />
                    }
                  </Basic.Div>
                  <Basic.Div style={{ flex: 1, paddingLeft: 7 }}>
                    <Basic.Div>
                      <Basic.ShortText value={ userContext.username } cutChar="" maxLength="40" style={{ fontSize: '1.1em', fontWeight: 'normal' }}/>
                    </Basic.Div>
                    <Basic.Div>
                      { identityManager.getFullName(identity) }
                    </Basic.Div>
                  </Basic.Div>
                </Basic.Div>
                <Basic.Div rendered={ isSwitchedUser } style={{ marginTop: 5 }}>
                  <Basic.Button
                    level="success"
                    buttonSize="xs"
                    onClick={ this._onSwitchUserLogout.bind(this) }
                    showLoading={ userContext.showLoading }>
                    { this.i18n('content.identity.switch-user.button.logout') }
                    <span style={{ marginLeft: 5 }}>
                      (
                      <Basic.ShortText
                        value={ userContext.originalUsername }
                        cutChar=""
                        maxLength="30"
                        style={{ fontSize: '1.1em', fontWeight: 'bold' }}/>
                      )
                    </span>
                  </Basic.Button>
                </Basic.Div>
              </li>
              { identityItems }
            </ul>
          }
        </li>
      );
    }
    //
    const mainItems = this.renderNavigationItems('main');
    const systemItems = this.renderNavigationItems('system');
    const sidebarItems = this.renderSidebarItems();
    const sidebarClassName = classnames(
      'navbar-default',
      'sidebar',
      { collapsed: navigationCollapsed }
    );
    //
    return (
      <Basic.Div>
        <header>
          <nav className="navbar navbar-default navbar-static-top" style={{ marginBottom: 0 }}>
            <Basic.Div className="navbar-header">
              <button
                type="button"
                className="navbar-toggle collapsed"
                onClick={ () => this.setState({ collapsed: !collapsed }) }>
                <span className="sr-only">{ this.i18n('navigation.toogle') }</span>
                <span className="icon-bar"/>
                <span className="icon-bar"/>
                <span className="icon-bar"/>
              </button>
              <Link to="/" title={ this.i18n('navigation.menu.home') } className="home">
                {' '}
              </Link>
            </Basic.Div>
            <Basic.Div id="navbar" className={ classnames('navbar-collapse', { 'hidden-xs': collapsed }) }>
              {
                !userContext.isExpired && !SecurityManager.isAuthenticated(userContext)
                ?
                <ul className="nav navbar-nav">
                  { mainItems }
                </ul>
                :
                null
              }
              <Basic.Div className="navbar-right">
                <NavigationSearch
                  className="navbar-form navbar-left hidden-sm hidden-xs"
                  rendered={ !userContext.isExpired && SecurityManager.isAuthenticated(userContext) }/>
                { environmentLabel }
                {
                  !isSwitchedUser
                  ||
                  <Basic.Div className="navbar-text">
                    <span
                      className={
                        isSwitchedUser
                        ?
                        'label label-warning'
                        :
                        ''
                      }
                      title={
                        this.i18n('content.identity.switch-user.switched.title', {
                          originalUsername: userContext.originalUsername,
                          username: userContext.username
                        })
                      }>
                      <Basic.Icon value="warning-sign"/>
                      <span>{ this.i18n('content.identity.switch-user.switched.label') }</span>
                    </span>
                  </Basic.Div>
                }
                { flags }
                <ul className="nav navbar-nav">
                  {
                    userContext.isExpired
                    ||
                    identityMenu
                  }
                  {
                    userContext.isExpired
                    ||
                    systemItems
                  }
                </ul>
              </Basic.Div>
            </Basic.Div>
            {
              !userContext.isExpired && SecurityManager.isAuthenticated(userContext)
              ?
              <Basic.Div className={ sidebarClassName } role="navigation">
                <Basic.Div className={ classnames('sidebar-nav', 'navbar-collapse', { 'hidden-xs': collapsed }) }>
                  { sidebarItems }
                </Basic.Div>
              </Basic.Div>
              :
              null
            }
          </nav>
        </header>
      </Basic.Div>
    );
  }
}

Navigation.propTypes = {
  rendered: PropTypes.bool,
  navigation: PropTypes.object,
  navigationCollapsed: PropTypes.bool,
  selectedNavigationItems: PropTypes.array,
  environment: PropTypes.string,
  userContext: PropTypes.object,
  i18nReady: PropTypes.string
};

Navigation.defaultProps = {
  rendered: true,
  navigation: null,
  navigationCollapsed: false,
  selectedNavigationItems: null,
  environment: null,
  userContext: null,
  i18nReady: null
};

function select(state) {
  const identifier = state.security.userContext.username;
  const profileUiKey = identityManager.resolveProfileUiKey(identifier);
  const profile = DataManager.getData(state, profileUiKey);
  //
  return {
    navigation: state.config.get('navigation'),
    navigationCollapsed: state.security.userContext.navigationCollapsed,
    selectedNavigationItems: state.config.get('selectedNavigationItems'),
    environment: ConfigurationManager.getEnvironmentStage(state),
    userContext: state.security.userContext,
    i18nReady: state.config.get('i18nReady'),
    identity: identityManager.getEntity(state, identifier),
    _imageUrl: profile ? profile.imageUrl : null,
    searchShowLoading: DataManager.isShowLoading(state, 'search') || DataManager.isShowLoading(state, 'search2')
  };
}

export default connect(select)(Navigation);
