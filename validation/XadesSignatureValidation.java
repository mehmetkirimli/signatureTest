package tr.gov.tubitak.uekae.esya.api.xades.example.validation;

import org.junit.Assert;
import tr.gov.tubitak.uekae.esya.api.xades.example.XadesSampleBase;
import tr.gov.tubitak.uekae.esya.api.xmlsignature.*;
import tr.gov.tubitak.uekae.esya.api.xmlsignature.document.FileDocument;
import tr.gov.tubitak.uekae.esya.api.xmlsignature.model.xades.UnsignedSignatureProperties;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertEquals;

/**
 * Provides validating functions for XML signatures.
 */
public class XadesSignatureValidation extends XadesSampleBase {
    /**
     * Generic validate function. Validates known types of XML signature.
     *
     * @param fileName name of the signature file to be validated
     * @throws Exception
     */
    public void validate(String fileName) throws Exception {

        Context context = createContext();

        /* optional - specifying policy from code
        // generate policy to be used in certificate validation
        ValidationPolicy policy = PolicyReader.readValidationPolicy(POLICY_FILE);

        CertValidationPolicies policies = new CertValidationPolicies();
        // null means default
        policies.register(null,policy);

        context.getConfig().getValidationConfig().setCertValidationPolicies(policies);
        */

        // add external resolver to resolve policies
        context.addExternalResolver(getPolicyResolver());

        XMLSignature signature = XMLSignature.parse(new FileDocument(new File(getTestDataFolder() + fileName)), context);

        // no parameters, use the certificate in key info
        ValidationResult result = signature.verify();
        System.out.println(result.toXml());
        assertEquals("Cant verify " + fileName, ValidationResultType.VALID, result.getType());

        signatureVerify(fileName, signature);

    }

    /**
     * Validate function for parallel signatures
     *
     * @param fileName name of the signature file to be validated
     * @throws Exception
     */
    public void validateParallel(String fileName) throws Exception {

        Context context = createContext();

        /* optional - specifying policy from code
        // generate policy to be used in certificate validation
        ValidationPolicy policy = PolicyReader.readValidationPolicy(POLICY_FILE);

        CertValidationPolicies policies = new CertValidationPolicies();
        // null means default
        policies.register(null,policy);

        context.getConfig().getValidationConfig().setCertValidationPolicies(policies);
        */

        // add external resolver to resolve policies
        context.addExternalResolver(getPolicyResolver());

        // XMLSignature list of root signatures
        List<XMLSignature> xmlSignatures = new ArrayList<XMLSignature>();

        // get signature as signed document in order to be able to validate parallel ones
        SignedDocument signedDocument = new SignedDocument(new FileDocument(new File(getTestDataFolder() + fileName)), context);
        xmlSignatures.addAll(signedDocument.getRootSignatures());

        for (XMLSignature signature : xmlSignatures) {
            // no parameters, use the certificate in key info
            ValidationResult result = signature.verify();
            System.out.println(result.toXml());
            Assert.assertTrue("Cant verify " + fileName,
                    result.getType() == ValidationResultType.VALID);

            signatureVerify(fileName, signature);
        }
    }

    private void signatureVerify(String fileName, XMLSignature signature) throws XMLSignatureException {

        UnsignedSignatureProperties usp = signature.getQualifyingProperties().getUnsignedSignatureProperties();
        if (usp != null) {
            List<XMLSignature> counterSignatures = usp.getAllCounterSignatures();
            for (XMLSignature counterSignature : counterSignatures) {
                ValidationResult counterResult = signature.verify();

                System.out.println(counterResult.toXml());

                assertEquals("Cant verify counter signature" + fileName + " : " + counterSignature.getId(),
                        ValidationResultType.VALID, counterResult.getType());

            }
        }
    }
}
