FROM maven:3.8.1-openjdk-11 AS build

WORKDIR /app

COPY . /app

# Устанавливаем Allure для отчетов, make
RUN apt-get update && \
   apt-get install -y postgresql postgresql-contrib

# Запускаем PostgreSQL в фоновом режиме
# RUN service postgresql start && \
#     sleep 5 # Подождите, пока PostgreSQL полностью запустится

RUN ls /etc/postgresql

# Изменяем метод аутентификации на md5
RUN PG_VERSION=$(psql --version) && \
    sed -i 's/peer/trust/g' /etc/postgresql/11/main/pg_hba.conf && \
    sed -i 's/md5/trust/g' /etc/postgresql/11/main/pg_hba.conf
#     echo "host all all 0.0.0.0/0 peer" >  /etc/postgresql/$PG_VERSION/main/pg_hba.conf && \
#     echo "listen_addresses='*'" >> /etc/postgresql/$PG_VERSION/main/postgresql.conf

# Запускаем PostgreSQL в фоновом режиме
# RUN service postgresql restart

# Создайте тестовую базу данных
# COPY ITCase_test_create.sh /app/
# RUN chmod +x /app/ITCase_test_create.sh && /app/ITCase_test_create.sh

# Устанавливаем пароль для пользователя postgres
# RUN sleep 5 && psql -U postgres -c "ALTER USER postgres PASSWORD 'your_password';"
# RUN sleep 5 && su postgres -c 'psql -c "CREATE USER postgres;"'

RUN apt-get install -y allure && \
       apt-get install -y make

# Консольный вывод
RUN ls /app && echo "Копирование собранных файлов из предыдущей стадии..."

# Запуск make для выполнения тестов
CMD service postgresql start && make tests
