<%
java.util.Enumeration e = request.getHeaderNames();
while(e.hasMoreElements()) {
  String name = (String)e.nextElement();
  String value = request.getHeader(name);
%>
<%= name %>: <%= value %><br/>
<%
}
%>

<%= request.getServerName() %><br/>
<%= request.getServerPort() %><br/>
<%= request.getLocalName() %><br/>
<%= request.getLocalPort() %><br/>
