<%-- 
    Document   : test
    Created on : May 1, 2012, 10:23:08 PM
    Author     : ste
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
        <script src='/campanile/lib/jquery.js' type="text/javascript"></script>
        <script src="/campanile/lib/rating/jquery.rating.pack.js" type="text/javascript"></script>
        <link rel="stylesheet" href="/campanile/lib/rating/jquery.rating.css" type="text/css" />
    </head>
    <body>
        <input name="star3" type="radio" class="star" title="1"/>
        <input name="star3" type="radio" class="star" title="2"/>
        <input name="star3" type="radio" class="star" title="3" checked="checked"/>
        <input name="star3" type="radio" class="star" title="4"/>
        <input name="star3" type="radio" class="star" title="5"/>
    </body>
    <script type="text/javascript" language="javascript">
    $(function(){ 
      $('#form :radio.star').rating(); 
    });
    </script>
</html>
