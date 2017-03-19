<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
<head>
    <title>Title</title>
    <style>
        table {
            counter-reset: user;
        }

        th[scope=row]:after {
            counter-increment: user;
            content: counter(user);
        }

        tr:nth-child(odd) {
            background: silver;
        }

        thead tr, tr:nth-child(even) {
            background: white !important;
        }
        td {
            border-right: 1px solid gray;
        }

        td:last-child {
            border-right: none;
        }

        td, th {
            padding: 5px;
            margin: 0;
            font-family: Calibri, Arial, Verdana, sans-serif;
            font-size: 14px;
        }
    </style>
</head>
<body>
<table cellspacing="0" cellpadding="0">
    <thead>
    <tr>
        <th>#</th>
        <th>Name</th>
        <th>Email</th>
        <th>Flag</th>
    </tr>
    </thead>
    <tbody>
    <c:forEach var="user" items="${users}">
        <tr>
            <th scope="row"></th>
            <td><c:out value="${user.getValue()}"/></td>
            <td><c:out value="${user.getEmail()}"/></td>
            <td><c:out value="${user.getFlag()}"/></td>
        </tr>
    </c:forEach>
    </tbody>
</table>
</body>
</html>
