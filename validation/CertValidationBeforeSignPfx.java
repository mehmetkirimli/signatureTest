package tr.gov.tubitak.uekae.esya.api.xades.example.validation;

import org.junit.Test;
import tr.gov.tubitak.uekae.esya.api.asn.x509.ECertificate;
import tr.gov.tubitak.uekae.esya.api.common.ESYAException;
import tr.gov.tubitak.uekae.esya.api.xades.example.XadesSampleBase;
import tr.gov.tubitak.uekae.esya.api.xmlsignature.Context;
import tr.gov.tubitak.uekae.esya.api.xmlsignature.XMLSignature;

import java.io.FileOutputStream;

/**
 * Sample for signing a document with pfx only if certificate is valid
 */
public class CertValidationBeforeSignPfx extends XadesSampleBase {

    public static final String SIGNATURE_FILENAME = "validate_before_sign_pfx.xml";

    /**
     * Creates BES with only valid certificates
     *
     * @throws Exception
     */
    @Test
    public void createBESWithCertificateCheckPfx() throws Exception {

        ECertificate pfxSignersCertificate = getPfxSigner().getSignersCertificate();

        // check validity of signing certificate
        CertValidation certValidation = new CertValidation();
        boolean valid = certValidation.validateCertificate(pfxSignersCertificate);

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
            signature.addKeyInfo(pfxSignersCertificate);

            // now sign it by using private key
            signature.sign(getPfxSigner());

            FileOutputStream fileOutputStream = new FileOutputStream(getTestDataFolder() + SIGNATURE_FILENAME);
            signature.write(fileOutputStream);
            fileOutputStream.close();

            XadesSignatureValidation signatureValidation = new XadesSignatureValidation();
            signatureValidation.validate(SIGNATURE_FILENAME);

        } else {
            throw new ESYAException("Certificate " + pfxSignersCertificate.toString() + " is not a valid certificate!");
        }

    }

}
