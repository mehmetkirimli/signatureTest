package tr.gov.tubitak.uekae.esya.api.xades.example.structures;

import org.junit.Test;
import tr.gov.tubitak.uekae.esya.api.asn.x509.ECertificate;
import tr.gov.tubitak.uekae.esya.api.smartcard.example.smartcardmanager.SmartCardManager;
import tr.gov.tubitak.uekae.esya.api.xades.example.XadesSampleBase;
import tr.gov.tubitak.uekae.esya.api.xades.example.validation.XadesSignatureValidation;
import tr.gov.tubitak.uekae.esya.api.xmlsignature.Context;
import tr.gov.tubitak.uekae.esya.api.xmlsignature.XMLSignature;

import java.io.FileOutputStream;

/**
 * <p>Create sample enveloping signature
 * <p>
 * <p>Enveloping means signed data is keep "in" the signature.
 * <p>
 * <p>this is usually done by BASE64 encoding the data to be signed.
 */
public class Enveloping extends XadesSampleBase {

    public static final String SIGNATURE_FILENAME = "enveloping.xml";

    @Test
    public void createEnvelopingBes() throws Exception {

        // create context with working directory
        Context context = createContext();

        // create signature according to context,
        // with default type (XADES_BES)
        XMLSignature signature = new XMLSignature(context);

        // add document as reference, and keep BASE64 version of data
        // in an <Object tag, in a way that reference points to
        // that <Object
        // (embed=true)
        signature.addDocument("./sample.txt", "text/plain", true);

        // false-true gets non-qualified certificates while true-false gets qualified ones
        ECertificate cert = SmartCardManager.getInstance().getSignatureCertificate(isQualified());

        // add certificate to show who signed the document
        signature.addKeyInfo(cert);

        // now sign it by using smart card
        signature.sign(SmartCardManager.getInstance().getSigner(getPin(), cert));

        FileOutputStream fileOutputStream = new FileOutputStream(getTestDataFolder() + SIGNATURE_FILENAME);
        signature.write(fileOutputStream);
        fileOutputStream.close();

        XadesSignatureValidation signatureValidation = new XadesSignatureValidation();
        signatureValidation.validate(SIGNATURE_FILENAME);

    }

}
