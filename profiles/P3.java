package tr.gov.tubitak.uekae.esya.api.xades.example.profiles;

import org.junit.Test;
import tr.gov.tubitak.uekae.esya.api.asn.x509.ECertificate;
import tr.gov.tubitak.uekae.esya.api.certificate.validation.policy.PolicyReader;
import tr.gov.tubitak.uekae.esya.api.signature.SignatureType;
import tr.gov.tubitak.uekae.esya.api.signature.certval.CertValidationPolicies;
import tr.gov.tubitak.uekae.esya.api.smartcard.example.smartcardmanager.SmartCardManager;
import tr.gov.tubitak.uekae.esya.api.xades.example.XadesSampleBase;
import tr.gov.tubitak.uekae.esya.api.xades.example.validation.XadesSignatureValidation;
import tr.gov.tubitak.uekae.esya.api.xmlsignature.Context;
import tr.gov.tubitak.uekae.esya.api.xmlsignature.XMLSignature;
import tr.gov.tubitak.uekae.esya.api.xmlsignature.XMLSignatureException;
import tr.gov.tubitak.uekae.esya.api.xmlsignature.document.FileDocument;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

/**
 * Profile 3 sample
 */
public class P3 extends XadesSampleBase {

    public static final String SIGNATURE_FILENAME = "p3.xml";

    /**
     * Creates T type signature according to the profile 3 specifications. It must be
     * upgraded to XL type using the second function (upgradeP3). If 'use-validation-
     * data-published-after-creation' is true in xml signature config file, after signing,
     * at least one new CRL for signing certificate must be published before upgrade.
     *
     * @throws Exception
     */

    @Test
    public void test() throws Exception {

        createP3();

        System.out.println("Yeni sil yayınlandıktan sonra 'upgrade' işlemi yapılmalıdır.");

        upgradeP3();
    }

    public void createP3() throws Exception {

        try {
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
            signature.setPolicyIdentifier(OID_POLICY_P3,
                    "Uzun Dönemli ve SİL Kontrollü Güvenli Elektronik İmza Politikası",
                    "http://www.eimza.gov.tr/EimzaPolitikalari/216792161015070321.pdf"
            );

            // now sign it by using smart card
            signature.sign(SmartCardManager.getInstance().getSigner(getPin(), cert));

            // upgrade to T
            signature.upgrade(SignatureType.ES_T);

            signature.write(new FileOutputStream(getTestDataFolder() + "p3_temp.xml"));
        } catch (XMLSignatureException x) {
            // cannot create signature
            x.printStackTrace();
        } catch (IOException x) {
            // probably couldn't write to the file
            x.printStackTrace();
        }
    }

    /**
     * Upgrades temporary T type signature to XL to create a signature with
     * profile 3 specifications. Do not run this upgrade code to be able to
     * validate using 'use-validation-data-published-after-creation' true
     * 'just after' creating temporary signature in the above function (createP3).
     * Wait for at least one new CRL for signing certificate to be published.
     * Also, revocation data must be CRL instead of OCSP in this profile.
     *
     * @throws Exception
     */

    public void upgradeP3() throws Exception {

        // create context with working directory
        Context context = createContext();

        // set policy such that it only works with CRL
        CertValidationPolicies policies = new CertValidationPolicies();
        policies.register(null, PolicyReader.readValidationPolicy(getPolicyFileCrl()));

        context.getConfig().getValidationConfig().setCertValidationPolicies(policies);

        // add resolver to resolve policies
        context.addExternalResolver(getPolicyResolver());

        // read temporary signature
        XMLSignature signature = XMLSignature.parse(new FileDocument(new File(getTestDataFolder() + "p3_temp.xml")), context);

        // upgrade to XL
        signature.upgrade(SignatureType.ES_XL);

        FileOutputStream fileOutputStream = new FileOutputStream(getTestDataFolder() + SIGNATURE_FILENAME);
        signature.write(fileOutputStream);
        fileOutputStream.close();

        XadesSignatureValidation signatureValidation = new XadesSignatureValidation();
        signatureValidation.validate(SIGNATURE_FILENAME);

    }
}
