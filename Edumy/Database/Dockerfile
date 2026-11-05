FROM mcr.microsoft.com/mssql/server:2022-latest

USER root

RUN mkdir -p /backup /var/opt/mssql && chown -R mssql:root /var/opt/mssql

COPY EduMyDb.bak /backup/EduMyDb.bak
COPY entrypoint.sh /usr/local/bin/entrypoint.sh

RUN sed -i 's/\r$//' /usr/local/bin/entrypoint.sh && chmod +x /usr/local/bin/entrypoint.sh

USER mssql

ENTRYPOINT ["/usr/local/bin/entrypoint.sh"]
