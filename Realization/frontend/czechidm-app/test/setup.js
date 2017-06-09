import jsdom from 'jsdom';
import fs from 'fs-extra';
import path from 'path';
import log4js from 'log4js';
import ConfigLoader from 'czechidm-core/src/utils/ConfigLoader';
import Immutable from 'immutable';

const logger = log4js.getLogger();
logger.setLevel('DEBUG');
global.LOGGER = logger;

// react + mocha integration - wee need to have document initialized for use react render in tests
// http://www.hammerlab.org/2015/02/21/testing-react-web-apps-with-mocha-part-2/
if (typeof document === 'undefined') {
  LOGGER.debug('test document init ... begin');
  global.DEBUG = false;
  global.document = jsdom.jsdom('<!doctype html><html><body></body></html>');
  global.window = document.defaultView;
  global.navigator = { userAgent: 'node.js '};
  LOGGER.debug('test document init ... done');
}

LOGGER.debug('test config init ... begin');
fs.copySync(path.resolve(__dirname, '../config/default/development.json'), path.resolve(__dirname, '../dist/config.json'));

const config = JSON.parse(fs.readFileSync(path.resolve(__dirname, '../dist/config.json'), 'utf8'));
const componentDescriptors = new Immutable.Map();
// TODO: init module descriptors
ConfigLoader.init(config, componentDescriptors);

LOGGER.debug('test config init ... done');
