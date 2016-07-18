

import CrtIdentityService from './CrtIdentityService';
import CrtCertificateTaskService from './CrtCertificateTaskService';
import CrtCertificateService from './CrtCertificateService';


const ServiceRoot = {
  CrtIdentityService: CrtIdentityService,
  CrtCertificateTaskService: CrtCertificateTaskService,
  CrtCertificateService: CrtCertificateService
};

ServiceRoot.version = '0.0.1';
module.exports = ServiceRoot;
