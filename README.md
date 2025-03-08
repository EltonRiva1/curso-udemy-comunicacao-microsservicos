# Comunicação entre Microsserviços

![Badge](https://img.shields.io/badge/Status-%20Concluído-green) ![Java](https://img.shields.io/badge/Java-17-blue) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen) ![Node.js](https://img.shields.io/badge/Node.js-14-green) ![Docker](https://img.shields.io/badge/Docker-20.10.7-blue)

Este projeto é uma implementação baseada no curso "Comunicação entre Microsserviços" da Udemy, ministrado por [Vinícius Negrisoli](https://github.com/vhnegrisoli). O objetivo é demonstrar a comunicação entre microsserviços utilizando tecnologias como Java, Spring Boot, Node.js, Docker, RabbitMQ, entre outras.

## 🚀 Tecnologias Utilizadas

- **Java 17**
- **Spring Boot 3.2.5**
- **Node.js 14**
- **Express.js**
- **PostgreSQL**
- **MongoDB**
- **RabbitMQ**
- **Docker**
- **Docker Compose**
- **JWT (JSON Web Token)**
- **Feign Client**

## 📂 Estrutura do Projeto

O projeto está dividido em três principais microsserviços:

1. **Auth-API**: Responsável pela autenticação e autorização dos usuários.
2. **Product-API**: Gerencia as operações relacionadas aos produtos.
3. **Sales-API**: Lida com as operações de vendas e integração com os demais serviços.

## 🔐 Segurança

A segurança do sistema é garantida através da implementação de autenticação e autorização utilizando JWT (JSON Web Token). Cada requisição aos microsserviços é validada para assegurar que o usuário possui as permissões necessárias.

## 🐳 Docker

O projeto utiliza Docker para containerização dos microsserviços, garantindo portabilidade e facilidade de deploy. O arquivo `docker-compose.yml` orquestra os containers dos serviços, incluindo dependências como bancos de dados e mensageria.

## 🔄 Comunicação entre Microsserviços

A comunicação entre os microsserviços é realizada de duas formas:

- **Síncrona**: Utilizando Feign Client para chamadas HTTP entre os serviços.
- **Assíncrona**: Através do RabbitMQ, que permite a troca de mensagens entre os serviços de forma eficiente e escalável.

## 📦 Como Executar o Projeto

### Pré-requisitos

- Docker instalado na máquina.
- Docker Compose configurado.

### Passos para Execução

1. Clone o repositório:

   ```bash
   git clone https://github.com/EltonRiva1/curso-udemy-comunicacao-microsservicos.git
   ```

2. Acesse o diretório do projeto:

   ```bash
   cd curso-udemy-comunicacao-microsservicos
   ```

3. Suba os containers utilizando o Docker Compose:

   ```bash
   docker-compose up
   ```

   Este comando irá construir e iniciar os microsserviços juntamente com suas dependências.

4. Acesse os serviços através das seguintes URLs:

   - **Auth-API**: `http://localhost:3000`
   - **Product-API**: `http://localhost:8081`
   - **Sales-API**: `http://localhost:8082`

## 🛠️ Endpoints Principais

### Auth-API

- **POST** `/api/auth/login`: Autentica um usuário e retorna um token JWT.
- **POST** `/api/auth/register`: Registra um novo usuário.

### Product-API

- **GET** `/api/products`: Lista todos os produtos.
- **POST** `/api/products`: Adiciona um novo produto.

### Sales-API

- **GET** `/api/sales`: Lista todas as vendas.
- **POST** `/api/sales`: Realiza uma nova venda.

## 🧪 Testes

Para executar os testes unitários e de integração, utilize os seguintes comandos dentro de cada microsserviço:

```bash
# Para serviços Java com Maven
./mvnw test

# Para serviços Node.js
npm test
```

---

🔹 Desenvolvido por [Elton Riva](https://github.com/EltonRiva1) 🚀

---

**Nota**: Este README foi elaborado com base nas informações disponíveis no repositório e referências relacionadas. Para detalhes específicos, consulte a documentação oficial de cada tecnologia utilizada. 
