com.wbozon.wb
├── api
│   ├── RateLimiter               // Управление задержками между запросами
│   └── RetryHandler              // Повтор запросов при ошибках
│
├── client
│   └── WildberriesApiClient      // Основной HTTP-клиент для работы с Wildberries API
│
├── controller
│   └── WildberriesController     // REST-контроллер для внешнего взаимодействия
│
├── dto
│   ├── PriceApiResponse          // DTO для ответа по ценам
│   ├── PriceApiData              // Вложенные данные по товарам
│   ├── ListGood                  // Модель товара
│   ├── StockApiResponse          // DTO для ответа по остаткам
│   └── StockEntity               // Модель остатка
│
├── model
│   ├── ProductCard               // Карточка товара
│   ├── Size                     // Размер товара
│   └── WareHouseEntity          // Склад
│
├── service
│   └── WildberriesService        // Сервисный слой: логика обработки данных
│
└── util
    └── JsonUtils                 // Утилиты для работы с JSON
Краткое описание слоёв
api — технические утилиты: лимиты, ретраи

client — HTTP-интеграция с Wildberries

controller — точка входа для REST-запросов

dto — структуры данных, соответствующие JSON-ответам

model — внутренние бизнес-модели

service — бизнес-логика

util — вспомогательные классы














+---------------------+           +---------------------+
|   PriceApiResponse  |           |   StockApiResponse  |
|---------------------|           |---------------------|
| - data: PriceApiData|           | - stocks: List<StockEntity> |
| + getData()         |           | + getStocks()       |
+---------------------+           +---------------------+
           |                                 |
           v                                 v
+---------------------+           +---------------------+
|   PriceApiData      |           |   StockEntity       |
|---------------------|           |---------------------|
| - listGoods: ListGood[]         | - sku: String       |
| + getListGoods()    |           | - quantity: int     |
+---------------------+           | - warehouseId: long |
           |                      +---------------------+
           v
+---------------------+
|     ListGood        |
|---------------------|
| - nmID: long        |
| - price: double     |
| - discount: double  |
| + getNmID()         |
| + getPrice()        |
+---------------------+
           |
           | maps to
           v
+---------------------+
|   ProductCard       |
|---------------------|
| - nmID: long        |
| - sizes: List<Size> |
| + getNmID()         |
| + getSizes()        |
+---------------------+
           |
           v
+---------------------+
|       Size          |
|---------------------|
| - skus: String[]    |
| + getSkus()         |
+---------------------+
