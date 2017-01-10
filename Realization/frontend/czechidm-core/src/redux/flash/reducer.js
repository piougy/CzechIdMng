import merge from 'object-assign';
import Immutable from 'immutable';
import { ADD_MESSAGE, HIDE_MESSAGE, HIDE_ALL_MESSAGES, REMOVE_MESSAGE, REMOVE_ALL_MESSAGES } from './FlashMessagesManager';

const INITIAL_STATE = {
  // maxHistory: 2,
  // TODO: repair redux local storage - its not possible persist partial state only (merge -> lodash merge)
  messages: new Immutable.OrderedMap({})
};

export function messages(state = INITIAL_STATE, action) {
  switch (action.type) {
    case ADD_MESSAGE: {
      const messagesData = state.messages.toArray();
      let nextId = messagesData.reduce((maxId, message) => Math.max(message.id, maxId), -1) + 1;
      if (!nextId) {
        nextId = 1;
      }
      const message = merge({}, action.message, {
        id: nextId
      });
      return merge({}, state, {
        messages: state.messages.set(message.id, message).slice(-25) }
      );
    }
    case REMOVE_MESSAGE: {
      return merge({}, state, {
        messages: state.messages.delete(action.id)
      });
    }
    case REMOVE_ALL_MESSAGES: {
      return merge({}, state, {
        messages: state.messages.clear()
      });
    }
    case HIDE_ALL_MESSAGES:
    case HIDE_MESSAGE: {
      const messagesData = state.messages.map(message =>
        (action.type === HIDE_ALL_MESSAGES || message.id === action.id || message.key === action.id)
          ?
          merge({}, message, { hidden: true })
          :
          message
      );
      return merge({}, state, {
        messages: messagesData
      } );
    }

    default:
      return state;
  }
}
