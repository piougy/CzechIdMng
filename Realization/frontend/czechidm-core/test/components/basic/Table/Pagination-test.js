import React from 'react';
import TestUtils from 'react-addons-test-utils';
import chai, { expect } from 'chai';
import dirtyChai from 'dirty-chai';
chai.use(dirtyChai);
//
import * as Basic from '../../../../src/components/basic';

/**
 * Basic.Pagination tests
 *
 * @author Radek Tomiška
 */
describe('Basic Pagination', function paginationTest() {
  /**
   * Clean DOM afrer each test
   * @param
   * @return
   */
  /*
  afterEach(function(done) {
    ReactDOM.unmountComponentAtNode(document.body) // Assuming mounted to document.body
    document.body.innerHTML = '';                // Just to be sure :-P
    setTimeout(done);
  });*/

  it('- total 105 - page size 10, 10 pages', function test() {
    const pagination = TestUtils.renderIntoDocument(<Basic.Pagination total={105} paginationHandler={() => {}}/>);

    expect(pagination.hasPrev()).to.be.false();
    expect(pagination.hasNext()).to.be.true();
    expect(pagination.state.currentPage).to.equal(0);
    expect(pagination.getMaxPage()).to.equal(10);
    // next page
    TestUtils.Simulate.click(pagination.refs['page-next']);
    expect(pagination.state.currentPage).to.equal(1);
  });

  it('- total 105 - from 20, page size 25, 10 pages', function test() {
    const pagination = TestUtils.renderIntoDocument(<Basic.Pagination total={105} page={4} size={25} paginationHandler={() => {}}/>);
    expect(pagination.hasPrev()).to.be.true();
    expect(pagination.hasNext()).to.be.false();
    expect(pagination.state.currentPage).to.equal(4);
    expect(pagination.getMaxPage()).to.equal(4);
    // not exist page
    pagination.setPage(10);
    expect(pagination.state.currentPage).to.equal(pagination.getMaxPage());
    // prev page
    TestUtils.Simulate.click(pagination.refs['page-prev']);
    expect(pagination.state.currentPage).to.equal(3);
    // first page
    TestUtils.Simulate.click(pagination.refs['page-first']);
    expect(pagination.state.currentPage).to.equal(0);
  });
});
