import chai, { expect } from 'chai';
import dirtyChai from 'dirty-chai';
chai.use(dirtyChai);

export default class TestFieldsUtil {

  /**
   * Method testComponentWithValues test react component for values
   *
   * @param component - React component. For example created by TestUtils.renderIntoDocument()
   * @param mapValueExcept - Map with test value and excepted value, Example { 'testValue': true }
   *
   */
  static testComponentWithValues(component, mapValueExcept) {
    for (const key in mapValueExcept) {
      if (mapValueExcept.hasOwnProperty(key)) {
        component.setValue(key);
        expect(component.isValid()).equal(mapValueExcept[key]);
      }
    }
  }
}
