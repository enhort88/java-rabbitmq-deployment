Отлично, ты уже продумал структуру и контент — я просто отформатировал, подчистил Markdown, добавил удобную навигацию, кодовые блоки и оформив как финальный вариант `README.md` для GitHub (скопируй целиком):

---

```markdown
#  Java RabbitMQ Command Dispatcher

**Динамический диспетчер команд через RabbitMQ**

Получает JSON-сообщения вида  
`{ "method": "sendEmail", "params": { "to": "a@b.c", "text": "Hi" } }`  
и вызывает нужный `@CommandMethod` с **строгим маппингом параметров**.

---

##  Особенности

- **Динамическая регистрация методов** через аннотацию `@CommandMethod`
-  **Строгое соответствие типов** (без автоконверсии `String → int`)
-  **Поддержка DTO** через Jackson
-  **DLQ + архивация** ошибочных сообщений
-  **Spring Boot + RabbitMQ + JSON**

---

##  Структура проекта

```

src/main/java/com/example/rabbitmqdemo/
├── config/
│   └── RabbitConfig.java              # Очереди, DLX, binding
├── dto/
│   └── CommandMessage.java            # { method, params }
├── meta/
│   └── CommandMethod.java             # Аннотация
├── service/
│   ├── CommandDispatcher.java         # Core: регистрация + dispatch
│   ├── CommandListener.java           # @RabbitListener → dispatch
│   ├── CommandMethodRegistrar.java    # Авторегистрация @CommandMethod
│   └── DeadLetterListener.java        # DLQ → архив

````

---

## Принцип работы

### 1. Аннотируем обработчики

```java
@CommandMethod
public void sendEmail(String to, String text) {
    log.info("Отправка email → {}: {}", to, text);
}
````

### 2. Отправляем команду через `CommandDispatcher`

```java
dispatcher.dispatch("sendEmail", Map.of(
    "to", "a@b.c",
    "text", "Привет"
));
```

> Если параметры не совпадают по типам — `IllegalArgumentException` → сообщение уходит в **DLQ**.

---

## Очереди (DLQ + архив)

```yaml
# application.yml
rabbit:
  queue: commands
  dlq: commands.dlq
  arch: commands.archive
  exchange: commands.exchange
  routing-key: cmd
```

* Ошибки → `commands.dlq` → `commands.archive`

---

## Запуск проекта

### 1. Запуск RabbitMQ в Docker

```bash
docker-compose up -d
```

### 2. Запуск приложения

```bash
./mvnw spring-boot:run
```

---

## Пример сообщения

```json
{
  "method": "createUser",
  "params": {
    "name": "Alice",
    "age": 25,
    "profile": { "bio": "Java dev", "active": true }
  }
}
```

---

## Интерфейс RabbitMQ

[http://localhost:15672](http://localhost:15672)
**Логин:** `guest` (для примера)
**Пароль:** `guest`

---

## Что добавить в проект (если ещё нет)

### 1. `CommandMessage.java`

```java
public record CommandMessage(String method, Map<String, Object> params) {}
```

### 2. `CommandMethod.java`

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CommandMethod {}
```

### 3. `application.yml` (пример)

```yaml
# ═════════════════════════════════════
# RABBITMQ CONNECTION
# ═════════════════════════════════════
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
    listener:
      simple:
        default-requeue-rejected: false  # → сразу в DLQ
        error-handler-fatal: true

# Отключаем спам в логах от ConditionalRejectingErrorHandler
logging:
  level:
    org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler: off

# ═════════════════════════════════════
# QUEUES AND ROUTING
# ═════════════════════════════════════
rabbit:
  queue: command-queue
  exchange: command-exchange
  routing-key: command.key
  dlq: dead-letter-command-queue
  arch: archive-queue
```

---

## Технологии

* Java 17+
* Spring Boot
* RabbitMQ
* Maven
* Lombok
* Jackson

---

## Автор

**Artem Ponikarov**
 `enhort@gmail.com`
 [GitHub Profile](https://github.com/enhort88)

---

>  Этот проект демонстрирует подход «Command Pattern через RabbitMQ» — простое, гибкое и типобезопасное исполнение команд из очереди.

```
Пример сообщений для Postman
-Вызов hello() без параметров
{
  "method": "hello",
  "params": {}
}


-Вызов createUser(User, String)
{
  "method": "createUser",
  "params": {
    "user": {
      "name": "Alice",
      "age": 25
    },
    "name": "Bob"
  }
}


