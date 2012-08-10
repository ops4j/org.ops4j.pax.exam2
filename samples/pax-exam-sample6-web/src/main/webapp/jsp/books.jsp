<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<html>
<body>
<h2>Books in the library</h2>

<table>
<c:forEach var="book" items="${books}">
  <tr>
    <td>${book.author.lastName }</td>
    <td>${book.author.firstName }</td>
    <td>${book.title }</td>
  </tr>
</c:forEach>
</table>
</body>
</html>
