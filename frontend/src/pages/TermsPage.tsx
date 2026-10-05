import { LegalLink } from "@/context/LegalContext"
import { LegalLayout } from "@/components/LegalLayout"
import { LEGAL_CONTACT_EMAIL, LEGAL_CONTROLLER } from "@/lib/legal"

export function TermsContent() {
  return (
    <>
      <section>
        <p>
          Estes termos valem para quem usa o Astra (astra-app.dev), no site ou no app para Windows. Ao criar uma
          conta ou entrar com Google ou GitHub, você concorda com eles e com a{" "}
          <LegalLink doc="privacy">Política de Privacidade</LegalLink>. O Astra é mantido por {LEGAL_CONTROLLER}.
        </p>
      </section>

      <section>
        <h2>1. O serviço</h2>
        <p>
          O Astra é um projeto pessoal e gratuito para organizar estudos e trabalho, com sessões de foco, cursos,
          metas, roadmaps, amigos, chat, chamadas, ranking e integração com o GitHub. Ele pode mudar, ganhar ou perder
          funções, ficar fora do ar ou ser encerrado a qualquer momento, sem obrigação de aviso prévio.
        </p>
      </section>

      <section>
        <h2>2. Sua conta</h2>
        <ul>
          <li>Você precisa ter 13 anos ou mais. Menores de 18 anos precisam da autorização dos pais ou responsáveis.</li>
          <li>Use dados verdadeiros e uma conta por pessoa.</li>
          <li>Você é responsável por manter sua senha em segredo e pelo que acontece na sua conta.</li>
        </ul>
      </section>

      <section>
        <h2>3. Regras de conduta</h2>
        <p>Não é permitido:</p>
        <ul>
          <li>assediar, ameaçar, discriminar ou ofender outras pessoas;</li>
          <li>enviar conteúdo ilegal, violento, sexual envolvendo menores ou que viole direitos de terceiros;</li>
          <li>enviar spam, golpes ou links maliciosos;</li>
          <li>se passar por outra pessoa;</li>
          <li>gravar ou divulgar chamadas e conversas sem o consentimento dos participantes;</li>
          <li>
            tentar invadir, sobrecarregar, burlar limites de segurança ou acessar dados de outros usuários.
          </li>
        </ul>
      </section>

      <section>
        <h2>4. Seu conteúdo</h2>
        <p>
          O que você cria e envia (sessões, notas, mensagens, imagens, foto de perfil) continua sendo seu, e você é
          responsável por ele. Você nos autoriza a guardar e exibir esse conteúdo somente para fazer o Astra
          funcionar, por exemplo, entregar suas mensagens aos destinatários.
        </p>
      </section>

      <section>
        <h2>5. Moderação</h2>
        <p>
          Podemos suspender, banir ou excluir contas que violem estes termos ou a lei, e remover conteúdo
          inadequado. Se acreditar que houve engano, escreva para{" "}
          <a href={`mailto:${LEGAL_CONTACT_EMAIL}`}>{LEGAL_CONTACT_EMAIL}</a>.
        </p>
      </section>

      <section>
        <h2>6. App para Windows</h2>
        <p>
          O instalador do app não tem assinatura digital, por isso o Windows mostra um aviso na primeira
          instalação. Baixe o app apenas pelo link oficial do Astra. O app se atualiza sozinho.
        </p>
      </section>

      <section>
        <h2>7. Serviços de terceiros</h2>
        <p>
          O login com Google e GitHub e a integração com o GitHub também seguem os termos e as políticas desses
          serviços.
        </p>
      </section>

      <section>
        <h2>8. Garantias e responsabilidade</h2>
        <p>
          O Astra é oferecido gratuitamente, como está, sem garantia de disponibilidade contínua ou de ausência de
          erros. Na medida permitida pela lei, não nos responsabilizamos por perda de dados, interrupções ou danos
          indiretos decorrentes do uso do serviço. Nada nestes termos afasta direitos que a lei garante a você.
        </p>
      </section>

      <section>
        <h2>9. Encerramento</h2>
        <p>
          Você pode parar de usar o Astra quando quiser e excluir sua conta em Editar perfil, na seção Privacidade e
          dados.
        </p>
      </section>

      <section>
        <h2>10. Mudanças nos termos</h2>
        <p>
          Podemos atualizar estes termos. A data no topo mostra a última mudança, e avisaremos no app quando houver
          mudança importante. Continuar usando o Astra depois disso significa que você concorda com a nova versão.
        </p>
      </section>

      <section>
        <h2>11. Lei aplicável e contato</h2>
        <p>
          Estes termos seguem as leis do Brasil. Dúvidas: <a href={`mailto:${LEGAL_CONTACT_EMAIL}`}>{LEGAL_CONTACT_EMAIL}</a>.
        </p>
      </section>
        </>
  )
}

export function TermsPage() {
  return (
    <LegalLayout title="Termos de Uso">
      <TermsContent />
    </LegalLayout>
  )
}
