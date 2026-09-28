# Contas Demo — Regras de negócio v1
Responsável: equipe fictícia Contas. Escopo: /api/accounts. Criticidade: alta para movimentação de saldo; média para cadastro.

- BR-01: documento sintético de 11 dígitos identifica uma única conta e não pode ser alterado. Não validamos CPF real nesta demo.
- BR-02: contas iniciam ativas com saldo zero; valores monetários usam BigDecimal, duas casas, sem arredondamento silencioso.
- BR-03: créditos e débitos devem ser positivos, com até duas casas e no máximo 100.000 por operação. Saldo máximo: 1.000.000.
- BR-04: débito não pode deixar saldo negativo. Falha deve preservar o saldo anterior.
- BR-05: a soma dos débitos do dia UTC não pode ultrapassar o limite diário cadastrado (0,01 a 10.000). O contador reinicia no próximo dia UTC.
- BR-06: conta bloqueada não permite crédito nem débito.
- BR-07: excluir conta exige saldo exatamente zero.
- BR-08: alterações concorrentes usam versão otimista. Conflito retorna HTTP 409; não sobrescrever saldo de outra transação.
- BR-09: auditor consulta; apenas operador cadastra, altera, exclui e movimenta. Requisições anônimas não acessam contas.
- BR-10: documento completo não deve aparecer em respostas nem logs. Exibir somente os quatro últimos dígitos.
- BR-11: listagem deve ser paginada com no máximo 100 registros por chamada para limitar custo de consulta e serialização.

Limitação conhecida do contrato v1: operações monetárias não têm chave de idempotência e clientes não devem repetir automaticamente POST após timeout. É requisito para evolução futura antes de qualquer uso real, não funcionalidade presente nesta base.
