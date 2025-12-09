# HS TimeCheck – Sistema de Gestão de Validades e Fluxos Operacionais

![Android](https://img.shields.io/badge/Android-14-green?logo=android)
![Kotlin](https://img.shields.io/badge/Kotlin-2.0-blue?logo=kotlin)
![Firebase](https://img.shields.io/badge/Firebase-Firestore-orange?logo=firebase)
![Status](https://img.shields.io/badge/Status-Em%20Desenvolvimento-yellow)
![License](https://img.shields.io/badge/Licença-Livre-lightgrey)

O **HS TimeCheck** é um sistema completo para controle de validades, auditoria operacional, fluxo comercial e gestão inteligente de produtos no varejo.  
O aplicativo pode utilizar informações de produtos obtidas através do **Open Food Facts**, incluindo dados nutricionais e imagens públicas licenciadas.

> **Dados e imagens provenientes do Open Food Facts são utilizados sob as licenças ODbL 1.0 e CC-BY-SA.**  
> Fonte: https://openfoodfacts.org  

---

# 📱 Mockups do Aplicativo
*(Substituir pelas imagens reais quando disponíveis)*

### Dashboard  
![Dashboard](mockups/dashboard_mockup.png)

### Scanner  
![Scanner](mockups/scanner_mockup.png)

### Tela do Produto  
![Produto](mockups/produto_mockup.png)

### Gerenciamento de Validades  
![Validades](mockups/validades_mockup.png)

---

# 🔧 Funcionalidades Principais

- Scanner inteligente (EAN-13 e códigos 20…)
- Consulta automática ao **Open Food Facts** quando disponível  
- Cadastro automático e manual de produtos
- Controle avançado de validades e alertas
- Status comerciais (Normal, Vencendo, Trabalhando Preço, Aguardando Aprovação)
- Status operacionais (Vencido, Verificação de Estoque)
- Histórico completo de todas as ações
- Importação de planilhas (padrão e customizadas)
- Exportação (CSV, Excel, PDF)
- Dashboard com indicadores
- Seleção múltipla com ações em lote
- Integração com Firebase Firestore

---

# 🧬 Modelo de Dados (UML)

## Produto
+----------------------+
| Produto |
+----------------------+
| id: UUID |
| codigoBarras: String |
| codigoInterno: String|
| descricao: String |
| precoAtual: Double |
| quantidadeAtual: Int |
| vendaDia: Int |
| validadeAtual: Date |
| status: Enum |
| statusOperacional: Enum |
+-------------------------------+
| validades: List<ValidadeItem> |
| historico: List<HistoricoItem>|
+-------------------------------+

shell
Copiar código

## Services
ProductService
├ carregarProdutos()
├ inserirOuAtualizar()
├ mudarStatus()
├ registrarVenda()
├ atualizarPreco()
├ atualizarQuantidade()
├ adicionarValidade()
└ removerValidade()

ProductRepository
├ carregarProdutos()
├ salvarProduto()
├ atualizarProduto()
└ deletarProduto()

shell
Copiar código

## Fluxo de Status
NORMAL → VENCENDO → VENCIDO
↑ ↓ ↓
| AGUARDANDO_APROVACAO
└────────── TRABALHANDO_PRECO

yaml
Copiar código

---

# 🛠 Instalação

1. Gere o APK via Android Studio ou Buildozer  
2. Transfira para o dispositivo  
3. Ative “Fontes desconhecidas”  
4. Instale o aplicativo  

---

# ☁ Configuração do Firebase

1. Crie um projeto no Firebase  
2. Ative o Firestore  
3. Baixe `google-services.json`  
4. Coloque em: `app/src/main/`
5. No build.gradle:
```gradle
implementation 'com.google.firebase:firebase-firestore-ktx'
implementation 'com.google.firebase:firebase-storage-ktx'
📥 Importação e 📤 Exportação
Importação
Planilha padrão HS TimeCheck

Planilhas personalizadas

Mapeamento automático

Exportação
CSV

Excel

PDF

Histórico individual por produto

🧠 Dashboard
Exibe:

Produtos vencidos

Produtos prestes a vencer

Pendentes de aprovação

Itens em verificação de estoque

Ações rápidas e atalhos essenciais

📝 Seleção Múltipla
Ações em lote:

Trabalhar preço

Enviar para aprovação

Verificação de estoque

Excluir

Voltar ao normal

Registrar histórico individual

📁 Estrutura do Projeto
css
Copiar código
src/
 ├ ui/
 ├ adapters/
 ├ models/
 ├ services/
 ├ repository/
 ├ scanner/
 └ utils/
🌎 English Version (Short)
HS TimeCheck – Smart Expiration & Retail Operations Manager
HS TimeCheck is a complete retail solution for managing expiration dates, commercial workflows, operational alerts, and product lifecycle.

Features
Smart barcode scanning

Automatic product creation (with Open Food Facts fallback)

Multi-status workflow

Complete event tracking

CSV / Excel / PDF export

Firebase Firestore backend

Architecture
nginx
Copiar código
UI → ProductService → HistoryService → ProductRepository → Firestore
Open Food Facts Attribution
Data and images from OpenFoodFacts.org
Licensed under ODbL 1.0 and CC BY-SA

👤 Autor
Herlony Santiago – HS Solutions

📄 Licença
MIT (or any you choose)