#!/bin/bash

# Проверка наличия аргумента
if [ $# -ne 1 ]; then
    echo "Usage: $0 <schema_name>"
    exit 1
fi
SCHEMA_NAME=$1

# Создаем базу данных, если она не существует
if ! psql -U postgres -lqt | cut -d \| -f 1 | grep -qw "itcase_test"; then
    psql -U postgres -c 'CREATE DATABASE itcase_test;'
    echo "Database itcase_test created."
else
    echo "Database itcase_test already exists."
fi
# Создаем схему, если она не существует
if ! psql -U postgres -d itcase_test -c "SELECT schema_name FROM information_schema.schemata WHERE schema_name = '$SCHEMA_NAME';" | grep -qw "$SCHEMA_NAME"; then
    psql -U postgres -d itcase_test -c "CREATE SCHEMA $SCHEMA_NAME;"
    echo "Schema $SCHEMA_NAME created."
else
    echo "Schema $SCHEMA_NAME already exists."
fi

psql -U postgres -d itcase_test -c "ALTER DATABASE itcase_test SET search_path TO $SCHEMA_NAME;"

# Выполняем SQL-запросы для создания таблиц в тестовой базе данных
psql -U postgres -d itcase_test -c "
    DROP TABLE IF EXISTS $SCHEMA_NAME.DTP CASCADE;
    CREATE TABLE $SCHEMA_NAME.DTP
    (
        id          serial primary key,
        description varchar(100),
        datetime    timestamp,
        coord_w     float,
        coord_l     float,
        dor         varchar(200),
        osv         varchar(50),
        count_ts    int,
        count_parts int
    );
    
    DROP TABLE IF EXISTS $SCHEMA_NAME.VEHICLE CASCADE;
    CREATE TABLE $SCHEMA_NAME.VEHICLE
    (
        id serial primary key,
        dtp_id int,
        marka_ts varchar(100),
        m_ts varchar(100),
        r_rul varchar(50),
        type_ts varchar(200),
        car_year int,
        color varchar(15),
        FOREIGN KEY(dtp_id) REFERENCES $SCHEMA_NAME.DTP(id)
    );
    
    DROP TABLE IF EXISTS $SCHEMA_NAME.PARTICIPANT CASCADE;
    CREATE TABLE $SCHEMA_NAME.PARTICIPANT
    (
        id serial primary key,
        vehicle_id int,
        category varchar(50),
        warnings varchar(400),
        SAFETY_BELT bool,
        pol varchar(15),
        health varchar(200),
        FOREIGN KEY (vehicle_id) REFERENCES $SCHEMA_NAME.VEHICLE(id)
    );

    DROP TABLE IF EXISTS $SCHEMA_NAME.users;
    CREATE TABLE $SCHEMA_NAME.Users
    (
        id serial primary key,
        login varchar(50),
        password varchar(50),
        role varchar(50)
    );"

# Вставляем данные в таблицу users
psql -U postgres -d itcase_test -c "
    INSERT INTO $SCHEMA_NAME.users (id, login, password, role) VALUES (1, 'lgn', 'pswrd', 'Analyst');
"
