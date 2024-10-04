#!/bin/bash

DB_NAME="itcase_test" # Имя вашей тестовой базы данных

# Проверка наличия аргумента
if [ $# -ne 1 ]; then
    echo "Usage: $0 <schema_name>"
    exit 1
fi
SCHEMA_NAME=$1

# Функция для проверки активных подключений
check_connections() {
    local count=$(psql -h localhost -U postgres -d $DB_NAME -t -c "SELECT COUNT(*) FROM pg_stat_activity WHERE datname = '$DB_NAME';")
    echo $count
}

# Удаление схемы
psql -h localhost -U postgres -d $DB_NAME -c "DROP SCHEMA IF EXISTS $SCHEMA_NAME CASCADE;"

echo "Schema $SCHEMA_NAME dropped."

# Проверка активных подключений к базе данных
CONNECTIONS=$(check_connections)

if [ "$CONNECTIONS" -eq 0 ]; then
    # Удаление базы данных
    psql -h localhost -U postgres -c "DROP DATABASE IF EXISTS $DB_NAME;"
    echo "Database $DB_NAME dropped."
else
    echo "Database $DB_NAME not dropped. It has active connections."
fi

