package tr.gov.tubitak.uekae.esya.api.xades.example.attributes;

import org.junit.Test;
import tr.gov.tubitak.uekae.esya.api.asn.x509.ECertificate;
import tr.gov.tubitak.uekae.esya.api.smartcard.example.smartcardmanager.SmartCardManager;
import tr.gov.tubitak.uekae.esya.api.xades.example.XadesSampleBase;
import tr.gov.tubitak.uekae.esya.api.xades.example.validation.XadesSignatureValidation;
import tr.gov.tubitak.uekae.esya.api.xmlsignature.Context;
import tr.gov.tubitak.uekae.esya.api.xmlsignature.XMLSignature;
import tr.gov.tubitak.uekae.esya.api.xmlsignature.model.xades.ClaimedRole;
import tr.gov.tubitak.uekae.esya.api.xmlsignature.model.xades.SignerRole;

import java.io.FileOutputStream;

/**
 * Adds SignerRole attribute to BES
 */
public class SignerRoleAttribute extends XadesSampleBase {

    public static final String SIGNATURE_FILENAME = "signer_role.xml";

    /**
     * Creates BES with SignerRole attribute
     *
     * @throws Exception
     */
    @Test
    public void createBESWithSignerRole() throws Exception {

        // create context with working directory
        Context context = createContext();

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

        // add signer role
        signature.getQualifyingProperties().getSignedSignatureProperties().setSignerRole(
                new SignerRole(context, new ClaimedRole[]{new ClaimedRole(context, "Manager")}));

        // now sign it by using smart card
        signature.sign(SmartCardManager.getInstance().getSigner(getPin(), cert));

        FileOutputStream fileOutputStream = new FileOutputStream(getTestDataFolder() + SIGNATURE_FILENAME);
        signature.write(fileOutputStream);
        fileOutputStream.close();

        XadesSignatureValidation signatureValidation = new XadesSignatureValidation();
        signatureValidation.validate(SIGNATURE_FILENAME);

    }
}
