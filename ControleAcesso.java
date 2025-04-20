// Alex Narok Stavasz e Logan Ail Bernardes Borges

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

        System.out.print("Digite a senha: ");
        String senha = scanner.nextLine();

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
        String senha = scanner.nextLine();

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
            System.out.println("5. Executar arquivo");
            System.out.println("6. Sair");
            System.out.print("Digite uma opção: ");

            String opcao = scanner.nextLine();
            if (opcao.equals("6")) {
                System.out.println("Tchau! :)");
                break;
            }

            if (opcao.matches("[1-5]")) {
                System.out.print("Digite o nome do arquivo: ");
                String arquivo = scanner.nextLine();

                String acao = switch (opcao) {
                    case "1", "3" -> "ler";
                    case "2", "4" -> "apagar";
                    case "5" -> "executar";
                    default -> "";
                };

                if (verificarPermissao(usuario, arquivo, acao)) {
                    System.out.println("Acesso permitido.");
                } else {
                    System.out.println("Acesso negado.");
                }
            } else {
                System.out.println("Opção inválida.");
            }
        }
    }

    static boolean verificarPermissao(String usuario, String arquivo, String acao) {
        Map<String, Map<String, String>> permissoes = lerPermissoes();
        String perms = permissoes.getOrDefault(usuario, new HashMap<>()).get(arquivo);
        if (perms == null) return false;

        return switch (acao) {
            case "ler" -> perms.contains("l");
            case "apagar" -> perms.contains("e"); // e de excluir
            case "executar" -> perms.contains("x");
            default -> false;
        };
    }

    // JSON Helpers

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

    static Map<String, Map<String, String>> lerPermissoes() {
        try (Reader reader = new FileReader(PERMISSOES_FILE)) {
            Type type = new TypeToken<Map<String, Map<String, String>>>() {}.getType();
            return gson.fromJson(reader, type);
        } catch (IOException e) {
            return new HashMap<>();
        }
    }

    // Classes internas
    static class Usuario {
        String senha;
        int tentativas;

        Usuario(String senha, int tentativas) {
            this.senha = senha;
            this.tentativas = tentativas;
        }
    }
}
