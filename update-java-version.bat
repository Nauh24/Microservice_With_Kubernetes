@echo off
echo Updating Java version in all pom.xml files...

cd worker-service
powershell -Command "(Get-Content pom.xml) -replace '<java.version>21</java.version>', '<java.version>17</java.version>' | Set-Content pom.xml"
cd ..

cd job-schedule-service
powershell -Command "(Get-Content pom.xml) -replace '<java.version>21</java.version>', '<java.version>17</java.version>' | Set-Content pom.xml"
cd ..

cd register-service
powershell -Command "(Get-Content pom.xml) -replace '<java.version>21</java.version>', '<java.version>17</java.version>' | Set-Content pom.xml"
cd ..

cd worker-contract-service
powershell -Command "(Get-Content pom.xml) -replace '<java.version>21</java.version>', '<java.version>17</java.version>' | Set-Content pom.xml"
cd ..

cd customer-service
powershell -Command "(Get-Content pom.xml) -replace '<java.version>21</java.version>', '<java.version>17</java.version>' | Set-Content pom.xml"
cd ..

cd job-service
powershell -Command "(Get-Content pom.xml) -replace '<java.version>21</java.version>', '<java.version>17</java.version>' | Set-Content pom.xml"
cd ..

cd api-gateway
powershell -Command "(Get-Content pom.xml) -replace '<java.version>21</java.version>', '<java.version>17</java.version>' | Set-Content pom.xml"
cd ..

echo Java version updated in all pom.xml files!
