'use strict';

// http://rackt.org/redux/docs/advanced/AsyncActions.html
// https://www.npmjs.com/package/immutable

import merge from 'object-assign';
import Immutable from 'immutable';
//
import { LOGOUT } from '../security/SecurityManager';
import {
  REQUEST_ENTITIES,
  RECEIVE_ENTITIES,
  CLEAR_ENTITIES,
  REQUEST_ENTITY,
  RECEIVE_ENTITY,
  DELETED_ENTITY,
  RECEIVE_ERROR,
  START_BULK_ACTION,
  PROCESS_BULK_ACTION,
  STOP_BULK_ACTION
} from './EntityManager';

import {
  CACHE_ITEMS,
  EDIT_ITEM,
  CANCEL_EDIT_ITEM,
  APPLY_EDIT_ITEM,
  CANCEL_FORM,
  ADD_ITEM,
  REMOVE_ITEM,
  STORE_DATA,
  CLEAR_DATA
} from './FormManager';

const INITIAL_STATE = {
  entity: {
    // entities are stored as map <id, entity>
    // Identity: Immutable.Map({})
  },
  ui: {
    // uiKeys are stored as object with structure:
    /*
    identity_table: {
      items: [], // reference to identities
      showLoading: true,
      total: null
      searchParameters: merge({}, DEFAULT_SEARCH_PARAMETERS, {
        sort: [{
          field: 'name',
          order: 'ASC'
        }]
      }),
      error: null
    }
    */
    forms: {},
    forms_editing: {}
  },
  // bulk actions modal progress bar
  bulk: {
    action: {
      name: null,
      title: null
    },
    showLoading: false,
    counter: 0,
    size: 0
  },
  // custom data store - create, edit etc.
  data: Immutable.Map({})
};

export function data(state = INITIAL_STATE, action) {
  const uiKey = action.uiKey;
  switch (action.type) {
    case REQUEST_ENTITIES: {
      const ui = merge({}, state.ui, {
        [uiKey]: merge({}, state.ui[uiKey], {
          showLoading: true,
          searchParameters: action.searchParameters,
          error: null
        })
      });
      return merge({}, state, {
        ui: merge({}, state.ui, ui)
      });
    }
    case RECEIVE_ENTITIES: {
      const entityType = action.entityType;
      let entities = state.entity[entityType] || Immutable.Map({});
      let ids = [];
      action.entities.map(entity => {
        ids.push(entity.id);
        entities = entities.set(entity.id, entity);
      });
      const entityTypes =  merge({}, state.entity, {
        [entityType]: entities
      });
      const ui = merge({}, state.ui, {
        [uiKey]: merge({}, state.ui[uiKey], {
          showLoading: false,
          searchParameters: action.searchParameters,
          entityType: entityType,
          items: ids,
          total: action.total,
          error: null
        })
      });
      return merge({}, state, {
        entity: entityTypes,
        ui: merge({}, state.ui, ui)
      });
    }
    case REQUEST_ENTITY: {
      const ui = merge({}, state.ui, {
        [uiKey]: merge({}, state.ui[uiKey], {
          showLoading: true,
          id: action.id,
          error: null
        })
      });
      return merge({}, state, {
        ui: merge({}, state.ui, ui)
      });
    }
    case RECEIVE_ENTITY: {
      const entityType = action.entityType;
      let entities = state.entity[entityType] || Immutable.Map({});
      entities = entities.set(action.id, action.entity);
      const entityTypes =  merge({}, state.entity, {
        [entityType]: entities
      });
      let items = state.ui[uiKey].items || [];
      if (!_.includes(items, action.id)) {
        items = _.slice(items);
        items.push(action.id);
      }
      const ui = merge({}, state.ui, {
        [uiKey]: merge({}, state.ui[uiKey], {
          showLoading: false,
          entityType: entityType,
          id: action.id,
          error: null,
          items: items
        })
      });
      return merge({}, state, {
        entity: entityTypes,
        ui: merge({}, state.ui, ui)
      });
    }
    case DELETED_ENTITY: {
      const entityType = action.entityType;
      let entities = state.entity[entityType] || Immutable.Map({});
      if (entities.has(action.id)) {
        entities = entities.delete(action.id);
      }
      const entityTypes =  merge({}, state.entity, {
        [entityType]: entities
      });
      // clear entity.id from all uiKeys items
      let ui = merge({}, state.ui);
      for (let processUiKey in state.ui) {
        if (state.ui[processUiKey].items) {
          const items = _.without(state.ui[processUiKey].items, action.id);
          ui = merge(ui, {
            [processUiKey]: merge({}, state.ui[processUiKey], {
              showLoading: false,
              id: action.id,
              error: null,
              items: items
            })
          });
        }
      }
      return merge({}, state, {
        entity: entityTypes,
        ui: ui
      });
    }
    case RECEIVE_ERROR: {
      const ui = merge({}, state.ui, {
        [uiKey]: merge({}, state.ui[uiKey], {
          showLoading: false,
          error: action.error
        })
      });
      return merge({}, state, {
        ui: merge({}, state.ui, ui)
      });
    }
    case CLEAR_ENTITIES: {
      const entityType = action.entityType;
      const uiKey = action.uiKey;
      const entityTypes =  merge({}, state.entity, {
        [entityType]: Immutable.Map({})
      });
      const ui = merge({}, state.ui, {
        [uiKey]: merge({}, state.ui[uiKey], {
          showLoading: false,
          searchParameters: null,
          items: [],
          total: 0,
          error: null
        })
      });
      return merge({}, state, {
        entity: entityTypes,
        ui: merge({}, state.ui, ui)
      });

    }
    case CACHE_ITEMS: {
      let entities = {};
      let formsMap = {};

      action.items.map(entity => {
        merge(entities, {[entity.id]: entity});
      });

      merge(formsMap, {[action.key] : entities});

      return merge({}, state, {ui: merge({}, state.ui, {forms: merge({}, state.ui.forms,formsMap)})});
    }
    case EDIT_ITEM: {
      let forms = {};
      let entityId = {};
      merge(entityId, {id: action.id});
      merge(forms, {[action.key]: entityId});
      return merge({}, state, {ui: merge({}, state.ui, {forms_editing: merge({}, state.ui.forms_editing,forms)})});
    }
    case ADD_ITEM: {
      let forms = {};
      let entityId = {};
      merge(entityId, {id: action.uuid, operationAdd: true});
      merge(forms, {[action.key]: entityId});
      const newState = merge({}, state,
        {
          ui: merge({}, state.ui, {
            forms_editing: merge({}, state.ui.forms_editing, forms)}
          )
        });
      return newState;
    }
    case REMOVE_ITEM: {
      let newState =  merge({}, state);
      delete newState.ui.forms[action.key][action.id];
      return newState;
    }
    case CANCEL_EDIT_ITEM: {
      let forms = {};
      let entityId = {};
      merge(entityId, {id: action.id});
      merge(forms, {[action.key]: entityId});

      if (state.ui.forms_editing && state.ui.forms_editing[action.key] && state.ui.forms_editing[action.key].id === action.id){
        delete state.ui.forms_editing[action.key].id;
      }

      return merge({}, state);
    }
    case APPLY_EDIT_ITEM: {
      let newState =  merge({}, state);
      newState.ui.forms[action.key][action.id] = action.data;
      if (newState.ui.forms_editing[action.key]){
        delete newState.ui.forms_editing[action.key];
      }
      return newState;
    }
    case CANCEL_FORM: {
      let newState =  merge({}, state);
      if (newState.ui.forms[action.key]){
        delete newState.ui.forms[action.key];
      }
      if (newState.ui.forms_editing[action.key]){
        delete newState.ui.forms_editing[action.key];
      }
      return newState;
    }
    case LOGOUT: {
      // clear whole state except setting
      const newState = INITIAL_STATE;
      // setting will be preserved
      return merge({}, newState, {
        entity: merge({}, newState.entity, {
          Setting: state.entity.Setting
        })
      });
    }
    case STORE_DATA: {
      return merge({}, state, {
        data: state.data.set(uiKey, action.data)
      });
    }
    case CLEAR_DATA: {
      // preserve state, if data doesn't contin given uiKey
      if (!state.data.has(uiKey)) {
        return state;
      }
      return merge({}, state, {
        data: data.delete(uiKey)
      });
    }
    case START_BULK_ACTION: {
      return merge({}, state, {
        bulk: {
          action: action.action,
          showLoading: true,
          counter: 0,
          size: action.size
        }
      });
    }
    case PROCESS_BULK_ACTION: {
      return merge({}, state, {
        bulk: merge({}, state.bulk, {
          counter: state.bulk.counter + 1
        })
      });
    }
    case STOP_BULK_ACTION: {
      return merge({}, state, {
        bulk: merge({}, state.bulk, {
          showLoading: false
        })
      });
    }

    default: {
      return state;
    }
  }
}
