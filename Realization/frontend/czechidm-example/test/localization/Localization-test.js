import chai, { expect } from 'chai';
import dirtyChai from 'dirty-chai';
import { LocalizationTester } from 'czechidm-core/src/utils/TestHelper';

chai.use(dirtyChai);

/**
 * Validate localization
 *
 * @author Radek Tomiška
 */
describe('Comparing JSON catalogs', () => {
  it('- example', () => {
    const cs = require('../../src/locales/cs.json');
    const en = require('../../src/locales/en.json');
    //
    const localizationTester = new LocalizationTester();
    expect(localizationTester.compareMessages(cs, en)).to.be.true();
    LOGGER.debug('example: Comparing JSON catalogs - ok');
  });
});
