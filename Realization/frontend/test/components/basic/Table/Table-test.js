'use strict';

import React from 'react';
import ReactDOM from 'react-dom';
import TestUtils from 'react-addons-test-utils';
import { expect } from 'chai';
import faker from 'faker';
import moment from 'moment';
import _ from 'lodash';
//
import * as Basic from '../../../../src/components/basic';
import TableRow from '../../../../src/components/basic/Table/Row';

describe('Basic Table', function() {

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

  describe('[empty data]', function() {
    const noData = 'noData text';
    const shallowRenderer = TestUtils.createRenderer();
    shallowRenderer.render(<Basic.Table noData={noData}/>);
    const table = shallowRenderer.getRenderOutput();
    it('- table without data should return Alert with no data', function() {
      expect(table.type).to.equal('div');
      expect(table.props.children.type).to.equal(Basic.Alert);
      expect(table.props.children.props.level).to.equal('info');
      expect(table.props.children.props.text).to.equal(noData);
    });

  });

  describe('[not rendered]', function() {
    const shallowRenderer = TestUtils.createRenderer();
    shallowRenderer.render(<Basic.Table rendered={false}/>);
    const table = shallowRenderer.getRenderOutput();
    it('- table should not be rendered', function() {
      expect(table).to.be.null;
    });
  });

  describe('[json data]', function() {
    let data = [];
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

    it('- with data should return loading wrapper', function() {
      expect(tableWrapper.props.children.type).to.equal(Basic.Loading); // tabla is wrapped by loading, if data is not null
      expect(tableWrapper.props.children.props.showLoading).to.equal(false);// and loding is not shown
    });

    const table = tableWrapper.props.children.props.children;
    let header;
    let body;
    it('- loading wrapper should return table with header and body with rows and columns by input data', () => {
      expect(table.type).to.equal('table');
      header = table.props.children.find(c => c && c.type === 'thead');
      body = table.props.children.find(c => c && c.type === 'tbody');
      let footer = table.props.children.find(c => c && c.type === 'tfoot');
      expect(header).to.not.be.null;
      expect(body).to.not.be.null;
      expect(footer).to.be.undefined; // footer shold be undefined
    });

    it('- header should contain one row with columns by data properties', function() {
      expect(header).to.not.be.undefined;
      const headerRow = header.props.children;
      expect(headerRow.type).to.equal(TableRow);
      const firstDataRow = data[0];
      let dataProperties = [];
      for (let property in firstDataRow) {
        if (_.isObject(firstDataRow[property])) { // simple second lvl - TODO: recursion
          for (let nestedProperty in firstDataRow[property]) {
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

    it('- body should contain rows by input data', function() {
      expect(body).to.not.be.undefined;
      expect(body.props.children.length).to.equal(data.length);
    });

    it('- table row identifier should equal to id', function() {
      const tableInstance = TestUtils.renderIntoDocument(<Basic.Table data={data}/>);
      expect(tableInstance.getIdentifierProperty()).to.equal('id');
      expect(tableInstance.getIdentifier(4)).to.equal(1004);
    });

    it('- table row selection', function() {
      const tableInstance = TestUtils.renderIntoDocument(<Basic.Table data={data}/>);
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
