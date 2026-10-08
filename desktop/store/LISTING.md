# Ficha da Microsoft Store (pt-BR)

Textos prontos para colar no Partner Center. Nada aqui é segredo; a conta de teste para a certificação NÃO fica neste arquivo.

## Identidade

| Campo | Valor |
|---|---|
| Nome do app | Astra: Ecossistema de Estudos |
| Categoria | Educação (alternativa: Produtividade) |
| Site | https://astra-app.dev |
| Política de privacidade | https://astra-app.dev/privacidade |
| Termos de uso (opcional) | https://astra-app.dev/termos |
| Contato de suporte | contato.astraapp@gmail.com |
| Direitos autorais | © 2026 Kauan Di Nubila |
| Idioma | Português (Brasil) |
| Preço | Grátis |

## Descrição curta (até 270 caracteres)

Cronômetro de estudo com Pomodoro, metas, roadmaps, ranking entre amigos, chat e chamadas em um só lugar. Acompanhe seu tempo de foco e estude junto com quem você quer.

## Descrição

O Astra reúne tudo o que você usa para estudar em um só app, com uma base de dados única: o que você registra numa tela aparece nas outras.

Estude com foco
• Cronômetro de sessões e Pomodoro, com modo foco em tela cheia.
• Categorias, cursos e metas para organizar o que você estuda.
• Dashboard com tempo de foco, sequência de dias e atividade do último ano.

Saiba para onde ir
• Roadmaps de aprendizado com etapas e progresso, prontos ou criados por você.
• Vincule seus cursos às etapas de cada roadmap e acompanhe o que já concluiu.

Estude junto
• Ranking entre amigos para manter a constância.
• Chat em tempo real, em conversas ou grupos.
• Chamadas de voz e vídeo e compartilhamento de tela, com som do computador e escolha entre nitidez e fluidez.

Integração com o GitHub
• Conecte sua conta e acompanhe commits, pull requests, issues e linguagens ao lado do seu tempo de estudo.

No Windows
• Janela própria, notificações nativas e a chamada continua firme mesmo com o app em segundo plano.

Seus dados são seus
• Você pode baixar tudo o que o Astra guarda sobre você e excluir a conta quando quiser, dentro do próprio app.

O Astra precisa de conexão com a internet.

## O que há de novo

Primeira versão do Astra na Microsoft Store.

## Recursos (um por linha, até 200 caracteres cada)

1. Sessões de estudo com cronômetro, Pomodoro e modo foco em tela cheia
2. Metas, cursos e categorias para organizar o que você estuda
3. Dashboard com tempo de foco, sequência de dias e atividade do último ano
4. Roadmaps de aprendizado com progresso por etapa
5. Ranking entre amigos para manter a constância
6. Chat em tempo real em conversas e grupos
7. Chamadas de voz e vídeo com compartilhamento de tela e som do computador
8. Integração com o GitHub: commits, pull requests, issues e linguagens
9. Notificações nativas do Windows
10. Baixe seus dados e exclua sua conta quando quiser

## Termos de busca (até 7, cada um com até 30 caracteres)

pomodoro, estudos, foco, cronômetro de estudo, roadmap, ranking de estudo, produtividade

## Imagens

| Arquivo | Uso |
|---|---|
| `screenshots/01-dashboard.png` ... `04-cursos.png` | Capturas de tela (2880×1800) |
| `StoreIcon-300.png` | Ícone do app na listagem (300×300) |

Quando houver prints de chamada e chat, acrescente-os (a Store aceita até 10).

## Classificação indicativa (questionário IARC)

- Sem violência, sexo, drogas, linguagem imprópria ou apostas.
- Há interação entre usuários (chat e chamadas) e conteúdo gerado por usuários: marque "sim" nessas perguntas. Isso costuma elevar a classificação para 12 ou 14 anos.
- Compartilha localização: não.
- Compras no app: não.

## Capacidades do pacote

O app usa a capacidade restrita `runFullTrust`. Justificativa para o formulário: "O Astra é um aplicativo Win32 empacotado (Electron/Chromium). A capacidade runFullTrust é necessária para executar o processo do aplicativo desktop."

## Notas para a certificação (até 2000 caracteres)

Modelo para colar. Preencha o e-mail e a senha de uma conta de teste só para os revisores (não use a sua conta pessoal) e troque a senha depois da aprovação.

```
O Astra abre o site https://astra-app.dev numa janela própria e precisa de internet.

Para testar: na tela inicial, escolha "Entrar" e use a conta de teste:
E-mail: <e-mail da conta de teste>
Senha: <senha da conta de teste>

O login com Google ou GitHub abre o navegador padrão do sistema e volta para o app pelo endereço astra://; o e-mail e a senha acima funcionam sem isso.

Microfone e câmera só são pedidos ao entrar numa chamada. O compartilhamento de tela abre um seletor próprio do app. As atualizações do app são feitas pela Microsoft Store.

Política de privacidade: https://astra-app.dev/privacidade
Contato: contato.astraapp@gmail.com
```

## Passo a passo

1. Criar a conta de desenvolvedor (gratuita para pessoa física): https://partner.microsoft.com/dashboard
2. Em Microsoft Store > Aplicativos e jogos > Novo produto, reservar o nome "Astra: Ecossistema de Estudos".
3. Em Gerenciamento do produto > Identidade do produto, copiar três valores: Package/Identity/Name, Package/Identity/Publisher e Package/Properties/PublisherDisplayName.
4. Gerar o pacote (veja o README do desktop): `npm run dist:store`, com esses valores nas variáveis `ASTRA_STORE_IDENTITY_NAME`, `ASTRA_STORE_PUBLISHER` e `ASTRA_STORE_PUBLISHER_DISPLAY_NAME`.
5. Criar o envio, enviar o `.appx` de `release-store/` e preencher a ficha com este arquivo.
6. Enviar para certificação (costuma levar de 1 a 3 dias úteis).
