#!/bin/sh
## Ondrej Kopr

sudo docker stop test-mssql
sudo docker rm test-mssql
sudo docker run --name=test-mssql -e 'ACCEPT_EULA=Y' -e 'SA_PASSWORD=bcvSuperHeslo-123' -p 1433:1433 -d microsoft/mssql-server-linux:latest


sudo docker cp import.sql test-mssql:/import.sql






sudo docker exec test-mssql /opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P bcvSuperHeslo-123 -d master -i /import.sql
