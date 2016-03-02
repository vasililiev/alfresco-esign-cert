package es.keensoft.alfresco.sign.webscript.bean;

public class SignatureParamsResponse {
	
	private String paramsCades;
	private String paramsPades;
	private String signatureAlg;
	
	public String getParamsCades() {
		return paramsCades;
	}
	public void setParamsCades(String paramsCades) {
		this.paramsCades = paramsCades;
	}
	public String getParamsPades() {
		return paramsPades;
	}
	public void setParamsPades(String paramsPades) {
		this.paramsPades = paramsPades;
	}
	public String getSignatureAlg() {
		return signatureAlg;
	}
	public void setSignatureAlg(String signatureAlg) {
		this.signatureAlg = signatureAlg;
	}

}
