import fs from 'fs';
import jPath from 'JSONPath';
import log4js from 'log4js';
import chai from 'chai';
import dirtyChai from 'dirty-chai';
chai.use(dirtyChai);

const accLocales = '../czechidm-acc/src/locales/';
const coreLocales = '../czechidm-core/src/locales/';
const exampleLocales = '../czechidm-example/src/locales/';
const vsLocales = '../czechidm-vs/src/locales/';
const pathsToLocales = [accLocales, coreLocales, exampleLocales, vsLocales];

const logger = log4js.getLogger();
logger.setLevel('DEBUG');
global.LOGGER = logger;

const extraWords = [];

function noMissingFiles(folderWithLocalizationFiles) {
  let foundCs = false;
  let foundEn = false;
  fs.readdirSync(folderWithLocalizationFiles).forEach(file => {
    if (file === 'cs.json') {
      foundCs = true;
    } else if (file === 'en.json') {
      foundEn = true;
    }
  });
  //
  if (!foundCs && !foundEn) {
    LOGGER.debug('Missing both localization files in: ' + folderWithLocalizationFiles);
    return false;
  } else if (!foundCs) {
    LOGGER.debug('Missing cs.json localization file in: ' + folderWithLocalizationFiles);
    return false;
  } else if (!foundEn) {
    LOGGER.debug('Missing en.json localization file in: ' + folderWithLocalizationFiles);
    return false;
  } else if (foundCs && foundEn) {
    return true;
  }
}

function isNumeric(num) {
  return !isNaN(num);
}

function isUpperCase(str) {
  return str === str.toUpperCase();
}

function cropPath(jpath) {
  // delete $ sign from path
  return jpath.substring(1, jpath.length);
}

function cropEnd(jpath) {
  // crop symbols at the end for easier comparing
  return jpath.substring(0, jpath.length - 2);
}

function cropLanguage(word) {
  return word.substring(4, word.length);
}

function jsonToPaths(jpath) {
  // read file and transform to localization paths
  const fileContent = fs.readFileSync(jpath, 'utf8', (err) => {
    LOGGER.debug(err);
  });
  const jsonObj = JSON.parse(fileContent);
  const jpaths = jPath({json: jsonObj, path: '$..*', resultType: 'path'});
  return jpaths;
}

function checkExtraWords() {
  let word;
  let found = false;
  while (extraWords.length > 0) {
    word = extraWords.shift();
    for (let i = 0; i < extraWords.length; i++) {
      if (cropLanguage(extraWords[i]) === (cropLanguage(word)) &&
          !extraWords[i].startsWith(word.substring(0, 2))) {
        found = true;
        extraWords.splice(i, 1);
      }
    }
    if (!found) {
      LOGGER.debug(word + ' -> extra word');
    } else {
      found = false;
    }
  }
}

function findClosestMatch(i, j, language1, language2) {
  // LOGGER.debug('FIND CLOSEST MATCH');
  let count1 = 0;
  let count2 = 0;
  let found1 = false;
  let found2 = false;

  for (let k = i + j + 1; k < language1.length; k++) {
    count1++;
    if (language1[k] === (language2[i])) {
      found1 = true;
      break;
    }
  }
  for (let k = i + 1; k < language2.length; k++) {
    count2++;
    if (language2[k] === (language1[i + j])) {
      found2 = true;
      break;
    }
  }

  if (count1 <= count2 && found1) {
    for (let k = i + j; k < i + j + count1; k++) {
      // LOGGER.debug('CS: ' + cropPath(language1[k]) + ' -> extra word');
      extraWords.push('CS: ' + cropPath(language1[k]));
    }
    i--;
    j += count1;
  } else if (count2 < count1 && found2) {
    for (let k = i; k < i + count2; k++) {
      // LOGGER.debug('EN: ' + cropPath(language2[k]) + ' -> extra word');
      extraWords.push('EN: ' + cropPath(language2[k]));
    }
    i += count2 - 1;
    j -= count2;
  } else if (found1) {
    for (let k = i + j; k < i + j + count1; k++) {
      // LOGGER.debug('CS: ' + cropPath(language1[k]) + ' -> extra word');
      extraWords.push('CS: ' + cropPath(language1[k]));
    }
    i--;
    j += count1;
  } else if (found2) {
    for (let k = i; k < i + count2; k++) {
      // LOGGER.debug('EN: ' + cropPath(language2[k]) + ' -> extra word');
      extraWords.push('EN: ' + cropPath(language2[k]));
    }
    i += count2 - 1;
    j -= count2;
  } else {
    LOGGER.debug(cropPath(language1[i + j]) + ' vs. ' + cropPath(language2[i]) + ' -> match not found for both words');
  }
  return [i, j];
}

function verifyCSNumericalSerie(i, j, language1, numPathEn) {
  // LOGGER.debug('VERIFY NUMERICAL SERIE');
  for (let k = i + j; k < language1.length; k++) {
    if (language1[k].startsWith(numPathEn)) {
      return true;
    }
  }
  return false;
}

function findCSNumericalSerie(i, j, language1, numPathEn) {
  // LOGGER.debug('FIND CS NUMERICAL SERIE');
  let found = false;
  let count = 0;

  for (let k = i + j; k < language1.length; k++) {
    if (language1[k].startsWith(numPathEn)) {
      found = true;
      break;
    }
    count++;
  }

  if (found) {
    for (let k = i + j; k < i + j + count; k++) {
      LOGGER.debug('CS: ' + cropPath(language1[k]) + ' -> extra word');
    }
    i--;
    j += count;
  } else {
    return null;
  }
  return [i, j];
}

function comparePlural(i, j, language1, language2) {
  // LOGGER.debug('COMPARE PLURAL');
  let foundCs1 = false;
  let foundCs2 = false;
  let foundCs5 = false;
  let number;
  // convert last symbol from Czech localization to number if it's possible
  for (let k = 0; k < 10; k++) {
    if (isNumeric(language1[i + j].charAt(language1[i + j].length - 3))) {
      number = Number(language1[i + j].charAt(language1[i + j].length - 3));
      // LOGGER.debug(number);
      if (number === 0) {
        LOGGER.debug('CS: ' + cropPath(language1[i + j]) + ' -> mising suffix "_0" in EN');
        j++;
      } else if (number === 1 && cropEnd(language2[i]).endsWith('_plural')) {
        foundCs1 = true;
        LOGGER.debug('CS: ' + cropPath(language1[i + j]) + ' -> mising EN singular');
        j++;
      } else if (number === 1) {
        foundCs1 = true;
        i++;
      } else if (number === 2 && cropEnd(language2[i]).endsWith('_plural')) {
        j++;
        foundCs2 = true;
      } else if (number === 5 && cropEnd(language2[i]).endsWith('_plural')) {
        foundCs5 = true;
        break;
      } else if (number === 2 || number === 5) {
        if (number === 2) {
          foundCs2 = true;
        } else {
          foundCs5 = true;
        }

        if (cropEnd(language2[i + 1]).endsWith('_plural')) {
          LOGGER.debug('EN: ' + cropPath(language2[i]) + ' -> missing singular number in CS');
          i++;
        } else {
          if (language2[i].startsWith(language2[i - 1])) {
            LOGGER.debug(cropPath(language1[i + j]) + ' vs. ' + cropPath(language2[i]) +
            ' -> missing singular number in CS and plural in EN');
            i++;
          } else {
            LOGGER.debug(cropPath(language1[i + j]) + ' vs. ' + cropPath(language2[i]) +
            ' -> missing plural in EN');
            j++;
          }
        }
      } else {
        LOGGER.debug('CS: ' + cropPath(language1[i + j]) + ' -> invalid number');
      }
      // LOGGER.debug(cropPath(language1[i + j]) + ' | ' + cropPath(language2[i]));
    } else {
      // If cs numerical serie is not complete
      if (language2[i].endsWith('_plural')) {
        LOGGER.debug('CS: ' + cropPath(language1[i + j - 1]) + ' -> missing amount number in CS');
        j--;
        break;
      } else if (foundCs2) {
        LOGGER.debug('CS: ' + cropPath(language1[i + j - 1])
        + ' -> please check numerical serie for possible missing numbers');
        break;
      }
      return [i, j];
    }
  }
  // control of Czech numbers, defining quantity
  if (foundCs1 || foundCs2 || foundCs5) {
    if (!foundCs1) {
      LOGGER.debug('CS: ' + cropPath(language1[i + j]) + ' -> missing number "1" in CS');
    } if (!foundCs2) {
      LOGGER.debug('CS: ' + cropPath(language1[i + j]) + ' -> missing number "2" in CS');
    } if (!foundCs5) {
      LOGGER.debug('CS: ' + cropPath(language1[i + j]) + ' -> missing number "5" in CS');
    }
  }
  return [i, j];
}

function compareMessages(language1, language2) {
  let i;
  let j = 0;
  let iterators;
  for (i = 0; i + j < language1.length && i < language2.length; i++) {
    // LOGGER.debug(language1[i + j] + ' VS. ' + language2[i]);
    if (language1[i + j] === language2[i]) {
      // Messages are in pair ~(_8^(I)
    } else {
      // Messages are NOT in pair, check if there is some context
      const temp1 = cropEnd(language1[i + j]);
      const temp2 = cropEnd(language2[i]);

      if (temp1.startsWith(temp2) && (temp1.length - 2 === temp2.length ||
          temp1.length + 4 === temp2.length)) {
        // check for plural match
        iterators = comparePlural(i, j, language1, language2);
        i = iterators[0];
        j = iterators[1];
      } else if (language2[i + 1].endsWith('_plural') && language2.length > i + 1 &&
        verifyCSNumericalSerie(i, j, language1, temp2)) {
        // if the next word in EN ends with _plural, program tries to find CS numerical serie
        iterators = findCSNumericalSerie(i, j, language1, temp2);
        i = iterators[0];
        j = iterators[1];
      } else if (temp2.endsWith('_plural')) {
        //
        if (temp2 === language2[i - 1].substring(0, (language2[i - 1].length - 9))) {
          // check if there is singular in EN
          if (temp1.startsWith(cropEnd(language2[i - 1]))) {
            LOGGER.debug('CS: ' + cropPath(language1[i + j - 1]) + ' -> defining plural in CS without suffix');
          }
        } else {
          // EN plural hasn't got singular
          LOGGER.debug('EN: ' + cropPath(language2[i]) + ' -> missing singular word in EN');
          // check if there is plural in CS, matching EN plural
          iterators = comparePlural(i, j, language1, language2);
          i = iterators[0];
          j = iterators[1];
        }
      } else if (temp2.endsWith('_0')) {
        // check if the base of the words is the same (means numerical serie)
        if (temp1.startsWith(temp2.substring(0, temp2.length - 2))) {
          LOGGER.debug('EN: ' + cropPath(language2[i]) + ' -> missing suffix "_0" in CS');
          i++;
          j--;
          iterators = comparePlural(i, j, language1, language2);
          i = iterators[0];
          j = iterators[1];
          //
        } else if (verifyCSNumericalSerie(i, j, language1, temp2.substring(0, temp2.length() - 2))) {
          // try to find rest of the numerical serie in CS
          iterators = findCSNumericalSerie(i, j, language1, temp2.substring(0, temp2.length() - 2));
          i = iterators[0];
          j = iterators[1];
        } else {
          // if the matching numerical serie not found
          LOGGER.debug('EN: ' + cropPath(language2[i]) + ' -> missing suffix "_0" in CS');
        }
      } else if (isUpperCase(temp1.charAt(temp1.length - 1)) &&
                 isUpperCase(temp1.charAt(temp1.length - 2)) &&
                 isUpperCase(temp2.charAt(temp2.length - 1)) &&
                 isUpperCase(temp2.charAt(temp2.length - 2))) {
        // check if it has more than one uppercase letters
        iterators = findClosestMatch(i, j, language1, language2);
        i = iterators[0];
        j = iterators[1];
      } else {
        if (language1.length > i + j + 1 && language2.length > i + 1) {
          // check if next pair is OK, than it could be typing error
          if (language1[i + j + 1] === language2[i + 1]) {
            LOGGER.debug(language1[i + j] + ' vs. ' + cropPath(language2[i]) + '-> mismatch in both languages');
          } else {
            // search for next closest match
            iterators = findClosestMatch(i, j, language1, language2);
            i = iterators[0];
            j = iterators[1];
          }
        } else {
          LOGGER.debug(cropPath(language1[i + j]) + ' vs. ' + cropPath(language2[i]) + ' -> match not found for both paths');
        }
      }
      // return;
      // LOGGER.debug(pathsCs[i + j] + ' VS. ' + pathsEn[i]);
    }
  }
  //
  // check for extra words when one of the msg list reached the end
  if (i + j < language1.length) {
    for (; i + j < language1.length; j++) {
      // LOGGER.debug('CS: ' + cropPath(language1[i + j]) + ' -> extra word');
      extraWords.push('CS: ' + cropPath(language1[i + j]));
    }
  }
  if (i < language2.length) {
    for (; i < language2.length; i++) {
      // LOGGER.debug('EN: ' + cropPath(language2[i]) + ' -> extra word');
      extraWords.push('EN: ' + cropPath(language2[i]));
    }
  }
  // control found paths for non matched pairs
  checkExtraWords();
}

// Main
for (let i = 0; i < pathsToLocales.length; i++) {
  if (noMissingFiles(pathsToLocales[i])) {
    const pathsCs = jsonToPaths(pathsToLocales[i] + '/cs.json');
    const pathsEn = jsonToPaths(pathsToLocales[i] + '/en.json');
    LOGGER.debug('Comparing JSON catalogs in: ' + pathsToLocales[i]);
    compareMessages(pathsCs, pathsEn);
  }
}
