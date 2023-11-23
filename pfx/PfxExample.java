package tr.gov.tubitak.uekae.esya.api.xades.example.pfx;

import org.junit.Test;
import tr.gov.tubitak.uekae.esya.api.asn.x509.ECertificate;
import tr.gov.tubitak.uekae.esya.api.xades.example.XadesSampleBase;
import tr.gov.tubitak.uekae.esya.api.xades.example.validation.XadesSignatureValidation;
import tr.gov.tubitak.uekae.esya.api.xmlsignature.Context;
import tr.gov.tubitak.uekae.esya.api.xmlsignature.XMLSignature;

import java.io.FileOutputStream;

/**
 * Creating electronic signature using PFX
 */
public class PfxExample extends XadesSampleBase {

    public static final String SIGNATURE_FILENAME = "pfxexample.xml";

    /**
     * Creates detached BES with PFX
     *
     * @throws Exception
     */
    @Test
    public void createDetachedBesWithPfx() throws Exception {

        // create context with working directory
        Context context = createContext();

        // create signature according to context,
        // with default type (XADES_BES)
        XMLSignature signature = new XMLSignature(context);

        // add document as reference, but do not embed it
        // into the signature (embed=false)
        signature.addDocument("./sample.txt", "text/plain", false);

        ECertificate pfxSignersCertificate = getPfxSigner().getSignersCertificate();

        // add signer's certificate
        signature.addKeyInfo(pfxSignersCertificate);

        // sign the document
        signature.sign(getPfxSigner());

        FileOutputStream fileOutputStream = new FileOutputStream(getTestDataFolder() + SIGNATURE_FILENAME);
        signature.write(fileOutputStream);
        fileOutputStream.close();

        XadesSignatureValidation signatureValidation = new XadesSignatureValidation();
        signatureValidation.validate(SIGNATURE_FILENAME);

    }
}
