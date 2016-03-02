package es.keensoft.alfresco.sign.webscript;

import java.io.IOException;

import org.alfresco.repo.content.MimetypeMap;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import com.google.gson.Gson;

import es.keensoft.alfresco.sign.webscript.bean.SignatureParamsResponse;

public class SignatureParams extends AbstractWebScript {
	
	private static Log log = LogFactory.getLog(SignatureParams.class);
	
	private String paramsCades;
	private String paramsPades;
	private String signatureAlg;
	
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
		
		Gson gson = new Gson();
		SignatureParamsResponse response = new SignatureParamsResponse();

		try {
			
			response.setParamsCades(paramsCades);
			response.setParamsPades(paramsPades);
			response.setSignatureAlg(signatureAlg);
			
		} catch (Exception e) {
			
			log.error(ExceptionUtils.getFullStackTrace(e));
			throw new WebScriptException(e.getMessage(), e);
			
		}

		res.setContentType(MimetypeMap.MIMETYPE_JSON);
		res.getWriter().write(gson.toJson(response));
	}

	public void setParamsCades(String paramsCades) {
		this.paramsCades = paramsCades;
	}

	public void setParamsPades(String paramsPades) {
		this.paramsPades = paramsPades;
	}

	public void setSignatureAlg(String signatureAlg) {
		this.signatureAlg = signatureAlg;
	}

}
