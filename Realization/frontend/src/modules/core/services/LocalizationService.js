'use strict';

import ConfigService from '../services/ConfigService';
import i18next from 'i18next';
import XHR from 'i18next-xhr-backend';
import LanguageDetector from 'i18next-browser-languagedetector';
import Cache from 'i18next-localstorage-cache';

const configService = new ConfigService();
const i18nextInstance = i18next
  .use(XHR)
  .use(Cache)
  .use(LanguageDetector);

/**
* Provides localization context
* # http://i18next.com/docs/api/#language
*/
export default class LocalizationService {

  /**
  * i18n inicialization
  */
  constructor(cb) {
    // init localization
    i18nextInstance
      .init({
        lng: 'cs',
        fallbackLng: 'cs',

        // have a common namespace used around the full app
        ns: configService.getEnabledModuleNames(),
        defaultNS: 'core',

        debug: false,

        interpolation: {
          escapeValue: false // not needed for react!!
        },

        backend: {
          // path where resources get loaded from
          loadPath: configService.getConfig('locales') + '/{{ns}}/locales/{{lng}}.json',

          // path to post missing resources
          // addPath: 'locales/add/{{lng}}/{{ns}}',

          // your backend server supports multiloading
          // /locales/resources.json?lng=de+en&ns=ns1+ns2
          allowMultiLoading: false,

          // allow cross domain requests
          crossDomain: false
        },
        cache: {
          // turn on or off
          enabled: false,
          // prefix for stored languages
          prefix: 'i18next_res_',
          // expiration
          expirationTime: 24*60*60*1000
        }
      },(error, t)=> {
        cb(error);
      });
  }

  /**
   * Returns localized message
   * - for supported options see http://i18next.com/pages/doc_features.html
   *
   * TODO: why static ?!
   *
   * @param  {string} key     localization key
   * @param  {object} options parameters
   * @return {string}         localized message
   */
  static i18n(key, options) {
    return i18nextInstance.t(key, options);
  }

  /**
   * Returns current language
   *
   * @return {string} locale
   */
  static getCurrentLanguage() {
    return i18nextInstance.language;
  }
}

/**
* Simple i18n wrapper
*/
export function i18n(key, options) {
  return LocalizationService.i18n(key, options);
}
