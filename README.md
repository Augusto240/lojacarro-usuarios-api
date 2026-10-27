# lojacarro-usuarios-api

API com o CRUD de usuarios da LojaCarro. Comecou como um projeto novo, separado da LojaCarro, e foi feita em Spring
Boot 4 com Java 17.

Autor: Jose Augusto

A LojaCarro chama essa API para descobrir o nome de quem fez cada operacao com carro.

## Rodando

Precisa de um MySQL em `localhost:3306` com usuario `root` e senha `root`. O banco `usuarios` e criado sozinho.

```
./mvnw clean package
java -jar target/usuarios-api.jar
```

Sobe na porta 8081. Para usar outro banco e so passar `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` e
`SPRING_DATASOURCE_PASSWORD`. Com o banco vazio ela cria sozinha o usuario `Administrador`, que e GERENTE.

## Usuario

Tem id, nome (obrigatorio, ate 100 caracteres) e cargo, que pode ser GERENTE ou VENDEDOR.

Qualquer um pode listar e consultar. Cadastrar, editar e excluir so o GERENTE. Quem faz a acao manda o proprio id
no header `X-Usuario-Id`: sem ele da 401 e se nao for gerente da 403. Tambem nao da para excluir o proprio usuario e
o ultimo gerente nao pode virar vendedor.

## Rotas

- `GET /usuarios` e `GET /usuarios/{id}`
- `GET /usuarios/{id}/nome`, que e a que a LojaCarro usa. Devolve so `{"nome": "Maria Oliveira"}`
- `POST /usuarios`, `PUT /usuarios/{id}` e `DELETE /usuarios/{id}` (so GERENTE)

## Logs

Toda requisicao e registrada com um id (`X-Request-Id`). Quando a chamada vem da LojaCarro o id e o mesmo dos logs
de la. Os arquivos ficam em `logs/`: `usuarios-api.log`, `erros.log` e `auditoria.log`.

## Testes

```
./mvnw clean verify
```
