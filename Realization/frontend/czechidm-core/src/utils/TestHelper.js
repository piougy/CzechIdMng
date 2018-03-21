import fs from 'fs';
import jPath from 'JSONPath';

/**
 * TestHelpers
 * - LocalizationTester
 *
 * TODO: logger - use lng1 / lng2 - other lng than cs / en can be given.
 * TODO: support more lng (plural is implemented for cs (1-2-5 ) and en (_plural))
 *
 * @author Petr Hanák
 * @author Radek Tomiška
 */
export class LocalizationTester {

  constructor() {
    this.extraWords = [];
    // validation result - method #compareMessages(...) has to be called
    // construct new LocalizationTester after #compareMessages(...) is executed (not reusable)
    this.result = true;
  }

  noMissingFiles(folderWithLocalizationFiles) {
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
      LOGGER.error('Missing both localization files in: ' + folderWithLocalizationFiles);
      return false;
    } else if (!foundCs) {
      LOGGER.error('Missing cs.json localization file in: ' + folderWithLocalizationFiles);
      return false;
    } else if (!foundEn) {
      LOGGER.error('Missing en.json localization file in: ' + folderWithLocalizationFiles);
      return false;
    } else if (foundCs && foundEn) {
      return true;
    }
  }

  isNumeric(num) {
    return !isNaN(num);
  }

  isUpperCase(str) {
    return str === str.toUpperCase();
  }

  cropPath(jpath) {
    // delete $ sign from path
    return jpath.substring(1, jpath.length);
  }

  cropEnd(jpath) {
    // crop symbols at the end for easier comparing
    return jpath.substring(0, jpath.length - 2);
  }

  cropLanguage(word) {
    return word.substring(4, word.length);
  }

  jsonToPaths(json) {
    return jPath({
      json,
      path: '$..*',
      resultType: 'path'
    });
  }

  checkExtraWords() {
    let word;
    let found = false;
    while (this.extraWords.length > 0) {
      word = this.extraWords.shift();
      for (let i = 0; i < this.extraWords.length; i++) {
        if (this.cropLanguage(this.extraWords[i]) === (this.cropLanguage(word)) &&
            !this.extraWords[i].startsWith(word.substring(0, 2))) {
          found = true;
          this.extraWords.splice(i, 1);
        }
      }
      if (!found) {
        LOGGER.error(word + ' -> extra word');
        this.result = false;
      } else {
        found = false;
      }
    }
  }

  findClosestMatch(i, j, language1, language2) {
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
        this.extraWords.push('CS: ' + this.cropPath(language1[k]));
      }
      i--;
      j += count1;
    } else if (count2 < count1 && found2) {
      for (let k = i; k < i + count2; k++) {
        // LOGGER.debug('EN: ' + cropPath(language2[k]) + ' -> extra word');
        this.extraWords.push('EN: ' + this.cropPath(language2[k]));
      }
      i += count2 - 1;
      j -= count2;
    } else if (found1) {
      for (let k = i + j; k < i + j + count1; k++) {
        // LOGGER.debug('CS: ' + cropPath(language1[k]) + ' -> extra word');
        this.extraWords.push('CS: ' + this.cropPath(language1[k]));
      }
      i--;
      j += count1;
    } else if (found2) {
      for (let k = i; k < i + count2; k++) {
        // LOGGER.debug('EN: ' + cropPath(language2[k]) + ' -> extra word');
        this.extraWords.push('EN: ' + this.cropPath(language2[k]));
      }
      i += count2 - 1;
      j -= count2;
    } else {
      LOGGER.error(this.cropPath(language1[i + j]) + ' vs. ' + this.cropPath(language2[i]) + ' -> match not found for both words');
      this.result = false;
    }
    return [i, j];
  }

  verifyCSNumericalSerie(i, j, language1, numPathEn) {
    // LOGGER.debug('VERIFY NUMERICAL SERIE');
    for (let k = i + j; k < language1.length; k++) {
      if (language1[k].startsWith(numPathEn)) {
        return true;
      }
    }
    return false;
  }

  findCSNumericalSerie(i, j, language1, numPathEn) {
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
        LOGGER.error('CS: ' + this.cropPath(language1[k]) + ' -> extra word');
        this.result = false;
      }
      i--;
      j += count;
    } else {
      return null;
    }
    return [i, j];
  }

  comparePlural(i, j, language1, language2) {
    // LOGGER.debug('COMPARE PLURAL');
    let foundCs1 = false;
    let foundCs2 = false;
    let foundCs5 = false;
    let number;
    // convert last symbol from Czech localization to number if it's possible
    for (let k = 0; k < 10; k++) {
      if (this.isNumeric(language1[i + j].charAt(language1[i + j].length - 3))) {
        number = Number(language1[i + j].charAt(language1[i + j].length - 3));
        // LOGGER.debug(number);
        if (number === 0) {
          LOGGER.error('CS: ' + this.cropPath(language1[i + j]) + ' -> mising suffix "_0" in EN');
          this.result = false;
          j++;
        } else if (number === 1 && this.cropEnd(language2[i]).endsWith('_plural')) {
          foundCs1 = true;
          LOGGER.error('CS: ' + this.cropPath(language1[i + j]) + ' -> mising EN singular');
          this.result = false;
          j++;
        } else if (number === 1) {
          foundCs1 = true;
          i++;
        } else if (number === 2 && this.cropEnd(language2[i]).endsWith('_plural')) {
          j++;
          foundCs2 = true;
        } else if (number === 5 && this.cropEnd(language2[i]).endsWith('_plural')) {
          foundCs5 = true;
          break;
        } else if (number === 2 || number === 5) {
          if (number === 2) {
            foundCs2 = true;
          } else {
            foundCs5 = true;
          }

          if (this.cropEnd(language2[i + 1]).endsWith('_plural')) {
            LOGGER.error('EN: ' + this.cropPath(language2[i]) + ' -> missing singular number in CS');
            this.result = false;
            i++;
          } else {
            if (language2[i].startsWith(language2[i - 1])) {
              LOGGER.error(this.cropPath(language1[i + j]) + ' vs. ' + this.cropPath(language2[i]) +
              ' -> missing singular number in CS and plural in EN');
              this.result = false;
              i++;
            } else {
              LOGGER.error(this.cropPath(language1[i + j]) + ' vs. ' + this.cropPath(language2[i]) +
              ' -> missing plural in EN');
              this.result = false;
              j++;
            }
          }
        } else {
          LOGGER.error('CS: ' + this.cropPath(language1[i + j]) + ' -> invalid number');
          this.result = false;
        }
        // LOGGER.debug(cropPath(language1[i + j]) + ' | ' + cropPath(language2[i]));
      } else {
        // If cs numerical serie is not complete
        if (language2[i].endsWith('_plural')) {
          LOGGER.debug('CS: ' + this.cropPath(language1[i + j - 1]) + ' -> missing amount number in CS');
          this.result = false;
          j--;
          break;
        } else if (foundCs2) {
          LOGGER.error('CS: ' + this.cropPath(language1[i + j - 1])
          + ' -> please check numerical serie for possible missing numbers');
          this.result = false;
          break;
        }
        return [i, j];
      }
    }
    // control of Czech numbers, defining quantity
    if (foundCs1 || foundCs2 || foundCs5) {
      if (!foundCs1) {
        LOGGER.error('CS: ' + this.cropPath(language1[i + j]) + ' -> missing number "1" in CS');
        this.result = false;
      } if (!foundCs2) {
        LOGGER.error('CS: ' + this.cropPath(language1[i + j]) + ' -> missing number "2" in CS');
        this.result = false;
      } if (!foundCs5) {
        LOGGER.error('CS: ' + this.cropPath(language1[i + j]) + ' -> missing number "5" in CS');
        this.result = false;
      }
    }
    return [i, j];
  }

  compareMessages(json1, json2) {
    if (!json1) {
      LOGGER.error('CS: json not found');
      return false;
    }
    if (!json2) {
      LOGGER.error('EN: json not found');
      return false;
    }
    //
    const language1 = this.jsonToPaths(json1);
    const language2 = this.jsonToPaths(json2);
    //
    let i;
    let j = 0;
    let iterators;
    for (i = 0; i + j < language1.length && i < language2.length; i++) {
      // LOGGER.debug(language1[i + j] + ' VS. ' + language2[i]);
      if (language1[i + j] === language2[i]) {
        // Messages are in pair ~(_8^(I)
      } else {
        // Messages are NOT in pair, check if there is some context
        const temp1 = this.cropEnd(language1[i + j]);
        const temp2 = this.cropEnd(language2[i]);

        if (temp1.startsWith(temp2) && (temp1.length - 2 === temp2.length ||
            temp1.length + 4 === temp2.length)) {
          // check for plural match
          iterators = this.comparePlural(i, j, language1, language2);
          i = iterators[0];
          j = iterators[1];
        } else if (language2[i + 1].endsWith('_plural') && language2.length > i + 1 &&
          this.verifyCSNumericalSerie(i, j, language1, temp2)) {
          // if the next word in EN ends with _plural, program tries to find CS numerical serie
          iterators = this.findCSNumericalSerie(i, j, language1, temp2);
          i = iterators[0];
          j = iterators[1];
        } else if (temp2.endsWith('_plural')) {
          //
          if (temp2 === language2[i - 1].substring(0, (language2[i - 1].length - 9))) {
            // check if there is singular in EN
            if (temp1.startsWith(this.cropEnd(language2[i - 1]))) {
              LOGGER.error('CS: ' + this.cropPath(language1[i + j - 1]) + ' -> defining plural in CS without suffix');
              this.result = false;
            }
          } else {
            // EN plural hasn't got singular
            LOGGER.error('EN: ' + this.cropPath(language2[i]) + ' -> missing singular word in EN');
            this.result = false;
            // check if there is plural in CS, matching EN plural
            iterators = this.comparePlural(i, j, language1, language2);
            i = iterators[0];
            j = iterators[1];
          }
        } else if (temp2.endsWith('_0')) {
          // check if the base of the words is the same (means numerical serie)
          if (temp1.startsWith(temp2.substring(0, temp2.length - 2))) {
            LOGGER.error('EN: ' + this.cropPath(language2[i]) + ' -> missing suffix "_0" in CS');
            this.result = false;
            i++;
            j--;
            iterators = this.comparePlural(i, j, language1, language2);
            i = iterators[0];
            j = iterators[1];
            //
          } else if (this.verifyCSNumericalSerie(i, j, language1, temp2.substring(0, temp2.length() - 2))) {
            // try to find rest of the numerical serie in CS
            iterators = this.findCSNumericalSerie(i, j, language1, temp2.substring(0, temp2.length() - 2));
            i = iterators[0];
            j = iterators[1];
          } else {
            // if the matching numerical serie not found
            LOGGER.error('EN: ' + this.cropPath(language2[i]) + ' -> missing suffix "_0" in CS');
            this.result = false;
          }
        } else if (this.isUpperCase(temp1.charAt(temp1.length - 1)) &&
                   this.isUpperCase(temp1.charAt(temp1.length - 2)) &&
                   this.isUpperCase(temp2.charAt(temp2.length - 1)) &&
                   this.isUpperCase(temp2.charAt(temp2.length - 2))) {
          // check if it has more than one uppercase letters
          iterators = this.findClosestMatch(i, j, language1, language2);
          i = iterators[0];
          j = iterators[1];
        } else {
          if (language1.length > i + j + 1 && language2.length > i + 1) {
            // check if next pair is OK, than it could be typing error
            if (language1[i + j + 1] === language2[i + 1]) {
              LOGGER.error(language1[i + j] + ' vs. ' + this.cropPath(language2[i]) + '-> mismatch in both languages');
              this.result = false;
            } else {
              // search for next closest match
              iterators = this.findClosestMatch(i, j, language1, language2);
              i = iterators[0];
              j = iterators[1];
            }
          } else {
            LOGGER.error(this.cropPath(language1[i + j]) + ' vs. ' + this.cropPath(language2[i]) + ' -> match not found for both paths');
            this.result = false;
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
        this.extraWords.push('CS: ' + this.cropPath(language1[i + j]));
      }
    }
    if (i < language2.length) {
      for (; i < language2.length; i++) {
        // LOGGER.debug('EN: ' + cropPath(language2[i]) + ' -> extra word');
        this.extraWords.push('EN: ' + this.cropPath(language2[i]));
      }
    }
    // control found paths for non matched pairs
    this.checkExtraWords();
    //
    return this.result;
  }
}
