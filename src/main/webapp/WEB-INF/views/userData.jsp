<%@ page import="java.util.Map" %>
<%@ page import="org.apache.commons.fileupload.FileItem" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<h2 class="center">Fields</h2>
<table>
    <tr>
        <th>Field</th>
        <th>Value</th>
    </tr>
    <%
        Map<String, String> fields = (Map<String, String>) request.getAttribute("fields");
        if (fields != null) {
            for (Map.Entry<String, String> entry : fields.entrySet()) {
    %>
    <tr>
        <td><%= entry.getKey() %></td>
        <td><%= entry.getValue() %></td>
    </tr>
    <%
            }
        }
    %>
</table>
<h2 class="center">Files</h2>
<table>
    <tr>
        <th>File Name</th>
        <th>File Size</th>
    </tr>
    <%
        Map<String, FileItem> files = (Map<String, FileItem>) request.getAttribute("files");
        if (files != null) {
            for (Map.Entry<String, FileItem> entry : files.entrySet()) {
    %>
    <tr>
        <td><%= entry.getValue().getName() %></td>
        <td><%= entry.getValue().getSize() %>&nbsp;byte</td>
    </tr>
    <%
            }
        }
    %>
</table>
<div style="margin-bottom: 40px"></div>