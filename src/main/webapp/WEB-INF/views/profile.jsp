<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <title>User Profile - Online Shop</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/style.css">
</head>
<body>
    <div class="header">
        <span>Account Configuration for: <strong><c:out value="${sessionScope.user.username}"/></strong></span>
        <div class="nav-links">
            <a href="${pageContext.request.contextPath}/products">Catalog</a> |
            <a href="${pageContext.request.contextPath}/orders">My Orders</a> |
            <a href="${pageContext.request.contextPath}/profile">Profile</a> |
            <a href="${pageContext.request.contextPath}/logout" class="logout-link">Sign Out</a>
        </div>
    </div>

    <div class="container" style="margin-top: 50px;">
        <h2>Manage Profile Settings</h2>

        <!-- Flash error message feedback processing -->
        <c:if test="${not empty error}">
            <div class="error-box">
                <c:out value="${error}"/>
            </div>
        </c:if>

        <!-- Flash success message feedback processing -->
        <c:if test="${not empty message}">
            <div class="success-box">
                <c:out value="${message}"/>
            </div>
        </c:if>

        <form action="${pageContext.request.contextPath}/profile" method="post">
            <div class="form-group">
                <label>Username (Identity account token):</label>
                <input type="text" value="<c:out value='${sessionScope.user.username}'/>" disabled class="disabled-input">
                <small style="color: #666; display: block; margin-top: 5px;">Username cannot be modified for security parameters.</small>
            </div>

            <div class="form-group">
                <label>Role Privilege Matrix:</label>
                <input type="text" value="<c:out value='${sessionScope.user.role}'/>" disabled class="disabled-input">
            </div>

            <div class="form-group">
                <label for="email">Email Address Configuration:</label>
                <input type="email" id="email" name="email" value="<c:out value='${sessionScope.user.email}'/>" required maxLength="100">
            </div>

            <button type="submit" class="btn">Update Profile Email</button>
        </form>
    </div>
</body>
</html>