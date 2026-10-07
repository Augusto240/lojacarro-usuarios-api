# LojaCarro - API de Usuarios

API com o CRUD de usuarios da LojaCarro. Feita em Spring Boot 4 (Java 17).

Autor: Jose Augusto

A API da LojaCarro chama esta API para descobrir o nome do usuario que fez cada operacao com carro.

## Como rodar

Precisa de um MySQL em `localhost:3306` com usuario `root` e senha `root`. O banco `usuarios` e criado sozinho.

```bash
./mvnw clean package
java -jar target/usuarios-api.jar
```

Sobe na porta 8081. Para usar outro banco, e so passar as variaveis `SPRING_DATASOURCE_URL`,
`SPRING_DATASOURCE_USERNAME` e `SPRING_DATASOURCE_PASSWORD`.

Quando o banco esta vazio ela cria o usuario `Administrador` (GERENTE).

## Usuario

- id
- nome (obrigatorio, ate 100 caracteres)
- cargo: GERENTE ou VENDEDOR

## Endpoints

| Metodo | Rota | Quem pode |
|--------|------|-----------|
| GET | `/usuarios` | todos |
| GET | `/usuarios/{id}` | todos |
| GET | `/usuarios/{id}/nome` | todos (usado pela LojaCarro) |
| POST | `/usuarios` | GERENTE |
| PUT | `/usuarios/{id}` | GERENTE |
| DELETE | `/usuarios/{id}` | GERENTE |

Quem esta fazendo a acao manda o proprio id no header `X-Usuario-Id`. Sem o header da 401, e se o cargo nao
for GERENTE da 403. Ninguem pode excluir o proprio usuario e o ultimo gerente nao pode virar vendedor.

Exemplo da rota usada pela LojaCarro:

```
GET /usuarios/2/nome
{"nome": "Maria Oliveira"}
```

## Logs

Toda requisicao e registrada com id (`X-Request-Id`). Quando a chamada vem da LojaCarro o id e o mesmo dos logs
de la. Arquivos em `logs/`: `usuarios-api.log`, `erros.log` e `auditoria.log` (cadastro, edicao, exclusao e
acesso negado).

## Testes

```bash
./mvnw clean verify
```
