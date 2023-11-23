package tr.gov.tubitak.uekae.esya.api.xades.example.attributes;

import org.junit.Test;
import org.w3c.dom.Element;
import tr.gov.tubitak.uekae.esya.api.asn.x509.ECertificate;
import tr.gov.tubitak.uekae.esya.api.smartcard.example.smartcardmanager.SmartCardManager;
import tr.gov.tubitak.uekae.esya.api.xades.example.XadesSampleBase;
import tr.gov.tubitak.uekae.esya.api.xades.example.validation.XadesSignatureValidation;
import tr.gov.tubitak.uekae.esya.api.xmlsignature.Context;
import tr.gov.tubitak.uekae.esya.api.xmlsignature.XMLSignature;
import tr.gov.tubitak.uekae.esya.api.xmlsignature.model.xades.CommitmentTypeId;
import tr.gov.tubitak.uekae.esya.api.xmlsignature.model.xades.CommitmentTypeIndication;
import tr.gov.tubitak.uekae.esya.api.xmlsignature.model.xades.CommitmentTypeQualifier;
import tr.gov.tubitak.uekae.esya.api.xmlsignature.model.xades.Identifier;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.util.Arrays;

/**
 * BES with CommitmentTypeIndication attribute sample
 */
public class CommitmentTypeIndicationAttribute extends XadesSampleBase {

    public static final String SIGNATURE_FILENAME = "commitment_type_indication.xml";

    /**
     * Creates BES with CommitmentTypeIndication attribute
     *
     * @throws Exception
     */
    @Test
    public void createBESWithCommitmentTypeIndication() throws Exception {

        // create context with working directory
        Context context = createContext();

        // create signature according to context,
        // with default type (XADES_BES)
        XMLSignature signature = new XMLSignature(context);

        // add document
        String ref1 = "#" + signature.addDocument("./sample.txt", "text/plain", true);
        String objId2 = signature.addPlainObject("Test data 1.", "text/plain", null);
        String ref2 = "#" + signature.addDocument("#" + objId2, null, false);

        // false-true gets non-qualified certificates while true-false gets qualified ones
        ECertificate cert = SmartCardManager.getInstance().getSignatureCertificate(isQualified());

        // add certificate to show who signed the document
        signature.addKeyInfo(cert);

        // add commitment type indication
        signature.getQualifyingProperties().getSignedProperties().createOrGetSignedDataObjectProperties().
                addCommitmentTypeIndication(createTestCTI(context, ref1, ref2));

        // now sign it by using smart card
        signature.sign(SmartCardManager.getInstance().getSigner(getPin(), cert));

        FileOutputStream fileOutputStream = new FileOutputStream(getTestDataFolder() + SIGNATURE_FILENAME);
        signature.write(fileOutputStream);
        fileOutputStream.close();

        XadesSignatureValidation signatureValidation = new XadesSignatureValidation();
        signatureValidation.validate(SIGNATURE_FILENAME);

    }

    private CommitmentTypeIndication createTestCTI(Context c, String ref1, String ref2) throws Exception {

        CommitmentTypeId typeId = new CommitmentTypeId(
                c,
                new Identifier(c, "http://uri.etsi.org/01903/v1.2.2#ProofOfOrigin", null),
                "Proof of origin indicates that the signer recognizes to have created, approved and sent the signed data object.",
                Arrays.asList(
                        "http://test.test/commitment1.txt</xades:DocumentationReference",
                        "file:///test/data/xml/commitment2.txt</xades:DocumentationReference",
                        "http://test.test/commitment3.txt</xades:DocumentationReference")
        );

        CommitmentTypeQualifier q1 = new CommitmentTypeQualifier(c);
        q1.addContent("test commitment a");
        q1.addContent(getQualifierSampleContent());
        q1.addContent("test commitment b");
        CommitmentTypeQualifier q2 = new CommitmentTypeQualifier(c, "commitment 2");
        CommitmentTypeQualifier q3 = new CommitmentTypeQualifier(c, "commitment 2");

        return new CommitmentTypeIndication(c, typeId,
                Arrays.asList(ref1, ref2), false,
                Arrays.asList(q1, q2, q3));
    }

    private Element getQualifierSampleContent() throws Exception {

        return stringToElement(
                "<xl:XadesLabs xmlns:xl=\"http://xadeslabs.com/xades\"> \n" +
                        "          <xl:Commitments type=\"ProofOfOrigin\">\n" +
                        "            <xl:Commitment>commitment 1</xl:Commitment>\n" +
                        "            <xl:Commitment>commitment 2</xl:Commitment>\n" +
                        "            <xl:Commitment>commitment 3</xl:Commitment>\n" +
                        "            <xl:Commitment>commitment 4</xl:Commitment>\n" +
                        "          </xl:Commitments>\n" +
                        "</xl:XadesLabs>");
    }

    private Element stringToElement(String aStr) throws Exception {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        return db.parse(new ByteArrayInputStream(aStr.getBytes())).getDocumentElement();
    }
}
