import chai, { expect } from 'chai';
import dirtyChai from 'dirty-chai';
import { LocalizationTester } from '../../src/utils/TestHelper';
chai.use(dirtyChai);

/**
 * Validate localization
 *
 * @author Petr Hanák
 * @author Radek Tomiška
 */
describe('Comparing JSON catalogs', function test() {
  it('- core', function validate() {
    const cs = require('../../src/locales/cs.json');
    const en = require('../../src/locales/en.json');
    //
    const localizationTester = new LocalizationTester();
    expect(localizationTester.compareMessages(cs, en)).to.be.true();
    LOGGER.debug('core: Comparing JSON catalogs - ok');
  });
});
