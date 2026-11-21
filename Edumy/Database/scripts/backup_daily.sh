#!/usr/bin/env bash
# ==============================================================================
# Edumy Automated Daily SQL Server Backup Script (NFR-06: RPO <= 24h)
# ==============================================================================
# Usage:
#   ./backup_daily.sh
# Crontab example (daily at 02:00 Asia/Ho_Chi_Minh):
#   0 2 * * * /opt/edumy/scripts/backup_daily.sh >> /var/log/edumy-backup.log 2>&1
# ==============================================================================

set -euo pipefail

DB_CONTAINER="${EDUMY_DB_CONTAINER:-edumy-database}"
DB_NAME="${EDUMY_DB_NAME:-EduMyDb}"
DB_USER="${EDUMY_DB_USER:-sa}"
DB_PASS="${EDUMY_DB_PASSWORD:?EDUMY_DB_PASSWORD environment variable must be set}"
BACKUP_DIR="${EDUMY_BACKUP_DIR:-/var/opt/mssql/backups}"
RETENTION_DAYS="${EDUMY_BACKUP_RETENTION_DAYS:-14}"
TIMESTAMP="$(date +%Y%m%d_%H%M%S)"
BACKUP_FILE="${BACKUP_DIR}/${DB_NAME}_${TIMESTAMP}.bak"

echo "[$(date -Iseconds)] Starting daily backup of ${DB_NAME} -> ${BACKUP_FILE}"

docker exec "${DB_CONTAINER}" mkdir -p "${BACKUP_DIR}"
docker exec "${DB_CONTAINER}" /opt/mssql-tools18/bin/sqlcmd \
  -S localhost -U "${DB_USER}" -P "${DB_PASS}" -C -b \
  -Q "BACKUP DATABASE [${DB_NAME}] TO DISK = N'${BACKUP_FILE}' WITH COMPRESSION, CHECKSUM, INIT, NAME = N'${DB_NAME}-Daily-${TIMESTAMP}';"

echo "[$(date -Iseconds)] Verifying backup checksum..."
docker exec "${DB_CONTAINER}" /opt/mssql-tools18/bin/sqlcmd \
  -S localhost -U "${DB_USER}" -P "${DB_PASS}" -C -b \
  -Q "RESTORE VERIFYONLY FROM DISK = N'${BACKUP_FILE}' WITH CHECKSUM;"

echo "[$(date -Iseconds)] Pruning backups older than ${RETENTION_DAYS} days..."
docker exec "${DB_CONTAINER}" find "${BACKUP_DIR}" -name "${DB_NAME}_*.bak" -type f -mtime +"${RETENTION_DAYS}" -delete

echo "[$(date -Iseconds)] Backup completed successfully: ${BACKUP_FILE}"
