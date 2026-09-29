<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <title>My Orders - Online Shop</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/style.css">
</head>
<body>
    <div class="header">
        <span>Shopping Session for: <strong><c:out value="${sessionScope.user.username}"/></strong></span>
        <div class="nav-links">
            <a href="${pageContext.request.contextPath}/products">Catalog</a> |
            <a href="${pageContext.request.contextPath}/orders">My Orders</a> |
            <a href="${pageContext.request.contextPath}/profile">Profile</a> |
            <a href="${pageContext.request.contextPath}/logout" class="logout-link">Sign Out</a>
        </div>
    </div>

    <div class="main-content">
        <h2>Your Purchase History</h2>

        <c:if test="${not empty error}">
            <div class="error-box"><c:out value="${error}"/></div>
        </c:if>

        <table class="data-table">
            <thead>
                <tr>
                    <th>Order ID</th>
                    <th>Product Name</th>
                    <th>Quantity</th>
                    <th>Total Price</th>
                    <th>Status</th>
                    <th>Action</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="order" items="${orders}">
                    <tr>
                        <td>#<c:out value="${order.id}"/></td>
                        <td><c:out value="${order.productName}"/></td>
                        <td><c:out value="${order.quantity}"/></td>
                        <td>$<c:out value="${order.totalPrice}"/></td>
                        <td>
                            <span class="status-badge ${order.status.toLowerCase()}">
                                <c:out value="${order.status}"/>
                            </span>
                        </td>
                        <td>
                            <c:if test="${order.status eq 'CREATED'}">
                                <form action="${pageContext.request.contextPath}/orders?action=cancel" method="post" onsubmit="return confirm('Cancel this order session?');">
                                    <input type="hidden" name="id" value="${order.id}">
                                    <button type="submit" class="btn btn-danger btn-sm">Cancel Order</button>
                                </form>
                            </c:if>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty orders}">
                    <tr>
                        <td colspan="6" class="text-center">You haven't placed any purchase orders yet.</td>
                    </tr>
                </c:if>
            </tbody>
        </table>
    </div>
</body>
</html>