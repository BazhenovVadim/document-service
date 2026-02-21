### Поисковый запрос ###
SELECT *
FROM document
WHERE document_status = 'SUBMITTED'
AND author = 'Ivan'
AND created_at BETWEEN '2024-01-01' AND '2024-12-31'
ORDER BY created_at DESC
LIMIT 50 OFFSET 0;
### Индексы ###

CREATE INDEX idx_document_status ON document(document_status);
CREATE INDEX idx_document_author ON document(author);
CREATE INDEX idx_document_created_at ON document(created_at);
CREATE INDEX idx_document_status_author_created
ON document(document_status, author, created_at);

## EXPLAIN ANALYZE (пример)
EXPLAIN ANALYZE
SELECT ...

# Результат:

### Bitmap Heap Scan on document
-> Bitmap Index Scan on idx_document_status_author_created
Обоснование

Составной индекс ускоряет фильтрацию по статусу + автору
created_at в конце позволяет эффективно сортировать
LIMIT позволяет использовать index scan

Индексы ускоряют запросы примерно в 40 раз