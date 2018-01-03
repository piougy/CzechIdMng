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

let contentCs = fs.readFileSync(accLocales + 'cs.json', 'utf8', (err, data) => {
  console.log(err);
});

let jsonCs = JSON.parse(contentCs);
let pathsCs = jPath({json: jsonCs, path: '$..*', resultType: 'path'});
// console.log(pathsCs);

let contentEn = fs.readFileSync(accLocales + 'en.json', 'utf8', (err, data) => {
  console.log(err);
});

let jsonEn = JSON.parse(contentEn);
let pathsEn = jPath({json: jsonCs, path: '$..message', resultType: 'path'});
console.log(pathsEn);

function compareMessages(language1, language2) {
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
