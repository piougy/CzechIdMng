import fs from 'fs';
import jPath from 'JSONPath';
import log4js from 'log4js';
import chai, { expect } from 'chai';
import dirtyChai from 'dirty-chai';
chai.use(dirtyChai);

const distLocales = 'dist/locales/';
const accLocales = '../czechidm-acc/src/locales/';
const accLocalesCs = '../czechidm-acc/src/locales/cs.json';
const accLocalesEn = '../czechidm-acc/src/locales/en.json';
const coreLocales = '../czechidm-core/src/locales/';
const exampleLocales = '../czechidm-example/src/locales/';

const logger = log4js.getLogger();
logger.setLevel('DEBUG');
global.LOGGER = logger;


// TODO add searching for files in dist folder
// basic reading of one module
let files = fs.readdirSync(accLocales);
LOGGER.debug(files);

// TODO Add searching for files and automatic reading of languages
function searchPaths() {
  //
}

function jsonToPaths(path) {
  // read CS file
  let contentCs = fs.readFileSync(path, 'utf8', (err, data) => {
    LOGGER.debug(err);
  });
  const jsonObj = JSON.parse(contentCs);
  const paths = jPath({json: jsonObj, path: '$..*', resultType: 'path'});
  LOGGER.debug(paths[0]);
  LOGGER.debug(typeof paths[0]);
  return paths;
}

function verifyCSNumericalSerie(i, j, language1, language2) {
  LOGGER.debug('VERIFY NUMERICAL SERIE');
}

function comparePlural(i, j, language1, language2) {
  LOGGER.debug('COMPARE PLURAL');
  // TODO substrings for plural comparing
  let foundCs1 = false;
  let foundCs2 = false;
  let foundCs5 = false;
  // convert last symbol from Czech localization to number if it's possible
  let number = Number(language1[i + j].charAt(language1[i + j].length - 1));
  LOGGER.debug(number);
  return {i, j};
}

function compareMessages(language1, language2) {
  let i;
  // change variables to 0 after testing of comparators
  let j = 0;
  for (i = 0; i + j < language1.length && i < language2.length; i++) {
    // LOGGER.debug(pathsCs[i + j] + ' VS. ' + pathsEn[i]);
    //
    if (language1[i + j] === language2[i]) {
      // Messages are in pair ~(_8^(I)
    } else {
      // Messages are NOT in pair, check if there is some context
      // Crop symbols from parser
      let temp1 = language1[i + j].substring(0, (language1[i + j].length - 2));
      let temp2 = language2[i].substring(0, (language2[i].length - 2));
      LOGGER.debug(temp1 + ' | ' + temp2);

      if (temp1.startsWith(temp2) && (temp1.length - 2 === temp2.length ||
          temp1.length + 4 === temp2.length)) {
        // check for plural match
        let iterators = comparePlural(i, j, language1, language2);
        i = iterators[0];
        j = iterators[1];
      } else if (language2[i + 1].endsWith('plural') && language2.length > i + 1 &&
        verifyCSNumericalSerie(i, j, language1, language2)) {
        // if the next word in EN ends with plural, program tries to find CS numerical serie
        LOGGER.debug('TEST COMPARE');
      }
      return;
      // LOGGER.debug(pathsCs[i + j] + ' VS. ' + pathsEn[i]);
    }
  }
}

// Main
let pathsCs = jsonToPaths(accLocalesCs);
let pathsEn = jsonToPaths(accLocalesEn);

compareMessages(pathsCs, pathsEn);
