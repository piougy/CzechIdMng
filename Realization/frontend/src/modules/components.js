'use strict';
/**
 *
 * This file will be generated manually from gulp - browserify doesn't suppord dynamic modules
 *
 */
import Immutable from 'immutable';

import core_componentDescriptor from './core/component-descriptor';
import crt_componentDescriptor from './crt/component-descriptor';
import vpn_componentDescriptor from './vpn/component-descriptor';

var componentDescriptors = new Immutable.Map();
// descriptors
componentDescriptors = componentDescriptors.set('core', core_componentDescriptor);
//componentDescriptors = componentDescriptors.set('crt', crt_componentDescriptor);
//componentDescriptors = componentDescriptors.set('vpn', vpn_componentDescriptor);

module.exports = {
  componentDescriptors: componentDescriptors
};
