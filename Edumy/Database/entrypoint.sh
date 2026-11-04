#!/bin/bash
set -e

# Run init in the background once SQL Server is ready
init_database() {
    echo "=========================================================="
    echo ">>> Waiting for SQL Server engine to be ready..."
    echo "=========================================================="

    SQLCMD=""
    if [ -f /opt/mssql-tools18/bin/sqlcmd ]; then
        SQLCMD="/opt/mssql-tools18/bin/sqlcmd -C"
    elif [ -f /opt/mssql-tools/bin/sqlcmd ]; then
        SQLCMD="/opt/mssql-tools/bin/sqlcmd"
    else
        SQLCMD="sqlcmd -C"
    fi

    # Wait up to 90 seconds for SQL Server to accept connections
    for i in {1..45}; do
        if $SQLCMD -S localhost -U sa -P "$MSSQL_SA_PASSWORD" -Q "SELECT 1" > /dev/null 2>&1; then
            echo ">>> SQL Server engine is active and accepting connections!"
            break
        fi
        sleep 2
    done

    # Check if EduMyDb has already been initialized in volume
    if [ ! -f /var/opt/mssql/.edumy_initialized ]; then
        if [ -f /backup/EduMyDb.bak ]; then
            echo ">>> [RESTORE] Restoring database EduMyDb from /backup/EduMyDb.bak..."
            $SQLCMD -S localhost -U sa -P "$MSSQL_SA_PASSWORD" -Q "
                IF EXISTS (SELECT name FROM sys.databases WHERE name = 'EduMyDb')
                BEGIN
                    ALTER DATABASE [EduMyDb] SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
                    DROP DATABASE [EduMyDb];
                END
                RESTORE DATABASE [EduMyDb] 
                FROM DISK = '/backup/EduMyDb.bak' 
                WITH MOVE 'EduMyDb' TO '/var/opt/mssql/data/EduMyDb.mdf', 
                     MOVE 'EduMyDb_log' TO '/var/opt/mssql/data/EduMyDb_log.ldf', 
                     REPLACE;
                ALTER DATABASE [EduMyDb] SET MULTI_USER;
            "
            echo ">>> [RESTORE] Successfully restored database EduMyDb!"
        else
            echo ">>> [INIT] No .bak file found. Creating database EduMyDb..."
            $SQLCMD -S localhost -U sa -P "$MSSQL_SA_PASSWORD" -Q "IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'EduMyDb') CREATE DATABASE EduMyDb;"
        fi

        touch /var/opt/mssql/.edumy_initialized
        echo "=========================================================="
        echo ">>> EduMyDb setup completed successfully!"
        echo "=========================================================="
    else
        echo ">>> EduMyDb volume already initialized. Skipping restore/init."
    fi
}

init_database &

exec /opt/mssql/bin/sqlservr
