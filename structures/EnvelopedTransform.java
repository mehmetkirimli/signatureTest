package tr.gov.tubitak.uekae.esya.api.xades.example.structures;

import org.junit.Test;
import org.w3c.dom.Document;
import tr.gov.tubitak.uekae.esya.api.asn.x509.ECertificate;
import tr.gov.tubitak.uekae.esya.api.smartcard.example.smartcardmanager.SmartCardManager;
import tr.gov.tubitak.uekae.esya.api.xades.example.XadesSampleBase;
import tr.gov.tubitak.uekae.esya.api.xades.example.validation.XadesSignatureValidation;
import tr.gov.tubitak.uekae.esya.api.xmlsignature.*;
import tr.gov.tubitak.uekae.esya.api.xmlsignature.model.Transform;
import tr.gov.tubitak.uekae.esya.api.xmlsignature.model.Transforms;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.FileOutputStream;

/**
 * Enveloped transform BES sample
 */
public class EnvelopedTransform extends XadesSampleBase {

    public static final String SIGNATURE_FILENAME = "enveloped_transform.xml";

    /**
     * Create enveloped transform BES
     *
     * @throws Exception
     */
    @Test
    public void createEnvelopedTransform() throws Exception {

        Document envelopeDoc = newEnvelope();

        Context context = createContext();
        context.setDocument(envelopeDoc);

        XMLSignature signature = new XMLSignature(context, false);

        // attach signature to envelope
        envelopeDoc.getDocumentElement().appendChild(signature.getElement());

        Transforms transforms = new Transforms(context);
        transforms.addTransform(new Transform(context, TransformType.ENVELOPED.getUrl()));

        // add whole document(="") with envelope transform, with SHA256
        // and don't include it into signature(false)
        signature.addDocument("", "text/xml", transforms,
                context.getConfig().getAlgorithmsConfig().getDigestMethod(), false);

        // false-true gets non-qualified certificates while true-false gets qualified ones
        ECertificate cert = SmartCardManager.getInstance().getSignatureCertificate(isQualified());

        // add certificate to show who signed the document
        signature.addKeyInfo(cert);

        // now sign it by using smart card
        signature.sign(SmartCardManager.getInstance().getSigner(getPin(), cert));

        Source source = new DOMSource(envelopeDoc);
        Transformer transformer = TransformerFactory.newInstance().newTransformer();

        // write to file
        transformer.transform(source, new StreamResult(new FileOutputStream(getTestDataFolder() + SIGNATURE_FILENAME)));

        XadesSignatureValidation signatureValidation = new XadesSignatureValidation();
        signatureValidation.validate(SIGNATURE_FILENAME);
    }
}
