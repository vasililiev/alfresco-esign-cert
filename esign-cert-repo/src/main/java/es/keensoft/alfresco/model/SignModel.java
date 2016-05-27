package es.keensoft.alfresco.model;

import org.alfresco.service.namespace.QName;

public class SignModel {
	
	public static final String URI = "http://www.alfresco.com/model/signmodel/1.0";
	
	public static final QName ASPECT_SIGNED = QName.createQName(URI, "signed");
	public static final QName PROP_TYPE = QName.createQName(URI, "type");
	public static enum SIGNATURE_TYPE {
		IMPLICIT, EXPLICIT
	}
	public static final QName ASSOC_SIGNATURE = QName.createQName(URI, "signatureAssoc");

	public static final QName ASPECT_SIGNATURE = QName.createQName(URI, "signature");
	public static final QName PROP_FORMAT = QName.createQName(URI, "format");
	public static final QName PROP_DATE = QName.createQName(URI, "date");
	public static enum SIGNATURE_FORMAT {
		PAdES_BES, CAdES_BES_DETACH
	}
	public static final QName PROP_CERTIFICATE_PRINCIPAL = QName.createQName(URI, "certificatePrincipal");
	public static final QName PROP_CERTIFICATE_NOT_AFTER = QName.createQName(URI, "certificateNotAfter");
	public static final QName PROP_CERTIFICATE_SERIAL_NUMBER = QName.createQName(URI, "certificateSerialNumber");
	public static final QName PROP_CERTIFICATE_ISSUER = QName.createQName(URI, "certificateIssuer");
	public static final QName ASSOC_DOC = QName.createQName(URI, "docAssoc");

	/**************************************/
	/********SIGNERS PROPERTIES************/
	/**************************************/

	public static final QName ASPECT_SIGNATURE1 = QName.createQName(URI, "firstSignature");
	public static final QName PROP_FORMAT1 = QName.createQName(URI, "1format");
	public static final QName PROP_DATE1 = QName.createQName(URI, "1date");
	public static final QName PROP_CERTIFICATE_PRINCIPAL1 = QName.createQName(URI, "1certificatePrincipal");
	public static final QName PROP_CERTIFICATE_NOT_AFTER1 = QName.createQName(URI, "1certificateNotAfter");
	public static final QName PROP_CERTIFICATE_SERIAL_NUMBER1 = QName.createQName(URI, "1certificateSerialNumber");
	public static final QName PROP_CERTIFICATE_ISSUER1 = QName.createQName(URI, "1certificateIssuer");

	public static final QName ASPECT_SIGNATURE2 = QName.createQName(URI, "secondSignature");
	public static final QName PROP_FORMAT2 = QName.createQName(URI, "2format");
	public static final QName PROP_DATE2 = QName.createQName(URI, "2date");
	public static final QName PROP_CERTIFICATE_PRINCIPAL2 = QName.createQName(URI, "2certificatePrincipal");
	public static final QName PROP_CERTIFICATE_NOT_AFTER2 = QName.createQName(URI, "2certificateNotAfter");
	public static final QName PROP_CERTIFICATE_SERIAL_NUMBER2 = QName.createQName(URI, "2certificateSerialNumber");
	public static final QName PROP_CERTIFICATE_ISSUER2 = QName.createQName(URI, "2certificateIssuer");

	public static final QName ASPECT_SIGNATURE3 = QName.createQName(URI, "thirdSignature");
	public static final QName PROP_FORMAT3 = QName.createQName(URI, "3format");
	public static final QName PROP_DATE3 = QName.createQName(URI, "3date");
	public static final QName PROP_CERTIFICATE_PRINCIPAL3 = QName.createQName(URI, "3certificatePrincipal");
	public static final QName PROP_CERTIFICATE_NOT_AFTER3 = QName.createQName(URI, "3certificateNotAfter");
	public static final QName PROP_CERTIFICATE_SERIAL_NUMBER3 = QName.createQName(URI, "3certificateSerialNumber");
	public static final QName PROP_CERTIFICATE_ISSUER3 = QName.createQName(URI, "3certificateIssuer");

	public static final QName ASPECT_SIGNATURE4 = QName.createQName(URI, "fourthSignature");
	public static final QName PROP_FORMAT4 = QName.createQName(URI, "4format");
	public static final QName PROP_DATE4 = QName.createQName(URI, "4date");
	public static final QName PROP_CERTIFICATE_PRINCIPAL4 = QName.createQName(URI, "4certificatePrincipal");
	public static final QName PROP_CERTIFICATE_NOT_AFTER4 = QName.createQName(URI, "4certificateNotAfter");
	public static final QName PROP_CERTIFICATE_SERIAL_NUMBER4 = QName.createQName(URI, "4certificateSerialNumber");
	public static final QName PROP_CERTIFICATE_ISSUER4 = QName.createQName(URI, "4certificateIssuer");

	public static final QName ASPECT_SIGNATURE5 = QName.createQName(URI, "fifthSignature");
	public static final QName PROP_FORMAT5 = QName.createQName(URI, "5format");
	public static final QName PROP_DATE5 = QName.createQName(URI, "5date");
	public static final QName PROP_CERTIFICATE_PRINCIPAL5 = QName.createQName(URI, "5certificatePrincipal");
	public static final QName PROP_CERTIFICATE_NOT_AFTER5 = QName.createQName(URI, "5certificateNotAfter");
	public static final QName PROP_CERTIFICATE_SERIAL_NUMBER5 = QName.createQName(URI, "5certificateSerialNumber");
	public static final QName PROP_CERTIFICATE_ISSUER5 = QName.createQName(URI, "5certificateIssuer");
}
