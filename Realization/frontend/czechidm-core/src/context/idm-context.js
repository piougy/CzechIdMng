import React from 'react';

/**
 * IdmContext - context for routes and store by default.
 * This context is propaget to all components use AbstractContextComponent.
 */
const IdmContext = React.createContext({
  routes: {},
  store: {}
});

export default IdmContext;
