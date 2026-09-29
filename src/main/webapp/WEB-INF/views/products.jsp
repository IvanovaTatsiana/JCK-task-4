<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Products Catalogue - Online Shop</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/style.css">
</head>
<body>
    <div class="header">
        <span>Welcome, <strong><c:out value="${sessionScope.user.username}"/></strong>!</span>
        <div class="nav-links">
            <a href="${pageContext.request.contextPath}/products">Catalog</a> |
            <a href="${pageContext.request.contextPath}/orders">My Orders</a> |
            <a href="${pageContext.request.contextPath}/profile">Profile</a> |
            <a href="${pageContext.request.contextPath}/logout" class="logout-link">Sign Out</a>
        </div>
    </div>

    <div class="main-content">
        <h2>Products Directory</h2>

        <c:if test="${not empty error}">
            <div class="error-box"><c:out value="${error}"/></div>
        </c:if>

        <!-- Admin Management Section -->
        <c:if test="${sessionScope.user.role eq 'ADMIN'}">
            <div class="admin-panel">
                <h3>Admin Panel: Add Product Asset</h3>
                <form action="${pageContext.request.contextPath}/products?action=add" method="post" class="inline-form">
                    <input type="text" name="name" placeholder="Item Name" required>
                    <input type="text" name="description" placeholder="Description">
                    <input type="number" name="price" step="0.01" placeholder="Price" required min="0">
                    <input type="number" name="quantity" placeholder="Qty" required min="0">
                    <button type="submit" class="btn btn-sm">Add Product</button>
                </form>
            </div>
        </c:if>

        <!-- Products Table Layout -->
        <table class="data-table">
            <thead>
                <tr>
                    <th>Name</th>
                    <th>Description</th>
                    <th>Price</th>
                    <th>Available Stock</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="product" items="${products}">
                    <tr>
                        <td><c:out value="${product.name}"/></td>
                        <td><c:out value="${product.description}"/></td>
                        <td>$<c:out value="${product.price}"/></td>
                        <td><c:out value="${product.quantity}"/> units</td>
                        <td>
                            <!-- Order Form for standard Users -->
                            <form action="${pageContext.request.contextPath}/orders?action=create" method="post" class="inline-form">
                                <input type="hidden" name="productId" value="${product.id}">
                                <input type="number" name="quantity" value="1" min="1" max="${product.quantity}" class="qty-input" required>
                                <button type="submit" class="btn btn-sm" ${product.quantity le 0 ? 'disabled' : ''}>Buy</button>
                            </form>

                            <!-- Delete Form for Admins only -->
                            <c:if test="${sessionScope.user.role eq 'ADMIN'}">
                                <form action="${pageContext.request.contextPath}/products?action=delete" method="post" class="inline-form" onsubmit="return confirm('Delete this artifact?');">
                                    <input type="hidden" name="id" value="${product.id}">
                                    <button type="submit" class="btn btn-danger btn-sm">Delete</button>
                                </form>
                            </c:if>
                        </td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>

        <!-- Pagination Controls -->
        <div class="pagination">
            <c:if test="${currentPage gt 1}">
                <a href="${pageContext.request.contextPath}/products?page=${currentPage - 1}">&laquo; Previous</a>
            </c:if>

            <span class="page-info">Page <c:out value="${currentPage}"/> of <c:out value="${totalPages}"/></span>

            <c:if test="${currentPage lt totalPages}">
                <a href="${pageContext.request.contextPath}/products?page=${currentPage + 1}">Next &raquo;</a>
            </c:if>
        </div>
    </div>
</body>
</html>