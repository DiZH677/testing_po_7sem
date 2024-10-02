#!/bin/bash

# Завершение всех соединений к базе данных itcase_test
psql -U postgres -d itcase_test -c "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = 'itcase_test';"

# Удаление базы данных itcase_test
psql -U postgres -c "DROP DATABASE itcase_test;"
