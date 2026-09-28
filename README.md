# Contas Demo — Spring Boot

Aplicação fictícia para exercitar revisão de código. A branch main representa uma base de produção simulada; não é adequada para operações bancárias reais. Use somente dados sintéticos.

## Executar

Java 21. Maven Wrapper incluído (baixa Maven no primeiro uso):

~~~powershell
.\mvnw.cmd test
$env:DEMO_PASSWORD = 'uma-senha-local-para-demo'
.\mvnw.cmd spring-boot:run
~~~

API em http://127.0.0.1:8081. H2 em memória, dados perdidos ao parar. Usuários operator (leitura/escrita) e auditor (somente leitura) usam DEMO_PASSWORD; padrão público apenas para demo: local-demo-only. Sem interface gráfica. A porta 8081 permite executar junto ao Sentinela em 3210.

## Endpoints

| Método | Caminho | Operação |
|---|---|---|
| POST | /api/accounts | Criar |
| GET | /api/accounts?page=0&size=20 | Listar (máximo 100) |
| GET | /api/accounts/{id} | Consultar |
| PUT | /api/accounts/{id} | Alterar nome, limite e status |
| DELETE | /api/accounts/{id} | Excluir se saldo zero |
| POST | /api/accounts/{id}/credits | Creditar |
| POST | /api/accounts/{id}/debits | Debitar |

Criar: `{"holder":"Pessoa Demo","document":"00000000001","dailyLimit":100.00}`

Alterar: `{"holder":"Pessoa Demo","dailyLimit":200.00,"status":"ACTIVE"}`

Movimentar: `{"amount":10.00}`. Envie Content-Type: application/json e autenticação Basic.

## Usar no Sentinela

URL: https://github.com/GuilhermeLeivas/repo-demo

Cadastre os documentos de docs/knowledge na base RAG, use main como destino e escolha uma das branches de cenário como origem. Os cenários são independentes e não devem ser mesclados à main. O gabarito fica fora deste repositório para evitar entregar a resposta ao revisor.

## Limitações intencionais da base

H2 efêmero, credenciais de demonstração, autenticação de máquina sem identidade de cliente final, sem idempotência de movimentação, sem ledger financeiro, sem integração entre contas, sem trilha de auditoria durável. Apenas um protótipo local. Os testes preservados nas branches servem como regressão e podem falhar em cenários defeituosos.

