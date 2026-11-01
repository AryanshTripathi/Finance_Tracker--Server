## How to check DB

```azure
docker exec -it server-postgres-1 psql -U myuser -d mydatabase -c "\dt"
```

#### DB Query
```azure
\c mydatabase
\dt
```


## Concepts to go through

- How hibernate works and hibernate vs postgres
- How to connect postgres with hibernate
- @GeneratedValue(strategy = GenerationType.UUID) vs leaving generation to the DB and reading it on insert
- Mapping of entities
- jakarta vs javax
- char vs varchar
- Key design decision: skip UserDetailsService/AuthenticationManager