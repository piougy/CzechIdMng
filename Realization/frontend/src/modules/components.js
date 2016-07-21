/**
 *
 * This file will be generated manually from gulp - browserify doesn't suppord dynamic modules
 *
 */
import Immutable from 'immutable';

// import crt_componentDescriptor from './crt/component-descriptor';
// import vpn_componentDescriptor from './vpn/component-descriptor';

let componentDescriptors = new Immutable.Map();
// descriptors
componentDescriptors = componentDescriptors.set('core', require('./core/component-descriptor'));
// componentDescriptors = componentDescriptors.set('crt', crt_componentDescriptor);
// componentDescriptors = componentDescriptors.set('vpn', vpn_componentDescriptor);

module.exports = {
  componentDescriptors
};
