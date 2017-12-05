import chai, { expect } from 'chai';
import dirtyChai from 'dirty-chai';
import fs from 'fs';

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
// console.log(content);
console.log('Délka! ' + contentCs.length);
let jsonCs = JSON.parse(contentCs);
let arrCs = [];
for (let i in Object.keys(jsonCs)) {
  arrCs.push(Object.keys(jsonCs)[i]);
  // console.log(arrCs[i]);
}
console.log(arrCs);

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
