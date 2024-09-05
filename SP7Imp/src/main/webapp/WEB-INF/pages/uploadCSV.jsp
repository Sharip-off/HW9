<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Upload a file</title>
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.1/css/bootstrap.min.css">
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.1/js/bootstrap.min.js"></script>
</head>
<body>
<form class="form-horizontal" role="form" action="/uploadCSV" method="post" enctype="multipart/form-data" >

    <div class="form-group">
        <div class="col-sm-offset-3 col-sm-4">
            <h1>File input</h1>
            <input type="file" id="CSVfile" name="CSVfile" accept=".csv" required>
        </div>
    </div>
    <div class="form-group">
        <div class="col-sm-offset-3 col-sm-4">
            <button type="submit" class="btn btn-default" value="upload">Sign in</button>
        </div>
    </div>
</form>

</body>
</html>