import React from 'react';
import {parse} from 'qs';
import {Route, Switch} from 'react-router-dom';
//
import ConfigLoader from 'czechidm-core/src/utils/ConfigLoader';
import AbstractComponent from '../AbstractComponent/AbstractComponent';
import { FlashMessagesManager, ConfigurationManager, SecurityManager} from '../../../redux';
import { i18n } from '../../../services/LocalizationService';
import {AuthenticateService} from '../../../services';
import IdmContext from '../../../context/idm-context';

/**
 * Automatically injects redux context (store) to component context,
 * localization,
 * add message to context.
 *
 * @author Radek Tomiška
 */
class AbstractContextComponent extends AbstractComponent {

  constructor(props, context) {
    super(props, context);
    this._parseUrlQuery(this.props.location);
    this.flashMessagesManager = new FlashMessagesManager();
  }

  /**
   * React router since V4 doesn't parse query (thanks).
   * So I have to make this here. Result is set to the
   * this.props.location (for back compatibility).
   */
  _parseUrlQuery(location) {
    if (location) {
      const { search } = location;
      if (search) {
        const trimedSearch = search.replace('?', '');
        const query = parse(trimedSearch);
        location.query = query;
      }
      // Query has default value
      if (!location.query) {
        location.query = {};
      }
    }
  }

  /**
   * Return component identifier, with can be used in localization etc.
   *
   * @return {string} component identifier
   */
  getComponentKey() {
    return null;
  }

  /**
   * Add flash message, see more in FlashMessages component
   *
   * @param {Message} message
   * @param {Event} event
   */
  addMessage(message, event) {
    if (event) {
      event.preventDefault();
    }
    this.context.store.dispatch(this.flashMessagesManager.addMessage(message));
  }

  /**
   * Add error flash message, see more in FlashMessages component
   *
   * @param {Error} error message (json failure)
   * @param {Event} event
   */
  addError(error, event) {
    if (event) {
      event.preventDefault();
    }
    this.context.store.dispatch(this.flashMessagesManager.addError(error, this.context));
  }

  /**
   * Add error flash message, see more in FlashMessages component
   *
   * @param {Message} message
   * @param {Error} error message (json failure)
   * @param {Event} event
   */
  addErrorMessage(message, error, event) {
    if (event) {
      event.preventDefault();
    }
    this.context.store.dispatch(this.flashMessagesManager.addErrorMessage(message, error, this.context));
  }

  /**
   * Hide all flash message, see more in FlashMessages component
   */
  hideAllMessages() {
    this.context.store.dispatch(this.flashMessagesManager.hideAllMessages());
  }

  /**
   * Hide flash message by id or key
   *
   * @param  {string} idOrKey message id or key
   */
  hideMessage(idOrKey) {
    this.context.store.dispatch(this.flashMessagesManager.hideMessage(idOrKey));
  }

  /**
   * Returns localized message
   * - for supported options see http://i18next.com/pages/doc_features.html
   *
   * @param  {string} key     localization key
   * @param  {object} options parameters
   * @return {string}         localized message
   */
  _i18n(key, options) {
    let result = i18n(key, options);
    // escape html
    if (options && options.escape === false && key !== result) {
      result = (<span dangerouslySetInnerHTML={{__html: i18n(key, options)}}/>);
    }
    return result;
  }

  /**
   * Automatically prepend component prefix to localization key
   * If overridened key isn't found in localization, then previous key is used
   *
   * @param  {string} key     localization key
   * @param  {object} options parameters
   * @return {string}         localized message
   */
  i18n(key, options) {
    if (!key) {
      return '';
    }
    //
    const componentKey = this.getComponentKey();
    //
    const resultKeyWithModule = (key.indexOf(':') > -1 || !componentKey) ? key : `${componentKey}.${key}`;
    const resultKeyWithoutModule = (resultKeyWithModule.indexOf(':') > -1) ? resultKeyWithModule.split(':')[1] : resultKeyWithModule;
    const i18nValue = this._i18n(resultKeyWithModule, options);
    if (i18nValue === resultKeyWithModule || i18nValue === resultKeyWithoutModule) {
      return this._i18n(key, options);
    }
    return i18nValue;
  }

  /**
   * Returns logger, which is configured for whole app in redux store
   *
   * @return {object} logger
   */
  getLogger() {
    if (this.context.store) {
      return this.context.store.getState().logger;
    }
    return LOGGER;
  }

  /**
   * Returns initialized flash message manager
   *
   * @return {FlashMessageManager}
   */
  getFlashManager() {
    return this.flashMessagesManager;
  }

  _getConcatPath(parentId, path) {
    if (!parentId) {
      return path;
    }

    // Unbend a path. Some routes are wrong defined in routes.js (missing or excess slash).
    if (parentId.endsWith('/') && path && path.startsWith('/')) {
      parentId = parentId.substring(0, parentId.length - 1);
    }

    if (path && !path.startsWith('/') && !parentId.endsWith('/')) {
      return `${parentId}/${path}`;
    }
    return `${parentId}${path}`;
  }

  /**
   * Finds route definitions for given path.
   */
  _getRouteComponents(path, routes, parentRoute) {
    let components = routes.filter(route => {
      const concatedPath = this._getConcatPath(parentRoute.path, route.path);

      if (this.trimSlash(concatedPath) === this.trimSlash(path)) {
        return true;
      }
      return false;
    });

    if (components && components.length > 0) {
      return components;
    }
    components = [];

    routes.forEach(route => {
      if (route.childRoutes && route.childRoutes.length > 0) {
        const subComponents = this._getRouteComponents(path, route.childRoutes, route);
        if (subComponents && subComponents.length > 0) {
          components.push(...subComponents);
        }
      }
      return null;
    });

    if (components && components.length > 0) {
      return components;
    }
    return null;
  }

  /**
   * If direct route doesn't have a component, then
   * we try to find component in childRoutes.
   */
  _getChildrenRoutesWithComponent(route, parentPath) {
    const childRoutesResult = [];
    if (route.component) {
      route.concatedPath = parentPath;
      return [route];
    } if (route.childRoutes) {
      route.childRoutes.forEach(childRoute => {
        childRoutesResult.push(...this._getChildrenRoutesWithComponent(childRoute,
          this._getConcatPath(parentPath, childRoute.path)));
      });
    }
    return childRoutesResult;
  }

  /**
   * Found route definitions for children (items from routes.js for this component).
   */
  _getRouteDefinitions() {
    const {match} = this.props;
    const routes = this.context.routes;

    const topLevelPath = routes.childRoutes[0].path;
    let currentPath = match.path;
    if (currentPath.startsWith(topLevelPath)) {
      currentPath = currentPath.substring(topLevelPath.length, currentPath.length);
    }
    let currentRoutes = [];

    // If currentPath is empty, then we are on top-level, so we can add all direct children.
    if (currentPath === '') {
      routes.childRoutes[0].component = {};
      currentRoutes = [routes.childRoutes[0]];
    } else {
      currentRoutes = this._getRouteComponents(currentPath, routes.childRoutes, routes);
    }
    const childRoutesResult = [];
    if (currentRoutes) {
      currentRoutes.forEach(route => {
        const childRoutes = route.childRoutes;
        if (childRoutes) {
          childRoutes.forEach(routeWithComponent => {
            childRoutesResult.push(routeWithComponent);
          });
        }
      });
    }
    return childRoutesResult;
  }

  trimSlash(routePath) {
    // Trim start of path from slash
    if (routePath.startsWith('/')) {
      routePath = routePath.substring(1, routePath.length);
    }
    // Trim end of path from slash
    if (routePath.endsWith('/')) {
      routePath = routePath.substring(0, routePath.length - 1);
    }
    return routePath;
  }

  /**
   * Get priority of path from given route.
   *
   */
  _getPriorityOfPath(route) {
    if (!route || !route.path) {
      return 0;
    }
    let routePath = route.path;
    routePath = this.trimSlash(routePath);
    const elements = routePath.split('/');
    let priority = elements.length * 2;

    // If is last item ends on dynamic parameter, the we decrease the priority.
    const lastElement = elements[elements.length - 1];
    if (lastElement.startsWith(':')) {
      priority -= 1;
    }

    // If route has a order, then we have to use it.
    if (route.order) {
      priority -= route.order;
    }
    route.pathPriority = priority;
    return priority;
  }

  /**
   * Return component. Check acccess to the route component.
   *
   * Module must be enabled too. If user doesn't have a rights or module for this
   * route is disabled, then is not return component form the route, but Error
   * component (403/503) or Login.
   */
  _getComponent(route) {
    if (route.module && !ConfigLoader.isEnabledModule(route.module)) {
      // Maybe useless, because routes are filtered in Index.js!
      return require('../../../content/error/503');
    }
    const userContext = AuthenticateService.getUserContext();
    // Check if is user authenticated, if not, then redirect to the login page.
    if (!userContext.isAuthenticated) {
      return require('../../../content/Login');
    }

    // Check access to the component
    if (!SecurityManager.hasAccess(route.access, userContext)) {
      if (SecurityManager.isAuthenticated(userContext)) {
        return require('../../../content/error/403');
      }
      return require('../../../content/Login');
    }
    return route.component;
  }

  /**
   * Creates react-router Routes components for this component (url).
   */
  generateRouteComponents() {
    const {match, location} = this.props;

    // Found children routes definitions (items from routes.js for this component).
    const childRoutes = this._getRouteDefinitions();
    if (!childRoutes) {
      return null;
    }

    const childRoutesWithComponent = [];
    childRoutes.forEach(route => {
      if (!route.component) {
        const routesWithComponent = this._getChildrenRoutesWithComponent(route, route.path);
        routesWithComponent.forEach(routeWithComponent => {
          childRoutesWithComponent.push(routeWithComponent);
        });
      } else {
        childRoutesWithComponent.push(route);
      }
    });

    // Sorting of a routes ... we need to have more specific routes first.
    childRoutesWithComponent.sort((routeA, routeB) => {
      const lengthOfPathA = this._getPriorityOfPath(routeA);
      const lengthOfPathB = this._getPriorityOfPath(routeB);

      return lengthOfPathB - lengthOfPathA;
    });

    const routes = [];
    // Generate react-redux Router components.
    childRoutesWithComponent.forEach(route => {
      const Component = this._getComponent(route);
      routes.push(<Route
        key={`${route.id}${match.url}`}
        path={this._getConcatPath(match.path, route.concatedPath ? route.concatedPath : route.path)}
        render={(props) => <Component {...props}/>
        }/>);
    });
    return routes;
  }

  /**
   * Creates react-router Routes components for this component (url) and wrap them to the Switch component.
   */
  getRoutes() {
    const routes = this.generateRouteComponents();

    return (
      <Switch>
        {routes}
      </Switch>
    );
  }

  /**
   * Returns true, when application (BE) is in development stage
   *
   * @return {Boolean}
   */
  isDevelopment() {
    return ConfigurationManager.getEnvironmentStage(this.context.store.getState()) === 'development';
  }
}

AbstractContextComponent.propTypes = {
  ...AbstractComponent.propTypes
};

AbstractContextComponent.defaultProps = {
  ...AbstractComponent.defaultProps
};

AbstractContextComponent.contextType = IdmContext;

// Wrap the component to inject dispatch and state into it
export default AbstractContextComponent;
