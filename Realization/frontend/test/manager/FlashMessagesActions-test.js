'use strict';

import { expect } from 'chai';
import faker from 'faker';
import configureMockStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import nock from 'nock';
import Immutable from 'immutable';

const middlewares = [ thunk ];
const mockStore = configureMockStore(middlewares);
//
import FlashMessagesManager, { ADD_MESSAGE, HIDE_MESSAGE, HIDE_ALL_MESSAGES, REMOVE_MESSAGE, REMOVE_ALL_MESSAGES, DEFAULT_SERVER_UNAVAILABLE_TIMEOUT } from '../../src/modules/core/redux/flash/FlashMessagesManager';
import * as flashMessagesReducers from '../../src/modules/core/redux/flash/reducer';


// https://github.com/reactjs/redux/blob/master/docs/recipes/WritingTests.md
describe('FlashMessagesManager', function() {

  describe.skip('[server is unavailable]', function() { // TODO: this test is unstable - https://mochajs.org/#retry-tests
    const flashMessagesManager = new FlashMessagesManager();
    flashMessagesManager.setServerUnavailableTimeout(1200);

    this.timeout(flashMessagesManager.getServerUnavailableTimeout() + 100);

    const error = {
      message: 'TypeError: NetworkError when attempting to fetch resource.'
    };
    const store = mockStore({ messages: Immutable.OrderedMap({}) });

    store.dispatch(flashMessagesManager.addError(error));
    it('- before timeout', function(done) {
      expect(store.getActions()).to.have.lengthOf(0);
      // after defined timeout - one message with key `error-app-load` will be shown
      setTimeout(done, flashMessagesManager.getServerUnavailableTimeout() + 50);
    });

    it('- server is unavailable - after timeout', function() {
      expect(store.getActions()).to.not.be.empty;
      const errorAppLoad = store.getActions().find(action => {
        return action.type === ADD_MESSAGE && action.message.key === 'error-app-load'
      });
      expect(errorAppLoad).to.not.be.null;
    });
  });

  function getMessage(key = 'key') {
    return {
      type: ADD_MESSAGE,
      message: { key: key, message: 'Hello!' }
    };
  }

  it('[addMessage] - add message to store', function() {
    let state = flashMessagesReducers.messages(undefined, getMessage());
    expect(state).to.not.be.null;
    expect(state.messages).to.not.be.null;
    expect(state.messages.get(1)).to.not.be.null;
    expect(state.messages.get(1).hidden).to.be.undefined;
    expect(state.messages.get(1).message).to.equal(getMessage().message.message);
  });

  it('[hideMessage] - hide message from store by id', function() {
    let state = flashMessagesReducers.messages(undefined, getMessage());
    state = flashMessagesReducers.messages(state, getMessage());
    state = flashMessagesReducers.messages(state, getMessage());
    expect(state).to.not.be.null;
    expect(state.messages).to.not.be.null;
    expect(state.messages.size).to.equal(3);
    expect(state.messages.get(1).hidden).to.be.undefined;
    expect(state.messages.get(2).hidden).to.be.undefined;
    expect(state.messages.get(3).hidden).to.be.undefined;

    state = flashMessagesReducers.messages(state, {
      type: HIDE_MESSAGE,
      id: 2
    });

    expect(state.messages.get(1).hidden).to.be.undefined;
    expect(state.messages.get(2).hidden).to.be.true;
    expect(state.messages.get(3).hidden).to.be.undefined;
  });

  it('[hideMessage] - hide message from store by key', function() {
    let state = flashMessagesReducers.messages(undefined, getMessage());
    state = flashMessagesReducers.messages(state, getMessage('new-key'));
    state = flashMessagesReducers.messages(state, getMessage());
    expect(state).to.not.be.null;
    expect(state.messages).to.not.be.null;
    expect(state.messages.size).to.equal(3);

    state = flashMessagesReducers.messages(state, {
      type: HIDE_MESSAGE,
      id: getMessage().message.key
    });

    expect(state.messages.get(1).hidden).to.be.true;
    expect(state.messages.get(2).hidden).to.be.undefined;
    expect(state.messages.get(3).hidden).to.be.true;
  });

  it('[hideAllMessages] - hide all messages from store', function() {
    let state = flashMessagesReducers.messages(undefined, getMessage());
    state = flashMessagesReducers.messages(state, getMessage());
    state = flashMessagesReducers.messages(state, getMessage('new-key'));
    expect(state).to.not.be.null;
    expect(state.messages).to.not.be.null;
    expect(state.messages.size).to.equal(3);

    state = flashMessagesReducers.messages(state, {
      type: HIDE_ALL_MESSAGES
    });

    expect(state.messages.get(1).hidden).to.be.true;
    expect(state.messages.get(2).hidden).to.be.true;
    expect(state.messages.get(3).hidden).to.be.true;
  });

  it('[removeMessage] - remove message from store by id', function() {
    let state = flashMessagesReducers.messages(undefined, getMessage());
    state = flashMessagesReducers.messages(state, getMessage());
    state = flashMessagesReducers.messages(state, getMessage());
    expect(state).to.not.be.null;
    expect(state.messages).to.not.be.null;
    expect(state.messages.size).to.equal(3);

    state = flashMessagesReducers.messages(state, {
      type: REMOVE_MESSAGE,
      id: 2
    });
    expect(state.messages.size).to.equal(2);

    expect(state.messages.get(1).hidden).to.be.undefined;
    expect(state.messages.has(2)).to.be.false;
    expect(state.messages.get(3).hidden).to.be.undefined;
  });

  it('[removeAllMessages] - remove all messages from store', function() {
    let state = flashMessagesReducers.messages(undefined, getMessage());
    state = flashMessagesReducers.messages(state, getMessage());
    state = flashMessagesReducers.messages(state, getMessage());
    expect(state).to.not.be.null;
    expect(state.messages).to.not.be.null;
    expect(state.messages.size).to.equal(3);

    state = flashMessagesReducers.messages(state, {
      type: REMOVE_ALL_MESSAGES
    });

    expect(state.messages.size).to.equal(0);
  });

  it('[maxHistory] - slice messages by max history size', function() {
    let state = flashMessagesReducers.messages(undefined, getMessage());
    for (let i = 0; i < 29; i++) {
      state = flashMessagesReducers.messages(state, getMessage());
    }
    // TODO: constant etc ...
    expect(state.messages.size).to.equal(25);
  });

});
