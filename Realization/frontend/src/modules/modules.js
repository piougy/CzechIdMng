/**
 *
 * This file will be generated manually from gulp - browserify doesn't suppord dynamic modules
 *
 */
import Immutable from 'immutable';

// import crt_moduleDescriptor from './crt/module-descriptor';
// import vpn_moduleDescriptor from './vpn/module-descriptor';

let moduleDescriptors = new Immutable.Map();
// descriptors
moduleDescriptors = moduleDescriptors.set('core', require('./core/module-descriptor'));
// moduleDescriptors = moduleDescriptors.set('crt', crt_moduleDescriptor);
// moduleDescriptors = moduleDescriptors.set('vpn', vpn_moduleDescriptor);

module.exports = {
  moduleDescriptors
};
