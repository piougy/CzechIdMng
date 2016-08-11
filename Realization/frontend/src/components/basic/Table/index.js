

import _ from 'lodash';
import Table from './Table';
import Pagination from './Pagination';
import Column from './Column';
import DefaultCell from './DefaultCell';
import SortHeaderCell from './SortHeaderCell';
import TextCell from './TextCell';
import LinkCell from './LinkCell';
import DateCell from './DateCell';
import BooleanCell from './BooleanCell';
import EnumCell from './EnumCell';

const TableRoot = {Table, Column, Pagination,
  Cell: DefaultCell, SortHeaderCell, TextCell,
  LinkCell, DateCell, BooleanCell, EnumCell
};
_.merge(TableRoot, { BasicTable: TableRoot });
TableRoot.version = '0.0.1';
module.exports = TableRoot;
