import { LegalLink } from "@/context/LegalContext"
import { LegalLayout } from "@/components/LegalLayout"
import { LEGAL_CONTACT_EMAIL, LEGAL_CONTROLLER } from "@/lib/legal"

export function PrivacyContent() {
  return (
    <>
      <section>
        <p>
          Esta política explica quais dados pessoais o Astra coleta, para que usa, com quem compartilha e quais são
          os seus direitos, de acordo com a Lei Geral de Proteção de Dados (Lei nº 13.709/2018, a LGPD).
        </p>
        <p>
          O Astra (astra-app.dev) é um projeto pessoal e gratuito, sem fins comerciais. O controlador dos dados é{" "}
          {LEGAL_CONTROLLER}. Para qualquer assunto sobre privacidade, escreva para{" "}
          <a href={`mailto:${LEGAL_CONTACT_EMAIL}`}>{LEGAL_CONTACT_EMAIL}</a>.
        </p>
      </section>

      <section>
        <h2>1. Dados que coletamos</h2>
        <ul>
          <li>
            <strong>Conta:</strong> nome, e-mail, identificador (nome#número), bio e foto de perfil. A senha nunca é
            guardada: guardamos só uma versão embaralhada dela (hash BCrypt).
          </li>
          <li>
            <strong>Login com Google ou GitHub:</strong> nome, e-mail e foto que o provedor nos envia quando você
            escolhe entrar por ele.
          </li>
          <li>
            <strong>Uso do app:</strong> sessões de foco (duração, data, nota, categoria e curso), cursos e módulos,
            metas e roadmaps.
          </li>
          <li>
            <strong>Social:</strong> amizades, grupos, mensagens e imagens enviadas no chat. As mensagens e as
            imagens são guardadas cifradas.
          </li>
          <li>
            <strong>Chamadas:</strong> áudio, vídeo e tela compartilhada vão direto entre os participantes e não são
            gravados. Quando a conexão direta não é possível, o tráfego passa por um servidor de repasse (relay),
            continua cifrado e o servidor não tem acesso ao conteúdo. Do lado do servidor, só sabemos quem está em
            cada chamada enquanto ela acontece; isso não é salvo.
          </li>
          <li>
            <strong>GitHub (opcional):</strong> se você conectar sua conta, guardamos o login, a foto, o token de
            acesso (cifrado) e um resumo da sua atividade: commits, pull requests, issues, repositórios e linguagens.
          </li>
          <li>
            <strong>Dados técnicos:</strong> endereço IP e informações da conexão, usados para segurança (por
            exemplo, limitar tentativas de login) e tratados também pelos provedores de infraestrutura listados
            abaixo.
          </li>
          <li>
            <strong>Aceite dos termos:</strong> a data em que você aceitou os Termos de Uso e esta política, e qual
            versão você aceitou.
          </li>
          <li>
            <strong>Cookies:</strong> usamos um único cookie, essencial, para manter você conectado. Não usamos
            cookies de anúncio, rastreamento ou analytics.
          </li>
        </ul>
      </section>

      <section>
        <h2>2. Para que usamos e com qual base legal</h2>
        <ul>
          <li>
            <strong>Fazer o Astra funcionar</strong> (conta, estatísticas, ranking, amigos, chat e chamadas): execução
            do serviço que você pediu ao criar a conta (art. 7º, V, da LGPD).
          </li>
          <li>
            <strong>Segurança e prevenção de abuso</strong> (limite de tentativas, bloqueio de contas, proteção contra
            ataques): legítimo interesse (art. 7º, IX).
          </li>
          <li>
            <strong>Integração com o GitHub:</strong> seu consentimento, dado ao conectar a conta (art. 7º, I). Você
            pode desconectar quando quiser, e escolher se seus amigos veem seu GitHub.
          </li>
        </ul>
        <p>Não vendemos seus dados, não usamos para anúncios e não criamos perfis de marketing.</p>
      </section>

      <section>
        <h2>3. Quem vê o quê dentro do Astra</h2>
        <ul>
          <li>Seus amigos veem seu nome, foto, bio, identificador e as mensagens que você troca com eles.</li>
          <li>
            O ranking mostra seu nome e seus minutos de foco: o ranking entre amigos para seus amigos, e o ranking
            global para os demais usuários.
          </li>
          <li>Seu GitHub só aparece para seus amigos se você autorizar.</li>
          <li>
            Os administradores do Astra podem ver a lista de contas (nome e e-mail) para moderação, e banir ou
            excluir contas que violem os <LegalLink doc="terms">Termos de Uso</LegalLink>.
          </li>
        </ul>
      </section>

      <section>
        <h2>4. Com quem compartilhamos</h2>
        <p>Usamos alguns serviços de terceiros para hospedar e operar o Astra:</p>
        <ul>
          <li>
            <strong>Vercel</strong> (hospedagem do site) e <strong>Cloudflare</strong> (rede e proteção contra
            ataques), com servidores nos Estados Unidos e em outros países.
          </li>
          <li>
            <strong>Oracle Cloud</strong> (servidor da aplicação e relay das chamadas) e o banco de dados, com
            servidores em São Paulo.
          </li>
          <li>
            <strong>Google e GitHub</strong>, quando você entra por eles ou conecta o GitHub.
          </li>
          <li>
            <strong>Have I Been Pwned</strong>, para checar se uma senha nova já vazou na internet. Enviamos só os 5
            primeiros caracteres do hash da senha, nunca a senha nem o hash inteiro.
          </li>
          <li>
            <strong>API pública do GitHub:</strong> ao abrir o perfil de um amigo que conectou o GitHub, o seu
            navegador consulta a API pública do GitHub para mostrar repositórios e seguidores, e o GitHub recebe seu
            endereço IP.
          </li>
        </ul>
        <p>
          Como alguns desses serviços ficam fora do Brasil, seus dados podem ser transferidos para outros países,
          sempre para essas finalidades e com provedores que adotam medidas de segurança (art. 33 da LGPD).
        </p>
        <p>Também podemos compartilhar dados quando a lei ou uma ordem judicial exigir.</p>
      </section>

      <section>
        <h2>5. Por quanto tempo guardamos</h2>
        <p>
          Guardamos seus dados enquanto sua conta existir. Você pode excluir sua conta quando quiser, em Editar
          perfil, na seção Privacidade e dados. Quando uma conta é excluída, os dados dela (perfil,
          sessões, cursos, metas, amizades, mensagens e conexão com o GitHub) são apagados do banco. O login fica
          salvo por até 30 dias em cada aparelho. Registros técnicos dos provedores seguem as políticas de cada um.
        </p>
      </section>

      <section>
        <h2>6. Segurança</h2>
        <p>
          Usamos conexão cifrada (HTTPS) em tudo, senhas com hash BCrypt, mensagens e tokens do GitHub cifrados no
          banco, checagem de dono em cada pedido, limite de tentativas de login e chamadas cifradas de ponta a ponta
          entre os participantes. Nenhum sistema é 100% seguro; se acontecer um incidente que traga risco a você,
          avisaremos você e a Autoridade Nacional de Proteção de Dados (ANPD).
        </p>
      </section>

      <section>
        <h2>7. Seus direitos</h2>
        <p>Pela LGPD (art. 18), você pode pedir a qualquer momento:</p>
        <ul>
          <li>confirmação de que tratamos seus dados e acesso a eles;</li>
          <li>correção de dados incompletos ou errados (nome, bio e foto você mesmo muda no perfil);</li>
          <li>anonimização, bloqueio ou exclusão de dados desnecessários;</li>
          <li>portabilidade dos seus dados;</li>
          <li>exclusão da sua conta e dos seus dados;</li>
          <li>informação sobre com quem compartilhamos seus dados;</li>
          <li>revogação do consentimento (por exemplo, desconectando o GitHub).</li>
        </ul>
        <p>
          Você mesmo pode baixar uma cópia dos seus dados e excluir sua conta em Editar perfil, na seção Privacidade
          e dados. Para os demais pedidos, escreva para <a href={`mailto:${LEGAL_CONTACT_EMAIL}`}>{LEGAL_CONTACT_EMAIL}</a> a partir do
          e-mail da sua conta. Respondemos em até 15 dias. Você também pode reclamar à ANPD.
        </p>
      </section>

      <section>
        <h2>8. Crianças e adolescentes</h2>
        <p>
          O Astra é para pessoas com 13 anos ou mais. Menores de 18 anos precisam da autorização dos pais ou
          responsáveis para usar o app.
        </p>
      </section>

      <section>
        <h2>9. Mudanças nesta política</h2>
        <p>
          Podemos atualizar esta política. A data no topo mostra a última mudança, e avisaremos no app quando houver
          mudança importante.
        </p>
      </section>
        </>
  )
}

export function PrivacyPage() {
  return (
    <LegalLayout title="Política de Privacidade">
      <PrivacyContent />
    </LegalLayout>
  )
}
