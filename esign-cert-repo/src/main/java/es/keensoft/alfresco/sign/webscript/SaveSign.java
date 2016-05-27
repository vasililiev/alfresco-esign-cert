package es.keensoft.alfresco.sign.webscript;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.QName;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import com.google.gson.Gson;

import es.keensoft.alfresco.model.SignModel;
import es.keensoft.alfresco.sign.webscript.bean.BasicResponse;
import es.keensoft.alfresco.sign.webscript.bean.SaveSignRequest;


public class SaveSign extends AbstractWebScript {
	
	private static Log log = LogFactory.getLog(Base64NodeContent.class);
	
	private static final String RETURN_CODE_OK = "OK";	
	private static final String MIMETYPE_CMS = "application/cms";
	private static final String PDF_EXTENSION = "pdf";
	private static final String JS_UNDEFINED = "undefined";
	private static final String CADES_BES_DETACHED = "CAdES-BES Detached";
	private static final String PADES = "PAdES";
	private static final String CSIG_EXTENSION = ".CSIG";
	
	private CheckOutCheckInService checkOutCheckInService;
	private VersionService versionService;
	private ContentService contentService;
	private NodeService nodeService;
	
	//For PAdES - To access the signature postition
	private static String SIGNATURE_POSTITION = null;
	
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
		
		Gson gson = new Gson();
		SaveSignRequest request;
		BasicResponse response = new BasicResponse();
		
		try {
			
			String jsonText = req.getContent().getContent();
			request = gson.fromJson(jsonText, SaveSignRequest.class);
			
			if(StringUtils.isBlank(request.getSignedData()) || request.getSignedData().equals(JS_UNDEFINED))
				throw new WebScriptException("Signed data is empty or null.");
			
			//Set the signature position (if PAdES)
			if(!StringUtils.isBlank(request.getSignerPostition()) && !request.getSignerPostition().equals(JS_UNDEFINED)) {
				SIGNATURE_POSTITION = request.getSignerPostition();
			}
			
			//Signature aspect definition
		    Map<QName, Serializable> aspectSignatureProperties = new HashMap<QName, Serializable>();
            
		    if(StringUtils.isBlank(request.getSignerData()) || request.getSignerData().equals(JS_UNDEFINED)) {
				log.warn("Signer data is empty or null.");
			} else {
				CertificateFactory cf = CertificateFactory.getInstance("X.509");
				ByteArrayInputStream bais = new ByteArrayInputStream(Base64.decodeBase64(request.getSignerData()));
				X509Certificate certificate = (X509Certificate) cf.generateCertificate(bais);
				//Add the certificate properties depending on the signature position/algorithm
				aspectSignatureProperties = setCertificateProperties(certificate);
			}
		    
		    //Document to sign
			NodeRef nodeRef = new NodeRef(request.getNodeRef());
			
			Map<QName, Serializable> aspectSignedProperties = new HashMap<QName, Serializable>();
			if (request.getMimeType().equals(PDF_EXTENSION)) { // PAdES
			    storeSignPDF(nodeRef, request.getSignedData(), aspectSignatureProperties);
			    aspectSignedProperties.put(SignModel.PROP_TYPE, I18NUtil.getMessage("signature.implicit"));
			} else { // CAdES
			    storeSignOther(nodeRef, request.getSignedData(), aspectSignatureProperties);
			    aspectSignedProperties.put(SignModel.PROP_TYPE, I18NUtil.getMessage("signature.explicit"));
			}
			nodeService.addAspect(nodeRef, SignModel.ASPECT_SIGNED, aspectSignedProperties);
			
			response.setCode(RETURN_CODE_OK);
			
		} catch (Exception e) {
			throw new WebScriptException(e.getMessage(), e);
		}

		res.getWriter().write(gson.toJson(response));
	}
	
	private void storeSignOther(NodeRef originalNodeRef, String signedData, Map<QName, Serializable> aspectProperties) throws IOException {
		
		NodeRef parentRef = nodeService.getPrimaryParent(originalNodeRef).getParentRef();
		String originalFileName = nodeService.getProperty(originalNodeRef, ContentModel.PROP_NAME).toString();
		String signatureFileName = FilenameUtils.getBaseName(originalFileName) + CSIG_EXTENSION;
		NodeRef signatureNodeRef = nodeService.createNode(parentRef, ContentModel.ASSOC_CONTAINS, 
				QName.createQName(signatureFileName), ContentModel.TYPE_CONTENT).getChildRef();

		ContentWriter writer = contentService.getWriter(signatureNodeRef, ContentModel.PROP_CONTENT, true);
		writer.setMimetype(MIMETYPE_CMS);
		OutputStream contentOutputStream = writer.getContentOutputStream();
		IOUtils.write(Base64.decodeBase64(signedData), contentOutputStream);
		contentOutputStream.close();
		
		nodeService.setProperty(signatureNodeRef, ContentModel.PROP_NAME, signatureFileName);
		
		nodeService.createAssociation(originalNodeRef, signatureNodeRef, SignModel.ASSOC_SIGNATURE);
		nodeService.createAssociation(signatureNodeRef, originalNodeRef, SignModel.ASSOC_DOC);
		
	    aspectProperties.put(SignModel.PROP_FORMAT, CADES_BES_DETACHED);
	    aspectProperties.put(SignModel.PROP_DATE, new Date());
		nodeService.addAspect(signatureNodeRef, SignModel.ASPECT_SIGNATURE, aspectProperties);
		
	}
	
	private void storeSignPDF(NodeRef nodeRef, String signedData, Map<QName, Serializable> aspectProperties) throws IOException {
		
		try {
			
			versionService.ensureVersioningEnabled(nodeRef, null);
			
			NodeRef chkout = checkOutCheckInService.checkout(nodeRef);
			if(SIGNATURE_POSTITION != null) {
				
			}
			ContentWriter writer = contentService.getWriter(chkout, ContentModel.PROP_CONTENT, true);
			OutputStream contentOutputStream = writer.getContentOutputStream();
			IOUtils.write(Base64.decodeBase64(signedData), contentOutputStream);
			contentOutputStream.close();
			
			checkOutCheckInService.checkin(chkout, getVersionProperties());
			
			//Set the proper aspect depending on the position
			switch(SIGNATURE_POSTITION) {
			    case "1":
				    aspectProperties.put(SignModel.PROP_FORMAT1, PADES);
				    aspectProperties.put(SignModel.PROP_DATE1, new Date());
					nodeService.addAspect(nodeRef, SignModel.ASPECT_SIGNATURE1, aspectProperties);
			    	break;
			    case "2":
				    aspectProperties.put(SignModel.PROP_FORMAT2, PADES);
				    aspectProperties.put(SignModel.PROP_DATE2, new Date());
					nodeService.addAspect(nodeRef, SignModel.ASPECT_SIGNATURE2, aspectProperties);
			    	break;
			    case "3":
				    aspectProperties.put(SignModel.PROP_FORMAT3, PADES);
				    aspectProperties.put(SignModel.PROP_DATE3, new Date());
					nodeService.addAspect(nodeRef, SignModel.ASPECT_SIGNATURE3, aspectProperties);
			    	break;
			    case "4":
				    aspectProperties.put(SignModel.PROP_FORMAT4, PADES);
				    aspectProperties.put(SignModel.PROP_DATE4, new Date());
					nodeService.addAspect(nodeRef, SignModel.ASPECT_SIGNATURE4, aspectProperties);
			    	break;
			    case "5":
				    aspectProperties.put(SignModel.PROP_FORMAT5, PADES);
				    aspectProperties.put(SignModel.PROP_DATE5, new Date());
					nodeService.addAspect(nodeRef, SignModel.ASPECT_SIGNATURE5, aspectProperties);
			    	break;
			    default:
			    	throw new WebScriptException("No signature position is defined");
			}
			
		} finally {
			if (checkOutCheckInService.isCheckedOut(nodeRef)) {
				checkOutCheckInService.cancelCheckout(nodeRef);
			}
		}
	
	}

	/**
	 * Return a HaspMap with the properties of the signature certificate depending on the chosen position
	 * If there isn't a position selected it will se the default properties of the aspect
	 * @param certificate X509Certificate object of the certificate
	 * @return HashMap with the key-value aspect properties
	 */
	private Map<QName, Serializable> setCertificateProperties(X509Certificate certificate) {
	    Map<QName, Serializable> aspectSignatureProperties = new HashMap<QName, Serializable>(); 
	    switch(SIGNATURE_POSTITION) {
		    case "1":
			    aspectSignatureProperties.put(SignModel.PROP_CERTIFICATE_PRINCIPAL1, certificate.getSubjectX500Principal().toString());
			    aspectSignatureProperties.put(SignModel.PROP_CERTIFICATE_SERIAL_NUMBER1, certificate.getSerialNumber().toString());
			    aspectSignatureProperties.put(SignModel.PROP_CERTIFICATE_NOT_AFTER1, certificate.getNotAfter());
			    aspectSignatureProperties.put(SignModel.PROP_CERTIFICATE_ISSUER1, certificate.getIssuerX500Principal().toString());
		    	break;
		    case "2":
			    aspectSignatureProperties.put(SignModel.PROP_CERTIFICATE_PRINCIPAL2, certificate.getSubjectX500Principal().toString());
			    aspectSignatureProperties.put(SignModel.PROP_CERTIFICATE_SERIAL_NUMBER2, certificate.getSerialNumber().toString());
			    aspectSignatureProperties.put(SignModel.PROP_CERTIFICATE_NOT_AFTER2, certificate.getNotAfter());
			    aspectSignatureProperties.put(SignModel.PROP_CERTIFICATE_ISSUER2, certificate.getIssuerX500Principal().toString());
		    	break;
		    case "3":
			    aspectSignatureProperties.put(SignModel.PROP_CERTIFICATE_PRINCIPAL3, certificate.getSubjectX500Principal().toString());
			    aspectSignatureProperties.put(SignModel.PROP_CERTIFICATE_SERIAL_NUMBER3, certificate.getSerialNumber().toString());
			    aspectSignatureProperties.put(SignModel.PROP_CERTIFICATE_NOT_AFTER3, certificate.getNotAfter());
			    aspectSignatureProperties.put(SignModel.PROP_CERTIFICATE_ISSUER3, certificate.getIssuerX500Principal().toString());
		    	break;
		    case "4":
			    aspectSignatureProperties.put(SignModel.PROP_CERTIFICATE_PRINCIPAL4, certificate.getSubjectX500Principal().toString());
			    aspectSignatureProperties.put(SignModel.PROP_CERTIFICATE_SERIAL_NUMBER4, certificate.getSerialNumber().toString());
			    aspectSignatureProperties.put(SignModel.PROP_CERTIFICATE_NOT_AFTER4, certificate.getNotAfter());
			    aspectSignatureProperties.put(SignModel.PROP_CERTIFICATE_ISSUER4, certificate.getIssuerX500Principal().toString());
		    	break;
		    case "5":
			    aspectSignatureProperties.put(SignModel.PROP_CERTIFICATE_PRINCIPAL5, certificate.getSubjectX500Principal().toString());
			    aspectSignatureProperties.put(SignModel.PROP_CERTIFICATE_SERIAL_NUMBER5, certificate.getSerialNumber().toString());
			    aspectSignatureProperties.put(SignModel.PROP_CERTIFICATE_NOT_AFTER5, certificate.getNotAfter());
			    aspectSignatureProperties.put(SignModel.PROP_CERTIFICATE_ISSUER5, certificate.getIssuerX500Principal().toString());
		    	break;
		    default:
			    aspectSignatureProperties.put(SignModel.PROP_CERTIFICATE_PRINCIPAL, certificate.getSubjectX500Principal().toString());
			    aspectSignatureProperties.put(SignModel.PROP_CERTIFICATE_SERIAL_NUMBER, certificate.getSerialNumber().toString());
			    aspectSignatureProperties.put(SignModel.PROP_CERTIFICATE_NOT_AFTER, certificate.getNotAfter());
			    aspectSignatureProperties.put(SignModel.PROP_CERTIFICATE_ISSUER, certificate.getIssuerX500Principal().toString());
		    	break;
	    }
	    
	    return aspectSignatureProperties;
	}
	
	private Map<String, Serializable> getVersionProperties() {
		Map<String, Serializable> versionProperties = new HashMap<String, Serializable>();
		versionProperties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MAJOR);		
		versionProperties.put(VersionModel.PROP_DESCRIPTION, I18NUtil.getMessage("version.description")); 		
		return versionProperties;
	}

	public CheckOutCheckInService getCheckOutCheckInService() {
		return checkOutCheckInService;
	}

	public void setCheckOutCheckInService(CheckOutCheckInService checkOutCheckInService) {
		this.checkOutCheckInService = checkOutCheckInService;
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

	public NodeService getNodeService() {
		return nodeService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

}
