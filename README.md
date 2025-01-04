# Link Shortener Service

## Описание

Этот сервис позволяет пользователям сокращать длинные URL-адреса, переходить по сокращенным ссылкам и управлять своими
ссылками. Сервис также автоматически удаляет истекшие ссылки.

## Как пользоваться сервисом

1. **Запуск приложения**:
    - Убедитесь, что у вас установлен Java 17, PostgreSQL и настроена среда разработки.
    - Выполните следующую команду:

```bash script
./gradlew run
```

2. **Ввод имени пользователя**:
    - При первом запуске вам будет предложено ввести имя пользователя. Если имя пользователя не существует, оно будет
      создано, и вам будет предоставлен уникальный ID.

3. **Использование команд**:
    - После входа в систему вы увидите меню с доступными командами.

## Поддерживаемые команды

1. **Сократить ссылку**:
    - Введите `1`, затем введите оригинальную ссылку, лимит переходов и время жизни. Сервис создаст короткую ссылку.

2. **Перейти по короткой ссылке**:
    - Введите `2`, затем введите короткую ссылку. Сервис откроет браузер и перенаправит вас на оригинальный URL.

3. **Изменить лимит переходов по короткой ссылке**:
    - Введите `3`, затем введите короткую ссылку, для которой вы хотите изменить лимит. Сервис изменить лимит для
      указанной ссылки.

4. **Удалить короткую ссылку**:
    - Введите `4`, затем введите короткую ссылку, которую вы хотите удалить. Сервис удалит указанную ссылку.

5. **Сменить пользователя**:
    - Введите `5`, чтобы сменить пользователя. Вам будет предложено ввести новое имя пользователя.

6. **Выйти**:
    - Введите `6`, чтобы выйти из программы.

## Как протестировать код

**Тестирование функциональности**:

- Запустите приложение и протестируйте все команды, описанные выше.
- Убедитесь, что сервис корректно обрабатывает вводимые данные и выполняет ожидаемые действия.

## Зависимости

- Java 17
- PostgreSQL
- Gradle

## Настройка

### Установить postgresql

```bash script
brew install postgresql
```

### Создайте базу данных выполнив скрипт

```bash script
psql postgres -f src/main/resources/setup.sql
```

Создайте файл `application.properties` в каталоге resources проекта с содержимым:

```
service.url=clck.ru/
db.url=jdbc:postgresql://localhost:5432/linkshortener
db.user=user
db.password=password
life.time.hours=24
```

Процесс создания сервиса сокращения ссылок

Процесс создания сервиса сокращения ссылок
Создание сервиса сокращения ссылок включает в себя несколько этапов, начиная с проектирования и заканчивая реализацией и тестированием. Ниже описан процесс создания такого сервиса, включая ключевые шаги и рекомендации.

1. Проектирование
   1.1 Определение требований
   Функциональные требования:
      1. Пользователь должен иметь возможность создавать короткие ссылки.
      2. Пользователь должен иметь возможность переходить по коротким ссылкам.
      3. Пользователь должен иметь возможность изменять лимит переходов по коротким ссылкам.
      4. Пользователь должен иметь возможность удалять короткие ссылки.
      5. Ссылки должны быть доступны для перехода, редактирования и удаления только тем пользователям, которыми они были созданы.

   Нефункциональные требования:
      1. Система должна быть надежной и устойчивой к ошибкам.
      2. Система должна обеспечивать безопасность данных пользователей.

   1.2 Архитектура
      Пакет для работы с базой данных (репозитории).
      Пакет для бизнес-логики (сервисы).
      Пакет для взаимодействия с пользователем (через консоль).
      Вспомогательные пакеты (утилиты, исключения)

2. Настройка окружения
   2.1 Выбор технологий
   Язык программирования: Java.
   База данных: PostgreSQL.
   Инструменты: Gradle для управления зависимостями и сборки.
   2.2 Установка и настройка
   Установка JDK (Java Development Kit).
   Установка PostgreSQL и создайте базу данных для вашего приложения.
   Установка Gradle.
3. Реализация
   3.1 Создание структуры проекта
   Создайте структуру проекта с необходимыми пакетами:

src
└── main
├── java
│   └── ru
│       └── skillfactory
│           └── linkshortener
│               ├── console
│               ├── db
│               ├── exception
│               ├── model
│               ├── service
│               ├── utils
│               ├── config
│               └── Main.java
└── resources
└── application.properties

3.2 Реализация классов
Класс Main: Точка входа в приложение.
Класс Config: Конфигурация приложения, включая параметры базы данных.
Классы репозиториев: Для работы с базой данных (LinksRepository, UsersRepository).
Классы моделей: Для представления данных (Link, User ).
Классы сервисов: Для бизнес-логики (LinkShortenerService, User Service).
Утилиты: Для вспомогательных функций (например, DeletingExpiredLinksScheduler).
3.3 Реализация логики
Реализованы методы для создания, редактирования, удаления и получения ссылок.
Реализованыа логику для проверки истекших ссылок и их удаления.
4. Тестирование
Выполнена проверка функций
   1. Пользователь должен иметь возможность создавать короткие ссылки.
   2. Пользователь должен иметь возможность переходить по коротким ссылкам.
   3. Пользователь должен иметь возможность изменять лимит переходов по коротким ссылкам.
   4. Пользователь должен иметь возможность удалять короткие ссылки.
   5. Ссылки должны быть доступны для перехода, редактирования и удаления только тем пользователям, которыми они были созданы.

Документация по пакетам
1. Пакет console
   Описание: Пакет отвечает за взаимодействие с пользователем через консоль. Он предоставляет интерфейс для ввода команд и отображения результатов.
   Класс LinkShortenerConsole:
   • Методы:
   ◦ LinkShortenerConsole(): Конструктор, инициализирует необходимые репозитории и запускает меню.
   ◦ printMenu(): Выводит доступные команды в консоль.
   ◦ getMenu(): Обрабатывает ввод пользователя и вызывает соответствующие методы.
   ◦ getUser (): Получает имя пользователя и возвращает его ID.
   ◦ createShortLink(): Создает короткую ссылку.
   ◦ changeClickLimit(): Изменяет лимит переходов для короткой ссылки.
   ◦ goToLink(): Переходит по короткой ссылке.
   ◦ changeUser (): Меняет текущего пользователя.
   ◦ deleteShortLink(): Удаляет короткую ссылку.
   ◦ exit(): Завершает работу приложения.
2. Пакет db
   Описание: Пакет отвечает за взаимодействие с базой данных. Он содержит классы для работы с таблицами пользователей и ссылок.
   Класс DatabaseConnection:
   • Методы:
   ◦ getInstance(): Возвращает единственный экземпляр класса.
   ◦ getConnection(): Устанавливает соединение с базой данных.
   Класс UsersRepository:
   • Методы:
   ◦ addUser (User user): Добавляет нового пользователя в базу данных.
   ◦ getUser ByName(String name): Получает пользователя по имени.
   Класс LinksRepository:
   • Методы:
   ◦ createShortLink(String originalUrl, int clickLimit, int userLifeTime, String userId): Создает короткую ссылку.
   ◦ redirect(String fullShortUrl, String userId): Перенаправляет по короткой ссылке.
   ◦ deleteLink(String fullShortUrl, String userId): Удаляет короткую ссылку.
   ◦ deleteExpiredLinks(String currentUser Id): Удаляет истекшие ссылки.
   ◦ updateClickLimit(String fullShortUrl, int newClickLimit, String userId): Обновляет лимит переходов.
3. Пакет service
   Описание: Пакет содержит бизнес-логику приложения. Он управляет взаимодействием между репозиториями и консольным интерфейсом.
   Класс User Service:
   • Методы:
   ◦ addUser (String username): Добавляет нового пользователя.
   ◦ getUser ByName(String username): Получает ID пользователя по имени.
   Класс LinkShortenerService:
   • Методы:
   ◦ createShortLink(String originalUrl, int clickLimit, int lifeTime, String userId): Создает короткую ссылку.
   ◦ redirect(String shortLink, String userId): Перенаправляет по короткой ссылке.
   ◦ changeClickLimit(String shortLink, int newClickLimit, String userId): Изменяет лимит переходов.
   ◦ deleteShortLink(String linkToDelete, String userId): Удаляет короткую ссылку.
4. Пакет utils
   Описание : Пакет содержит вспомогательные классы и утилиты, которые используются в других частях приложения.
   Класс DeletingExpiredLinksScheduler:
   • Методы:
   ◦ startCleanup(String userId): Запускает поток для удаления истекших ссылок для указанного пользователя.
   ◦ stopCleanup(): Останавливает поток, если он запущен.
5. Пакет config
   Описание: Пакет отвечает за конфигурацию приложения, включая параметры подключения к базе данных и другие настройки.
   Класс Config:
   • Методы:
   ◦ getInstance(): Возвращает единственный экземпляр класса конфигурации.
   ◦ getDbUrl(): Возвращает URL базы данных.
   ◦ getDbUser (): Возвращает имя пользователя для подключения к базе данных.
   ◦ getDbPassword(): Возвращает пароль для подключения к базе данных.
   ◦ getServiceUrl(): Возвращает базовый URL сервиса сокращения ссылок.
   ◦ getLifeTime(): Возвращает значение времени жизни ссылки по умолчанию.
6. Пакет model
   Описание: Пакет содержит классы моделей, которые представляют данные в приложении.
   Класс User :
   • Поля:
   ◦ id: Уникальный идентификатор пользователя.
   ◦ name: Имя пользователя.
   Класс Link:
   • Поля:
   ◦ shortUrl: Короткая ссылка.
   ◦ originalUrl: Оригинальная ссылка.
   ◦ userId: Идентификатор пользователя, создавшего ссылку.
7. Пакет exception
   Описание: Пакет содержит пользовательские исключения, которые могут возникать в приложении.
   Класс DatabaseException:
   • Описание: Исключение, которое выбрасывается при ошибках, связанных с базой данных.
