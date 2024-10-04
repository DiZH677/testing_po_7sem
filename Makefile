WORK_DIR = ./compile_out
SHUFFLE = false
BACKEND_CLASS = src/main/java/app/backend/BackendHttpServer.java
BACKEND_PID_FILE = backend.pid

.PHONY: all

all: clean tests

tests:
	#rm -rf allure-results
	$(MAKE) unit && $(MAKE) integration && $(MAKE) e2e || true
ifeq ($(REPORT),true)
	$(MAKE) generate-report
endif
	@echo "DONEEEE"

unit:
ifeq ($(SHUFFLE),true)
	mvn clean test -Dsurefire.runOrder=random
else
	mvn clean test
endif

integration:
	./ITCase_test_create.sh
	mvn failsafe:integration-test
	./ITCase_test_drop.sh

e2e:
	./ITCase_test_create.sh
	@echo "Starting Backend..."
	export DTP_TESTING_TRUE
	DTP_TESTING_TRUE=true mvn exec:java -Dexec.mainClass="app.backend.BackendHttpServer" &
	sleep 5  # Даем время на запуск сервера
	mvn test -Pe2e
	@echo "Stopping Backend..."
	bash -c 'unset DTP_TESTING_TRUE'
	./ITCase_test_drop.sh 2>/dev/null
	pkill -f Backend  # Останавливаем сервер после завершения тестов

generate-report:
	cp -r ./allure-report/history ./allure-results
	allure generate allure-results --clean
	allure serve allure-results

clean:
	rm -rf $(WORK_DIR) 2>/dev/null || true
	rm *.jar 2>/dev/null || true
	rm -rf allure-results