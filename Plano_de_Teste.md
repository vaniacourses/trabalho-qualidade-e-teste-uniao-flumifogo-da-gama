# Plano de Teste

## Registro de Mudanças

| Versão | Data       | Autor          | Descrição |
|--------|------------|----------------|-----------|
| 1.0    | 29/04/2026 | Italo Coelho   | Criação do plano de teste para o ecossistema integrado do Sistema Bancário e Web App utilizando IA. Foi utilizada a ferramenta de IA acoplada ao VSCode (Claude), a partir do contexto do repositório e do documento de template do plano de teste. Link do prompt: https://gemini.google.com/share/96abc3585ba4 |

| Versão | Data       | Autor          | Descrição |
|--------|------------|----------------|-----------|
| 2.0    | 06/05/2026 | Eric Leal      | Alteração do plano de teste com adequações e contextualização de algumas partes | 


## 1. Introdução

Este plano de teste documenta a estratégia de qualidade para o ecossistema composto pelo Sistema Bancário (back-end) e o Web App (front-end). O foco é garantir que as regras de negócio do banco sejam validadas por meio de testes unitários automatizados, enquanto os fluxos de interface e aceitação da aplicação web são validados com testes manuais executados e registrados na planilha `plano_testes_manuais`.

A estratégia adotada é híbrida: testes de baixo nível garantem a estabilidade das regras financeiras no back-end, e testes manuais de alto nível validam a integração do usuário final com o sistema bancário via interface web. O plano também considera processos de triagem de defeitos, critérios de suspensão/retomada.

### 1.1 Escopo

**No escopo**

- Sistema Bancário:
  - Validação de regras de negócio financeiras, incluindo contas, transações, saldos, juros e limites.
  - Testes unitários existentes e em execução no projeto de back-end (`winxbank`) cobrindo classes de domínio e serviços.
  - Verificação de integração mínima entre os módulos de persistência e regras de negócio quando aplicável.
  - Classes abordadas:
    * CartaoCreditoTest.java
    * ClienteWinxTest.java
    * ContaCorrenteTest.java
    * ContaPoupancaTest.java
    * ContaTest.java 

- Web App:
  - Testes manuais de aceitação e interface para funcionalidades de login, consulta de saldo, transferência, cadastro/edição de cliente e navegação geral.
  - Execução e evidência de casos de teste na planilha `plano_testes_manuais`.
  - Verificação de comportamento em navegadores principais, usabilidade e consistência visual para os fluxos críticos.


**Fora do escopo**

- Testes automatizados de regressão UI/UX no Web App, como testes com Selenium ou Cypress.
- Testes de performance e carga para o Web App e o back-end.
- Testes de segurança aprofundados como pentest, análise de vulnerabilidades ou scan de API.
- Implantação em produção, validação de rede corporativa específica e segurança de infraestrutura.
- Testes de compatibilidade móvel além de navegadores modernos.

### 1.2 Objetivos de Qualidade

- Ainda não projetado no escopo da primeira entrega.

### 1.3 Papéis e Responsabilidades

1.3 Papéis e Responsabilidades
Equipe de Testes (Eric, Arthur, Gabriel, Italo, João Vitor):
Participar da definição do escopo, cenários de teste e estratégias adotadas.
Atuar de forma colaborativa na elaboração, revisão e execução dos testes.
Executar testes manuais e unitários, garantindo a validação das funcionalidades do sistema.

Destaques de Contribuição:
Gabriel:
Responsável pela preparação e provisionamento dos ambientes de teste nos repositórios.
Gabriel e Italo:
Atuou no suporte à execução dos testes web, especialmente utilizando Docker para configuração dos ambientes.
Eric:
Responsável pela padronização e elaboração do documento de plano de testes manuais.
Arthur, Gabriel, Italo, João Vitor:
Contribuir na identificação, registro e análise de falhas encontradas.

## 2. Metodologia de Teste

### 2.1 Visão Geral

A metodologia é baseada em testes incrementais e em camadas: o back-end recebe testes automatizados de unidade para garantir qualidade técnica e regras de negócio; o front-end é coberto com testes manuais de aceitação que exercitam fluxos reais de usuário. Esta abordagem equilibra a velocidade da automação no banco com a validação humana da experiência do Web App.

### 2.2 Fases de Teste

- Testes Unitários no back-end:
  - Execução contínua dos testes automatizados já implementados no projeto `winxbank`.
  - Cobertura de classes de domínio, operações de conta, cálculos de juros e validações de entrada..

- Testes Manuais Funcionais no Web App:
  - Execução dos casos de uso críticos registrados na planilha `plano_testes_manuais`.
  - Verificação de login, navegação, exibição de dados bancários, operações de transferência e mensagens de erro.

### 2.3 Triagem de Erros

- Registro:
  - Defeitos identificados durante testes manuais ou unitários são reportados em [Github Issues].
  - Cada defeito deve conter descrição, tipo e evidência quando aplicável.

### 2.4 Critérios de Suspensão e Requisitos de Retomada

Critérios de suspensão:
- Mais de 10% dos casos de teste manuais críticos falham sem solução imediata.
- Uma falha crítica no back-end impede a execução da maior parte dos testes unitários ou invalida resultados de integração.
- Ambiente de teste indisponível por incidente de infraestrutura.

Condições de retomada:
- Defeitos críticos identificados foram corrigidos e validados.
- O ambiente de teste está estável e acessível.
- Regressão mínima confirmada após a correção, com novos testes unitários ou execução dos casos afetados.

### 2.5 Completude do Teste

Critérios de sucesso:
- 100% dos testes unitários automatizados do Sistema Bancário estão passando.
- Todos os casos críticos e prioritários da planilha `plano_testes_manuais` foram executados e aprovados.
- Defeitos críticos e de alta prioridade detectados foram corrigidos e revalidados.
- Nenhum bloqueio conhecido permanece para os fluxos de negócio mais importantes.

### 2.6 Atividades do projeto, estimativas e cronograma

Atividades previstas:
- Preparação de ambiente e revisão do plano: 1 dia.
- Execução de testes unitários do back-end: 2 dias.
- Revisão e atualização da planilha de casos manuais: 2 dia.
- Execução de testes manuais no Web App: 2 dias.
- Triagem de defeitos e revisões com desenvolvimento: 1 dias.

Cronograma fictício:
- Dia 1: Revisão do plano e configuração dos ambientes.
- Dia 2: Execução inicial dos testes unitários do Sistema Bancário.
- Dia 3: Revisão e priorização dos casos manuais do Web App, execução dos testes manuais e registro de resultados.
- Dia 4: Registro falhas.
- Dia 5: Documentação dos testes unitários e manuais.

## 3. Entregáveis de Teste

- Plano de Teste documentado.
- Planilha de casos de teste manuais `plano_testes_manuais` com resultados e evidências.
- Relatórios de execução de testes unitários e status de cobertura.
- Registro de defeitos.

## 4. Necessidades de Recursos e Ambiente

### 4.1 Ferramentas de Teste

- [Github Issues] para gerenciamento de defeitos e rastreamento de bugs.
- Framework de testes unitários para o Sistema Bancário (`JUnit`, `Mockito` ou equivalente) no projeto Java.
- Planilha eletrônica para o Web App (`Excel`, `Google Sheets`) contendo `plano_testes_manuais`.
- Ferramenta de controle de versão para acessar código-fonte e histórico.
- Ferramenta de comunicação/colaboração para revisão de defeitos e status.

### 4.2 Ambiente de Teste

- Servidor de aplicação ou ambiente local para execução do back-end do Sistema Bancário.
- Ambiente de desenvolvimento para o Web App Angular, com acesso ao frontend e à API do banco.
- Navegadores modernos compatíveis para teste do Web App (por exemplo, Chrome, Edge, Firefox).
- Máquina com suporte a execução de testes unitários e ambiente Node/Java conforme necessário.
- Acesso aos dados de teste e configurações necessárias para simular contas e transações.

## 5. Termos / Acrônimos

| Termo           | Definição |
|-----------------|-----------|
| SUT             | **Software Under Test**: conjunto de software sendo testado — neste caso, o Sistema Bancário e o Web App integrados. |
| API             | **Application Programming Interface**: interface usada pelo Web App para comunicação com o back-end bancário. |
| QA              | **Quality Assurance**: atividades relacionadas à garantia de qualidade. |
| Teste Unitário  | Teste de menor nível que valida unidades isoladas de código. |
| Teste Manual    | Teste realizado por um analista, seguindo passos documentados, sem automação. |
| Defeito         | Problema ou discrepância entre o comportamento observado e o esperado. |
| Regressão       | Introdução de defeitos em funcionalidades que anteriormente estavam funcionando. |
| Plano de Teste  | Documento que descreve escopo, metodologia, critérios e entregáveis dos testes. |
| Caso de Teste   | Conjunto de condições e etapas executadas para validar um requisito específico. |
