package es.keensoft.alfresco.behaviour;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfPKCS7;
import com.itextpdf.text.pdf.PdfReader;

import es.keensoft.alfresco.model.SignModel;

public class CustomBehaviour implements NodeServicePolicies.OnDeleteAssociationPolicy, NodeServicePolicies.OnCreateNodePolicy {
	
	private PolicyComponent policyComponent;
	private NodeService nodeService;
	private VersionService versionService;
	private ContentService contentService;
	
	private static final String PADES = "PAdES";
	private static Log log = LogFactory.getLog(CustomBehaviour.class);
	
	public void init() {
		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnDeleteAssociationPolicy.QNAME, 
				SignModel.ASPECT_SIGNATURE, new JavaBehaviour(this, "onDeleteAssociation", 
				NotificationFrequency.TRANSACTION_COMMIT));
		log.debug("Enter create node");
		policyComponent.bindClassBehaviour(
		        NodeServicePolicies.OnCreateNodePolicy.QNAME,
		        ContentModel.PROP_CONTENT,
		        new JavaBehaviour(this, "onCreateNode", NotificationFrequency.TRANSACTION_COMMIT)
		    );
	}
	

	@Override
	public void onCreateNode(ChildAssociationRef childNodeRef) {

		log.debug("Enter create node");
		NodeRef node = childNodeRef.getChildRef();
		ContentData contentData = (ContentData) nodeService.getProperty(node, ContentModel.PROP_CONTENT);
		//Do this check only if the uploaded document is a PDF
		if(contentData.getMimetype().equalsIgnoreCase("application/pdf")) {

			log.debug("Is PDF");
			try {
				ArrayList<Map<QName, Serializable>> signatures = getDigitalSignatures(node);
				//Add the aspect asociation
				if(signatures != null) {
					for(Map<QName, Serializable> aspectProperties : signatures) {
						String originalFileName = nodeService.getProperty(node, ContentModel.PROP_NAME).toString();
						String signatureFileName = FilenameUtils.getBaseName(originalFileName) + "-" 
						+ System.currentTimeMillis() + "-" + PADES;
					
						// Creating a node reference without type (no content and no folder), remains invisible for Share
						NodeRef signatureNodeRef = nodeService.createNode(
								nodeService.getPrimaryParent(node).getParentRef(),
								ContentModel.ASSOC_CONTAINS, 
								QName.createQName(signatureFileName), 
								ContentModel.TYPE_CMOBJECT).getChildRef();
						
						nodeService.createAssociation(node, signatureNodeRef, SignModel.ASSOC_SIGNATURE);
						nodeService.createAssociation(signatureNodeRef, node, SignModel.ASSOC_DOC);
						
					    aspectProperties.put(SignModel.PROP_FORMAT, PADES);
						nodeService.addAspect(signatureNodeRef, SignModel.ASPECT_SIGNATURE, aspectProperties);
					}
				}
			}
			catch(Exception ex) {
				log.error(ex.toString());
			}
		}
	}

	@Override
	public void onDeleteAssociation(AssociationRef nodeAssocRef) {
		// Delete SIGNED aspect on SIGNATURE deletion
		if (nodeService.exists(nodeAssocRef.getTargetRef())) {
		    nodeService.removeAspect(nodeAssocRef.getTargetRef(), SignModel.ASPECT_SIGNED);
		}
	}
	
	public ArrayList<Map<QName, Serializable>> getDigitalSignatures(NodeRef node) throws IOException, KeyStoreException, Exception, CertificateException {
		
		ContentReader contentReader = contentService.getReader(node, ContentModel.PROP_CONTENT);
		InputStream is = contentReader.getContentInputStream();
		
		PdfReader reader = new PdfReader(is);
        AcroFields af = reader.getAcroFields();
        ArrayList<String> names = af.getSignatureNames();
        if(names == null || names.isEmpty()) return null;
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(null, null);
        ArrayList<Map<QName, Serializable>> aspects = new ArrayList<Map<QName, Serializable>>();
        for (String name : names) {
            PdfPKCS7 pk = af.verifySignature(name);
            X509Certificate certificate = pk.getSigningCertificate();
           
            //Set aspect properties for each signature
            Map<QName, Serializable> aspectSignatureProperties = new HashMap<QName, Serializable>(); 
            aspectSignatureProperties.put(SignModel.PROP_DATE, convertCalendarToDate(pk.getSignDate()));
    		aspectSignatureProperties.put(SignModel.PROP_CERTIFICATE_PRINCIPAL, certificate.getSubjectX500Principal().toString());
    	    aspectSignatureProperties.put(SignModel.PROP_CERTIFICATE_SERIAL_NUMBER, certificate.getSerialNumber().toString());
    	    aspectSignatureProperties.put(SignModel.PROP_CERTIFICATE_NOT_AFTER, certificate.getNotAfter());
    	    aspectSignatureProperties.put(SignModel.PROP_CERTIFICATE_ISSUER, certificate.getIssuerX500Principal().toString());   
    	    aspects.add(aspectSignatureProperties);
        }
		return aspects;
	}
	
	
	@SuppressWarnings({"deprecation" })
	private Date convertCalendarToDate(Calendar cal) {
		Date date = new Date();
		date.setDate(cal.get(Calendar.DATE));
		date.setMonth(cal.get(Calendar.MONTH));
		date.setYear(cal.get(Calendar.YEAR) - 1900);
		date.setHours(cal.get(Calendar.HOUR));
		date.setMinutes(cal.get(Calendar.MINUTE));
		date.setSeconds(cal.get(Calendar.SECOND));
		System.out.println(date);
		return date;
	}
	
	public PolicyComponent getPolicyComponent() {
		return policyComponent;
	}

	public void setPolicyComponent(PolicyComponent policyComponent) {
		this.policyComponent = policyComponent;
	}

	public NodeService getNodeService() {
		return nodeService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public VersionService getVersionService() {
		return versionService;
	}

	public void setVersionService(VersionService versionService) {
		this.versionService = versionService;
	}
	
	public ContentService getContentService() {
		return contentService;
	}
	
	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}
}