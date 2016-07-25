/**
 *
 * This file will be generated manually from gulp - browserify doesn't suppord dynamic modules
 *
 */
import Immutable from 'immutable';

let componentDescriptors = new Immutable.Map();
// descriptors
componentDescriptors = componentDescriptors.set('core', require('./core/component-descriptor'));

module.exports = {
  componentDescriptors
};
