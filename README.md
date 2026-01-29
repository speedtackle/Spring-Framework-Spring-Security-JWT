# 🔐 dio-spring-security-jwt

Projeto de estudo focado em **entender JWT com Spring Security 6+ de forma profunda e didática**, usando o GitHub como um **banco pessoal de aprendizado**.

Este README não serve apenas para explicar *o que funciona*, mas principalmente:

* **por que funciona**
* **onde dá erro**
* **como debugar**
* **como reutilizar em projetos reais**

---

## 🎯 Objetivo do Projeto

* Implementar autenticação **stateless** com JWT
* Entender o **pipeline interno do Spring Security**
* Aprender a configurar filtros manualmente
* Compreender erros comuns de JWT (Base64, chave fraca, expiração, roles)
* Criar uma base sólida para APIs seguras

---

## 🧱 Stack Utilizada

* Java 21+
* Spring Boot 4.x
* Spring Security 6
* JJWT 0.11.5
* JPA / Hibernate
* H2 Database

---

## 🧠 Visão Geral do Funcionamento

```text
┌────────────┐
│   Cliente  │
└─────┬──────┘
      │ POST /login (username + senha)
      ▼
┌────────────────┐
│ LoginController │
└─────┬──────────┘
      │ JWT gerado
      ▼
┌────────────────┐
│   JWT Token    │
└─────┬──────────┘
      │ Authorization: Bearer <token>
      ▼
┌────────────────┐
│   JWTFilter    │  ← executa em TODA request
└─────┬──────────┘
      │ token válido?
      ▼
┌────────────────────┐
│ SecurityContext    │
└─────┬──────────────┘
      ▼
┌────────────────┐
│ Controllers    │
└────────────────┘
```

---

# 📁 Explicação das Classes (Didático)

## 🔹 `JWTProperties`

📌 **Responsabilidade**
Ler as propriedades de segurança do `application.properties`.

📌 **Por que existe?**
Evita valores hardcoded e centraliza configurações sensíveis:

* Prefixo do token (`Bearer`)
* Chave secreta
* Tempo de expiração

📌 **Aprendizado importante:**
JWT é extremamente rigoroso com chave e tempo. Qualquer erro aqui quebra todo o sistema.

---

## 🔹 `JWTObject`

📌 **O que representa?**
O **payload do JWT** (conteúdo interno do token).

📌 **Campos principais:**

* `subject` → usuário autenticado
* `roles` → permissões
* `issuedAt` → quando foi criado
* `expiration` → quando expira

📌 **Erro comum aprendido:**
Se o `subject` não for definido, o Spring autentica um usuário `null`.

---

## 🔹 `JWTCreator`

📌 **Responsabilidade central:**

* Criar o JWT
* Validar token recebido
* Converter JWT ↔ `JWTObject`

📌 **Aqui acontece:**

* Assinatura HMAC
* Decodificação Base64
* Validação de integridade

📌 **Erro real enfrentado:**

```
WeakKeyException
```

➡️ chave com menos de **256 bits**

📌 **Aprendizado:**
JWT **não aceita chave fraca**. Isso não é opcional.

---

## 🔹 `JWTFilter`

📌 **Classe MAIS IMPORTANTE do projeto**

📌 **O que é?**
Um `OncePerRequestFilter` que roda **em TODA requisição HTTP**.

📌 **Responsabilidades:**

1. Ler o header `Authorization`
2. Extrair o token
3. Validar assinatura e expiração
4. Criar `Authentication`
5. Popular o `SecurityContext`

📌 **Fluxo interno:**

```text
Request HTTP
     ↓
JWTFilter
     ↓
Token existe?
 ┌───────┴────────┐
 │                │
Sim              Não
 │                │
Valida token   Limpa contexto
 │
Cria Authentication
 │
SecurityContextHolder
```

📌 **Sem esse filtro:**
Mesmo com token válido, o Spring Security **não reconhece o usuário**.

---

## 🔹 `SecurityFilterChain`

📌 **Substitui o antigo** `WebSecurityConfigurerAdapter`

📌 **Aqui é definido:**

* Rotas públicas (`/login`)
* Rotas protegidas
* Política stateless
* Ordem dos filtros

📌 **Trecho mais crítico:**

```java
.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
```

➡️ garante que o JWT seja processado **antes** da autenticação padrão

---

## 🔹 `LoginController`

📌 **Função:** autenticar usuário e gerar JWT

📌 **Fluxo:**

```text
/login
 ↓
Busca usuário no banco
 ↓
Valida senha (PasswordEncoder)
 ↓
Cria JWTObject
 ↓
Gera token
 ↓
Retorna Sessao
```

📌 **Importante:**

* Não existe sessão
* Não existe estado no servidor

---

# 🧩 Camadas da Aplicação (o que faltava)

## 🔹 Entidades (`model`)

### `User`

Representa o usuário persistido no banco.

Responsabilidades:

* Armazenar `username`, `password` e `roles`
* Mapear relacionamento **User ↔ Roles**

Aprendizado:

* O Spring Security **não usa diretamente essa classe**
* Ela serve como **fonte de dados** para autenticação

---

## 🔹 Repositórios (`repository`)

### `UserRepository`

Interface JPA responsável por acessar o banco.

Método crítico:

* `findByUsername(String username)`

Esse método é a **ponte entre o login e o banco**.

---

## 🔹 Service Layer (`service`)

### `UserService`

Responsabilidades:

* Criar usuários
* Aplicar `PasswordEncoder`
* Garantir que senhas **nunca sejam salvas em texto plano**

Fluxo:

```text
Controller
  ↓
UserService
  ↓
PasswordEncoder
  ↓
Repository
```

Aprendizado:

> Segurança começa **antes** do login

---

## 🔹 Controllers adicionais

### `UserController`

Endpoint para criação de usuários.

```http
POST /users
```

Responsabilidade:

* Delegar criação ao `UserService`

Importante:

* Endpoint geralmente público apenas para estudo
* Em produção, deveria ser restrito

---

### `WelcomeController`

Controller simples para testar proteção por role.

Aprendizado:

* Excelente para validar se o JWTFilter está funcionando

---

# 🌐 Endpoints HTTP Disponíveis

## 🔓 Público

### 🔹 Login

```http
POST /login
Content-Type: application/json
```

```json
{
  "username": "admin",
  "password": "123"
}
```

---

## 🔒 Protegidos (exemplo)

```http
GET /admin
Authorization: Bearer <token>
```

📌 Sem token → `403 Forbidden`
📌 Token inválido → `403 Forbidden`
📌 Token válido → `200 OK`

---

# 🧪 H2 Database

* Console:

```
http://localhost:8080/h2-console
```

* JDBC URL:

```
jdbc:h2:mem:testdb
```

---

# ❌ Erros Reais que Aconteceram (Aprendizado)

| Erro             | Causa          | Aprendizado                  |
| ---------------- | -------------- | ---------------------------- |
| WeakKeyException | chave fraca    | JWT exige ≥ 256 bits         |
| Base64 error     | chave inválida | chave deve ser Base64 válida |
| 403 inesperado   | filtro ausente | ordem dos filtros importa    |
| senha inválida   | encoder errado | hash ≠ texto plano           |

---

# 🧠 Checklist Mental de JWT (para nunca esquecer)

Antes de qualquer bug:

* [ ] Token está chegando no header `Authorization`?
* [ ] Prefixo está correto (`Bearer `)?
* [ ] Chave é Base64 válida?
* [ ] Chave tem **≥ 256 bits**?
* [ ] `subject` foi setado no JWT?
* [ ] Roles estão no formato `ROLE_XXX`?
* [ ] Filtro JWT está **antes** do `UsernamePasswordAuthenticationFilter`?
* [ ] `SessionCreationPolicy.STATELESS` está ativo?

Se algum item falhar → **403 garantido**.

---

# 🔎 Fluxo de Debug (onde colocar breakpoint)

## Login

1. `LoginController.logar()`
2. `encoder.matches()`
3. `JWTCreator.create()`

## Request autenticada

1. `JWTFilter.doFilterInternal()`
2. Leitura do header
3. `JWTCreator.create(token, ...)`
4. `SecurityContextHolder.setAuthentication()`

Se o erro acontece **antes** do controller → problema é segurança.

---

# 🧠 Mapa Mental do Spring Security

```text
HTTP Request
     ↓
Security Filter Chain
     ↓
JWTFilter
     ↓
SecurityContextHolder
     ↓
Authorization (roles)
     ↓
Controller
```

Regra de ouro:

> Se o `SecurityContext` não foi populado, nada depois funciona.

---

# 💼 Explicação em Nível de Entrevista Técnica

### ❓ Por que usar JWT?

* Escalável
* Stateless
* Ideal para microsserviços

### ❓ Onde ocorre a autenticação?

No **filtro**, não no controller.

### ❓ O Spring guarda sessão?

Não. O token carrega o estado.

### ❓ Onde ocorre autorização?

Após o filtro, com base nas roles.

### ❓ Principal erro de iniciantes?

Achar que JWT é só gerar token.

---

# 🧠 Conclusão

Este projeto prova que:

> **JWT não é difícil — ele é rigoroso.**

O Spring Security:

* não perdoa atalhos
* exige entendimento do fluxo
* recompensa quem domina filtros e contexto

Este repositório é:

* 📚 meu banco de aprendizado
* 🔐 minha referência de segurança
* 🚀 base para projetos profissionais

---

📌 *"Se você entende o filtro, você domina o Spring Security."*
