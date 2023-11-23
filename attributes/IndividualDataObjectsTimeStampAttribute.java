package tr.gov.tubitak.uekae.esya.api.xades.example.attributes;

import org.junit.Test;
import tr.gov.tubitak.uekae.esya.api.asn.x509.ECertificate;
import tr.gov.tubitak.uekae.esya.api.smartcard.example.smartcardmanager.SmartCardManager;
import tr.gov.tubitak.uekae.esya.api.xades.example.XadesSampleBase;
import tr.gov.tubitak.uekae.esya.api.xades.example.validation.XadesSignatureValidation;
import tr.gov.tubitak.uekae.esya.api.xmlsignature.Context;
import tr.gov.tubitak.uekae.esya.api.xmlsignature.XMLSignature;
import tr.gov.tubitak.uekae.esya.api.xmlsignature.model.xades.IndividualDataObjectsTimeStamp;
import tr.gov.tubitak.uekae.esya.api.xmlsignature.model.xades.timestamp.Include;

import java.io.FileOutputStream;
import java.util.Calendar;

/**
 * Adds IndividualDataObjectsTimestamp attribute to BES
 */
public class IndividualDataObjectsTimeStampAttribute extends XadesSampleBase {

    public static final String SIGNATURE_FILENAME = "individual_data_objects_timestamp.xml";

    /**
     * Creates BES with IndividualDataObjectsTimestamp attribute
     *
     * @throws Exception
     */
    @Test
    public void createBESWithIndividualDataObjectsTimeStamp() throws Exception {

        // create context with working directory
        Context context = createContext();

        // create signature according to context,
        // with default type (XADES_BES)
        XMLSignature signature = new XMLSignature(context);

        // add document into the signature and get the reference
        String ref1 = "#" + signature.addDocument("./sample.txt", "text/plain", true);

        // add another object
        String objId2 = signature.addPlainObject("Test Data 1", "text/plain", null);
        String ref2 = "#" + signature.addDocument("#" + objId2, null, false);

        // false-true gets non-qualified certificates while true-false gets qualified ones
        ECertificate cert = SmartCardManager.getInstance().getSignatureCertificate(isQualified());

        // add certificate to show who signed the document
        signature.addKeyInfo(cert);

        // create new individual data objects timestamp structure
        IndividualDataObjectsTimeStamp timestamp = new IndividualDataObjectsTimeStamp(context);

        // add objects to timestamp structure
        timestamp.addInclude(new Include(context, ref1, Boolean.TRUE));
        timestamp.addInclude(new Include(context, ref2, Boolean.TRUE));

        // get encapsulated timestamp to individual data objects timestamp
        timestamp.addEncapsulatedTimeStamp(signature);

        // add individual data objects timestamp to signature
        signature.getQualifyingProperties().getSignedProperties().createOrGetSignedDataObjectProperties().
                addIndividualDataObjectsTimeStamp(timestamp);

        // optional - add timestamp validation data
        signature.addTimeStampValidationData(timestamp, Calendar.getInstance());

        // now sign it by using smart card
        signature.sign(SmartCardManager.getInstance().getSigner(getPin(), cert));

        FileOutputStream fileOutputStream = new FileOutputStream(getTestDataFolder() + SIGNATURE_FILENAME);
        signature.write(fileOutputStream);
        fileOutputStream.close();

        XadesSignatureValidation signatureValidation = new XadesSignatureValidation();
        signatureValidation.validate(SIGNATURE_FILENAME);

    }
}
