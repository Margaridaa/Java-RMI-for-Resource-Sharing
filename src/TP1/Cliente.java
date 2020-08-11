package TP1;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;

public class Cliente extends java.rmi.server.UnicastRemoteObject implements ClienteInterface {

    static ServidorInterface s;
    private int numero;
    private String nome;
    private static ArrayList<Integer> carros_requisitados = new ArrayList<>();

    private static String menu = "\n1 - Inserir recurso\n"
            + "2 - Consultar recursos\n"
            + "3 - Requisitar/Reservar um recurso\n"
            + "4 - Devolver um recurso\n"
            + "5 - Listar recursos";

    // Construtor
    protected Cliente(String nome) throws RemoteException {
        super();
        this.nome = nome;
    }

    /**
     * Método remoto que permite ao servidor escrever para o cliente.
     * @param s string captada no servidor
     * @throws RemoteException
     */
    @Override
    public void printNoCliente(String s) throws RemoteException {
        System.out.println("\nSERVER: " + s);
    }

    public static void main(String[] args) {

        // Criação e instanciação do Security Manager
        System.setSecurityManager(new SecurityManager());

        try {
            System.out.print("Indique o seu nome: ");
            String nome = Ler.umaString();
            // Cria-se a si próprio
            Cliente c = new Cliente(nome);

            // Procura o servidor no serviço de nomes
            s = (ServidorInterface) Naming.lookup("ServidorTP1");

            // Subscreve ao servidor
            s.subscribe((ClienteInterface) c);

            while (true) {

                // É exibido o menu ao cliente, do qual escolherá uma opção
                System.out.println(menu);
                System.out.print("Opção: ");
                int op = Ler.umInt();

                switch (op) {

                    // Inserir recurso
                    case 1: {
                        System.out.print("Nome do carro: ");
                        String nomeCarro = Ler.umaString();

                        System.out.print("Descrição: ");
                        String descricaoCarro = Ler.umaString();

                        System.out.print("Preço de aluguer: ");
                        double precoCarro = Ler.umDouble();

                        // Invoca o método remoto do servidor que
                        // permite inserir um novo recurso.
                        System.out.println(s.inserirRecurso(nomeCarro, descricaoCarro, precoCarro));
                        break;
                    }

                    // Consultar recursos
                    case 2: {
                        // Se existem recursos...
                        if (s.existemRecursos()) {
                            System.out.print("Q.: O que procura? (As palavras-chave devem ser separadas por espaço) \nR.: ");
                            String procura = Ler.umaString();

                            // Invoca o método remoto do servidor e envia
                            // a string que contém todas as palavras-chave que
                            // o cliente procura num carro.
                            s.consultarRecursos((ClienteInterface) c, procura);
                        }

                        // Se não existem recursos...
                         else
                            System.out.println("Não há recursos registados.");
                        break;
                    }

                    // Requisitar/Reservar um recurso
                    case 3: {
                        // Se existem recursos...
                        if (s.existemRecursos()) {
                            System.out.println(s.todosRecursos());

                            System.out.print("Identificador do carro que pretende: ");
                            int id = Ler.umInt();

                            // Invoca o método remoto do servidor para
                            // requisitar o recurso com identificador = id
                            s.requisitarRecurso((ClienteInterface) c, id);
                        }

                        // Se não existem recursos...
                        else
                            System.out.println("Não há recursos registados.");

                        break;
                    }

                    // Devolver um recurso
                    case 4: {
                        // Obtém a lista dos id dos carros que estão requisitados pelo cliente.
                        ArrayList ids_validos = s.clienteCarrosRequisitados((ClienteInterface) c);

                        // Se o cliente não tem nenhum recurso atribuído a si próprio.
                        if (ids_validos.size()==0) {
                            System.out.println("Não existem recursos atribuídos a si.");
                        }

                        // Se o cliente tem recursos atribuídos a si.
                        else {
                            // Leitura do identificador do recurso a devolver
                            System.out.print("Identificador do carro a devolver (-1 para cancelar): ");
                            int id_devolver = Ler.umInt();

                            if (id_devolver != -1) {

                                // Enquanto o cliente não introduzir um id válido...
                                while (!ids_validos.contains(id_devolver)) {

                                    System.out.println("Identificador inválido.");
                                    System.out.println("Identificador do carro a devolver:");
                                    id_devolver = Ler.umInt();
                                }

                                // Invoca o método remoto do servidor para
                                // devolver o recurso de id = id_devolver
                                s.devolverRecurso((ClienteInterface) c, id_devolver);
                            }

                            else
                                System.out.println("Cancelado.");
                        }
                        break;
                    }

                    // Listar recursos
                    case 5: {

                        // Se existem recursos...
                        if (s.existemRecursos())
                            // Exibe todos os recursos através da invocação
                            // do método remoto do servidor "todosRecursos".
                            System.out.println(s.todosRecursos());

                        // Se não existem recursos...
                        else
                            System.out.println("Não há recursos registados.");

                        break;
                    }

                    default:
                        System.out.println("Opção inválida.");
                }
            }

        } catch (NotBoundException | MalformedURLException | RemoteException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     *  Método remoto invocado no servidor
     *  para permitir que este atribua
     *  um identificador numérico aos clientes.
     * @param numero a ser atribuído ao cliente
     */
    @Override
    public void setNumero(int numero) {
        this.numero = numero;
    }

    /**
     * Método remoto invocado no servidor para
     * obter o número do cliente.
     * @return
     */
    @Override
    public int getNumero() {
        return numero;
    }

    /**
     * Método remoto invocado pelo servidor para
     * obter o nome do cliente
     * @return
     */
    @Override
    public String getNome() {
        return nome;
    }

    @Override
    public ArrayList getRequisitados() throws java.rmi.RemoteException {
        return carros_requisitados;
    }

    @Override
    public void setRequisitados(ArrayList requisitados) {
        Cliente.carros_requisitados = requisitados;
    }

    /**
     * Método remoto invocado pelo servidor sempre que
     * o cliente requisite um novo carro.
     * @param id_carro id do carro requisitado
     * @throws RemoteException
     */
    @Override
    public void addRequisicoes(int id_carro) throws RemoteException {
        carros_requisitados.add(id_carro);
    }

    @Override
    public String toString() {
        return this.nome;
    }
}
