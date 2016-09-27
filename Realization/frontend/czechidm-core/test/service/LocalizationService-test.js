import { expect } from 'chai';
import coreLocale from '../../src/locales/cs.json';
import { LocalizationService } from '../../src/services';

describe('LocalizationService', function localizationServiceTest() {
  // i18next is singleton and cant be defined twice
  it('- unloaded localization', function test() {
    expect(LocalizationService.i18n('app.name')).to.be.undefined();
  });

  it('- author has to be BCV solutions s.r.o.', function test() {
    expect(coreLocale.app.author.name).to.equal('BCV solutions s.r.o.');
  });

  it('- load localization');
});
