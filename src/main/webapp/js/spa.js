const env = {
    apiHost: "http://localhost:8080/Java221/"
}

function request(url, params) {
    if (!url.startsWith(env.apiHost)) {
        url = env.apiHost + url;
    }
    /*    if (typeof params !== "undefined"){
            params = {
                ...params,
                headers: {
                    Authorization: `Bearer` + stat
                }

            };
        }else if(typeof params.headers.Authorization !== "undefined") {}*/
    return new Promise((resolve, reject) => {

        fetch(url, params)
            .then(r => {
                if (r.headers.get("Content-Type").startsWith("application/json") && r.ok) {
                    return r.json();
                }
            })
            .then(j => {
                if (j.status.isSuccessful) {
                    resolve(j.data)
                } else {
                    reject(j.data)
                }
            })
    })

}

const initialState = {
    auth: {
        token: null,
    },
    page: "home",
    shop: {
        categories: []
    },
    cart: [],
}

function reducer(state = initialState, action) {
    switch (action.type) {
        case 'auth' :
            return {
                ...state,
                auth: {
                    ...state.auth,
                    token: action.payload
                }
            };
        case 'cart' :
            return {
                ...state,
                cart: action.payload
            };
        case 'navigate' :
            window.location.hash = action.payload
            return {
                ...state,
                page: action.payload
            };
        case "setCategory":
            return {
                ...state,
                shop: {
                    ...state.shop,
                    categories: action.payload
                }
            }
        default:
            throw new Error(`Unknown action type: ${action.type}`);
    }

}

const StateContext = React.createContext(null);

function Spa() {
    const [state, dispatch] = React.useReducer(reducer, initialState);
    const [login, setLogin] = React.useState("");
    const [password, setPassword] = React.useState("");
    const [error, setError] = React.useState(false);
    const [isAuth, setAuth] = React.useState(false);
    const [isAdmin, setAdmin] = React.useState(false);
    const [resource, setResource] = React.useState("");
    const loginChange = React.useCallback((e) => setLogin(e.target.value));
    const passwordChange = React.useCallback((e) => setPassword(e.target.value));
    const authClick = React.useCallback(() => {

        const credentials = btoa(login + ":" + password);
        fetch(`${env.apiHost}auth`, {
            method: 'GET',
            headers: {
                'Authorization': 'Basic ' + credentials
            }
        }).then(r => r.json())
            .then(j => {
                console.log(j);
                if (j.status.isSuccessful) {
                    window.sessionStorage.setItem("token221", JSON.stringify(j.data));
                    fetch("spa", {
                        method: 'POST',
                        headers: {
                            'Authorization': 'Bearer ' + j.data.tokenId,
                        }
                    }).then(r => r.json())
                        .then(j => {

                            window.sessionStorage.setItem("userName", JSON.stringify(j.data.name));
                            window.sessionStorage.setItem("userAvatar", JSON.stringify(j.data.avatar));
                            window.sessionStorage.setItem("userRole", JSON.stringify(j.data.role));
                            setAdmin(j.data.role.roleName === "Admin")
                            window.location.reload();
                        });
                    setAuth(true);
                } else {
                    setError(j.data);
                }
            })
    });
    const exitClick = React.useCallback(() => {
        window.sessionStorage.removeItem("token221");
        window.sessionStorage.removeItem("userRole");
        setAuth(false);
    });
    const resourceClick = React.useCallback(() => {
        const token = window.sessionStorage.getItem("token221");
        if (!token) {
            alert("Запит ресурсу в неавторизованому режимі");
            return;
        }
        fetch("spa", {
            method: 'POST',
            headers: {
                'Authorization': 'Bearer ' + JSON.parse(token).tokenId,
            }
        }).then(r => r.json()).then(j => {
            setResource(JSON.stringify(j));
        });

    });
    const checkToken = React.useCallback((forceAuth) => {
        let token = window.sessionStorage.getItem("token221");
        // console.log( token, !!token, isAuth );
        if (token) {
            token = JSON.parse(token);
            if (new Date(token.exp) < new Date()) {
                exitClick();
            } else {
                if (forceAuth) {
                    setAuth(true);
                    dispatch({type: 'auth', payload: token})
                }
            }
        } else {
            setAuth(false);
        }
    });
    const hashChanged = React.useCallback(() => {
        const hash = window.location.hash;
        if (hash.length > 1) {
            dispatch({type: 'navigate', payload: hash.substring(1)});
        }
    });
    const loadCart = React.useCallback(() => {
        request(`shop/cart`, {
            headers: {
                Authorization: `Bearer ` + state.auth.token.tokenId,
            }
        })
            .then(data => {
                dispatch({type: "cart", payload: data})
            })
            .catch(console.error);
    })

    React.useEffect(() => {
        if (state.auth.token !== null) {
            loadCart();
        } else {
            dispatch({type: 'cart', payload: []})
        }

    }, [state.auth])
    React.useEffect(() => {
        hashChanged();
        checkToken(true);

        window.addEventListener('hashchange', hashChanged);
        const interval = setInterval(checkToken(false), 1000);
        if (state.shop.categories.length === 0) {
            fetch("shop/category")
                .then(r => r.json())
                .then(j => dispatch({type: 'setCategory', payload: j.data}));
        }

        return () => {
            clearInterval(interval);
            window.removeEventListener('hashchange', hashChanged);
        }
    }, []);
    const navigate = React.useCallback((route) => {
        const action = {type: "navigate", payload: route};
        dispatch(action);
    })

    return <StateContext.Provider value={{state, dispatch, loadCart}}>
        <div className="spa-header">
            <h1>SPA</h1>
            {isAuth &&
                <div className="spa-auth">
                    <img src={`file/${window.sessionStorage.getItem("userAvatar").replace(/"/g, '')}`}
                         alt="avatar"
                         title=""
                         className="right nav-avatar"/>
                    <h3 className="right">{window.sessionStorage.getItem("userName").replace(/"/g, '')}</h3>
                </div>
            }
        </div>
        {!isAuth &&
            <div>
                <b>Логін</b><input name="login_input" onChange={loginChange}/><br/>
                <b>Пароль</b><input type="password" onChange={passwordChange}/><br/>
                <button onClick={authClick}>Одержати токен</button>
                {error && <b>{error}</b>}
            </div>
        }{isAuth &&
        <div>
            <button onClick={resourceClick} className="btn light-blue">Ресурс</button>
            <button onClick={exitClick} className="btn indigo lighten-4">Вихід</button>
            <p>{resource}</p>
            <b onClick={() => navigate("home")}>Home</b>&emsp;
            <b onClick={() => navigate("shop")}>Shop</b>&emsp;
            <b onClick={() => navigate("cart")}>Cart({state.cart.reduce((cnt, c) => cnt + c.quantity, 0)}/{state.cart.reduce((price, c) => price + c.quantity * c.product.price, 0)} UAH)</b>&emsp;
            {state.page === "home" && <Home/>}
            {state.page === "shop" && <Shop isAdmin={isAdmin}/>}
            {state.page === "cart" && <Cart/>}
            {state.page.startsWith('category/') && <Category id={state.page.substring(9)} isAdmin={isAdmin}/>}
            {state.page.startsWith('product/') && <Product id={state.page.substring(8)}/>}
        </div>

    }
    </StateContext.Provider>;
}

function Category({id, isAdmin}) {
    const {state, dispatch, loadCart} = React.useContext(StateContext)
    const role = JSON.parse(window.sessionStorage.getItem("userRole"));
    const [products, setProducts] = React.useState([]);
    const loadProducts = React.useCallback(() => {
        request(`shop/product?categoryId=${id}`)
            .then(setProducts)
            .catch(err => {
                console.log(err);
                setProducts([]);
            })
        console.log(role)
    });
    React.useEffect(() => {
        loadProducts();
    }, [id]);
    const addProduct = React.useCallback((e) => {

        e.preventDefault();
        const formData = new FormData(e.target);
        console.log(state.auth.token)
        fetch(`${env.apiHost}shop/product`, {

            method: 'POST',
            headers: {
                "Authorization": `Bearer ` + state.auth.token.tokenId,
            },
            body: formData
        }).then(r => r.json())
            .then(j => {
                if (j.status.isSuccessful) {
                    loadProducts();
                    document.getElementById("add-product-form").reset();
                } else {
                    alert(j.data);
                }
            });
    })
    const addToCart = React.useCallback((id) => {
        console.log(id);
        const userId = state.auth.token.userId;
        //TODO: check presence
        request(`shop/cart`, {
            method: 'POST',
            headers: {
                Authorization: 'Bearer ' + state.auth.token.tokenId,
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                userId,
                productId: id,
            })
        })
            .then(() => loadCart())
            .catch(console.log)
    })
    return <div>
        {products && <div>
            Category: {id}<br/>
            <b onClick={() => dispatch({type: 'navigate', payload: 'home'})}>До Крамниці</b>
            <br/>
            {products.map(p => <div key={p.id}
                                    className="shop-product"
                                    onClick={() => dispatch({
                                        type: 'navigate',
                                        payload: 'product/' + (p.slug || p.id)
                                    })}>
                <b>{p.name}</b>
                <picture>
                    <img src={"file/" + p.imageUrl} alt="prod"/>
                </picture>
                <div className="row">
                    <div className="col s9">
                        <strong>{p.price}₴</strong>&emsp;
                        <small>{p.description}</small>
                    </div>
                    <div className="col s3">
                        <a
                            className="btn-floating cart-fab waves-effect waves-light red"
                            onClick={(e) => {
                                e.stopPropagation()
                                addToCart(p.id)
                            }}>
                            <i className="material-icons">shopping_bag</i>
                        </a>
                    </div>
                </div>
            </div>)}
            <br/>
            {(state.auth.token && !(role.roleName === "Admin" && role.canCreate === 1)) &&
                <form id="add-product-form" onSubmit={addProduct} encType="multipart/form-data">
                    <hr/>
                    <input name="product-name" placeholder="Назва"/>
                    <input name="product-slug" placeholder="Slug"/><br/>
                    <input name="product-price" type="number" step="0.01" placeholder="Ціна"/><br/>
                    Картинка: <input type="file" name="product-img"/><br/>
                    <textarea name="product-description" placeholder="Опис"></textarea><br/>
                    <input type="hidden" name="product-category-id" value={id}/>
                    <button type="submit">Додати</button>
                </form>}
        </div>}
        {!products && <div>
            <h2>Група товарів {id} не існує</h2>
        </div>}
    </div>;
}

function Product({id}) {
    const [product, setProduct] = React.useState(null);
    React.useEffect(() => {
        // fetch(`${env.apiHost}shop/product?id=${id}`)
        //     .then(r => r.json())
        //     .then(j => {
        //         if (j.status.isSuccessful) {
        //             setProduct(j.data);
        //         } else {
        //             console.error(j.data);
        //             setProduct(null);
        //         }
        //     });
        request(`shop/product?id=${id}`)
            .then(setProduct)
            .catch(err => {
                console.log(err)
                setProduct(null);
            });
    }, [id]);
    return <div>
        <h1>Сторінка товару</h1>
        {product && <div>
            <p>{product.name}</p>
        </div>}

        {!product && <div>
            <p>Шукаємо...</p>
        </div>}
        <hr/>
        <CategoriesList mode="table"/>
    </div>;
}

function CategoriesList({mode}) {
    const {state, dispatch} = React.useContext(StateContext);
    switch (mode) {
        case "table":
            return (
                <div>
                    {state.shop.categories.map(c =>
                        <div key={c.id}
                             className="shop-category"
                             onClick={() => dispatch({type: 'navigate', payload: 'category/' + (c.slug || c.id)})}>
                            <b>{c.name}</b>
                            <picture>
                                <img src={"file/" + c.imageUrl} alt="grp"/>
                            </picture>
                            <p>{c.description}</p>
                        </div>)}
                </div>
            )
        case "ribbon":
            return (
                <div className="ribbon">
                    {state.shop.categories.map(c =>
                        <div key={c.id}
                             className="shop-category-ribbon"
                             onClick={() => dispatch({type: 'navigate', payload: 'category/' + (c.slug || c.id)})}>
                            <picture>
                                <img src={"file/" + c.imageUrl} alt="grp"/>
                            </picture>
                            <b>{c.name}</b>
                        </div>)}
                </div>
            )
    }
}

function Shop({isAdmin}) {
    const addCategory = React.useCallback((e) => {
        e.preventDefault();

        const formData = new FormData(e.target);
        fetch("shop/category", {
            method: 'POST',
            body: formData
        }).then(r => r.json()).then(console.log)
        console.log(e);
    });
    return <React.Fragment>
        <h2>Shop</h2>
        {!isAdmin &&
            <form onSubmit={addCategory} encType="multipart/form-data">
                <input name="category-name" placeholder="Category"/>
                <input name="category-slug" placeholder="Slug"/>
                Image: <input type="file" name="category-img"/>
                <textarea name="category-description" placeholder="Description"></textarea>
                <button type="submit">Add</button>
            </form>
        }

    </React.Fragment>
}

function Home() {
    const {state, dispatch} = React.useContext(StateContext);

    React.useEffect(() => {
        if (state.shop.categories.length === 0) {
            fetch("shop/category")
                .then(r => r.json())
                .then(j => dispatch({type: "setCategory", payload: j.data}));
        }
    }, [])
    return <React.Fragment>
        <h2>Home</h2>
        <b onClick={() => dispatch({type: "navigate", payload: "shop"})}>To the Admin</b>
        <CategoriesList mode="table"/>
    </React.Fragment>
}

function Cart() {
    const {state, loadCart} = React.useContext(StateContext);
    const changeQuantity = React.useCallback((cartItem, action) => {
        switch (action) {
            case'inc':
                updateCart(cartItem, 1);
                break;
            case 'dec':
                updateCart(cartItem, -1);
                break;
            case 'del':
                updateCart(cartItem, -cartItem.quantity);
                break;
        }
    })

    const updateCart = React.useCallback((cartItem, delta) => {
        if (Number(cartItem.quantity) + Number(delta) === 0 && !confirm(`Remove ${cartItem.product.name} from cart ?`)) {
            return;
        }
        request(`shop/cart`, {
            method: 'PUT',
            headers: {
                Authorization: 'Bearer ' + state.auth.token.tokenId,
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                cartId: cartItem.cartId,
                productId: cartItem.productId,
                delta: delta,
            })
        })
            .then(() => loadCart())
            .catch(console.log)
    })

    const buyClick = React.useCallback(() => {
        if (confirm(`Confirm ${state.cart.reduce((prev, c) => prev + c.quantity * c.product.price)} UAH pay ?`)) {
            request(`shop/cart?cart-id=${state.cart[0].cartId}`, {
                method: 'PATCH',
                headers: {
                    Authorization: 'Bearer ' + state.auth.token.tokenId,
                    'Content-Type': 'application/json',
                },
            })
                .then(() => loadCart())
                .catch(console.log)
        }
    })
    return state.cart.length ? <div className="cart-container">
        <div className="cart-header">
            Cart
        </div>
        <div className="cart-items">
            {state.cart.map(c => (
                <div key={c.productId} className="cart-item">
                    <img src={`file/${c.product.imageUrl}`} alt="product"/>
                    <div className="cart-item-info">
                        <div className="cart-item-name">{c.product.name}</div>
                        <div className="cart-item-price">{c.product.price}</div>
                        <div className="cart-item-quantity">
                            <button className="cart-item-decrease" onClick={() => changeQuantity(c, 'dec')}>-</button>
                            <span className="quantity">{c.quantity}</span>
                            <button className="cart-item-increase" onClick={() => changeQuantity(c, 'inc')}>+</button>
                        </div>
                    </div>
                    <button className="cart-item-remove" onClick={() => changeQuantity(c, 'del')}>×</button>
                </div>
            ))}
        </div>
        <div className="cart-footer">
            <span
                className="cart-total">Total: {state.cart.reduce((cnt, c) => cnt + c.quantity, 0)} products / {state.cart.reduce((price, c) => price + c.quantity * c.product.price, 0)} UAH</span>
            <button className="cart-checkout-button" onClick={() => buyClick()}>Make order</button>
        </div>
    </div> : <div className="cart-header">There are no products in cart</div>

}

ReactDOM
    .createRoot(document.getElementById("spa-container"))
    .render(<Spa/>);