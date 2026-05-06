# Trabalho (Entrega 1) - Qualidade e Teste de Software

Repositório contendo a primeira entrega do trabalho prático de Qualidade e Teste de Software, com implementação de testes unitários e testes manuais em dois sistemas diferentes.

## Documentos
Documento do plano de teste: [plano_de_Teste.pdf](https://github.com/user-attachments/files/27451145/Plano_de_Teste.pdf) <br/>
Documento dos testes unitários: [plano_testes_unitarios.pdf](https://github.com/user-attachments/files/27446711/plano_testes_unitarios.pdf) <br/>
Documento do testes manuais: [plano_testes_manuais.xlsx](https://github.com/user-attachments/files/27446741/plano_testes_manuais.xlsx) <br/>
Documento teste manual utilizando a ferramenta TestLink: [teste_manual_testlink.pdf](https://github.com/user-attachments/files/27092109/testreport.Teste.de.funcionalidade.do.site.TrackOrJargh.pdf)


## Separação dos códigos fonte
- Pasta winxbank é o código fonte do sistema bancário onde foram feitos os testes unitários.
- Pasta webapp-1 é o código fonte do sistema de avaliação de séries/filmes/livros onde foram feitos os testes manuais.

---

## 📋 Estrutura do Projetos

Este projeto está dividido em dois sistemas com estratégias de teste distintas:

### 1. **WinxBank** - Sistema Bancário (Testes Unitários)
Localização: `./winxbank/`

Sistema bancário desenvolvido em **Java com Maven**, com foco em testes unitários automatizados.

**Tecnologias:**
- Java
- Maven (gerenciamento de dependências)
- JUnit (framework de testes)

### Localização
`winxbank/test/br/winxbank/`

### Arquivos de Teste Implementados
- **CartaoCreditoTest.java** - Testes da funcionalidade de cartão de crédito
- **ClienteWinxTest.java** - Testes da entidade Cliente
- **ContaCorrenteTest.java** - Testes da conta corrente
- **ContaPoupancaTest.java** - Testes da conta poupança
- **ContaTest.java** - Testes genéricos de conta

### Como Executar

Dentro da pasta `winxbank/`, execute:
```bash
mvn test
```

Para executar um teste específico:
```bash
mvn test -Dtest=NomeDoTesteTest
```

Para executar a aplicação:
```bash
mvn clean compile exec:java
```

---

### 2. **Webapp-1** - Sistema de Avaliação (Testes Manuais)
Localização: `./webapp-1/`

Aplicação web para avaliação de séries, filmes e livros com backend em **Spring Boot** e frontend em **Angular**, com foco em testes manuais.

**Tecnologias:**
- Backend: Spring Boot (Java)
- Frontend: Angular (TypeScript)
- Banco de Dados: MySQL (Docker)
- Containerização: Docker

**Localização do Plano de Testes:** <br/>
`plano_testes_manuais.csv`

**Como Executar**
```bash
docker-compose -f docker/docker-compose.yml up
```

**Cobertura de Testes**
Os testes manuais cobrem fluxos críticos da aplicação, incluindo:
- Gerenciamento de perfil de usuário
- Validações de entrada
- Fluxos de navegação
