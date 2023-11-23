package tr.gov.tubitak.uekae.esya.api.xades.example.sign;

import org.junit.Test;
import tr.gov.tubitak.uekae.esya.api.asn.x509.ECertificate;
import tr.gov.tubitak.uekae.esya.api.common.crypto.Algorithms;
import tr.gov.tubitak.uekae.esya.api.common.crypto.BaseSigner;
import tr.gov.tubitak.uekae.esya.api.crypto.alg.DigestAlg;
import tr.gov.tubitak.uekae.esya.api.crypto.params.RSAPSSParams;
import tr.gov.tubitak.uekae.esya.api.smartcard.example.smartcardmanager.SmartCardManager;
import tr.gov.tubitak.uekae.esya.api.xades.example.XadesSampleBase;
import tr.gov.tubitak.uekae.esya.api.xades.example.validation.XadesSignatureValidation;
import tr.gov.tubitak.uekae.esya.api.xmlsignature.Context;
import tr.gov.tubitak.uekae.esya.api.xmlsignature.XMLSignature;

import java.io.FileOutputStream;
import java.security.spec.AlgorithmParameterSpec;



/**
 * BES signing sample
 */
public class Bes extends XadesSampleBase {

    public static final String SIGNATURE_FILENAME = "bes.xml";

    /**
     * Creates detached BES
     *
     * @throws Exception
     */
    @Test
    public void createBesWith_RSA_SHA256() throws Exception {
       testCreateEnveloping(Algorithms.SIGNATURE_RSA_SHA256, null);
    }

    @Test
    public void createBesWith_RSA_PSS() throws Exception{
        testCreateEnveloping(Algorithms.SIGNATURE_RSA_PSS, new RSAPSSParams(DigestAlg.SHA256).toPSSParameterSpec());
    }

    @Test
    public void createBesWith_ECDSA_SHA384() throws Exception{
        testCreateEnveloping(Algorithms.SIGNATURE_ECDSA_SHA384, null);
    }



    //This function selects SIGNATURE_RSA_SHA256 algorithm for RSA keys and convenient algorithm for EC.
    @Test
    public void testSelectSigningAlgAccordingToCert() throws Exception{

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

        BaseSigner signer = SmartCardManager.getInstance().getSigner(getPin(), cert);
        // now sign it by using smart card
        signature.sign(signer);

        FileOutputStream fileOutputStream = new FileOutputStream(getTestDataFolder() + SIGNATURE_FILENAME);
        signature.write(fileOutputStream);
        fileOutputStream.close();

        XadesSignatureValidation signatureValidation = new XadesSignatureValidation();
        signatureValidation.validate(SIGNATURE_FILENAME);
    }

    public void testCreateEnveloping(String signingAlgorithm, AlgorithmParameterSpec params) throws Exception{

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
        // now sign it by using smart card
        signature.sign(SmartCardManager.getInstance().getSigner(getPin(), cert, signingAlgorithm, params));

        FileOutputStream fileOutputStream = new FileOutputStream(getTestDataFolder() + SIGNATURE_FILENAME);
        signature.write(fileOutputStream);
        fileOutputStream.close();

        XadesSignatureValidation signatureValidation = new XadesSignatureValidation();
        signatureValidation.validate(SIGNATURE_FILENAME);
    }
}
