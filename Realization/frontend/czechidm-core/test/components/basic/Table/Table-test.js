import React from 'react';
import TestUtils from 'react-addons-test-utils';
import faker from 'faker';
import _ from 'lodash';
import chai, { expect } from 'chai';
import dirtyChai from 'dirty-chai';
chai.use(dirtyChai);
//
import * as Basic from '../../../../src/components/basic';
import TableRow from '../../../../src/components/basic/Table/Row';

/**
 * Basic.Table tests
 *
 * @author Radek Tomiška
 */
describe.skip('Basic Table', function basicTableTestSuite() {
  /**
   * Clean DOM afrer each test
   * @param
   * @return
   *//*
  afterEach(function(done) {
    ReactDOM.unmountComponentAtNode(document.body) // Assuming mounted to document.body
    document.body.innerHTML = '';                // Just to be sure :-P
    setTimeout(done);
  })*/

  describe('[empty data]', function basicTableTest() {
    const noData = 'noData text';
    const shallowRenderer = TestUtils.createRenderer();
    shallowRenderer.render(<Basic.Table noData={noData}/>);
    const tableWrapper = shallowRenderer.getRenderOutput();
    it('- table without data should return Alert with no data', function test() {
      const body = tableWrapper.props.children.props.children.props.children[1][0];
      const noDataAlert = body.props.children.props.children;
      expect(noDataAlert).to.not.be.null();
      expect(noDataAlert.type).to.equal(Basic.Alert);
      expect(noDataAlert.props.level).to.equal('info');
      expect(noDataAlert.props.text).to.equal(noData);
    });
  });

  describe('[not rendered]', function basicTableTest() {
    const shallowRenderer = TestUtils.createRenderer();
    shallowRenderer.render(<Basic.Table rendered={false}/>);
    const table = shallowRenderer.getRenderOutput();
    it('- table should not be rendered', function test() {
      expect(table).to.be.null();
    });
  });

  describe('[json data]', function basicTableTest() {
    const data = [];
    for (let i = 0; i < 10; i++) {
      data.push({
        id: i + 1000,
        Description: faker.lorem.sentence(),
        Name: faker.name.findName(),
        Email: faker.internet.email(),
        Nested: {
          One: faker.name.findName(),
          Two: faker.name.findName(),
          Three: faker.name.findName()
        }
      });
    }

    const shallowRenderer = TestUtils.createRenderer();
    shallowRenderer.render(<Basic.Table data={data}/>);
    const tableWrapper = shallowRenderer.getRenderOutput();

    it('- with data should return loading wrapper', function test() {
      expect(tableWrapper.props.children.type).to.equal(Basic.Loading); // tabla is wrapped by loading, if data is not null
      expect(tableWrapper.props.children.props.showLoading).to.equal(false);// and loding is not shown
    });

    const table = tableWrapper.props.children.props.children;
    const header = table.props.children[1][0];
    const body = table.props.children[1][1];
    //
    it('- loading wrapper should return table with header and body with rows and columns by input data', () => {
      expect(table.type).to.equal('table');
      const footer = table.props.children.find(c => c && c.type === 'tfoot');
      expect(header).to.not.be.null();
      expect(body).to.not.be.null();
      expect(footer).to.be.undefined(); // footer shold be undefined
    });

    it('- header should contain one row with columns by data properties', function test() {
      expect(header).to.not.be.undefined();
      const headerRow = header.props.children;
      expect(headerRow.type).to.equal(TableRow);
      const firstDataRow = data[0];
      const dataProperties = [];
      for (const property in firstDataRow) {
        if (_.isObject(firstDataRow[property])) { // simple second lvl - TODO: recursion
          for (const nestedProperty in firstDataRow[property]) {
            if (!firstDataRow[property].hasOwnProperty(nestedProperty)) {
              continue;
            }
            dataProperties.push(property + '.' + nestedProperty);
          }
        } else {
          dataProperties.push(property);
        }
      }
      expect(headerRow.props.columns.length).to.equal(dataProperties.length);
      headerRow.props.columns.forEach(column => {
        expect(dataProperties).to.include(column.props.property);
      });
    });

    it('- body should contain rows by input data', function test() {
      expect(body).to.not.be.undefined();
      expect(body.props.children.length).to.equal(data.length);
    });

    it('- table row identifier should equal to id', function test() {
      const tableInstance = TestUtils.renderIntoDocument(<Basic.Table data={data}/>);
      expect(tableInstance.getIdentifierProperty()).to.equal('id');
      expect(tableInstance.getIdentifier(4)).to.equal(1004);
    });

    it('- disabled table row selection', function test() {
      const tableInstance = TestUtils.renderIntoDocument(<Basic.Table data={data} />);
      tableInstance.selectRow(null, true);
      expect(tableInstance.getSelectedRows().length).to.eql(0);
    });

    it('- table row selection', function test() {
      const tableInstance = TestUtils.renderIntoDocument(<Basic.Table data={data} showRowSelection/>);
      tableInstance.selectRow(3, true);
      tableInstance.selectRow(5, true);
      expect(tableInstance.getSelectedRows()).to.eql([1003, 1005]);
      tableInstance.selectRow(5, false);
      expect(tableInstance.getSelectedRows()).to.eql([1003]);
      tableInstance.selectRow(null, true);
      expect(tableInstance.getSelectedRows().length).to.eql(data.length);
      tableInstance.selectRow(null, false);
      expect(tableInstance.getSelectedRows().length).to.equal(0);
    });
  });
});
