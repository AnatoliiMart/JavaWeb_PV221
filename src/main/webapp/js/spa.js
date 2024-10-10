const initialState = {
    auth: {
        token: null,
    },
    page: "home",
    shop: {
        categories: []
    }
}

function reducer(state = initialState, action) {
    switch (action.type) {
        case 'navigate' :
            window.location.hash = action.payload
            return {
                ...state,
                page: action.payload
            };
        case 'auth' :
            return {
                ...state,
                auth: {
                    ...state.auth,
                    token: action.payload
                }
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
        fetch("auth", {
            method: 'GET',
            headers: {
                'Authorization': 'Basic ' + credentials
            }
        }).then(r => r.json())
            .then(j => {
                console.log(j);
                if (j.status.phrase === "Ok") {
                    window.sessionStorage.setItem("token221", JSON.stringify(j.data));
                    fetch("spa", {
                        method: 'POST',
                        headers: {
                            'Authorization': 'Bearer ' + j.data.tokenId,
                        }
                    }).then(r => r.json())
                        .then(j => {
                            console.log(j);
                            window.sessionStorage.setItem("userName", JSON.stringify(j.data.name));
                            window.sessionStorage.setItem("userAvatar", JSON.stringify(j.data.avatar));
                            window.sessionStorage.setItem("userRole", JSON.stringify(j.data.role));
                            setAdmin(j.data.role.roleName === "Admin")
                        });
                    setAuth(true);
                } else {
                    setError(j.data);
                }
            })
    });
    const exitClick = React.useCallback(() => {
        window.sessionStorage.removeItem("token221");
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
    const checkToken = React.useCallback(() => {
        let token = window.sessionStorage.getItem("token221");
        // console.log( token, !!token, isAuth );
        if (token) {
            token = JSON.parse(token);
            if (new Date(token.exp) < new Date()) {
                exitClick();
            } else {
                if (!isAuth) {
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

    React.useEffect(() => {
        hashChanged();
        checkToken();
        window.addEventListener('hashchange', hashChanged);
        const interval = setInterval(checkToken, 1000);
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

    return <StateContext.Provider value={{state, dispatch}}>
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
                <b>Логін</b><input onChange={loginChange}/><br/>
                <b>Пароль</b><input type="password" onChange={passwordChange}/><br/>
                <button onClick={authClick}>Одержати токен</button>
                {error && <b>{error}</b>}
            </div>
        }{isAuth &&
        <div>
            <button onClick={resourceClick} className="btn light-blue">Ресурс</button>
            <button onClick={exitClick} className="btn indigo lighten-4">Вихід</button>
            <p>{resource}</p>
            <b onClick={() => navigate("home")}>Home</b>
            <b onClick={() => navigate("shop")}>Shop</b>
            {state.page === "home" && <Home/>}
            {state.page === "shop" && <Shop isAdmin={isAdmin}/>}
            {state.page.startsWith('category/') && <Category id={state.page.substring(9)}/>}
            {state.page.startsWith('product/') && <Product id={state.page.substring(8)}/>}
        </div>

    }
    </StateContext.Provider>;
}

function Category({id}) {
    const {state, dispatch} = React.useContext(StateContext)

    const [products, setProducts] = React.useState([]);
    const loadProducts = React.useCallback(() => {
        fetch("shop/product?categoryId=" + id)
            .then(r => r.json())
            .then(j => {
                setProducts(j.data);
            });
    });
    React.useEffect(() => {
        loadProducts();
    }, []);
    const addProduct = React.useCallback((e) => {

        e.preventDefault();
        const formData = new FormData(e.target);
        console.log(state.auth.token)
        fetch("shop/product", {

            method: 'POST',
            headers: {
                "Authorization": `Bearer ` + state.auth.token.tokenId,
            },
            body: formData
        }).then(r => r.json())
            .then(j => {
                if (j.status === "Ok") {
                    loadProducts();
                    document.getElementById("add-product-form").reset();
                } else {
                    alert(j.data);
                }
            });
    })
    return <div>
        Category: {id} <br/>
        <b onClick={() => dispatch({type: "navigate", payload: 'home'})}>To the shop</b>
        <br/>
        {products.map(p => <div key={p.id}
                                className="shop-product"
                                onClick={() => dispatch({type: 'navigate', payload: 'product/' + (p.slug || p.id)})}>
            <b>{p.name}</b>
            <picture>
                <img src={"file/" + p.imageUrl} alt="prod"/>
            </picture>
            <p><strong>{p.price}</strong> <small>{p.description}</small></p>
        </div>)}
        <br/>
        {state.auth.token &&
            <form id="add-product-form" onSubmit={addProduct} encType="multipart/form-data">
                <input name="product-name" placeholder="Name"/>
                <input name="product-slug" placeholder="Slug"/> <br/>
                <input name="product-price" type="number" step="0.01" placeholder="Price"/> <br/>
                Image: <input type="file" name="product-img"/>
                <textarea name="product-description" placeholder="Description"></textarea>
                <input type="hidden" name="product-category-id" value={id}/>
                <button type="submit">Add</button>
            </form>

        }
    </div>;
}

function Product({id}) {
    const [product, setProduct] = React.useState(null);
    React.useEffect(() => {
        fetch("shop/product?id=" + id)
            .then(r => r.json())
            .then(j => {
                if (j.status === "Ok") {
                    setProduct(j.data);
                } else {
                    console.error(j.data);
                    setProduct(null);
                }
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
        <CategoriesList/>
    </div>;
}

function CategoriesList() {
    const {state, dispatch} = React.useContext(StateContext);
    return <div>
        {state.shop.categories.map(c =>
            <div key={c.id}
                 className="shop-category"
                 onClick={() => dispatch({type: 'navigate', payload: 'category/' + c.id})}>
                <b>{c.name}</b>
                <picture>
                    <img src={"file/" + c.imageUrl} alt="grp"/>
                </picture>
                <p>{c.description}</p>
            </div>)}
    </div>;
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
        <CategoriesList/>
    </React.Fragment>
}

ReactDOM
    .createRoot(document.getElementById("spa-container"))
    .render(<Spa/>);