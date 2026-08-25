-- Expande o roadmap "DevOps" (de 19 pra ~40 etapas), mesma linha das
-- expansões anteriores.

-- ===== Mais filhos nas etapas principais existentes =====

INSERT INTO roadmap_step (id, roadmap_id, parent_step_id, title, position, description) VALUES
    ('c1000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000030', '00000000-0000-0000-0000-000000000031', 'Bash scripting', 4, 'Automatizar tarefas repetitivas de terminal com scripts shell — a cola que conecta praticamente todas as outras ferramentas de DevOps.'),
    ('c1000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000030', '00000000-0000-0000-0000-000000000032', 'Registries de imagem (Docker Hub, ECR)', 4, 'Onde as imagens de container ficam armazenadas e versionadas antes de serem puxadas por qualquer ambiente que for rodá-las.'),
    ('c1000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000030', '00000000-0000-0000-0000-000000000033', 'Trunk-based development / GitFlow', 4, 'Estratégias de branching que afetam diretamente a frequência e o risco de cada deploy.'),
    ('c1000000-0000-0000-0000-000000000004', '00000000-0000-0000-0000-000000000030', '00000000-0000-0000-0000-000000000034', 'Helm charts', 4, 'Empacotar aplicações Kubernetes inteiras (deployments, services, configs) como um pacote versionado e reutilizável.'),
    ('c1000000-0000-0000-0000-000000000005', '00000000-0000-0000-0000-000000000030', '00000000-0000-0000-0000-000000000035', 'Alerting e on-call', 4, 'Transformar métricas em alertas acionáveis, sem gerar ruído que faz o time ignorar tudo.');

INSERT INTO roadmap_step_resource (step_id, label, url, position) VALUES
    ('c1000000-0000-0000-0000-000000000001', 'Bash Scripting Guide', 'https://www.gnu.org/software/bash/manual/bash.html', 0),
    ('c1000000-0000-0000-0000-000000000002', 'Docker — Registries', 'https://docs.docker.com/reference/cli/docker/image/push/', 0),
    ('c1000000-0000-0000-0000-000000000003', 'Trunk Based Development', 'https://trunkbaseddevelopment.com/', 0),
    ('c1000000-0000-0000-0000-000000000004', 'Helm Docs', 'https://helm.sh/docs/', 0),
    ('c1000000-0000-0000-0000-000000000005', 'Google SRE Book — Alerting', 'https://sre.google/sre-book/monitoring-distributed-systems/', 0);

-- ===== Novas etapas principais =====

INSERT INTO roadmap_step (id, roadmap_id, parent_step_id, title, position, description) VALUES
    ('c1000000-0000-0000-0000-000000000010', '00000000-0000-0000-0000-000000000030', NULL, 'Segurança em DevOps (DevSecOps)', 6, 'Segurança como parte do pipeline, não uma etapa manual no final: segredos protegidos, dependências escaneadas, acesso mínimo necessário.'),
    ('c1000000-0000-0000-0000-000000000020', '00000000-0000-0000-0000-000000000030', NULL, 'Configuração e automação', 7, 'Gerenciar a configuração de dezenas de servidores como código, em vez de entrar em cada um manualmente.'),
    ('c1000000-0000-0000-0000-000000000030', '00000000-0000-0000-0000-000000000030', NULL, 'Alta disponibilidade e recuperação', 8, 'Projetar pra quando (não se) algo falhar: backups testados, tráfego distribuído, capacidade que se ajusta à demanda.'),
    ('c1000000-0000-0000-0000-000000000040', '00000000-0000-0000-0000-000000000030', NULL, 'Cultura e práticas de SRE', 9, 'DevOps não é só ferramenta: é acordo de time sobre confiabilidade aceitável e como aprender com falhas sem procurar culpado.');

INSERT INTO roadmap_step_resource (step_id, label, url, position) VALUES
    ('c1000000-0000-0000-0000-000000000010', 'OWASP DevSecOps Guideline', 'https://owasp.org/www-project-devsecops-guideline/', 0),
    ('c1000000-0000-0000-0000-000000000020', 'Ansible Documentation', 'https://docs.ansible.com/', 0),
    ('c1000000-0000-0000-0000-000000000030', 'AWS Well-Architected — Reliability', 'https://docs.aws.amazon.com/wellarchitected/latest/reliability-pillar/welcome.html', 0),
    ('c1000000-0000-0000-0000-000000000040', 'Google SRE Book', 'https://sre.google/sre-book/table-of-contents/', 0);

-- ---- Subetapas de Segurança em DevOps ----
INSERT INTO roadmap_step (id, roadmap_id, parent_step_id, title, position, description) VALUES
    ('c1000000-0000-0000-0000-000000000011', '00000000-0000-0000-0000-000000000030', 'c1000000-0000-0000-0000-000000000010', 'Secrets management', 1, 'Nunca versionar senha ou chave de API em texto puro — cofres como Vault ou secrets do próprio provedor de nuvem cuidam disso.'),
    ('c1000000-0000-0000-0000-000000000012', '00000000-0000-0000-0000-000000000030', 'c1000000-0000-0000-0000-000000000010', 'Scanning de vulnerabilidades', 2, 'Ferramentas que analisam imagens de container e dependências automaticamente atrás de CVEs conhecidas.'),
    ('c1000000-0000-0000-0000-000000000013', '00000000-0000-0000-0000-000000000030', 'c1000000-0000-0000-0000-000000000010', 'Least privilege / IAM', 3, 'Cada serviço e pessoa só com o acesso mínimo necessário pra sua função — reduz o estrago possível de uma credencial vazada.');

INSERT INTO roadmap_step_resource (step_id, label, url, position) VALUES
    ('c1000000-0000-0000-0000-000000000011', 'HashiCorp Vault Docs', 'https://developer.hashicorp.com/vault/docs', 0),
    ('c1000000-0000-0000-0000-000000000012', 'Trivy (vulnerability scanner)', 'https://trivy.dev/', 0),
    ('c1000000-0000-0000-0000-000000000013', 'AWS — IAM Best Practices', 'https://docs.aws.amazon.com/IAM/latest/UserGuide/best-practices.html', 0);

-- ---- Subetapas de Configuração e automação ----
INSERT INTO roadmap_step (id, roadmap_id, parent_step_id, title, position, description) VALUES
    ('c1000000-0000-0000-0000-000000000021', '00000000-0000-0000-0000-000000000030', 'c1000000-0000-0000-0000-000000000020', 'Ansible', 1, 'Automação de configuração sem agente instalado no servidor de destino — só SSH e playbooks YAML.'),
    ('c1000000-0000-0000-0000-000000000022', '00000000-0000-0000-0000-000000000030', 'c1000000-0000-0000-0000-000000000020', 'Configuration drift', 2, 'O que acontece quando servidores que deveriam ser idênticos vão silenciosamente divergindo por mudanças manuais.'),
    ('c1000000-0000-0000-0000-000000000023', '00000000-0000-0000-0000-000000000030', 'c1000000-0000-0000-0000-000000000020', 'GitOps', 3, 'O estado desejado da infraestrutura vive num repositório Git, e uma ferramenta (ArgoCD, Flux) sincroniza o cluster automaticamente com ele.');

INSERT INTO roadmap_step_resource (step_id, label, url, position) VALUES
    ('c1000000-0000-0000-0000-000000000021', 'Ansible Documentation', 'https://docs.ansible.com/', 0),
    ('c1000000-0000-0000-0000-000000000022', 'Configuration Drift (Martin Fowler bliki)', 'https://martinfowler.com/bliki/ConfigurationDrift.html', 0),
    ('c1000000-0000-0000-0000-000000000023', 'What is GitOps (Red Hat)', 'https://www.redhat.com/en/topics/devops/what-is-gitops', 0);

-- ---- Subetapas de Alta disponibilidade e recuperação ----
INSERT INTO roadmap_step (id, roadmap_id, parent_step_id, title, position, description) VALUES
    ('c1000000-0000-0000-0000-000000000031', '00000000-0000-0000-0000-000000000030', 'c1000000-0000-0000-0000-000000000030', 'Backup e disaster recovery', 1, 'Ter backup não adianta se nunca foi testado restaurar — RTO e RPO definem quanto tempo e dado a empresa aceita perder.'),
    ('c1000000-0000-0000-0000-000000000032', '00000000-0000-0000-0000-000000000030', 'c1000000-0000-0000-0000-000000000030', 'Load balancing', 2, 'Distribuir tráfego entre várias instâncias da aplicação, tirando de circulação as que ficam não saudáveis.'),
    ('c1000000-0000-0000-0000-000000000033', '00000000-0000-0000-0000-000000000030', 'c1000000-0000-0000-0000-000000000030', 'Auto-scaling', 3, 'Adicionar ou remover instâncias automaticamente conforme a demanda sobe ou cai, em vez de provisionar pro pico o tempo todo.');

INSERT INTO roadmap_step_resource (step_id, label, url, position) VALUES
    ('c1000000-0000-0000-0000-000000000031', 'AWS — Disaster Recovery', 'https://docs.aws.amazon.com/whitepapers/latest/disaster-recovery-workloads-on-aws/disaster-recovery-options-in-the-cloud.html', 0),
    ('c1000000-0000-0000-0000-000000000032', 'NGINX — Load Balancing', 'https://docs.nginx.com/nginx/admin-guide/load-balancer/http-load-balancer/', 0),
    ('c1000000-0000-0000-0000-000000000033', 'Kubernetes — Horizontal Pod Autoscaling', 'https://kubernetes.io/docs/tasks/run-application/horizontal-pod-autoscale/', 0);

-- ---- Subetapas de Cultura e práticas de SRE ----
INSERT INTO roadmap_step (id, roadmap_id, parent_step_id, title, position, description) VALUES
    ('c1000000-0000-0000-0000-000000000041', '00000000-0000-0000-0000-000000000030', 'c1000000-0000-0000-0000-000000000040', 'SLIs e SLOs', 1, 'Definir numericamente o que "confiável o suficiente" significa pro seu sistema, em vez de perseguir 100% de uptime sem necessidade.'),
    ('c1000000-0000-0000-0000-000000000042', '00000000-0000-0000-0000-000000000030', 'c1000000-0000-0000-0000-000000000040', 'Postmortems sem culpa', 2, 'Depois de um incidente, documentar o que aconteceu e por quê, focando em melhorar o sistema em vez de apontar quem errou.'),
    ('c1000000-0000-0000-0000-000000000043', '00000000-0000-0000-0000-000000000030', 'c1000000-0000-0000-0000-000000000040', 'Feature flags', 3, 'Ligar/desligar funcionalidades em produção sem precisar de novo deploy, permitindo rollout gradual e reversão instantânea.');

INSERT INTO roadmap_step_resource (step_id, label, url, position) VALUES
    ('c1000000-0000-0000-0000-000000000041', 'Google SRE Book — SLOs', 'https://sre.google/sre-book/service-level-objectives/', 0),
    ('c1000000-0000-0000-0000-000000000042', 'Google SRE Book — Postmortem Culture', 'https://sre.google/sre-book/postmortem-culture/', 0),
    ('c1000000-0000-0000-0000-000000000043', 'Martin Fowler — Feature Toggles', 'https://martinfowler.com/articles/feature-toggles.html', 0);
