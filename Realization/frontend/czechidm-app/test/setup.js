import log4js from 'log4js';
//
const logger = log4js.getLogger();
logger.setLevel('DEBUG');
global.LOGGER = logger;
global.TEST_PROFILE = true;
//
LOGGER.debug('test config init ... begin');
if (typeof document === 'undefined') {
  // FIXME: react + mocha integration - we need to have document initialized for use react render in tests
  // http://www.hammerlab.org/2015/02/21/testing-react-web-apps-with-mocha-part-2/
  global.DEBUG = false;
}
//
LOGGER.debug('test config init ... done');
