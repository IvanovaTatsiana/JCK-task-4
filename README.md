# Shop MVC — Servlet/JSP/Tomcat/PostgreSQL

## Требования
- JDK 17+
- Maven 3.9+
- Tomcat 10.1+
- PostgreSQL 14+

## 1. База данных
Создайте БД `shop_db`, затем выполните `src/main/resources/schema.sql`.

По умолчанию:
- DB user: `postgres`
- DB password: `postgres`

Если у вас другие данные, измените `src/main/resources/db.properties`.

## 2. IntelliJ IDEA
Open -> выберите папку проекта -> Maven import.
Для запуска: Run -> Edit Configurations -> Tomcat Server -> Local.
Deployment -> добавьте artifact `shop-mvc:war exploded`.
Application context: `/shop-mvc`.

## 3. Авторизация
Администратор:
- login: `admin`
- password: `admin`

Обычный пользователь регистрируется через Sign Up.

## Реализованные use cases
- Sign in
- Sign out
- Sign up
- Просмотр товаров
- Добавление товара (ADMIN)
- Создание заказа
- Просмотр своих заказов
- Отмена заказа
- Удаление товара (ADMIN)
- Редактирование email профиля

Пароли оставлены в открытом виде только для учебного примера; в production использовать BCrypt/Argon2.
