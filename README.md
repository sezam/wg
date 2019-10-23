Разработан веб-сервис, который позволяет работать с виджетами через HTTP REST API.

Виджет - это объект на плоскости в прямоугольной системе координат, имеющий координаты (X, Y), Z-index, ширину, высоту, дату последней модификации и уникальный идентификатор. 
X, Y и Z-index - целые числа (могут быть отрицательными).
X, Y -  левый верхний угол виджета.
Z-index - это общая для всех виджетов последовательность, определяющая порядок наложения виджетов  (независимо от их координат). Допускает пропуски. Чем выше значение, тем выше на плоскости лежит виджет. 

Операции, предоставляемые веб-сервисом:
Создание виджета. Задав координаты, Z-index, ширину и высоту, в ответе мы должны получить полное представление виджета.
    
    POST /api/widgets
    {"posX":150,"posY":250,"width":150,"height":250,"zorder":0}
    
    {"id":1,"posX":150,"posY":250,"width":150,"height":250,"updateAt":"2019-10-22T17:44:32.653+0000","zorder":0}

Идентификатор генерируется на сервере.
Если мы не указываем Z-index при создании виджета, то виджет перемещается на передний план. Если указываем существующий Z-index, то новый виджет сдвигает все виджеты с таким же и большим индексом в большую сторону.
    
    POST /api/widgets
    {"posX":100,"posY":200,"width":150,"height":250}
    
    {"id":2,"posX":100,"posY":200,"width":150,"height":250,"updateAt":"2019-10-22T17:52:23.153+0000","zorder":0}
       
Получение виджета по его идентификатору. В ответе мы получаем полное представление виджета.
    
    GET /api/widgets/1
   
    {"id":1,"posX":150,"posY":250,"width":150,"height":250,"updateAt":"2019-10-22T17:44:32.653+0000","zorder":0}
            
Изменение данных виджета по его идентификатору. В ответе мы получаем обновленное полное представление виджета.
    
    PUT /api/widgets/1
    {"posX":101,"posY":201,"width":151,"height":251}
    
    {"id":2,"posX":101,"posY":201,"width":151,"height":251,"updateAt":"2019-10-22T17:52:23.153+0000","zorder":0}
    
Нельзя изменить идентификатор виджета.
Нельзя удалить атрибуты виджета.

Все изменения над виджетами должны происходить атомарно. То есть, если мы меняем XY координаты виджета, то мы не должны получить промежуточное состояние при конкурентном чтении.
    @Transactional // операции над виждетом проводятся внутри транзакции
    
Удаление виджета. Мы можем удалить виджет по его идентификатору.
    
    DELETE /api/widgets/1
    {"deleted":"true"}
    
Получение списка виджетов. В ответе мы должны получить список всех виджетов отсортированных по Z-index, от меньшего к большему.
    
    GET /api/widgets
    [{"id":1,"posX":150,"posY":250,"width":150,"height":250,"updateAt":"2019-10-22T17:44:32.653+0000","zorder":1},
     {"id":2,"posX":101,"posY":201,"width":151,"height":251,"updateAt":"2019-10-22T17:52:23.153+0000","zorder":0}]

Технические требования:
API должно соответствовать REST архитектуре.
Делать только серверную часть, визуализацию делать не нужно.
Это должно быть Spring Boot приложение.
Данные должны храниться только в оперативной памяти. Для организации хранилища можно использовать любые классы стандартной библиотеки Java. Запрещается использовать любые внешние хранилища и базы данных.
    Данные хранятся в памяти и дублируются в БД
    
Не менее 30% кода должно быть покрыто тестами (желательно наличие и unit, и интеграционных тестов).
Для сборки использовать Maven.
Исходники предоставить в публичном git-репозитории.

    https://github.com/sezam/widget-generator

Усложнения
Усложнения опциональны - Вы можете делать или не делать их на ваш выбор. 
1. Пагинация
Реализовать механизм пагинации для запроса на список виджетов, который по умолчанию будет выдавать по 10 элементов, но с возможность указать другое количество (до 500).

2. Фильтрация
При получении списка виджетов, мы можем указать область, в которой виджеты расположены. В результат попадают только виджеты, которые целиком попадают в область.
Например, у нас есть 3 виджета, которые имеют ширину и высоту равные 100. Центры этих виджетов лежат в точках 50:50, 50:100 и 100:100. Мы хотим получить только виджеты, которые находятся внутри прямоугольника, левая нижняя точка которого находится в координате 0:0, а правая верхняя - 100:150. 
В таком случае мы должны получить первый и второй виджет.
Важным условием является найти способ реализации, при котором асимптотическая сложность алгоритма в среднем была меньше O(n).

3. Rate limiting
Необходимо реализовать возможность для ограничения количества запросов, отправляемых на сервер в интервал времени, и в случае превышения заданного ограничения сервер возвращает соответствующий ответ. Ограничения глобальные - для всех клиентов веб-сервиса. По умолчанию есть rate limit для всех запросов, но должна быть возможность установить значение для конкретного endpoint-а. Все значения (по умолчанию и переопределенные) можно изменять без перезапуска приложения. В заголовках ответа сервер должен возвращать параметры rate limit-a для вызванного endpoint-а: сам rate limit, количество доступных запросов, дата следующего сброса значения (граница окна). 

Пример настроек: rate limit по умолчанию - 1000 запросов в минуту, rate limit на получение всех виджетов доски - 200 запросов в минуту.

    wigen.rate_limit=1000

4. SQL хранилище
Необходимо создать еще одну реализацию хранилища, которая работает с SQL БД. Можно использовать любую реализацию in-memory БД (например, H2). 
Выбор реализации хранилища (обычная или in-memory sql) должен определяться в конфигурации сервера и не требовать изменений в коде.
    
    
    spring.datasource.url=jdbc:h2:file:./h2db
