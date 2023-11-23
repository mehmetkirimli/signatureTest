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
 * Parallel enveloped signature sample
 */
public class ParallelEnveloped extends XadesSampleBase {

    public static final String SIGNATURE_FILENAME = "parallel_enveloped.xml";

    /**
     * Creates two signatures in a document, that signs same inner data
     *
     * @throws Exception
     */
    @Test
    public void createParallelEnveloped() throws Exception {

        Context context = createContext();

        SignedDocument signatures = new SignedDocument(context);

        // read and add an external XML
        Document doc = Resolver.resolve("./sample.xml", context);
        String fragment = signatures.addDocument(doc);

        XMLSignature signature1 = signatures.createSignature();

        // add document as inner reference
        signature1.addDocument("#" + fragment, "text/xml", false);

        // false-true gets non-qualified certificates while true-false gets qualified ones
        ECertificate cert = SmartCardManager.getInstance().getSignatureCertificate(isQualified());

        // add certificate to show who signed the document
        signature1.addKeyInfo(cert);

        // now sign it by using smart card
        signature1.sign(SmartCardManager.getInstance().getSigner(getPin(), cert));

        XMLSignature signature2 = signatures.createSignature();

        // add document as inner reference
        signature2.addDocument("#" + fragment, "text/plain", false);

        // add certificate to show who signed the document
        signature2.addKeyInfo(cert);

        // now sign it by using smart card
        signature2.sign(SmartCardManager.getInstance().getSigner(getPin(), cert));

        // write combined document
        FileOutputStream fileOutputStream = new FileOutputStream(getTestDataFolder() + SIGNATURE_FILENAME);
        signatures.write(fileOutputStream);
        fileOutputStream.close();

        XadesSignatureValidation signatureValidation = new XadesSignatureValidation();
        signatureValidation.validateParallel(SIGNATURE_FILENAME);

    }
}
