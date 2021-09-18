# mercedes-benz-front-end-service

SpringBoot application that runs on port 8081 exposes HTTP endpoint (http://localhost:8081/api/v1/benz/customer) for three operations: 
* GET - read a customer's data (accepts <b>username</b> as an query parameter)
  * eg. http://localhost:8081/api/v1/benz/customer?username=shivank09
* POST - create a customer (accepts <b>fileType</b> as query parameter and customer details as a <b>JSON request body</b>)
* PUT - create/update an existing customer's data (accepts <b>fileType</b> as query parameter and customer details as a <b>JSON request body</b>)
  * eg. http://localhost:8081/api/v1/benz/customer?fileType=XML  
    and request body such as: {"username": "shivank09","firstname": "shivank","lastname": "mishra","age": 26}
    
The application encrypts (using <b>AES symmetric encryption</b>) and transforms to <b>protobuf format</b> (the schema for which is included in the resources folder for reference) and sends it over <b>Kafka</b> to back-end-service (<b>not HTTP/HTTPS</b>, as required). Six JUnit test cases have been included which can be run with Spring Boot's Embedded Kafka for use. For your verification, you can run the back-end-service in a separate terminal to see its behaviour of decrypting and storing messages.
