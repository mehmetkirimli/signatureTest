package tr.gov.tubitak.uekae.esya.api.xades.example.upgrades.parallel;

import org.junit.Test;
import tr.gov.tubitak.uekae.esya.api.signature.SignatureType;
import tr.gov.tubitak.uekae.esya.api.xades.example.XadesSampleBase;
import tr.gov.tubitak.uekae.esya.api.xades.example.multiple.ParallelDetached;
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
 * Provides functions for upgrade of parallel BES to type T
 */
public class UpgradeToT extends XadesSampleBase {

    public static final String SIGNATURE_FILENAME = "t_from_bes_parallel.xml";

    /**
     * Upgrades parallel BES to type T
     *
     * @throws Exception
     */
    @Test
    public void upgradeParallelBESToT() throws Exception {

        // create context with working directory
        Context context = createContext();

        // parse the previously created enveloped signature
        org.w3c.dom.Document document = parseDoc(ParallelDetached.SIGNATURE_FILENAME, context);

        // get and upgrade the signature 1 in DOM document
        XMLSignature signature1 = readSignature(document, context, 0);
        signature1.upgrade(SignatureType.ES_T);

        // get and upgrade the signature 2 in DOM document
        XMLSignature signature2 = readSignature(document, context, 1);
        signature2.upgrade(SignatureType.ES_T);

        // writing enveloped XML to the file
        Source source = new DOMSource(document);
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.transform(source, new StreamResult(new FileOutputStream(getTestDataFolder() + SIGNATURE_FILENAME)));

        XadesSignatureValidation signatureValidation = new XadesSignatureValidation();
        signatureValidation.validate(SIGNATURE_FILENAME);
    }

}
