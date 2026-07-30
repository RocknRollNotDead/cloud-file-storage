
# Облачное хранилище файлов

Шестой учебный проект из [роадмапа Сергея Жукова](https://zhukovsd.github.io/java-backend-learning-course/).
[ТЗ проекта](https://zhukovsd.github.io/java-backend-learning-course/projects/cloud-file-storage/).

Задеплоен на https://cloud-file-storage.codeportfolio.ru/
Api доступно по http://193.168.46.216:8080/
Swagger доступен по http://193.168.46.216:8080/swagger-ui.html или по https://cloud-file-storage.codeportfolio.ru/swagger-ui.html

## Стек и структура

**Backend**

- REST API на Spring Boot со Spring Security, а в качестве хранения используются postgresql, Redis и minIO (S3)

**Frontend** — [взят со страницы с проектом](https://github.com/zhukovsd/cloud-storage-frontend), автор Сергей Жуков.
Но я чуть-чуть подправил под себя, мой репозиторий с форком этого фронта - https://github.com/RocknRollNotDead/cloud-storage-frontend

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

(доступно только администратору:)

**GET `/admin-panel/users`** — список всех пользователей с обьемом их файлов

**DELETE `/admin-panel/users/{id}`** — удалить файлы пользователя


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
https://cloud-file-storage.codeportfolio.ru/swagger-ui/index.html, http://193.168.46.216:8080/swagger-ui/index.html
https://cloud-file-storage.codeportfolio.ru/swagger-ui.html, http://193.168.46.216:8080/swagger-ui.html

## Комментарии к эндпоинтам

При загрузке файлов при ошибке загрузки файла все остальные файлы догрузятся, потом прилетит ошибка с именами загруженных и не загруженных файлов.


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

а)
```bash
docker build -t app .
docker save -o app.tar app
```

или

б)
```bash
docker build -t ghcr.io/usernameonhub/cloud-file-storage:latest .
docker push -t ghcr.io/usernameonhub/cloud-file-storage:latest .
```



**2.3 отправить образ на сервер**


```bash
scp -r C:\Users\myuser\путь\app.tar root@000.000.0.000:~/cloud-file-storage
```
отправляет в директорию `пользователь/cloud-file-storage` на удалённом сервере

распаковать образ на сервере так

а)
```bash
docker load -i app.tar
```

или сделать так:

б)

Перед пунктом 2.3 заменить в докер compose файле
```yaml
image: app # заменить на:
image: ghcr.io/usernameonhub/cloud-file-storage:latest
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

а)
```bash
docker compose down
docker compose up --build -d
```

либо

б)
```bash
docker compose pull
docker compose up -d
```

и потом посмотреть логи

```bash
docker exec -it myapp bash
ls -la /usr/local/tomcat/logs
```

### 3. Перенос на домен

Добавить настройку nginx в docker-compose

```yaml
  nginx-proxy:
    
  acme:

```

acme сам получит SSL сертификат по переменным

```yaml
    frontend:
      VIRTUAL_HOST: 
      VIRTUAL_PORT: 
      LETSENCRYPT_HOST: 
      LETSENCRYPT_EMAIL:
```

И после этого всё приложение доступно по https://cloud-file-storage.codeportfolio.ru (https с SSL сертификатом)


## О том, что изучил на этом проекте

Spring Boot, Spring Security, Spring Data Jpa, работу с Redis, S3, gradle, Swagger и интеграционные тесты.