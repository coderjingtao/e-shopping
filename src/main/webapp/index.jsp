<html>
<body>
<h1>Hello! Welcome to backend of the E-SHOP system.</h1>

<h4>My name is Jingtao Liu. One RESTful and standardized backend of a B2C e-commerce Web Service based on Java.</h4>
<h4>If you want the interfaces to test this system, please email me: <a href="liujingtao529@gmail.com">liujingtao529@gmail.com</a> </h4>

<h2 id="projectdisplay">Project Code</h2>

<h4>Project code: <a href="https://github.com/coderjingtao/e-shopping">https://github.com/coderjingtao/e-shopping</a></h4>

<h2 id="framework">Framework</h2>

<ul>
    <li>SpringMVC4.0</li>

    <li>Spring4.0</li>

    <li>Mybatis3.4</li>
</ul>

<h2 id="security">Security Technology</h2>

<ul>
    <li>User's transverse privileges protection</li>

    <li>User's vertical privileges protection</li>

    <li>MD5 and salt encryption of the password</li>

    <li>Administrator and User privileges control</li>
</ul>

<h2 id="technologypoints">Technology Points</h2>

<ul>
    <li>Guava Cache for a temporary token of the user</li>

    <li>Encapsulation of Server Response Object for Json</li>

    <li>Date Structure of Infinite category levels</li>

    <li>SpringMVC uploading file to an FTP server</li>

    <li>Use joda to handle time</li>

    <li>Use Mybatis-PageHelper to paginate</li>

    <li>Use BigDecimal to solve the precision problem of money</li>

    <li>Build and connect vsftpd as a product images server</li>
</ul>

<h2 id="deployment">Deployment</h2>

<ul>
    <li>OS


        <ul>
            <li>centos6.8 64bit </li></ul>
    </li>

    <li>Software


        <ul>
            <li>JDK1.7 / JDK1.8</li>

            <li>Tomcat 7.0.73</li>

            <li>Maven 3.0.5</li>

            <li>vsftpd</li>

            <li>Nginx 1.10.2</li>

            <li>Mysql</li>

            <li>Git 2.8.0</li></ul>
    </li>

    <li>Configuration


        <ul>
            <li>iptables : open ports for ftp or remote debug</li>

            <li>Linux Shell: for automatic deployment</li></ul>
    </li>
</ul>

<h2 id="functionality">Functionality</h2>

<ul>
    <li>User Module


        <ul>
            <li>Login / Logout</li>

            <li>Register</li>

            <li>Username validation</li>

            <li>Forget Password</li>

            <li>Submit password-protection question</li>

            <li>Reset password</li>

            <li>Get user detail info</li>

            <li>Update User info</li></ul>
    </li>

    <li>Category Module


        <ul>
            <li>Add Category</li>

            <li>Get Category</li>

            <li>Edit Category</li>

            <li>Recursive all categories</li></ul>
    </li>

    <li>Product Module


        <ul>
            <li>Search product</li>

            <li>Sort product</li>

            <li>product detail</li>

            <li>product list</li>

            <li>upload picture</li>

            <li>upload picture in richtext</li>

            <li>on-shelf / off-shelf</li>

            <li>Add product</li>

            <li>update product</li></ul>
    </li>

    <li>Cart Module


        <ul>
            <li>add products to cart</li>

            <li>update the number of cart items</li>

            <li>remove cart items from a cart</li>

            <li>select / unselect cart items </li>

            <li>query cart items</li></ul>
    </li>

    <li>Shipping Module


        <ul>
            <li>add address</li>

            <li>remove address</li>

            <li>update address</li>

            <li>address list with pagination</li>

            <li>adress detail</li></ul>
    </li>

    <li>Order Module


        <ul>
            <li>create order</li>

            <li>products info in one order</li>

            <li>order list</li>

            <li>order detail</li>

            <li>cancel order</li>

            <li>manage order


                <ul>
                    <li>search order</li>

                    <li>send out goods to deliver</li></ul>
            </li></ul>
    </li>

    <li>Payment Module


        <ul>
            <li>integrate 3rd-party payment demo into payment module</li>

            <li>pay via 3rd-party payment platform</li>

            <li>handle callback from payment platform</li>

            <li>query payment status</li></ul>
    </li>
</ul>

<h2 id="authorsnote">Author's note</h2>

<p>I will gradually improve this project to a distributed system with redis and load balancer, and eventually, I think it can handle large traffic as a comprehensive e-commerce platform. If you have a better idea or some suggestions, please contact me: liujingtao529@gmail.com</p>

<h2>Uploading Test</h2>
<hr>
<p>SpringMVC Upload file Test</p>
<form name="form1" action="/manage/product/upload.do" method="post" enctype="multipart/form-data">
    <input type="file" name="upload_file"/>
    <input type="submit" value="Upload">
</form>

<hr>
<p>RichText Image Upload Test</p>
<form name="form2" action="/manage/product/richtext_img_upload.do" method="post" enctype="multipart/form-data">
    <input type="file" name="upload_file"/>
    <input type="submit" value="RichText Upload">
</form>
</body>
</html>
