

import _ from 'lodash';
import DataManagerRoot from './data';
import SecurityManager from './security/SecurityManager';
import FlashMessagesManager from './flash/FlashMessagesManager';

const TopManagerRoot = _.merge({}, DataManagerRoot, {
  SecurityManager: SecurityManager,
  FlashMessagesManager: FlashMessagesManager
});

TopManagerRoot.version = '0.0.1';
module.exports = TopManagerRoot;
