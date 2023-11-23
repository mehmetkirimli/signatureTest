package tr.gov.tubitak.uekae.esya.api.xades.example;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import tr.gov.tubitak.uekae.esya.api.SampleBase;
import tr.gov.tubitak.uekae.esya.api.common.ESYAException;
import tr.gov.tubitak.uekae.esya.api.crypto.alg.SignatureAlg;
import tr.gov.tubitak.uekae.esya.api.signature.util.PfxSigner;
import tr.gov.tubitak.uekae.esya.api.xmlsignature.Context;
import tr.gov.tubitak.uekae.esya.api.xmlsignature.XMLSignature;
import tr.gov.tubitak.uekae.esya.api.xmlsignature.XMLSignatureException;
import tr.gov.tubitak.uekae.esya.api.xmlsignature.config.Config;
import tr.gov.tubitak.uekae.esya.api.xmlsignature.resolver.OfflineResolver;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.File;

/**
 * Represents a base for signature samples
 */
public class XadesSampleBase extends SampleBase {

    public static final int[] OID_POLICY_P2 = new int[]{2, 16, 792, 1, 61, 0, 1, 5070, 3, 1, 1};
    public static final int[] OID_POLICY_P3 = new int[]{2, 16, 792, 1, 61, 0, 1, 5070, 3, 2, 1};
    public static final int[] OID_POLICY_P4 = new int[]{2, 16, 792, 1, 61, 0, 1, 5070, 3, 3, 1};

    private static String configFile;     // config file path
    private static String testDataFolder;   // base directory where signatures created

    private static String policyFile;       // certificate validation policy file path
    private static String policyFileCrl;    // path of policy file without OCSP
    private static PfxSigner pfxSigner;     // pfx signer

    private static OfflineResolver offlineResolver;   // policy resolver for profile examples

    /**
     * Initialize paths and other variables
     */
    static {

        testDataFolder = getRootDir() + "/testdata/";
        configFile = getRootDir() + "/config/xmlsignature-config.xml";
        policyFile = getRootDir() + "/config/certval-policy-test.xml";
        policyFileCrl = getRootDir() + "/config/certval-policy-test-crl.xml";

        String pfxPass = "745418";
        String pfxFile = getRootDir() + "/sertifika deposu/test1@test.com_745418.pfx";
        pfxSigner = new PfxSigner(SignatureAlg.RSA_SHA256, pfxFile, pfxPass.toCharArray());

        System.out.println("Base dir : " + testDataFolder);

        offlineResolver = new OfflineResolver();
        offlineResolver.register("urn:oid:2.16.792.1.61.0.1.5070.3.1.1", getRootDir() + "/config/profiller/Elektronik_Imza_Kullanim_Profilleri_Rehberi.pdf", "text/plain");
        offlineResolver.register("urn:oid:2.16.792.1.61.0.1.5070.3.2.1", getRootDir() + "/config/profiller/Elektronik_Imza_Kullanim_Profilleri_Rehberi.pdf", "text/plain");
        offlineResolver.register("urn:oid:2.16.792.1.61.0.1.5070.3.3.1", getRootDir() + "/config/profiller/Elektronik_Imza_Kullanim_Profilleri_Rehberi.pdf", "text/plain");
    }

    private final String ENVELOPE_XML =  // sample XML document used for enveloped signature
            "<envelope>\n" +
                    "  <data id=\"data1\">\n" +
                    "    <item>Item 1</item>\n" +
                    "    <item>Item 2</item>\n" +
                    "    <item>Item 3</item>\n" +
                    "  </data>\n" +
                    "</envelope>\n";

    /**
     * Creates context for signature creation and validation
     *
     * @return created context
     */
    protected Context createContext() {

        Context context = null;
        try {
            context = new Context(testDataFolder);
            context.setConfig(new Config(configFile));
        } catch (XMLSignatureException e) {
            e.printStackTrace();
        }
        return context;
    }

    /**
     * Creates sample envelope XML that will contain signature inside
     *
     * @return envelope in Document format
     * @throws ESYAException
     */
    protected Document newEnvelope() throws ESYAException {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();

            return db.parse(new ByteArrayInputStream(ENVELOPE_XML.getBytes()));
        } catch (Exception x) {
            // we shouldn't be here if ENVELOPE_XML is valid
            x.printStackTrace();
        }
        throw new ESYAException("Cant construct envelope xml ");
    }

    /**
     * Reads an XML document into DOM document format
     *
     * @param uri      XML file to be read
     * @param aContext signature context
     * @return DOM document format of read XML document
     * @throws Exception
     */
    protected Document parseDoc(String uri, Context aContext) throws Exception {

        // generate document builders for parsing
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();

        // open the document
        File f = new File(testDataFolder + uri);

        // parse into DOM format
        org.w3c.dom.Document document = db.parse(f);
        aContext.setDocument(document);

        return document;
    }

    /**
     * Gets the signature by searching for tag in an enveloped signature
     *
     * @param aDocument XML document to be looked for
     * @param aContext  signature context
     * @return XML signature in the XML document
     * @throws Exception
     */
    protected XMLSignature readSignature(Document aDocument, Context aContext) throws Exception {

        // get the signature in enveloped signature format
        Element signatureElement = ((Element) aDocument.getDocumentElement().getElementsByTagName("ds:Signature").item(0));

        // return the XML signature created with signature element
        return new XMLSignature(signatureElement, aContext);
    }

    /**
     * Gets the signature by searching for tag in a parallel signature
     *
     * @param aDocument XML document to be looked for
     * @param aContext  signature context
     * @param item      order of signature to be read in parallel structure
     * @return XML signature in the XML document
     * @throws Exception
     */
    protected XMLSignature readSignature(Document aDocument, Context aContext, int item) throws Exception {

        // get the first signature element searching for the tag in the XML document
        Element signatureElement = ((Element) ((Element) aDocument.getDocumentElement().getElementsByTagName("signatures").item(0)).getElementsByTagName("ds:Signature").item(item));

        // return the XML signature using signature element
        return new XMLSignature(signatureElement, aContext);
    }

    /**
     * Gets test data folder
     *
     * @return the test data folder
     */
    protected String getTestDataFolder() {
        return testDataFolder;
    }

    /**
     * Gets offline policy resolver for profile examples
     *
     * @return the offline resolver
     */
    protected OfflineResolver getPolicyResolver() {
        return offlineResolver;
    }

    /**
     * Gets policy file crl
     *
     * @return the policy file crl
     */
    protected String getPolicyFileCrl() {
        return policyFileCrl;
    }

    /**
     * Gets policy file
     *
     * @return the policy file
     */
    protected String getPolicyFile() {
        return policyFile;
    }

    /**
     * Gets pfx signer
     *
     * @return the pfx signer
     */
    protected PfxSigner getPfxSigner() {
        return pfxSigner;
    }

}
