package tr.gov.tubitak.uekae.esya.api.xades.example.multiple;

import org.junit.Test;
import tr.gov.tubitak.uekae.esya.api.asn.x509.ECertificate;
import tr.gov.tubitak.uekae.esya.api.smartcard.example.smartcardmanager.SmartCardManager;
import tr.gov.tubitak.uekae.esya.api.xades.example.XadesSampleBase;
import tr.gov.tubitak.uekae.esya.api.xades.example.validation.XadesSignatureValidation;
import tr.gov.tubitak.uekae.esya.api.xmlsignature.Context;
import tr.gov.tubitak.uekae.esya.api.xmlsignature.SignedDocument;
import tr.gov.tubitak.uekae.esya.api.xmlsignature.XMLSignature;
import tr.gov.tubitak.uekae.esya.api.xmlsignature.document.Document;
import tr.gov.tubitak.uekae.esya.api.xmlsignature.resolver.Resolver;

import java.io.FileOutputStream;

/**
 * Counter signature to a parallel signature sample
 */
public class CounterParallel extends XadesSampleBase {

    public static final String SIGNATURE_FILENAME = "counter_parallel.xml";

    /**
     * Adds counter signature to a parallel detached one
     *
     * @throws Exception
     */
    @Test
    public void signCounterParallel() throws Exception {

        Context context = createContext();

        // read previously created signature
        Document signatureFile = Resolver.resolve(ParallelDetached.SIGNATURE_FILENAME, context);
        SignedDocument signedDocument = new SignedDocument(signatureFile, context);

        // get first signature
        XMLSignature signature = signedDocument.getSignature(0);

        // create counter signature to the first one
        XMLSignature counterSignature = signature.createCounterSignature();

        // false-true gets non-qualified certificates while true-false gets qualified ones
        ECertificate cert = SmartCardManager.getInstance().getSignatureCertificate(isQualified());

        // add certificate to show who signed the document
        counterSignature.addKeyInfo(cert);

        // now sign it by using smart card
        counterSignature.sign(SmartCardManager.getInstance().getSigner(getPin(), cert));

        // signed doc contains both previous signature and now a counter signature
        // in first signature
        FileOutputStream fileOutputStream = new FileOutputStream(getTestDataFolder() + SIGNATURE_FILENAME);
        signedDocument.write(fileOutputStream);
        fileOutputStream.close();

        XadesSignatureValidation signatureValidation = new XadesSignatureValidation();
        signatureValidation.validateParallel(SIGNATURE_FILENAME);
    }

}
