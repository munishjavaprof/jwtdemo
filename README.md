# jwtdemo

### start mongoDB

docker run -d -p 27017:27017 --name mongodb mongo

### API enpoints

1. POST http://localhost:8080/api/register
{
"username":"user",
"password":"password"
}


2. POST http://localhost:8080/api/login
{
"username":"user",
"password":"password"
}


3. PUT http://localhost:8080/api/password
   Header=> Authorization: Bearer <JWT Token>
{
"currentPassword":"password",
"newPassword":"newpassword"
}
