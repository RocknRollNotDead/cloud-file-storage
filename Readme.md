
# Облачное хранилище файлов

Шестой учебный проект из [роадмапа Сергея Жукова](https://zhukovsd.github.io/java-backend-learning-course/).
[ТЗ проекта](https://zhukovsd.github.io/java-backend-learning-course/projects/cloud-file-storage/).

Будет задеплоен на http://193.168.46.216:8081 и https://cloud-file-storage.codeportfolio.ru/

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

Также эта информация указана в swagger - 
https://cloud-file-storage.codeportfolio.ru/swagger-ui/index.html, http://193.168.46.216:8081/swagger-ui/index.html
https://cloud-file-storage.codeportfolio.ru/swagger-ui.html, http://193.168.46.216:8081/swagger-ui.html

## Как буду деплоить

### 1. Зайти в Ubuntu

- Арендовать vps сервер с Ubuntu (самый дешёвый) на одном из российских провайдеров - Beget Cloud, Timeweb Cloud, Selectel и др. Российские, такие, как [Beget](https://beget.com), не рекомендую.
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

собрать образ 

```bash
docker build -t app .
docker save -o app.tar app
```

**2.3 отправить образ на сервер**

```bash
scp -r C:\Users\myuser\путь\app.tar root@000.000.0.000:~/cloud-file-storage
```
отправляет в директорию `пользователь/cloud-file-storage` на удалённом сервере

распаковать образ на сервере

```bash
docker load -i app.tar
```

**2.4 запустить docker-compose**

```bash
docker compose up -d
```

**2.5 исправление багов**

посмотреть логи

```bash
docker logs name_folder-app-1
```


исправить на своём компьютере и в cmd не заходя на удалённый сервер отправить
```bash
scp -r docker-compose.yml root@000.000.0.000:~/cloud-file-storage/
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

Добавиить настройку nginx в docker-compose

```yaml
  nginx-proxy:
    
  acme:

```

acme сам получит SSL сертификат по переменным

И после этого всё приложение будет доступно и по http://193.168.46.216:8081 (как по тз) и https://cloud-file-storage.codeportfolio.ru (https с SSL сертификатом)


## О том, что планирую изучить на этом проекте

Spring Boot, Spring Security, Spring Data Jpa, работу с Redis, S3, gradle, Swagger и интеграционные тесты.