<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="es" lang="es">
<head>
	<script type="text/javascript" src="<%=request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath()%>/sign/miniapplet.js"></script>
</head>

<body>	
	<script type="text/javascript">
	
	    MiniApplet.cargarMiniApplet('<%=request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath()%>/sign');
	    
	    var signatureAlg = '<%=request.getParameter("signatureAlg")%>';
	    
	    <% if (request.getParameter("mimeType").equals("pdf")) { %>
	    
	        var str = '<%=request.getParameter("paramsPades")%>';
	        str = str.split('\t').join('\n');
			
			function doSign(dataToSign, signedData, signerRole) {
				signedData.value = MiniApplet.sign(dataToSign.value, 
						signatureAlg, 
						"PAdES", 
						str, 
						parent.show_signed, 
						parent.show_error);			
			}
			
		<% } else { %>
		
	        var str = '<%=request.getParameter("paramsCades")%>';
	        str = str.split('\t').join('\n');
        
			function doSign(dataToSign, signedData, signerRole) {
				signedData.value = MiniApplet.sign(dataToSign.value, 
						signatureAlg, 
						"CAdES", 
						str, 
						parent.show_signed, 
						parent.show_error);			
			}

		<% } %>
		
	</script>		
</body>
</html>