import java.io.*;
import java.util.*;
import org.json.*;

public class SistemaDePermissao {

    private static final String USUARIOS_FILE = "usuarios.json";
    private static final String PERMISSOES_FILE = "permissoes.json";

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);

        JSONObject usuarios = carregarJSON(USUARIOS_FILE);
        JSONObject permissoes = carregarJSON(PERMISSOES_FILE);

        // Se não houver admin, força a criação
        if (!usuarios.has("admin")) {
            System.out.println("Nenhum administrador encontrado. Vamos criar o usuário 'admin'.");
            System.out.print("Digite a senha para o administrador: ");
            String senhaAdmin = scanner.nextLine();
            usuarios.put("admin", senhaAdmin);
            permissoes.put("admin", new JSONObject());
            salvarJSON(USUARIOS_FILE, usuarios);
            salvarJSON(PERMISSOES_FILE, permissoes);
            System.out.println("Administrador criado com sucesso!\n");
        }

        while (true) {
            System.out.println("=== MENU ===");
            System.out.println("1. Cadastrar novo usuário");
            System.out.println("2. Fazer login");
            System.out.println("3. Sair");
            System.out.print("Escolha uma opção: ");
            String escolha = scanner.nextLine();

            if (escolha.equals("1")) {
                System.out.println("\n=== CADASTRO DE USUÁRIO ===");
                System.out.print("Digite um novo login: ");
                String novoLogin = scanner.nextLine();
                if (usuarios.has(novoLogin)) {
                    System.out.println("Usuário já existe.\n");
                    continue;
                }

                System.out.print("Digite uma nova senha: ");
                String novaSenha = scanner.nextLine();

                usuarios.put(novoLogin, novaSenha);
                permissoes.put(novoLogin, new JSONObject()); // Nenhuma permissão
                salvarJSON(USUARIOS_FILE, usuarios);
                salvarJSON(PERMISSOES_FILE, permissoes);
                System.out.println("Usuário cadastrado com sucesso!\n");

            } else if (escolha.equals("2")) {
                System.out.println("\n=== LOGIN ===");
                System.out.print("Digite seu login: ");
                String login = scanner.nextLine();
                System.out.print("Digite sua senha: ");
                String senha = scanner.nextLine();

                if (!autenticarUsuario(usuarios, login, senha)) {
                    System.out.println("Usuário ou senha inválidos.\n");
                    continue;
                }

                // Apenas admin pode ser o primeiro a logar
                if (!login.equals("admin") && somenteAdminPodeLogarPrimeiro(usuarios)) {
                    System.out.println("Apenas o administrador pode se logar neste momento.\n");
                    continue;
                }

                System.out.println("Bem-vindo, " + login + "!");

                System.out.print("Qual ação deseja realizar? (ler, escrever, apagar): ");
                String acao = scanner.nextLine();

                System.out.print("Sobre qual recurso? ");
                String recurso = scanner.nextLine();

                if (temPermissao(permissoes, login, recurso, acao)) {
                    System.out.println("Acesso permitido\n");
                } else {
                    System.out.println("Acesso negado\n");
                }

            } else if (escolha.equals("3")) {
                System.out.println("Encerrando o programa...");
                break;
            } else {
                System.out.println("Opção inválida!\n");
            }
        }

        scanner.close();
    }

    private static boolean autenticarUsuario(JSONObject usuarios, String login, String senha) {
        return usuarios.has(login) && usuarios.getString(login).equals(senha);
    }

    private static boolean temPermissao(JSONObject permissoes, String usuario, String recurso, String acao) {
        if (!permissoes.has(usuario)) return false;
        JSONObject userPerm = permissoes.getJSONObject(usuario);
        if (!userPerm.has(recurso)) return false;
        JSONArray acoesPermitidas = userPerm.getJSONArray(recurso);
        return acoesPermitidas.toList().contains(acao);
    }

    private static JSONObject carregarJSON(String filename) throws IOException, JSONException {
        File file = new File(filename);
        if (!file.exists()) return new JSONObject();
        String content = new String(java.nio.file.Files.readAllBytes(file.toPath()));
        return new JSONObject(content);
    }

    private static void salvarJSON(String filename, JSONObject json) throws IOException {
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write(json.toString(4)); // identado
        }
    }

    private static boolean somenteAdminPodeLogarPrimeiro(JSONObject usuarios) {
        return usuarios.length() == 1 && usuarios.has("admin");
    }
}
