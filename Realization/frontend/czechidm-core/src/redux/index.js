/**
 * Managers register
 *
 * import { IdentityManager } from './redux' can be used in react components (ui layer)
 *
 * @author Radek Tomiška
 */
import _ from 'lodash';
import DataManagerRoot from './data';
import SecurityManager from './security/SecurityManager';
import FlashMessagesManager from './flash/FlashMessagesManager';

const TopManagerRoot = _.merge({}, DataManagerRoot, {
  SecurityManager,
  FlashMessagesManager
});

TopManagerRoot.version = '0.0.1';
module.exports = TopManagerRoot;
