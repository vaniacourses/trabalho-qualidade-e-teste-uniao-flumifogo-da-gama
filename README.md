# Trabalho (Entrega 2) - Qualidade e Teste de Software

Repositório contendo a segunda entrega do trabalho prático acadêmico de Qualidade e Teste de Software. Esta etapa aprofunda as práticas de garantia de qualidade, introduzindo testes de integração, isolamento de dependências, métricas estruturais rigorosas, inspeção de código e testes automatizados de sistema.

O projeto continua dividido em dois sistemas com abordagens de qualidade distintas: **WinxBank** (foco em cobertura estrutural, mutação e inspeção de código) e **Webapp-1** (foco em avaliação de qualidade ISO 25010, testes E2E e não funcionais).

## 📄 Documentação (Google Docs)
*Conforme exigência, os documentos foram elaborados de forma colaborativa no Google Docs para registro de histórico.*

* **Avaliação de Qualidade (ISO 25010):** [[Documento](https://docs.google.com/document/d/1Hy3M7kfq1z5C7sG1zR2PlqwsO_UjCIY6E0erjkjWPTo/edit?tab=t.0#heading=h.u4t64fhy3m02)]
* **Relatório de Resultados (Testes, Cobertura, Mutação e SonarQube):** [[Documento](https://docs.google.com/document/d/1yEef11D-6ugCnXzZRFyFjp-3QiBtWCAQXwihWo8W1Is/edit?tab=t.0)]
* **Relatório de Testes de Sistema (E2E e Não Funcionais):** [Insira o link do Google Docs aqui]

---

## 🏦 1. WinxBank - Sistema Bancário (Testes de Backend)
**Localização:** `./winxbank/`

Sistema bancário desenvolvido em Java com Maven. Nesta entrega, o foco evoluiu da criação básica de testes para a validação rigorosa da arquitetura e qualidade do código.

### Estratégias e Ferramentas Aplicadas
* **Testes Unitários com Mocks:** Utilização da biblioteca **Mockito** para isolar dependências em classes complexas (ex: `CartaoCredito`, `ContaCorrente`, `Conta`, `ClienteWinx`), garantindo testes unitários puros.
* **Testes de Integração:** Implementação de cenários reais validando a comunicação entre classes reais e regras de negócio transacionais.
* **Cobertura Estrutural (JaCoCo):** Adequação dos casos de teste para atingir a meta mínima de **80% de cobertura de arestas (branches)** nas classes avaliadas.
* **Testes Baseados em Defeitos (Pitest):** Injeção de mutantes no código de produção para validar a força da suíte de testes, alcançando um escore de mutação de no mínimo **80%** para as classes selecionadas.
* **Inspeção de Código Estática (SonarQube):** Análise do código-fonte para identificação de *code smells* e vulnerabilidades. Os relatórios contêm o "antes e depois" da refatoração das classes pelos membros da equipe.

* ### Como Executar os Relatórios

## 🎬 2. Webapp-1 - Sistema de Avaliação (Testes de Sistema e Qualidade)
**Localização:** `./webapp-1/`

Aplicação web para avaliação de mídias (Spring Boot + Angular). Nesta entrega, a validação manual deu lugar a avaliações normativas e automação E2E.

### Estratégias e Ferramentas Aplicadas
* **Avaliação Normativa (ISO/IEC 25010):** Documento detalhado indicando as medidas dos atributos de qualidade do sistema (Adequação Funcional, Eficiência, Compatibilidade, Usabilidade, Confiabilidade, Segurança, Manutenibilidade e Flexibilidade) em uma escala de 1 a 5, validando as especificações ideais do produto.
* **Testes de Sistema (E2E - End-to-End):** Implementação de testes de interface utilizando **Selenium** (ou ferramenta similar). Foram desenvolvidos testes automatizados cobrindo os fluxos principais de cada membro da equipe.
* **Testes Não Funcionais:** Avaliação prática de atributos específicos de qualidade, como testes de carga/desempenho ou validação de segurança da aplicação web.

* ### Como Executar os Testes E2E
