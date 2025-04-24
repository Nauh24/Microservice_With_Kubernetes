@echo off
echo Fixing job-service bean conflict...

echo Deleting conflicting file...
del "job-service\src\main\java\com\aad\microservice\job_service\service\JobCategoryServiceImpl.java"

echo Cleaning target directory...
cd job-service
call mvn clean
cd ..

echo Building job-service...
cd job-service
call mvn package -DskipTests
cd ..

echo Done! Now you can run job-service.
