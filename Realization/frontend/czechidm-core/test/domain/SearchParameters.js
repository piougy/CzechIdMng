import chai, { expect } from 'chai';
import dirtyChai from 'dirty-chai';
chai.use(dirtyChai);
import _ from 'lodash';

import SearchParameters from '../../src/domain/SearchParameters';

/**
 * Util.Ui. method tests
 *
 * @author Marek Klement
 * @author Radek Tomiška
 */

describe('SearchParameters', function testUiUtils() {
  describe('[equals]', function testEquals() {
    //
    it('- empty are equals', function test() {
      const spOne = new SearchParameters();
      const spTwo = new SearchParameters();

      expect(spOne.equals(spTwo)).to.be.true();
    });

    it('- filter with array equals', function test() {
      const spOne = new SearchParameters().setFilter('test', ['one', 'two', 'three']);
      const spTwo = new SearchParameters().setFilter('test', ['one', 'two', 'three']);

      expect(spOne.equals(spTwo)).to.be.true();
    });

    it('- filter with array not equals', function test() {
      const spOne = new SearchParameters().setFilter('test', ['one', 'two', 'three']);
      const spTwo = new SearchParameters().setFilter('test', ['one', 'three']);
      //
      expect(_.isEqual(spTwo.getFilters().keySeq().toArray(), ['test'])).to.be.true();
      expect(_.isEqual(spTwo.getFilters().get('test'), ['one', 'three'])).to.be.true();
      expect(spOne.equals(spTwo)).to.be.false();
    });

    it('- filter wit same keys, different order equesl', function test() {
      const spOne = new SearchParameters().setFilter('test', ['one']).setFilter('test2', ['2']);
      const spTwo = new SearchParameters().setFilter('test2', ['2']).setFilter('test', ['one']);

      expect(spOne.equals(spTwo)).to.be.true();
    });
  });
});
