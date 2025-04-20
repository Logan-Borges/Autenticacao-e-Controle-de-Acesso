// Dupla: Alex Narok Stavasz e Logan Ail Bernardes Borges

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

public class ControleAcesso {
    static final String USUARIOS_FILE = "usuarios.json";
    static final String BLOQUEADOS_FILE = "bloqueados.json";
    static final String PERMISSOES_FILE = "permissoes.json";
    static Scanner scanner = new Scanner(System.in);
    static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void main(String[] args) {
        System.out.println("Bem vindo!");
        while (true) {
            System.out.println("=================");
            System.out.println("Digite uma opção para começar:");
            System.out.println("1. Cadastrar");
            System.out.println("2. Autenticar");
            System.out.println("3. Sair");
            System.out.print(">>> ");

            String opcao = scanner.nextLine();
            switch (opcao) {
                case "1":
                    cadastrarUsuario();
                    break;
                case "2":
                    autenticarUsuario();
                    break;
                case "3":
                    System.out.println("Saindo...");
                    return;
                default:
                    System.out.println("Opção inválida.");
            }
        }
    }

    static void cadastrarUsuario() {
        Map<String, Usuario> usuarios = lerUsuarios();
        System.out.print("Digite o novo nome de usuário: ");
        String login = scanner.nextLine();

        if (usuarios.containsKey(login)) {
            System.out.println("Usuário já existe.");
            return;
        }

        String senha;
        while (true) {
            System.out.print("Digite a senha: ");
            senha = lerSenhaOculta();

            if (senhaForte(senha)) break;
            System.out.println("Senha fraca. Use pelo menos 8 caracteres, com letra maiúscula, minúscula, número e símbolo.");
        }

        usuarios.put(login, new Usuario(senha, 0));
        salvarUsuarios(usuarios);
        System.out.println("Usuário cadastrado com sucesso!");
    }

    static void autenticarUsuario() {
        Map<String, Usuario> usuarios = lerUsuarios();
        Set<String> bloqueados = lerBloqueados();

        System.out.print("Digite seu nome de usuário: ");
        String login = scanner.nextLine();

        if (bloqueados.contains(login)) {
            System.out.println("Usuário bloqueado. Tente novamente mais tarde.");
            return;
        }

        System.out.print("Digite sua senha: ");
        String senha = lerSenhaOculta();

        Usuario usuario = usuarios.get(login);
        if (usuario != null && usuario.senha.equals(senha)) {
            usuario.tentativas = 0;
            salvarUsuarios(usuarios);
            System.out.println("Usuário " + login + " autenticado! :)");
            exibirMenuAcoes(login);
        } else {
            if (usuario != null) {
                usuario.tentativas++;
                salvarUsuarios(usuarios);
                if (usuario.tentativas >= 5) {
                    bloqueados.add(login);
                    salvarBloqueados(bloqueados);
                    System.out.println("Usuário bloqueado por 5 tentativas erradas.");
                } else {
                    System.out.println("Login ou senha incorreto.");
                }
            } else {
                System.out.println("Login ou senha incorreto.");
            }
        }
    }

    static void exibirMenuAcoes(String usuario) {
        while (true) {
            System.out.println("\nComandos disponíveis:");
            System.out.println("1. Listar arquivos");
            System.out.println("2. Criar arquivo");
            System.out.println("3. Ler arquivo");
            System.out.println("4. Excluir arquivo");
            System.out.println("5. Sair");
            System.out.print("Digite uma opção: ");

            String opcao = scanner.nextLine();
            if (opcao.equals("5")) {
                System.out.println("Tchau! :)");
                break;
            }

            if (opcao.matches("[1-4]")) {
                if (opcao.equals("1")) {
                    // Listar todos os arquivos do usuário
                    Map<String, Map<String, List<String>>> permissoes = lerPermissoes();
                    Map<String, List<String>> arquivos = permissoes.getOrDefault(usuario, new HashMap<>());

                    if (arquivos.isEmpty()) {
                        System.out.println("Nenhum arquivo encontrado.");
                    } else {
                        System.out.println("Arquivos cadastrados:");
                        for (String nomeArquivo : arquivos.keySet()) {
                            System.out.println("- " + nomeArquivo);
                        }
                    }
                } else {
                    System.out.print("Digite o nome do arquivo: ");
                    String arquivo = scanner.nextLine();

                    String acao = switch (opcao) {
                        case "2" -> "escrever";
                        case "3" -> "ler";
                        case "4" -> "apagar";
                        default -> "";
                    };

                    if (verificarPermissao(usuario, arquivo, acao)) {
                        System.out.println("Acesso permitido.");
                    } else {
                        System.out.println("Acesso negado.");
                    }
                }
            } else {
                System.out.println("Opção inválida.");
            }
        }
    }

    static boolean verificarPermissao(String usuario, String arquivo, String acao) {
        Map<String, Map<String, List<String>>> permissoes = lerPermissoes();
        List<String> permissoesDoArquivo = permissoes
                .getOrDefault(usuario, new HashMap<>())
                .get(arquivo);

        if (permissoesDoArquivo == null) return false;
        return permissoesDoArquivo.contains(acao);
    }

    static Map<String, Usuario> lerUsuarios() {
        try (Reader reader = new FileReader(USUARIOS_FILE)) {
            Type type = new TypeToken<Map<String, Usuario>>() {}.getType();
            return gson.fromJson(reader, type);
        } catch (IOException e) {
            return new HashMap<>();
        }
    }

    static void salvarUsuarios(Map<String, Usuario> usuarios) {
        try (Writer writer = new FileWriter(USUARIOS_FILE)) {
            gson.toJson(usuarios, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static Set<String> lerBloqueados() {
        try (Reader reader = new FileReader(BLOQUEADOS_FILE)) {
            Type type = new TypeToken<Set<String>>() {}.getType();
            return gson.fromJson(reader, type);
        } catch (IOException e) {
            return new HashSet<>();
        }
    }

    static void salvarBloqueados(Set<String> bloqueados) {
        try (Writer writer = new FileWriter(BLOQUEADOS_FILE)) {
            gson.toJson(bloqueados, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static Map<String, Map<String, List<String>>> lerPermissoes() {
        try (Reader reader = new FileReader(PERMISSOES_FILE)) {
            Type type = new TypeToken<Map<String, Map<String, List<String>>>>() {}.getType();
            return gson.fromJson(reader, type);
        } catch (IOException e) {
            return new HashMap<>();
        }
    }

    static boolean senhaForte(String senha) {
        if (senha.length() < 8) return false;
        if (!senha.matches(".*[A-Z].*")) return false;
        if (!senha.matches(".*[a-z].*")) return false;
        if (!senha.matches(".*\\d.*")) return false;
        if (!senha.matches(".*[^a-zA-Z0-9].*")) return false;
        return true;
    }

    static String lerSenhaOculta() {
        Console console = System.console();
        if (console != null) {
            char[] senhaArray = console.readPassword();
            return new String(senhaArray);
        } else {
            System.out.print("ATENÇÃO: Não foi possível ocultar a senha devido a IDE. Digite visivelmente: ");
            return scanner.nextLine();
        }
    }

    static class Usuario {
        String senha;
        int tentativas;

        Usuario(String senha, int tentativas) {
            this.senha = senha;
            this.tentativas = tentativas;
        }
    }
}
