FROM maven:3.8.1-openjdk-11 AS build

WORKDIR /app

COPY . /app


# Устанавливаем Allure для отчетов, make
RUN apt-get update && \
   apt-get install -y postgresql postgresql-contrib && \
   apt-get install -y lsof

RUN ls /etc/postgresql

# Изменяем метод аутентификации на md5
RUN PG_VERSION=$(psql --version) && \
    sed -i 's/peer/trust/g' /etc/postgresql/11/main/pg_hba.conf && \
    sed -i 's/md5/trust/g' /etc/postgresql/11/main/pg_hba.conf

RUN apt-get update && apt-get install -y \
    wget \
    unzip \
    make

RUN wget -q https://github.com/allure-framework/allure2/releases/download/2.20.1/allure-2.20.1.zip && \
    unzip allure-2.20.1.zip -d /opt/ && \
    ln -s /opt/allure-2.20.1/bin/allure /usr/local/bin/allure && \
    rm allure-2.20.1.zip


# Консольный вывод
RUN ls /app && echo "Копирование собранных файлов из предыдущей стадии..."

# Запуск make для выполнения тестов
CMD service postgresql start && \
    sleep 5 && \
    exec ./tests.sh 0
