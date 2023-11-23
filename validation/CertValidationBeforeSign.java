package tr.gov.tubitak.uekae.esya.api.xades.example.validation;

import org.junit.Test;
import tr.gov.tubitak.uekae.esya.api.asn.x509.ECertificate;
import tr.gov.tubitak.uekae.esya.api.common.ESYAException;
import tr.gov.tubitak.uekae.esya.api.smartcard.example.smartcardmanager.JSmartCardManager;
import tr.gov.tubitak.uekae.esya.api.xades.example.XadesSampleBase;
import tr.gov.tubitak.uekae.esya.api.xmlsignature.Context;
import tr.gov.tubitak.uekae.esya.api.xmlsignature.XMLSignature;

import java.io.FileOutputStream;
import java.security.cert.X509Certificate;

/**
 * Sample for signing a document with smart card only if certificate is valid
 */
public class CertValidationBeforeSign extends XadesSampleBase {

    public static final String SIGNATURE_FILENAME = "validate_before_sign.xml";

    /**
     * Creates BES using smart card with only valid certificates
     *
     * @throws Exception
     */
    @Test
    public void createBESWithCertificateCheck() throws Exception {

        // false-true gets non-qualified certificates while true-false gets qualified ones
        X509Certificate cert = JSmartCardManager.getInstance().getSignatureCertificate(isQualified());
        ECertificate certificate = new ECertificate(cert.getEncoded());

        // check validity of signing certificate
        CertValidation certValidation = new CertValidation();
        boolean valid = certValidation.validateCertificate(certificate);

        if (valid) {
            // create context with working directory
            Context context = createContext();

            // create signature according to context,
            // with default type (XADES_BES)
            XMLSignature signature = new XMLSignature(context);

            // add document as reference, but do not embed it
            // into the signature (embed=false)
            signature.addDocument("./sample.txt", "text/plain", false);

            // add certificate to show who signed the document
            signature.addKeyInfo(certificate);

            // now sign it by using smart card
            signature.sign(JSmartCardManager.getInstance().getSigner(getPin(), cert));

            FileOutputStream fileOutputStream = new FileOutputStream(getTestDataFolder() + SIGNATURE_FILENAME);
            signature.write(fileOutputStream);
            fileOutputStream.close();

            XadesSignatureValidation signatureValidation = new XadesSignatureValidation();
            signatureValidation.validate(SIGNATURE_FILENAME);

        } else {
            throw new ESYAException("Certificate " + certificate.toString() + " is not a valid certificate!");
        }

    }

}
