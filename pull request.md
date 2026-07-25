### Первая правка

1. склонировать репо.

a) Если уже есть форк (чужой репо на моем гитхабе откуда все изменения будут приходить на основной репо)
Если я нахожусь в другом гит проекте, то делаем явно `git clone ссылка.git название_папки_куда_придут_файлы_из_проекта_cloud-storage-frontend`
Если не нахожусь, то просто `git clone ссылка.git`
б) Если форка нет, создаём форк на гитхабе, потом тоже самое
в) если репо уже скопирован с основного репо, то делаем форк, потом
```bash
git remote set-url origin ссылка-на-форк
```

2. Переключаемся на нужную ветку в случае необходимости `git checkout patch-1` либо создаём её `git checkout -b patch-1`

(или сразу `git clone -b patch-1 ссылка.git`)

Редактируем нужные файлы

```bash
npm install
npm run build
```

3. Пушим изменения
```bash
git add dist
git commit -m "Add dist build output"
git push origin patch-1
```

(если ветки на гитхаб не было, то так:)
`git push -u origin patch-1`

если просит пароль, то надо ввести токен, но лучше до этого заранее сделать так

```bash
pkg install gh
gh auth login
```
либо так
```bash
git config --global credential.helper store
```

4. Сделать пул реквест.
При заходе на оригинальный репо уже будет кнопка создать пул реквест. 
Либо нужно перейти по ссылке
```bash
https://github.com/RocknRollNotDead/cloud-storage-frontend/pull/new/patch-1
//или с однозначным указанием веток:
https://github.com/zhukovsd/cloud-storage-frontend/compare/master...RocknRollNotDead:cloud-storage-frontend:patch-1
```
Либо при установленном gh сделать
`gh pr create --fill`

### Создание новой правки

Для этого нужна новая ветка
`git checkout -b patch-2-new`
_произвести изменения_
```bash
git add .
git commit -m "сделал изменения"
git push -u origin patch-2-new
```





