# Tarefas (Android)

App simples de lista de tarefas com widget para a tela inicial.

- Adicione, conclua (toque na tarefa) e exclua tarefas.
- Widget redimensionável: mostra as tarefas, toque numa delas para concluir,
  e o botão **+** abre o app direto no campo de nova tarefa.
- Tema claro/escuro automático. Sem internet, sem permissões.

## Como instalar no celular

1. A cada push que mexe em `android-app/`, o GitHub Actions gera o APK e cria
   uma Release `tarefas-vN` com o arquivo `Tarefas.apk`.
2. No celular, abra a página **Releases** do repositório, toque em
   `Tarefas.apk` e permita "instalar apps desconhecidos" para o navegador.
3. Para o widget: segure num espaço vazio da tela inicial → **Widgets** →
   **Tarefas**.

O APK é assinado com `app/tarefas.keystore` (chave pessoal, senha `android`),
então versões novas instalam por cima sem perder as tarefas.

## Compilar localmente

Requer o Android SDK (Android Studio):

```sh
cd android-app
./gradlew assembleRelease
```
