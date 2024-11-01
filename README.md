<h2>Docker instructions:</h2>

<h3>Pull images:</h3>

<h4>

1) docker pull postgres
2) docker pull mongo
3) docker pull rabbitmq:4.0-management

</h4>

<h3>Create postgres container:</h3>

<h4>

1) docker run -d -e POSTGRES_USER=admin -e POSTGRES_PASSWORD=password
   --name tasker_postgres -p 5432:5432
   -v C:\Users\Elias\tasker-data\postgres_data:/var/lib/postgresql/data postgres
2) docker exec -it tasker_postgres bash
3) psql -h localhost -p 5432 -U admin -W
4) create database tasker;
5) \c tasker;
6) create user tasker_user with encrypted password 'tasker_pass';
7) grant all on schema public to tasker_user;

</h4>

<h3>Create mongo container:</h3>

<h4>

1) docker run -d -e MONGO_INITDB_ROOT_USERNAME=admin -e MONGO_INITDB_ROOT_PASSWORD=password
   --name tasker_mongo -p 27017:27017
   -v C:\Users\Elias\tasker-data\mongo_data:/data/db mongo
2) docker exec -it tasker_mongo bash
3) mongosh -u admin -p password
4) use tasker;
5) db.createUser({user: "tasker_user", pwd: "tasker_pass", roles: [{role: "readWrite", db: "tasker"}]});

</h4>

<h3>Create rabbitmq container:</h3>

<h4>

1) docker run -d -e RABBITMQ_DEFAULT_USER=rabbitmq_user -e RABBITMQ_DEFAULT_PASS=rabbitmq_pass
   --name tasker_rabbitmq -p 5672:5672 -p 15672:15672 -p 61613:61613 rabbitmq:4.0-management
2) docker exec -it tasker_rabbitmq bash
3) rabbitmq-plugins enable rabbitmq_stomp

</h4>