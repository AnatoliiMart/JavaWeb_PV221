<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%  String[] products = {
        "1 Product1 10.55",
        "2 Product2 20.75",
        "3 Product3 30.00",
        "4 Product4 13.58",
        "5 Product5 11.44",
        "6 Product6 65.20"
        };
%>
<html>
  <head>
    <title>Title</title>
  </head>
  <body>
  <h1>Java Web</h1>
  <h2>JSP</h2>
  <p>
    Java Server Pages - технологі формування динамічних сторінок сайтів
    за допомогою серверної активності
  </p>
  <h3>Вирази</h3>
  <p>
    Інструкції, що мають результат, причому мається на увазі, що цей результат
    стає частиною HTML
    &lt; %= вираз мовою Java %&gt; <br/>
    Наприклад, <code>&lt;%= 2 + 3 %&gt; <%= 2+3 %> </code>
  </p>
  <h3>Інструкції</h3>
  <p>
    Директиви, що не мають результату, або результат яких ігнорується <br/>
    &lt;% інструкція мовою Java %&gt; <br/>
    Наприклад, <code>&lt;% int x = 10; %&gt; <% int x = 10; %></code>
    <code>&lt;%= x %&gt; = <%= x %></code>
  </p>
  <h3>Умовна верстка</h3>
  <p>
    Умовне форматування HTML коду, причому негативне плече умовного оператора
    взагалі не потрапляє до HTML. <br/>
    <pre>
      &lt;% if(Умова) { %&gt;
        HTML-якщо-true
      &lt;% } else { %&gt;
        HTML-якщо-false
      &lt;% } %&gt;
    </pre>
    <br/>
    <% if( x % 2 == 0 ) { %>
    <b>x - парне число</b>
    <% } else { %>
    <i>х - непарне число</i>
    <% } %>

  <h3>Цикли</h3>
  <p>
    Повторне включення до HTML однакових (або майже) блоків верстки
  </p>
  <pre>
  &lt;% for (int i = 0; i < 10 ; i++) { %&gt;
    HTML, що повторюється, за потребиз виразом &lt;%= i %&gt;
    &lt;% } %&gt;
  </pre>
  <% for (int i = 0; i < 10 ; i++) { %>
  <span> <%= i %></span>&emsp;
  <%}%>
  <%String[] arr = {"Product 1", "Product 2", "Product 3", "Product 4", "Product 5"};%>
  <ul>
    <%for (String str : arr) {%>
         <li><%= str %></li>
    <% } %>
  </ul>
  <h3>Взаємодія з файлами HTML/JSP</h3>
  <p>

  </p>
  &lt;jsp:include page="fragment.jsp"/&gt;
  <jsp:include page="../../fragment.jsp"/>
      <pre>
          Browser             Tomcat
        HTTP - 8080         (Listen:8080)   CGI
        HTTP->8080      -->     Parse    [Req, Resp] -> python.exe, index.py
        <--------------- HTTP print <--------------- '<html>... Hello ...</html>'
      </pre>


  <h2>Product List</h2>
  <table>
    <thead>
    <tr>
      <th>#</th>
      <th>Description</th>
      <th>Price</th>
    </tr>
    </thead>
    <tbody>
    <%
      for (String product : products) {
        String[] parts = product.split(" ", 3);
        if (parts.length == 3) {
          String number = parts[0];
          String description = parts[1];
          String price = parts[2];
    %>
    <tr>
      <td><%= number %></td>
      <td><%= description %></td>
      <td><%= price %></td>
    </tr>
    <%
        }
      }
    %>
    </tbody>
  </table>
  <div style="margin-bottom: 40px"></div>
  </body>
</html>
