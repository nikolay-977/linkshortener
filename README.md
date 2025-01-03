# Link Shortener Service

## Описание

Этот сервис позволяет пользователям сокращать длинные URL-адреса, переходить по сокращенным ссылкам и управлять своими ссылками. Сервис также автоматически удаляет истекшие ссылки.

## Как пользоваться сервисом

1. **Запуск приложения**:
    - Убедитесь, что у вас установлен Java 17, PostgreSQL и настроена среда разработки.
    - Скомпилируйте проект и запустите класс `Main`.

2. **Ввод имени пользователя**:
    - При первом запуске вам будет предложено ввести имя пользователя. Если имя пользователя не существует, оно будет создано, и вам будет предоставлен уникальный ID.

3. **Использование команд**:
    - После входа в систему вы увидите меню с доступными командами.

## Поддерживаемые команды

1. **Сократить ссылку**:
    - Введите `1`, затем введите оригинальную ссылку и лимит переходов. Сервис создаст короткую ссылку.

2. **Перейти по короткой ссылке**:
    - Введите `2`, затем введите короткую ссылку. Сервис откроет браузер и перенаправит вас на оригинальный URL.

3. **Удалить короткую ссылку**:
    - Введите `3`, затем введите короткую ссылку, которую вы хотите удалить. Сервис удалит указанную ссылку.

4. **Сменить пользователя**:
    - Введите `4`, чтобы сменить пользователя. Вам будет предложено ввести новое имя пользователя.

5. **Выйти**:
    - Введите `5`, чтобы выйти из программы.

## Как протестировать код

**Тестирование функциональности**:
    - Запустите приложение и протестируйте все команды, описанные выше.
    - Убедитесь, что сервис корректно обрабатывает вводимые данные и выполняет ожидаемые действия.

## Зависимости

- Java 17
- PostgreSQL

## Настройка

### Установить postgresql
```bash script
brew install postgresql
```

### Создайте базу данных выполнив скрипт
```bash script
psql postgres -f src/main/resources/setup.sql
```

Создайте файл `application.properties` в каталоге  resources проекта с содержимым:

service.url=clck.ru/
db.url=jdbc:postgresql://localhost:5432/linkshortener
db.user=user
db.password=password
life.time.hours=24