# e-shopping
One RESTful and standardized backend of a B2C e-commerce Web Service based on Java
SSM frameworks. I am now persistently building it for a standardized solution for small e-commerce enterprises from one-server solution to distributed system solution.
## Project display
www.365shop.ml
## Framework
- SpringMVC4.0
- Spring4.0
- Mybatis3.4

## Source package structure
- common: common components
- controller: HTTP request switcher
- dao: data access object
- pojo: database object
- service: business logical object
- util: utilities / tools
- vo: view object for display

## Security
- User's transverse privileges protection
- User's vertical privileges protection
- MD5 and salt encryption of the password
- Administrator and User privileges control

## Technology Points
- Guava Cache for a temporary token of the user
- Encapsulation of Server Response Object for Json
- Date Structure of Infinite category levels
- SpringMVC uploading file to an FTP server
- Use joda to handle time
- Use Mybatis-PageHelper to paginate
- Use BigDecimal to solve the precision problem of money
- Build and connect vsftpd as a product images server

## Deployment
- OS
    - centos6.8 64bit
- Software
    - JDK1.7 / JDK1.8
    - Tomcat 7.0.73
    - Maven 3.0.5
    - vsftpd
    - Nginx 1.10.2
    - Mysql
    - Git 2.8.0
- Configuration
    - iptables: open ports for FTP or remote debug
    - Linux Shell: for automatic deployment

## Functionality
- User Module
    - Login / Logout
    - Register
    - Username validation
    - Forget Password
    - Submit password-protection question
    - Reset password
    - Get user detail info
    - Update User info
- Category Module
    - Add Category
    - Get Category
    - Edit Category
    - Recursive all categories
- Product Module
    - Search product
    - Sort product
    - product detail
    - product list
    - upload picture
    - upload picture in richtext
    - on-shelf / off-shelf
    - Add product
    - update product
- Cart Module
    - add products to cart
    - update the number of cart items
    - remove cart items from a cart
    - select / unselect cart items
    - query cart items
- Shipping Module
    - add address
    - remove address
    - update address
    - address list with pagination
    - adress detail
- Order Module
    - create order
    - products info in one order
    - order list
    - order detail
    - cancel order
    - manage order
        - search order
        - send out goods to deliver
- Payment Module
    - integrate 3rd-party payment demo into payment module
    - pay via 3rd-party payment platform
    - handle callback from payment platform
    - query payment status

## Author's note
I will gradually improve this project to a distributed system with redis and load balancer, and eventually, I think it can handle large traffic as a comprehensive e-commerce platform. If you have a better idea or some suggestions, please contact me: liujingtao529@gmail.com
