<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<div id="rating">
<input name="star3" type="radio" class="star" title="1"/>
<input name="star3" type="radio" class="star" title="2"/>
<input name="star3" type="radio" class="star" title="3" checked="checked"/>
<input name="star3" type="radio" class="star" title="4"/>
<input name="star3" type="radio" class="star" title="5"/>
</div>


<script src="/campanile/lib/rating/jquery.rating.pack.js" type="text/javascript">
    $(function(){ 
      $('#form :radio.star').rating(); 
    });
</script>
<script lang="javascript">
    //document.getElementById('g-pagination-message').innerHTML=document.getElementById('rating').innerHTML;
    //document.getElementById('rating').innerHTML='';
</script>
