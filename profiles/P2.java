package tr.gov.tubitak.uekae.esya.api.xades.example.profiles;

import org.junit.Test;
import tr.gov.tubitak.uekae.esya.api.asn.x509.ECertificate;
import tr.gov.tubitak.uekae.esya.api.signature.SignatureType;
import tr.gov.tubitak.uekae.esya.api.smartcard.example.smartcardmanager.SmartCardManager;
import tr.gov.tubitak.uekae.esya.api.xades.example.XadesSampleBase;
import tr.gov.tubitak.uekae.esya.api.xades.example.validation.XadesSignatureValidation;
import tr.gov.tubitak.uekae.esya.api.xmlsignature.Context;
import tr.gov.tubitak.uekae.esya.api.xmlsignature.XMLSignature;

import java.io.FileOutputStream;
import java.util.Calendar;

/**
 * Profile 2 sample
 */
public class P2 extends XadesSampleBase {

    public static final String SIGNATURE_FILENAME = "p2.xml";

    /**
     * Creates signature according to the profile 2 specifications
     *
     * @throws Exception
     */
    @Test
    public void createP2() throws Exception {

        // create context with working directory
        Context context = createContext();

        // add resolver to resolve policies
        context.addExternalResolver(getPolicyResolver());

        // create signature according to context,
        // with default type (XADES_BES)
        XMLSignature signature = new XMLSignature(context);

        // add document as reference, but do not embed it
        // into the signature (embed=false)
        signature.addDocument("./sample.txt", "text/plain", false);

        // false-true gets non-qualified certificates while true-false gets qualified ones
        ECertificate cert = SmartCardManager.getInstance().getSignatureCertificate(isQualified());

        // add certificate to show who signed the document
        signature.addKeyInfo(cert);

        // set time now
        signature.setSigningTime(Calendar.getInstance());

        // set policy info defined and required by profile
        signature.setPolicyIdentifier(OID_POLICY_P2,
                "Kısa Dönemli ve SİL Kontrollü Güvenli Elektronik İmza Politikası",
                "http://www.eimza.gov.tr/EimzaPolitikalari/216792161015070321.pdf"
        );

        // now sign it by using smart card
        signature.sign(SmartCardManager.getInstance().getSigner(getPin(), cert));

        // upgrade to T
        signature.upgrade(SignatureType.ES_T);

        FileOutputStream fileOutputStream = new FileOutputStream(getTestDataFolder() + SIGNATURE_FILENAME);
        signature.write(fileOutputStream);
        fileOutputStream.close();

        XadesSignatureValidation signatureValidation = new XadesSignatureValidation();
        signatureValidation.validate(SIGNATURE_FILENAME);

    }
}
