package tr.gov.tubitak.uekae.esya.api.xades.example.structures;

import org.junit.Test;
import org.w3c.dom.Document;
import tr.gov.tubitak.uekae.esya.api.asn.x509.ECertificate;
import tr.gov.tubitak.uekae.esya.api.smartcard.example.smartcardmanager.SmartCardManager;
import tr.gov.tubitak.uekae.esya.api.xades.example.XadesSampleBase;
import tr.gov.tubitak.uekae.esya.api.xades.example.validation.XadesSignatureValidation;
import tr.gov.tubitak.uekae.esya.api.xmlsignature.Context;
import tr.gov.tubitak.uekae.esya.api.xmlsignature.XMLSignature;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.FileOutputStream;

/**
 * Enveloped BES sample
 */
public class Enveloped extends XadesSampleBase {

    public static final String SIGNATURE_FILENAME = "enveloped.xml";

    /**
     * Create enveloped BES
     *
     * @throws Exception
     */
    @Test
    public void createEnveloped() throws Exception {

        // here is our custom envelope XML
        Document envelopeDoc = newEnvelope();

        // create context with working directory
        Context context = createContext();

        // define where signature belongs to
        context.setDocument(envelopeDoc);

        // create signature according to context,
        // with default type (XADES_BES)
        XMLSignature signature = new XMLSignature(context, false);

        // attach signature to envelope
        envelopeDoc.getDocumentElement().appendChild(signature.getElement());

        // add document as reference,
        signature.addDocument("#data1", "text/xml", false);

        // false-true gets non-qualified certificates while true-false gets qualified ones
        ECertificate cert = SmartCardManager.getInstance().getSignatureCertificate(isQualified());

        // add certificate to show who signed the document
        signature.addKeyInfo(cert);

        // now sign it by using smart card
        signature.sign(SmartCardManager.getInstance().getSigner(getPin(), cert));

        // this time we do not use signature.write because we need to write
        // whole document instead of signature
        Source source = new DOMSource(envelopeDoc);
        Transformer transformer = TransformerFactory.newInstance().newTransformer();

        // write to file
        transformer.transform(source, new StreamResult(new FileOutputStream(getTestDataFolder() + SIGNATURE_FILENAME)));

        XadesSignatureValidation signatureValidation = new XadesSignatureValidation();
        signatureValidation.validate(SIGNATURE_FILENAME);
    }

}
