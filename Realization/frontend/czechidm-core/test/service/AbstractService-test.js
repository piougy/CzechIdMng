import chai, { expect } from 'chai';
import dirtyChai from 'dirty-chai';
chai.use(dirtyChai);
//
import { AbstractService } from '../../src/modules/core/services';
import SearchParameters from '../../src/modules/core/domain/SearchParameters';

/**
 * "Blank" AbstractService
 */
class DefaultAbstractService extends AbstractService {

  getApiPath() {
    return null;
  }
}

describe('AbstractService', function abstractServiceTest() {
  it('- constructor should throw TypeError exception', function test() {
    /* eslint no-new: 0 */
    expect(() => { new AbstractService(); }).to.throw(TypeError);
  });

  this.defaultAbstractService = null;

  it('- constructor of DefaultAbstractService should not throw TypeError exception (getApiPath method ids defined) ', function test() {
    expect(() => { this.defaultAbstractService = new DefaultAbstractService(); }).to.not.throw(TypeError);
  });

  it('- default search parameters should be sorted by id asc', function test() {
    expect(this.defaultAbstractService.getDefaultSearchParameters().getSorts().size).to.equal(1);
    expect(this.defaultAbstractService.getDefaultSearchParameters().getSorts().has('id')).to.be.true();
  });

  it('- default search parameters should not contain a filters', function test() {
    expect(this.defaultAbstractService.getDefaultSearchParameters().getFilters().size).to.equal(0);
  });

  describe('[#mergeSearchParameters]', function testMergeSearchParameters() {
    let mergedSearchParameters;

    it('- merge default search parameters with empty search params should eql to default search parameters', function test() {
      mergedSearchParameters = this.defaultAbstractService.mergeSearchParameters(this.defaultAbstractService.getDefaultSearchParameters(), null);
      expect(mergedSearchParameters).to.eql(this.defaultAbstractService.getDefaultSearchParameters());
      mergedSearchParameters = this.defaultAbstractService.mergeSearchParameters(null, this.defaultAbstractService.getDefaultSearchParameters());
      expect(mergedSearchParameters).to.eql(this.defaultAbstractService.getDefaultSearchParameters());
    });

    let defaultSearchParameters = new SearchParameters();
    defaultSearchParameters = defaultSearchParameters.setFilter('id', 1);
    defaultSearchParameters = defaultSearchParameters.setSort('name');
    defaultSearchParameters = defaultSearchParameters.setPage(0);
    defaultSearchParameters = defaultSearchParameters.setSize(100);

    let userSearchParameters = new SearchParameters();
    userSearchParameters = userSearchParameters.setFilter('id', 2).setFilter('name', '1');
    userSearchParameters = userSearchParameters.setSort('name');
    userSearchParameters = userSearchParameters.setPage(1);
    userSearchParameters = userSearchParameters.setSize(20);


    it('- merge default search parameters with user defined search parameters - user defined search parameters has higher priority', function test() {
      mergedSearchParameters = this.defaultAbstractService.mergeSearchParameters(defaultSearchParameters, userSearchParameters);
      expect(mergedSearchParameters.getFilters().size).to.equal(2);
      expect(mergedSearchParameters.getFilters().get('id')).to.not.equal(defaultSearchParameters.getFilters().get('id'));
      expect(mergedSearchParameters.getFilters().get('id')).to.equal(userSearchParameters.getFilters().get('id'));
      expect(mergedSearchParameters.getFilters().get('name')).to.equal(userSearchParameters.getFilters().get('name'));
      expect(mergedSearchParameters.getSorts()).to.eql(userSearchParameters.getSorts());
      expect(mergedSearchParameters.getSize()).to.equal(userSearchParameters.getSize());
      expect(mergedSearchParameters.getPage()).to.equal(userSearchParameters.getPage());
    });
  });
});
