import AbstractComponent from './AbstractComponent/AbstractComponent';
import AbstractContextComponent from './AbstractContextComponent/AbstractContextComponent';
import AbstractContent from './AbstractContent/AbstractContent';
import AbstractForm from './AbstractForm/AbstractForm';
import AbstractFormComponent from './AbstractFormComponent/AbstractFormComponent';
import BasicForm from './BasicForm/BasicForm';
import Checkbox from './Checkbox/Checkbox';
import SelectBox from './SelectBox/SelectBox';
import EnumSelectBox from './EnumSelectBox/EnumSelectBox';
import BooleanSelectBox from './BooleanSelectBox/BooleanSelectBox';
import TextArea from './TextArea/TextArea';
import TextField from './TextField/TextField';
import FlashMessages from './FlashMessages/FlashMessages';
import HelpIcon from './HelpIcon/HelpIcon';
import Icon from './Icon/Icon';
import Loading from './Loading/Loading';
import { Panel, PanelHeader, PanelBody, PanelFooter } from './Panel/Panel';
import Toolbar from './Toolbar/Toolbar';
import { BasicTable } from './Table';
import Button from './Button/Button';
import SplitButton from './Button/SplitButton';
import MenuItem from './Button/MenuItem';
import DateTimePicker from './DateTimePicker/DateTimePicker';
import Alert from './Alert/Alert';
import Label from './Label/Label';
import Modal from './Modal/Modal';
import ProgressBar from './ProgressBar/ProgressBar';
import Confirm from './Confirm/Confirm';
import Row from './Row/Row';
import Well from './Well/Well';
import Tabs from './Tabs/Tabs';
import PageHeader from './PageHeader/PageHeader';
import ContentHeader from './ContentHeader/ContentHeader';
import LabelWrapper from './LabelWrapper/LabelWrapper';
import DateValue from './DateValue/DateValue';
import EnumLabel from './EnumLabel/EnumLabel';
import Collapse from './Collapse/Collapse';
import Tooltip from './Tooltip/Tooltip';
import Dropzone from './Dropzone/Dropzone';

const Components = {
  AbstractComponent,
  AbstractContextComponent,
  AbstractContent,
  AbstractForm,
  AbstractFormComponent,
  BasicForm,
  Checkbox,
  SelectBox,
  EnumSelectBox,
  BooleanSelectBox,
  TextArea,
  TextField,
  DateTimePicker,
  FlashMessages,
  HelpIcon,
  Icon,
  Loading,
  Panel,
  PanelHeader,
  PanelBody,
  PanelFooter,
  Toolbar,
  BasicTable,
  Table: BasicTable.Table,
  Column: BasicTable.Column,
  Cell: BasicTable.Cell,
  SortHeaderCell: BasicTable.SortHeaderCell,
  TextCell: BasicTable.TextCell,
  LinkCell: BasicTable.LinkCell,
  DateCell: BasicTable.DateCell,
  BooleanCell: BasicTable.BooleanCell,
  EnumCell: BasicTable.EnumCell,
  Pagination: BasicTable.Pagination,
  Button,
  SplitButton,
  MenuItem,
  Alert,
  Label,
  Modal,
  ProgressBar,
  Confirm,
  Row,
  Well,
  Tabs,
  Tab: Tabs.Tab,
  PageHeader,
  ContentHeader,
  LabelWrapper,
  DateValue,
  EnumLabel,
  Collapse,
  Tooltip,
  Dropzone
};

Components.version = '0.0.1';
module.exports = Components;
