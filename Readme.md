
# Облачное хранилище файлов

Шестой учебный проект из [роадмапа Сергея Жукова](https://zhukovsd.github.io/java-backend-learning-course/).
[ТЗ проекта](https://zhukovsd.github.io/java-backend-learning-course/projects/cloud-file-storage/).

Будет задеплоен на http://193.168.46.216:8080/cloud-file-storage и https://codeportfolio.ru/cloud-file-storage

## Стек и структура

**Backend**

- REST API на Spring Boot со Spring Security, а в качестве хранения используются mariaDB (MySQL), Redis и minIO (S3)

**Frontend** — [взят со страницы с проектом](https://github.com/zhukovsd/cloud-storage-frontend), автор Сергей Жуков.

## Функциональность

Все эндпоинты находятся под общим путём `/api`. Пример: `/api/auth/sign-up`.

---

#### Регистрация и авторизация

| Метод | Путь | Описание |
|---|---|---|
| POST | `/auth/sign-up` | Регистрация |
| POST | `/auth/sign-in` | Авторизация |
| POST | `/auth/sign-out` | Выход из аккаунта |

---

#### Пользователи

**GET `/user/me`** — текущий авторизованный пользователь

---

#### Работа с файлами и папками

| Метод | Путь | Описание |
|---|---|---|
| GET | `/resource?path=$path` | Информация о ресурсе |
| DELETE | `/resource?path=$path` | Удаление ресурса |
| GET | `/resource/download?path=$path` | Скачивание файла/папки |
| POST | `/resource/move?from=$from&to=$to` | Переименование/перемещение |
| GET | `/resource/search?query=$query` | Поиск ресурсов |
| POST | `/resource?path=$path` | Загрузка файла(ов) |
| GET | `/directory?path=$path` | Содержимое папки |
| POST | `/directory?path=$path` | Создание пустой папки |


#### Общие коды ответа

Используются во всех эндпоинтах API.

| Код | Значение |
|---|---|
| `200 OK` | Успешный запрос, тело ответа содержит запрошенные данные |
| `201 Created` | Успешное создание ресурса (регистрация, загрузка файла, создание папки) |
| `204 No Content` | Успешный запрос, тело ответа отсутствует (логаут, удаление) |
| `400 Bad Request` | Ошибка валидации — невалидный или отсутствующий параметр, невалидное тело запроса |
| `401 Unauthorized` | Запрос выполняется неавторизованным пользователем |
| `404 Not Found` | Запрошенный ресурс не найден |
| `409 Conflict` | Конфликт — ресурс с таким именем/путём уже существует |
| `500 Internal Server Error` | Непредвиденная ошибка на сервере |

---

## Swagger

Также эта информация указана в swagger - https://codeportfolio.ru/cloud-file-storage/swagger-ui/index.html, http://193.168.46.216:8080/cloud-file-storage/swagger-ui/index.html

## Как буду деплоить

### 1. Зайти в Ubuntu

- Арендовать vps сервер с Ubuntu (самый дешёвый) на одном из российских провайдеров - Beget Cloud, Timeweb Cloud, Selectel и др. Рекомендую [Beget](https://beget.com)
- Там будут данные для входа в виде ssh login@000.000.000.000 и password, где вместо login - выданный логин, вместо 0.0.0.0 выданный ip адрес, а вместо password - выданный пароль
- Открыть командную строку БЕЗ имени администратора и ввести 'ssh login@000.000.000.000' * Enter * и потом password: 'mypassword' для захода в линукс терминал на сервере

### 2. Настроить Docker

**2.1 установить Docker и docker-compose**

```bash
apt update && apt upgrade -y
sudo apt install -y docker.io docker-compose-v2
```
или
```bash
sudo snap install docker
```

**2.2 собрать Dockerfile и docker-compose.yml**

```yaml
services:
  db:
    ...
  app:
    ...
volumes:
```

**2.3 отправить проект на сервер**

```bash
scp -r C:\Users\myuser\путь\cloud-file-storage root@000.000.0.000:~/
```
отправляет в директорию `пользователь/cloud-file-storage` на удалённом сервере

**2.4 запустить docker-compose**

```bash
docker compose up -d
```

**2.5 исправление багов**

(если поменять пару слов):
```bash
nano ~/cloud-file-storage/Dockerfile
nano ~/cloud-file-storage/src/main/webapp/js/config.js
```

(если больше):
исправить на своём компьютере и в cmd не заходя на удалённый сервер отправить
```bash
scp -r C:\Users\myuser\путь\cloud-file-storage\src\main\webapp\js\app.js root@000.000.0.000:~/cloud-file-storage/src/main/webapp/js/app.js
```

и потом на удалённом сервере 

```bash
docker compose down
DOCKER_BUILDKIT=0 docker compose up --build -d
```
Флаг с buildkit снят из-за того, что с флагом docker пытается параллельно загрузить все зависимости, и сервер этого не вывозит и не даёт нормально скачать.

и потом посмотреть логи

```bash
docker exec -it myapp bash
ls -la /usr/local/tomcat/logs
```

### 3. Перенос на домен

Добавиить настройку Caddy в docker-compose

```yaml
  caddy:
    image: caddy:latest
    container_name: caddy
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./Caddyfile:/etc/caddy/Caddyfile
      - caddy_data:/data
      - caddy_config:/config
    depends_on:
      - app
```

docker сам скачает caddy в созданный им контейнер, а caddy сам получит SSL сертификат по записям в Caddyfile

```Caddyfile
codeportfolio.ru {
    reverse_proxy app:8080
}
```

И после этого всё приложение будет доступно и по http://193.168.46.216:8080/cloud-file-storage (как по тз) и https://codeportfolio.ru/cloud-file-storage (https с SSL сертификатом)


## О том, что планирую изучить на этом проекте

Spring Boot, Spring Security, Spring Data Jpa, работу с Redis, S3, gradle, Swagger и интеграционные тесты.