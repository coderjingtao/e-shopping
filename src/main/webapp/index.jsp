<html>
<body>
<h2>Hello! Welcome to the E-SHOP system.</h2>

<p>SpringMVC Upload file Test</p>
<form name="form1" action="/manage/product/upload.do" method="post" enctype="multipart/form-data">
    <input type="file" name="upload_file"/>
    <input type="submit" value="Upload">
</form>

<p>RichText Image Upload Test</p>
<form name="form2" action="/manage/product/richtext_img_upload.do" method="post" enctype="multipart/form-data">
    <input type="file" name="upload_file"/>
    <input type="submit" value="RichText Upload">
</form>
</body>
</html>
