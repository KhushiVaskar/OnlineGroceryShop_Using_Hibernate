let currentUser = null;

document.addEventListener('DOMContentLoaded', async () => {
    await checkAuthStatus();
    const path = window.location.pathname;
    if (path.includes('products.html')) loadProducts();
    if (path.includes('cart.html')) loadCart();
    if (path.includes('orders.html')) loadOrders();
	
});

async function checkAuthStatus() {
    try {
        const response = await fetch('/api/auth/status');
        if (response.ok) {
            currentUser = await response.json();
            updateNav();
        }
    } catch (error) {
        console.error('Error checking auth status:', error);
    }
}

function updateNav() {
    const nav = document.querySelector('nav');
    if (currentUser) {
        let adminLinks = '';
        if (currentUser.role === 'admin') {
            adminLinks = `
                <li><a href="/add-product.html">Add Product</a></li>
                <li><a href="/manage-products.html">Manage Products</a></li>
            `;
        }

        nav.innerHTML = `
            <ul class="nav-list">
                <li><a href="/index.html">Home</a></li>
                <li><a href="/products.html">Products</a></li>
                <li><a href="/cart.html">Cart</a></li>
                <li><a href="/orders.html">Orders</a></li>
                ${adminLinks}
                <li><a href="#" onclick="logout()">Logout (${currentUser.username})</a></li>
            </ul>
        `;
    } else {
        nav.innerHTML = `
            <ul class="nav-list">
                <li><a href="/index.html">Home</a></li>
                <li><a href="/products.html">Products</a></li>
                <li><a href="/cart.html">Cart</a></li>
                <li><a href="/login.html">Login</a></li>
                <li><a href="/register.html">Register</a></li>
            </ul>
        `;
    }
}


async function login(event) {
    event.preventDefault();
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    try {
        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, passwordHash: password })
        });
        if (response.ok) {
            currentUser = await response.json();
            window.location.href = '/index.html';
        } else {
            alert('Login failed');
        }
    } catch (error) {
        console.error('Login error:', error);
    }
}

async function register(event) {
    event.preventDefault();
    const username = document.getElementById('username').value;
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;
    const role = document.getElementById('role').value;
    try {
        const response = await fetch('/api/auth/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, email, passwordHash: password, role })
        });
        if (response.ok) {
            window.location.href = '/login.html';
        } else {
            alert('Registration failed');
        }
    } catch (error) {
        console.error('Registration error:', error);
    }
}

async function logout() {
    await fetch('/api/auth/logout');
    currentUser = null;
    window.location.href = '/index.html';
}

async function loadProducts() {
    try {
        const response = await fetch('/api/products');
        const products = await response.json();
        displayProducts(products);
    } catch (error) {
        console.error('Error loading products:', error);
    }
}

function displayProducts(products) {
    const productGrid = document.getElementById('productGrid');
    productGrid.innerHTML = '';

    products.forEach(product => {
        const productCard = document.createElement('div');
        productCard.className = 'product-card';
        productCard.innerHTML = `
            <h3>${product.productname}</h3>
            <p class="price">$${product.price}</p>
            <p>Stock: ${product.stock}</p>
            <p>Quantity: ${product.quantity}</p>
            <button onclick="addToCart(${product.productId})">Add to Cart</button>
        `;
        if (currentUser && currentUser.role === 'admin') {
            productCard.innerHTML += `
                <button onclick="editProduct(${product.productId}, '${product.productname}', ${product.price}, ${product.stock}, ${product.quantity})">Edit</button>
                <button onclick="deleteProduct(${product.productId})">Delete</button>
            `;
        }
        productGrid.appendChild(productCard);
    });
}

function editProduct(id, name, price, stock, quantity) {
    const formHtml = `
        <h3>Edit Product</h3>
        <form onsubmit="submitProductUpdate(event, ${id})">
            <input type="text" id="editName" value="${name}" required />
            <input type="number" id="editPrice" value="${price}" step="0.01" required />
            <input type="number" id="editStock" value="${stock}" required />
            <input type="number" id="editQuantity" value="${quantity}" required />
            <button type="submit">Update</button>
            <button type="button" onclick="document.getElementById('editForm').innerHTML = ''">Cancel</button>
        </form>
    `;
    document.getElementById('editForm').innerHTML = formHtml;
}

async function submitProductUpdate(event, id) {
    event.preventDefault();

    const updatedProduct = {
        productname: document.getElementById('editName').value,
        price: parseFloat(document.getElementById('editPrice').value),
        stock: parseInt(document.getElementById('editStock').value),
        quantity: parseInt(document.getElementById('editQuantity').value)
    };

    try {
        const response = await fetch(`/api/products/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(updatedProduct)
        });

        if (response.ok) {
            alert('Product updated!');
            loadProducts();
            document.getElementById('editForm').innerHTML = '';
        } else {
            alert('Failed to update product');
        }
    } catch (error) {
        console.error('Update error:', error);
    }
}

async function deleteProduct(id) {
    if (!confirm('Are you sure you want to delete this product?')) return;

    try {
        const response = await fetch(`/api/products/${id}`, { method: 'DELETE' });
        if (response.ok) {
            alert('Product deleted.');
            loadProducts();
        } else {
            alert('Failed to delete product.');
        }
    } catch (error) {
        console.error('Delete error:', error);
    }
}

async function addToCart(productId) {
    if (!currentUser) {
        window.location.href = '/login.html';
        return;
    }

    try {
        const response = await fetch('/api/cart', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ productId })
        });
        if (response.ok) {
            alert('Added to cart!');
        }
    } catch (error) {
        console.error('Error adding to cart:', error);
    }
}

async function loadCart() {
    if (!currentUser) {
        window.location.href = '/login.html';
        return;
    }

    try {
        const response = await fetch('/api/cart');
        const cartItems = await response.json();
        displayCart(cartItems);
    } catch (error) {
        console.error('Error loading cart:', error);
    }
}

function displayCart(cartItems) {
    const cartGrid = document.getElementById('cartGrid');
    cartGrid.innerHTML = '';

    cartItems.forEach(item => {
        const cartItem = document.createElement('div');
        cartItem.className = 'cart-item';
        cartItem.innerHTML = `
            <h3>${item.product.productname}</h3>
            <p>Price: $${item.product.price}</p>
            <p>Quantity: ${item.product.quantity}</p>
            <p>Total: $${(item.product.price * item.product.quantity).toFixed(2)}</p>
        `;
        cartGrid.appendChild(cartItem);
    });

    if (cartItems.length > 0) {
        const checkoutBtn = document.createElement('button');
        checkoutBtn.textContent = 'Checkout';
        checkoutBtn.onclick = checkout;
        cartGrid.appendChild(checkoutBtn);
    }
}

async function checkout() {
    try {
        const response = await fetch('/api/orders', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' }
        });

        if (response.ok) {
            alert('Order placed successfully!');
            window.location.href = '/orders.html';
        }
    } catch (error) {
        console.error('Checkout error:', error);
    }
}

async function loadOrders() {
    if (!currentUser) {
        window.location.href = '/login.html';
        return;
    }

    try {
        const response = await fetch('/api/orders');
        const orders = await response.json();
        displayOrders(orders);
    } catch (error) {
        console.error('Error loading orders:', error);
    }
}

function displayOrders(orders) {
    const orderGrid = document.getElementById('orderGrid');
    orderGrid.innerHTML = '';

    orders.forEach(order => {
        const orderCard = document.createElement('div');
        orderCard.className = 'order-card';
        let itemsHtml = order.orderItems.map(item => `
            <p>${item.product.productname} - ${item.product.quantity} x $${item.unitPrice}</p>
        `).join('');

        orderCard.innerHTML = `
            <h3>Order #${order.orderId}</h3>
            <p>Date: ${new Date(order.orderDate).toLocaleString()}</p>
            ${itemsHtml}
            <p>Total: $${order.totalAmount}</p>
            <p>Status: ${order.status}</p>
        `;
        orderGrid.appendChild(orderCard);
    });
}

async function addProduct(event) {
    event.preventDefault();
    if (!currentUser || currentUser.role !== 'admin') {
        alert('Access denied! Only admins can add products.');
        return;
    }

    const product = {
        productname: document.getElementById('product').value,
        price: parseFloat(document.getElementById('price').value),
        stock: parseInt(document.getElementById('stock').value),
        quantity: parseInt(document.getElementById('quantity').value)
    };

    try {
        const response = await fetch('/api/products', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(product)
        });

        if (response.ok) {
            alert('Product added successfully!');
            window.location.href = '/products.html';
        } else {
            const error = await response.json();
            alert('Failed to add product: ' + error.message);
        }
    } catch (error) {
        console.error('Error adding product:', error);
        alert('Error adding product');
    }
}
