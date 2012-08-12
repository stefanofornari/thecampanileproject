<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<div class="rating">
<input name="star3" type="radio" class="star" title="1"<c:if test='${rating == 1}'> checked="checked"</c:if>/>
<input name="star3" type="radio" class="star" title="2"<c:if test='${rating == 2}'> checked="checked"</c:if>/>
<input name="star3" type="radio" class="star" title="3"<c:if test='${rating == 3}'> checked="checked"</c:if>/>
<input name="star3" type="radio" class="star" title="4"<c:if test='${rating == 4}'> checked="checked"</c:if>/>
<input name="star3" type="radio" class="star" title="5"<c:if test='${rating == 5}'> checked="checked"</c:if>/>
<script src="/campanile/lib/rating/jquery.rating.pack.js" type="text/javascript"></script>
</div>