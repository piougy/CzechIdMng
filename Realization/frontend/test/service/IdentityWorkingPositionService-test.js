import moment from 'moment';
import chai, { expect } from 'chai';
import dirtyChai from 'dirty-chai';
chai.use(dirtyChai);
//
import { IdentityWorkingPositionService } from '../../src/modules/core/services';

describe('IdentityWorkingPositionService', function identityWorkingPositionServiceTestSuite() {
  const identityWorkingPositionService = new IdentityWorkingPositionService('test');

  describe('#valid WorkingPosition', function identityWorkingPositionServiceTest() {
    it('- validFrom in past and validTill in future should be true', function test() {
      const workingPosition = {
        validFrom: moment().subtract(1, 'day'),
        validTill: moment().add(1, 'day')
      };
      expect(identityWorkingPositionService.isValid(workingPosition)).to.be.true();
    });

    it('- validFrom as null and validTill as null should be true', function test() {
      const workingPosition = {
        validFrom: null,
        validTill: null
      };
      expect(identityWorkingPositionService.isValid(workingPosition)).to.be.true();
    });

    it('- validFrom as null and validTill in future should be true', function test() {
      const workingPosition = {
        validFrom: null,
        validTill: moment().add(1, 'day')
      };
      expect(identityWorkingPositionService.isValid(workingPosition)).to.be.true();
    });

    it('- validFrom in past and validTill as null should be true', function test() {
      const workingPosition = {
        validFrom: moment().subtract(1, 'day'),
        validTill: null
      };
      expect(identityWorkingPositionService.isValid(workingPosition)).to.be.true();
    });
  });

  describe('#invalid WorkingPosition', function invalidPositionTest() {
    it('- validFrom as null and validTill in past should be false', function test() {
      const workingPosition = {
        validFrom: null,
        validTill: moment().subtract(1, 'day')
      };
      expect(identityWorkingPositionService.isValid(workingPosition)).to.be.false();
    });

    it('- validFrom in future and validTill as null should be false', function test() {
      const workingPosition = {
        validFrom: moment().add(1, 'day'),
        validTill: null
      };
      expect(identityWorkingPositionService.isValid(workingPosition)).to.be.false();
    });

    it('- validFrom in past and validTill in past should be false', function test() {
      const workingPosition = {
        validFrom: moment().subtract(1, 'day'),
        validTill: moment().subtract(1, 'day')
      };
      expect(identityWorkingPositionService.isValid(workingPosition)).to.be.false();
    });

    it('- workingPosition as null should be false', function test() {
      expect(identityWorkingPositionService.isValid(null)).to.be.false();
    });
  });
});
