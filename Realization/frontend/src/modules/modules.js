/**
 *
 * This file will be generated manually from gulp - browserify doesn't suppord dynamic modules
 *
 */
import Immutable from 'immutable';

let moduleDescriptors = new Immutable.Map();
// descriptors
moduleDescriptors = moduleDescriptors.set('core', require('./core/module-descriptor'));

module.exports = {
  moduleDescriptors
};
