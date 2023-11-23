package tr.gov.tubitak.uekae.esya.api.xades.example.upgrades.xl;

import org.junit.Test;
import tr.gov.tubitak.uekae.esya.api.signature.SignatureType;
import tr.gov.tubitak.uekae.esya.api.xades.example.XadesSampleBase;
import tr.gov.tubitak.uekae.esya.api.xades.example.validation.XadesSignatureValidation;
import tr.gov.tubitak.uekae.esya.api.xmlsignature.Context;
import tr.gov.tubitak.uekae.esya.api.xmlsignature.XMLSignature;
import tr.gov.tubitak.uekae.esya.api.xmlsignature.document.FileDocument;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Provides upgrade function from XL to A
 */
public class UpgradeToA extends XadesSampleBase {

    public static final String SIGNATURE_FILENAME = "a_from_xl.xml";

    /**
     * Upgrades XL to A. XL signature needs to be provided before upgrade process.
     * It can be created in formats.XL.
     *
     * @throws Exception
     */
    @Test
    public void upgradeXLToA() throws Exception {

        // create context with working directory
        Context context = createContext();

        // read signature to be upgraded
        XMLSignature signature = XMLSignature.parse(new FileDocument(new File(getTestDataFolder() + "xl.xml")), context);

        // upgrade to A
        signature.upgrade(SignatureType.ES_A);

        FileOutputStream fileOutputStream = new FileOutputStream(getTestDataFolder() + SIGNATURE_FILENAME);
        signature.write(fileOutputStream);
        fileOutputStream.close();

        XadesSignatureValidation signatureValidation = new XadesSignatureValidation();
        signatureValidation.validate(SIGNATURE_FILENAME);

    }
}
