<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<div id="rating">
<ul class="g-paginator"><li class="g-pagination">Hello!</li></ul>
</div>

<script lang="javascript">
    document.getElementById('g-pagination-message').innerHTML=document.getElementById('rating').innerHTML;
    document.getElementById('rating').innerHTML='';
</script>