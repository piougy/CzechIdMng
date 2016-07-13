import jsdom from 'jsdom';
import fs from 'fs-extra';
import path from 'path';

// react + mocha integration - wee need to have document initialized for use react render in tests
// http://www.hammerlab.org/2015/02/21/testing-react-web-apps-with-mocha-part-2/
if (typeof document === 'undefined') {
  console.log('test document init ... begin');
  global.DEBUG = false;
  global.document = jsdom.jsdom('<!doctype html><html><body></body></html>');
  global.window = document.defaultView;
  global.navigator = { userAgent: 'node.js '};
  console.log('test document init ... done');
}


function existsSync(filePath) {
  try {
    fs.statSync(filePath);
  } catch (err) {
    if (err.code === 'ENOENT') {
      return false;
    }
  }
  return true;
}

//if (!existsSync(path.resolve(__dirname, '../config.json'))) {
  console.log('test config init ... begin');
  fs.copySync(path.resolve(__dirname, '../config/default/development.json'), path.resolve(__dirname, '../config.json'));
  console.log('test config init ... done');
//}
