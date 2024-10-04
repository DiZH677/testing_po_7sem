#!/bin/bash

# Функция для генерации отчета Allure
generate_report() {
    cp -r ./allure-report/history ./allure-results
    allure generate allure-results --clean
    if [ "$IS_REPORT" = "1" ] || [ -z "$IS_REPORT" ]; then
      allure serve allure-results
    fi
}

run_unit() {
    mvn clean install -Punit
    if [ $? -ne 0 ]; then
        echo "Unit tests failed. Exiting."
        generate_report
        exit 1
    fi
}

run_integration() {
    SCHEMA_NAME="test_schema_$(uuidgen | tr '-' '_')"
    echo "Generated schema name: $SCHEMA_NAME"
    ./ITCase_test_create.sh "$SCHEMA_NAME"
    mvn clean install -Pintegration -DtestSchema="$SCHEMA_NAME"
    if [ $? -ne 0 ]; then
        echo "Integration tests failed. Exiting."
        ./ITCase_test_drop.sh "$SCHEMA_NAME" 2>/dev/null
        generate_report
        exit 1
    fi
    ./ITCase_test_drop.sh "$SCHEMA_NAME" 2>/dev/null
}

run_e2e() {
    SCHEMA_NAME="test_schema_$(uuidgen | tr '-' '_')"
    ./ITCase_test_create.sh "$SCHEMA_NAME"
      while lsof -i :8000 >/dev/null; do
          echo "Port 8000 is already in use. Waiting for it to become available..."
          sleep 10
      done
    DTP_TESTING_TRUE=true mvn exec:java -Dexec.mainClass="app.backend.BackendHttpServer" -DtestSchema="$SCHEMA_NAME" &
    SERVER_PID=$!  # Сохраняем PID сервера

    # Проверка, что сервер начал слушать на порту 8000
    for i in {1..10}; do
        if lsof -i :8000 >/dev/null; then
            echo "Server is running on port 8000."
            break
        fi
        echo "Waiting for server to start on port 8000..."
        sleep 5
    done
    mvn clean install -Pe2e
    TEST_RESULT=$?
    bash -c 'unset DTP_TESTING_TRUE'
    pkill -f Backend  # Останавливаем сервер после завершения тестов
    ./ITCase_test_drop.sh "$SCHEMA_NAME" 2>/dev/null
    if [ $TEST_RESULT -ne 0 ]; then
        echo "E2E tests failed. Exiting."
        generate_report
        exit 1
    fi
}

rm -rf allure-results 2>/dev/null

IS_REPORT=$1
TEST_TYPE=$2

if [ "$TEST_TYPE" = "unit" ]; then
    run_unit
elif [ "$TEST_TYPE" = "integration" ]; then
    run_integration
elif [ "$TEST_TYPE" = "e2e" ]; then
    run_e2e
else
    # Запуск всех тестов
    run_unit
    run_integration
    run_e2e
fi

generate_report

echo "All tests passed!"
