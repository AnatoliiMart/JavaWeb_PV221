function Spa() {

    const [login, setLogin] = React.useState("")
    const loginChange = React.useCallback((e) => setLogin(e.target.value));
    const [password, setPassword] = React.useState("")
    const passwordChange = React.useCallback((e) => setPassword(e.target.value));


    const authClick = React.useCallback(() => {
        const credentials = btoa(login + ":" + password);

        console.log(credentials)
         fetch("auth", {
             method: "GET",
             headers: {
                 'Authorization': `Basic ${credentials}`
             }
         }).then(r => r.json()).then(console.log)
    });


    return <React.Fragment>
        <h1>SPA</h1>
        <div>
            <b>Login</b><input onChange={loginChange} type="text"/>
            <b>Password</b><input onChange={passwordChange} type="password"/>
            <button onClick={authClick}>Get token</button>
        </div>
    </React.Fragment>
}

ReactDOM.createRoot(document.getElementById("spa-container")).render(
    <Spa/>
)