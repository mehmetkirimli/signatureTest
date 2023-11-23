package tr.gov.tubitak.uekae.esya.api.xades.example.validation;

import tr.gov.tubitak.uekae.esya.api.asn.x509.ECertificate;
import tr.gov.tubitak.uekae.esya.api.certificate.validation.CertificateStatus;
import tr.gov.tubitak.uekae.esya.api.certificate.validation.CertificateValidation;
import tr.gov.tubitak.uekae.esya.api.certificate.validation.ValidationSystem;
import tr.gov.tubitak.uekae.esya.api.certificate.validation.check.certificate.CertificateStatusInfo;
import tr.gov.tubitak.uekae.esya.api.certificate.validation.policy.PolicyReader;
import tr.gov.tubitak.uekae.esya.api.certificate.validation.policy.ValidationPolicy;
import tr.gov.tubitak.uekae.esya.api.xades.example.XadesSampleBase;

import java.util.Calendar;

/**
 * Provides validation functions for certificates
 */
public class CertValidation extends XadesSampleBase {

    /**
     * Validates given certificate
     *
     * @param certificate to be validated
     * @return true if certificate is valid, false otherwise
     * @throws Exception
     */
    public boolean validateCertificate(ECertificate certificate) throws Exception {

        try {
            // generate policy which going to be used in validation
            ValidationPolicy policy = new ValidationPolicy();
            String policyPath = getPolicyFile();
            policy = PolicyReader.readValidationPolicy(policyPath);

            // generate validation system
            ValidationSystem vs = CertificateValidation.createValidationSystem(policy);
            vs.setBaseValidationTime(Calendar.getInstance());

            // validate certificate
            CertificateStatusInfo csi = CertificateValidation.validateCertificate(vs, certificate);

            // return true if certificate is valid, false otherwise
            if (csi.getCertificateStatus() != CertificateStatus.VALID)
                return false;
            else if (csi.getCertificateStatus() == CertificateStatus.VALID)
                return true;
        } catch (Exception e) {
            throw new Exception("An error occurred while validating certificate", e);
        }
        return false;
    }

}
