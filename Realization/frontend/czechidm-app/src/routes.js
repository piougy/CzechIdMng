
import _ from 'lodash';
import Immutable from 'immutable';
//
import { Managers} from 'czechidm-core';
import Dashboard from 'czechidm-core/src/content/Dashboard';
import ConfigLoader from 'czechidm-core/src/utils/ConfigLoader';
import App from './layout/App';

/**
* Definition of a routes. Make conversions and concate routes from all modules.
*
* @author Vít Švanda
* @author Radek Tomiška
*/

/**
* Returns unique route id by path (or component if path is not defined)
*
* @param  {string} parentRouteId
* @param  {route} route
* @return {string}
*/
function getRouteId(parentRouteId, route) {
  let id = '';
  if (parentRouteId) {
    id += parentRouteId;
  }
  if (route.path) {
    id += route.path;
  } else {
    id += route.component;
  }
  return id;
}

/**
* Transform route tree to flat map.
* Adds route dafeul values.
* Removes routes from disabled module.
*
* @param  {immutable} routesMap result routes
* @param  {string} moduleId module identifier
* @param  {string} parentRouteId parent route identifier (we need them for unique route id)
* @param  {route} route processing route
* @return {immutable} result routes
*/
function fillRouteMap(routesMap, moduleId, parentRouteId, route) {
  const routeId = getRouteId(parentRouteId, route);
  // fill module to route from parent route
  if (route.module !== undefined) {
    moduleId = route.module;
  }
  // module is disabled - skip whole subtree
  if (moduleId && !ConfigLoader.isEnabledModule(moduleId)) {
    return routesMap;
  }
  // cloned route with filled default values
  const clonedRoute = {
    id: routeId,
    parentId: parentRouteId,
    module: route.module,
    access: route.access || [{ type: 'IS_AUTHENTICATED' }],
    component: route.component,
    path: route.path,
    priority: route.priority || 0,
    order: route.order || 0
  };
  // add route to flat map by priority and order
  if (!routesMap.has(routeId)
  || routesMap.get(routeId).priority < clonedRoute.priority // higher priority
  || (routesMap.get(routeId).priority === clonedRoute.priority && routesMap.get(routeId).order > clonedRoute.order)) { // lower order
    routesMap = routesMap.set(routeId, clonedRoute);
  }
  // children of route with less priority are forgotten
  if (route.childRoutes && (routesMap.get(routeId).priority <= clonedRoute.priority)) {
    route.childRoutes.forEach(childRoute => {
      routesMap = fillRouteMap(routesMap, moduleId, routeId, childRoute);
    });
  }
  return routesMap;
}

function trimSlash(routePath) {
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
 * Fix given route. Solve problem, when route could have more specific path, but
 * this path is defined in every children. Then is problem with correct ordering
 * this routes (since react-router V4).
 *
 * This method try to find this routes and fix it. For example path of 'code-lists/'
 * is changed to 'code-lists/:entityId'.
 */
function routeFixer(route) {
  if (route.childRoutes) {
    let firstElement = null;
    let isSame = false;
    // Check if all children has first path element same.
    route.childRoutes.forEach(childRoute => {
      if (childRoute.path) {
        const path = trimSlash(childRoute.path);
        const pathElements = path.split('/');
        if (pathElements.length > 1 && (!firstElement || firstElement === pathElements[0])) {
          firstElement = pathElements[0];
          isSame = true;
        } else {
          isSame = false;
        }
      }
    });
    // If are same, then is used in parent route and is removed form children.
    if (isSame) {
      route.childRoutes.forEach(childRoute => {
        if (childRoute.path) {
          const path = trimSlash(childRoute.path);
          childRoute.path = path.substring(firstElement.length, path.length);
        }
      });
      route.path = `${trimSlash(route.path)}/${firstElement}`;
    }
  }
}

/**
* Rebuild tree from flat map and original route tree.
*
* @param  {immutable} routesMap flat routes map (see fillRouteMap)
* @param  {route} targetRoute cloned route
* @param  {string} parentRouteId parent route identifier (we need them for unique route id)
* @param  {route} route original route (wee need to rebuild tree in the same structure)
*/
function fillRouteTree(routesMap, targetRoute, parentRouteId, route) {
  const routeId = getRouteId(parentRouteId, route);
  //
  if (routesMap.has(routeId)) {
    _.merge(targetRoute, routesMap.get(routeId));
  } else {
    // route was not found - is disabled etc.
    return;
  }
  if (route.childRoutes) {
    targetRoute.childRoutes = [];
    route.childRoutes.forEach(childRoute => {
      const targetChildRoute = {};
      targetRoute.childRoutes.push(targetChildRoute);
      fillRouteTree(routesMap, targetChildRoute, routeId, childRoute);
    });
    // sort routes by order
    route.childRoutes = route.childRoutes.sort((one, two) => one.order - two.order);
  }
}

const routeAssembler = require('../dist/modules/routeAssembler');
//
// prepare routes in flat map
let routeMap = new Immutable.OrderedMap();
routeAssembler.childRoutes.forEach(moduleRoute => { // wee need to skip "decorator" routes
  if (moduleRoute.childRoutes) {
    moduleRoute.childRoutes.forEach(route => {
      routeMap = fillRouteMap(routeMap, moduleRoute.module, null, route);
    });
  }
});
//
// rebuild target routes
let resultRoutes = [];
routeAssembler.childRoutes.forEach(moduleRoute => { // wee need to skip "decorator" routes
  if (moduleRoute.childRoutes) {
    moduleRoute.childRoutes.forEach(route => {
      const resultRoute = {};
      resultRoutes.push(resultRoute);
      fillRouteTree(routeMap, resultRoute, null, route);
    });
  }
});

// Fix a routes
resultRoutes.forEach(route => {
  routeFixer(route);
});

// sort routes by order
//
resultRoutes = resultRoutes.filter(i => i.id !== null && i.id !== undefined).sort((one, two) => one.order - two.order);
//
const routes = {
  childRoutes: [
    {
      path: '/',
      getComponent: (location, cb) => {
        cb(null, App);
      },
      indexRoute: {
        component: Dashboard,
        onEnter: Managers.SecurityManager.checkAccess,
        access: [{ type: 'IS_AUTHENTICATED' }]
      },
      childRoutes: resultRoutes
    }
  ]
};

export default routes;
