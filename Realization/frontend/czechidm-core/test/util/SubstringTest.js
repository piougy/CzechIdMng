import chai, { expect } from 'chai';
import dirtyChai from 'dirty-chai';
chai.use(dirtyChai);

import UiUtils from '../../src/utils/UiUtils';

describe('SubstingTest', function substringTest() {
  /**
   * Tests of cutting in the end
   * Basic function test
   */
  it('- load module components', function test() {
    expect(UiUtils.substringBegin('ahojj/j/', 5, '/')).to.equal('ahojj');
  });
  /**
   * Basic function test
   */
  it('- load module components', function test() {
    expect(UiUtils.substringBegin('ahojjj/jjhjhjgj/', 10, '/')).to.equal('ahojjj');
  });
  /**
   * Basic function test
   */
  it('- load module components', function test() {
    expect(UiUtils.substringBegin('ahojj/j/', 4, '/')).to.equal('');
  });
  /**
   * Test of position of cutChar same as needed length
   */
  it('- load module components', function test() {
    expect(UiUtils.substringBegin('ahojj/j/', 6, '/')).to.equal('ahojj');
  });
  /**
   * Test of low number wit cutChar at begining
   */
  it('- load module components', function test() {
    expect(UiUtils.substringBegin('/ahojj/j/', 2, '/')).to.equal('');
  });
  /**
   * Test with blank text
   */
  it('- load module components', function test() {
    expect(UiUtils.substringBegin('', 5, '/')).to.equal('');
  });
  /**
   * Test for null
   */
  it('- load module components', function test() {
    expect(UiUtils.substringBegin(null, 2, '/')).to.equal(null);
  });
  /**
   * Tests of cutting in the begining
   * Test of position of cutChar higher than needed length
   */
  it('- load module components', function test() {
    expect(UiUtils.substringEnd('ahojj/j/', 4, '/')).to.equal('/j/');
  });
  /**
   * Test of position of cutChar same as needed length
   */
  it('- load module components', function test() {
    expect(UiUtils.substringEnd('ahojj/j/', 3, '/')).to.equal('/j/');
  });
  /**
   * Test of position of cutChar lower than needed length
   */
  it('- load module components', function test() {
    expect(UiUtils.substringEnd('ahojj/j/', 2, '/')).to.equal('/');
  });
  /**
   * Test with blank text
   */
  it('- load module components', function test() {
    expect(UiUtils.substringEnd('', 2, '/')).to.equal('/');
  });
  /**
   * Test for null
   */
  it('- load module components', function test() {
    expect(UiUtils.substringEnd(null, 2, '/')).to.equal(null);
  });
});
