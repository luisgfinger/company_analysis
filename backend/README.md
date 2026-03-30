# Backend de Produtos

<a id="indice"></a>
## Índice

- [Visão geral](#visao-geral)
- [Tecnologias utilizadas](#tecnologias-utilizadas)
- [Funcionalidades atuais](#funcionalidades-atuais)
- [Estrutura do projeto](#estrutura-do-projeto)
- [Como a aplicação funciona](#como-a-aplicacao-funciona)
- [Módulo implementado](#modulo-implementado)
- [Módulo `products`](#modulo-products)
- [Tratamento do CSV](#tratamento-do-csv)
- [Persistência e regra de upsert](#persistencia-e-regra-de-upsert)
- [API REST](#api-rest)
- [`GET /api/v1/products`](#get-api-v1-products)
- [`GET /api/v1/products/all`](#get-api-v1-products-all)
- [`GET /api/v1/products/{id}`](#get-api-v1-products-id)
- [Exemplo de resposta](#exemplo-de-resposta)
- [CORS](#cors)
- [Configuração atual](#configuracao-atual)
- [Como executar](#como-executar)
- [Criar o banco de dados](#criar-o-banco-de-dados)
- [Preparar a pasta de entrada](#preparar-a-pasta-de-entrada)
- [Executar a aplicação](#executar-a-aplicacao)
- [Como rodar os testes](#como-rodar-os-testes)
- [Validação atual](#validacao-atual)
- [Limitações atuais](#limitacoes-atuais)
- [Próximos passos recomendados](#proximos-passos-recomendados)

<a id="visao-geral"></a>
## Visão geral

Este projeto é um backend em **Java com Spring Boot** para **importação de produtos via CSV** e **consulta de produtos por API REST**. Hoje, a aplicação tem dois papéis principais:

- importar automaticamente arquivos `.csv` da pasta `data/` durante o startup;
- disponibilizar endpoints `GET` para listar e consultar os produtos persistidos.

O módulo implementado atualmente é apenas o de **produtos**.

<a id="tecnologias-utilizadas"></a>
## Tecnologias utilizadas

- **Java 17+**
- **Spring Boot 4.0.4**
- **Spring Web MVC**
- **Spring Data JPA**
- **PostgreSQL**
- **H2** para testes
- **Maven**
- **JUnit 5**

<a id="funcionalidades-atuais"></a>
## Funcionalidades atuais
- Importação automática de arquivos CSV ao iniciar a aplicação.
- Descoberta recursiva de arquivos `.csv` dentro de `data/`.
- Resolução de módulo pelo caminho do arquivo.
- Processamento do módulo `products`.
- Leitura de CSV com tentativa em **UTF-8** e fallback para **Windows-1252**.
- Remoção de BOM, quando presente.
- Persistência com regra de **upsert** por `code` e `barcode`.
- API REST para busca paginada, listagem completa e consulta por `id`.
- CORS liberado para `GET` e `OPTIONS` em `/api/**`.

<a id="estrutura-do-projeto"></a>
## Estrutura do projeto

```text
backend/
|-- data/                                # pasta local, ignorada pelo Git
|   `-- products/
|       `-- *.csv
|-- src/
|   |-- main/
|   |   |-- java/com/postoBackend/backend/
|   |   |   |-- config/
|   |   |   |-- controller/
|   |   |   |-- dataImport/
|   |   |   |-- domain/
|   |   |   |-- repository/
|   |   |   `-- service/
|   |   `-- resources/
|   |       `-- application.properties
|   `-- test/
|       |-- java/
|       `-- resources/
|-- pom.xml
`-- README.md
```

<a id="como-a-aplicacao-funciona"></a>
## Como a aplicação funciona

Ao iniciar fora do perfil `test`, o `ImportRunner` executa a importação automaticamente a partir de `data/`.

O fluxo atual é:

1. A aplicação sobe com Spring Boot.
2. O `ImportRunner` chama o `ImportService` com o diretório base `data/`.
3. O `FileDiscoveryService` percorre a árvore e encontra todos os arquivos `.csv`.
4. O `ImportContextResolver` identifica o módulo pelo caminho do arquivo.
5. O `ImportProcessorResolver` escolhe o processador correto.
6. O `ProductProcessor` lê o CSV, filtra as linhas válidas, mapeia os dados e persiste os produtos.

Se a pasta `data/` não existir, a aplicação falha no startup, porque a varredura do diretório não encontra o caminho esperado.

<a id="modulo-implementado"></a>
## Módulo implementado

<a id="modulo-products"></a>
### `products`

O módulo disponível hoje é `products`, esperado em caminhos como:

```text
data/products/produtos.csv
```

O processador usa estas colunas do CSV:

- `Nome Grupo` para `category`;
- `Cod.Produto` para `code`;
- `Codigo deBarras` para `barcode`;
- `Nome` para `name`;
- `PrecoVenda` para `price`.

Uma linha só é considerada válida quando:

- possui pelo menos 9 colunas;
- não é cabeçalho;
- contém `code`, `name` e `price`.

<a id="tratamento-do-csv"></a>
## Tratamento do CSV

O leitor de CSV tem o seguinte comportamento:

- tenta ler primeiro em **UTF-8**;
- se houver erro de codificação, tenta novamente em **Windows-1252**;
- remove BOM no início da linha, quando existir;
- usa `;` como separador;
- preserva colunas vazias;
- entrega todas as linhas ao processador, e o filtro decide quais são relevantes.

O preço é normalizado antes da persistência, convertendo formatos como `7,69` para `7.69`.

<a id="persistencia-e-regra-de-upsert"></a>
## Persistência e regra de upsert

A entidade `Product` possui os campos:

- `id`
- `code`
- `name`
- `price`
- `category`
- `barcode`

Regras atuais de banco e domínio:

- `code` é obrigatório e único;
- `name` é obrigatório;
- `price` é obrigatório;
- `barcode` é único quando informado;
- valores em branco são normalizados para `null`.

Regra de upsert durante a importação:

- se já existir um produto com o mesmo `code`, ele é atualizado;
- se não existir por `code`, mas existir por `barcode`, ele é atualizado;
- se `code` e `barcode` apontarem para produtos diferentes, a importação lança erro;
- `category` e `barcode` só sobrescrevem o registro existente quando vierem preenchidos no CSV.

<a id="api-rest"></a>
## API REST

Base da API:

```text
/api/v1/products
```

<a id="get-api-v1-products"></a>
### `GET /api/v1/products`

Retorna uma página de produtos.

Parâmetros suportados:

- `q`: busca textual em `name`, `code`, `category` e `barcode`;
- `category`
- `code`
- `barcode`
- `page`
- `size`
- `sort`

Padrão atual:

- `size = 20`
- `sort = name`

Exemplo:

```http
GET /api/v1/products?q=diesel&category=combust&page=0&size=10&sort=name,asc
```

<a id="get-api-v1-products-all"></a>
### `GET /api/v1/products/all`

Retorna todos os produtos filtrados, sem paginação.

Parâmetros suportados:

- `q`
- `category`
- `code`
- `barcode`
- `sort`

Quando `sort` não é informado, a ordenação padrão é:

```text
name ASC, id ASC
```

Exemplo:

```http
GET /api/v1/products/all?barcode=123&sort=price,desc
```

<a id="get-api-v1-products-id"></a>
### `GET /api/v1/products/{id}`

Retorna um único produto por identificador.

Se o produto não existir, a API responde com **404 Not Found**.

<a id="exemplo-de-resposta"></a>
### Exemplo de resposta

```json
{
  "id": 1,
  "code": "2171",
  "name": "DIESEL S10",
  "price": 7.69,
  "category": "COMBUSTÍVEIS",
  "barcode": "123"
}
```

<a id="cors"></a>
## CORS

A aplicação permite chamadas para `/api/**` com:

- qualquer origem;
- métodos `GET` e `OPTIONS`;
- qualquer header.

<a id="configuracao-atual"></a>
## Configuração atual

As propriedades atuais de execução estão em `src/main/resources/application.properties`:

```properties
spring.application.name=backend

spring.datasource.url=jdbc:postgresql://localhost:5432/apgdb
spring.datasource.username=postgres
spring.datasource.password=1234

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

Antes de executar, ajuste `url`, usuário e senha conforme o seu ambiente.

<a id="como-executar"></a>
## Como executar

<a id="criar-o-banco-de-dados"></a>
### 1. Criar o banco de dados

Exemplo:

```sql
CREATE DATABASE apgdb;
```

<a id="preparar-a-pasta-de-entrada"></a>
### 2. Preparar a pasta de entrada

A pasta `data/` está no `.gitignore`, então ela deve existir localmente.

Estrutura esperada:

```text
data/
`-- products/
    `-- products.csv
```

<a id="executar-a-aplicacao"></a>
### 3. Executar a aplicação

Com Maven:

```bash
mvn spring-boot:run
```

Com Maven Wrapper no Windows:

```bash
mvnw.cmd spring-boot:run
```

Com Maven Wrapper no Linux ou macOS:

```bash
./mvnw spring-boot:run
```

Depois do startup:

- os arquivos CSV encontrados em `data/` serão importados automaticamente;
- a API REST ficará disponível para consulta dos produtos persistidos.

<a id="como-rodar-os-testes"></a>
## Como rodar os testes

```bash
mvn test
```

Os testes usam o perfil `test` com **H2 em memória**, então não dependem do PostgreSQL local para executar.

Cobertura atual dos testes:

- carga do contexto Spring;
- leitura de CSV em UTF-8;
- fallback para Windows-1252;
- comportamento do controller de produtos;
- filtros e consulta do serviço;
- restrições de unicidade no repositório;
- regras de upsert por `code` e `barcode`.

<a id="validacao-atual"></a>
## Validação atual

Validação executada em **20/03/2026**:

- comando: `mvn test`
- resultado: **17 testes executados**
- falhas: **0**
- status: **BUILD SUCCESS**

<a id="limitacoes-atuais"></a>
## Limitações atuais

- só existe o módulo `products`;
- a importação ocorre apenas no startup;
- a API expõe apenas endpoints de leitura;
- não há endpoint para disparar importação manualmente;
- se `data/` não existir, a aplicação falha ao iniciar;
- as credenciais de banco ainda estão fixas em arquivo de configuração.

<a id="proximos-passos-recomendados"></a>
## Próximos passos recomendados

- externalizar as credenciais por variáveis de ambiente ou profiles;
- criar endpoint para disparar importação sob demanda;
- adicionar logs e métricas de importação;
- ampliar a cobertura de testes para o fluxo completo de importação;
- suportar novos módulos além de `products`.
