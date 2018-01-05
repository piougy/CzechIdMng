import chai, { expect } from 'chai';
import dirtyChai from 'dirty-chai';
import fs from 'fs';
import jPath from 'JSONPath';

chai.use(dirtyChai);

const distLocales = 'dist/locales/';
const accLocales = '../czechidm-acc/src/locales/';
const coreLocales = '../czechidm-core/src/locales/';
const exampleLocales = '../czechidm-example/src/locales/';

let files = fs.readdirSync(accLocales);
console.log(files);

// Add searching for files and automatic reading of languages
// read CS file
let contentCs = fs.readFileSync(accLocales + 'cs.json', 'utf8', (err, data) => {
  console.log(err);
});
let jsonCs = JSON.parse(contentCs);
let pathsCs = jPath({json: jsonCs, path: '$..*', resultType: 'path'});
console.log(pathsCs[0]);
console.log(typeof pathsCs[0]);

// Read EN file
let contentEn = fs.readFileSync(accLocales + 'en.json', 'utf8', (err, data) => {
  console.log(err);
});
let jsonEn = JSON.parse(contentEn);
let pathsEn = jPath({json: jsonEn, path: '$..*', resultType: 'path'});
console.log(pathsEn[0]);
console.log(typeof pathsEn[0]);

function compareMessages(language1, language2) {
  let i;
  // change variables to 0 after testing of comparators
  let j = 0;
  for (i = 0; i + j < language1.length && i < language2.length; i++) {
    console.log(pathsCs[i + j] + ' VS. ' + pathsEn[i]);
    if (pathsCs[i + j] === pathsEn[i]) {
      // Messages are in pair
    } else {
      // Messages are NOT in pair, check if there is some context
      console.log('NE!');
      console.log(pathsCs[i + j].substring(0, (pathsCs[i + j].length - 2)));
      console.log(pathsEn[i].substring(0, (pathsEn[i].length - 2)));
      return;
      // console.log(pathsCs[i + j] + ' VS. ' + pathsEn[i]);
    }
  }

  if (language1.length > language2.length) {
    console.log('Language 1 is way longer than 2!');
  } else {
    console.log('Language 2 is way longer than 1!');
  }
}

compareMessages(pathsCs, pathsEn);

// Functionality check
// describe('Testing tests: ', function() {
//   it('To be matcher compares with ===', function() {
//     let a = 12;
//     let b = a;
//     expect(a).to.eql(b);
//     expect(a).not.to.be.null();
//   });
// });
//
// describe('A suite', function() {
//   it('contains spec with an expectation', function() {
//     expect(true).to.be.true('Reason: Paradox');
//   });
// });
