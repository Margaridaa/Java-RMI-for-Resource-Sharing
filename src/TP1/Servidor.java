package TP1;

import java.io.*;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;

public class Servidor extends java.rmi.server.UnicastRemoteObject implements ServidorInterface {

    private ArrayList<ClienteInterface> clientes = new ArrayList();
    private ArrayList<Carro> carros = new ArrayList<>();

    // Construtor
    protected Servidor() throws RemoteException {
        super();

        // Persistência de dados:
        // Obtém a lista dos recursos
        readFromFile();
    }

    public static void main(String[] args) {

        // Criação e instanciação do Security Manager
        System.setSecurityManager(new SecurityManager());

        try {
            // Cria-se a si próprio
            Servidor server = new Servidor();

            // Cria o RMI Registry no porto 1099
            LocateRegistry.createRegistry(1099);

            // Instancia-se no serviço de nomes como ServidorEx2
            Naming.rebind("ServidorTP1", server);

            System.out.println("Server ON");

        } catch (RemoteException | MalformedURLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Método remoto que permite ao cliente subscrever-se no servidor.
     * @param c interface do cliente
     * @throws RemoteException
     */
    @Override
    public void subscribe(ClienteInterface c) throws RemoteException {

        System.out.println("INFO: Cliente " + c.getNome() + ", de número " + clientes.size() + " acabou de subscrever!");
        // Atribuição de um número sequencial ao cliente
        c.setNumero(clientes.size());
        // Cliente é adicionado à lista de clientes
        clientes.add(c);
    }

    /**
     * Método auxiliar para verificação da existência,
     * ou não, de recursos, independentemente do seu estado.
     * @return true se existirem registos, false caso contrário.
     * @throws RemoteException
     */
    @Override
    public boolean existemRecursos() throws RemoteException {
        if (carros.size() == 0)
            return false;
        else
            return true;
    }

    /**
     * Método remoto invocado no cliente aquando da inserção de um novo recurso.
     * @param nomeCarro nome do carro a inserir
     * @param descricaoCarro descrição
     * @param precoCarro preço
     * @throws RemoteException
     */
    @Override
    public synchronized String inserirRecurso(String nomeCarro, String descricaoCarro, double precoCarro) throws RemoteException {

        Carro carro = new Carro(nomeCarro, descricaoCarro, precoCarro);
        carro.setEstado("disponível");
        carro.setId(carros.size());
        carros.add(carro);

        updateFile();

        return "Carro " + nomeCarro + " adicionado com sucesso!";
    }

    /**
     * Método remoto invocado no cliente aquando da consulta dos recursos
     * Sempre que se verifique uma correspondência, o cliente recebe uma nova
     * mensagem do servidor.
     * @param c cliente em questão
     * @param s string que especifica aquilo que o cliente procura
     * @throws RemoteException
     */
    @Override
    public void consultarRecursos(ClienteInterface c, String s) throws RemoteException {

        // Lista de carros que correspondem ao que o cliente procura
        ArrayList<Carro> carros_correspondencia = new ArrayList<>();

        // Dividir a string em palavras-chave
        String keywords[] = s.split(" ");

        // Procura e exibe todos os recursos que contêm descrições
        // que se aproximam daquilo que o cliente procura.
        for (Carro carro : carros) {

            for (String keyword : keywords) {

                if (carro.getDescricao().contains(keyword))
                    if (!carros_correspondencia.contains(carro)) {
                        carros_correspondencia.add(carro);
                        c.printNoCliente("Correspondência " + carros_correspondencia.size() + ":\n"
                                + carro.toString());
                    }
            }
        }

        // Se não forem encontradas correspondências
        if (carros_correspondencia.size() == 0) {
            c.printNoCliente("Não foram encontradas correspondências.");
        }
    }

    /**
     * Método remoto invocado no cliente para requisitar um recurso
     * @param c cliente em questão
     * @param id do recurso a ser requisitado
     * @throws RemoteException
     */
    @Override
    public synchronized void requisitarRecurso(ClienteInterface c, int id) throws RemoteException {

        // Se o identificador é inválido...
        if (id >= carros.size()) {
            c.printNoCliente("Identificador inválido. Requisição cancelada.");
        }

        // Se o identificador é válido...
        else {
            Carro carro_pretendido = carros.get(id);
            String estado_carro_pretendido = carro_pretendido.getEstado();

            // Verificar se o cliente já o requisitou uma vez
            // (se é o dono atual ou se o reservou).
            if (c.getRequisitados().contains(carro_pretendido.getId()) || carro_pretendido.getReservas().contains(c))
                c.printNoCliente("Já requisitou este carro.");

            // Se o cliente pode realmente requisitar este carro
            // (se não é o dono atual ou se não o reservou ainda)
            else {
                // Se o carro está disponível, o utilizador pode requisitá-lo.
                if (estado_carro_pretendido.equals("disponível")) {
                    carro_pretendido.setEstado("requisitado");
                    carro_pretendido.setDono_atual(c.getNome());

                    System.out.println("INFO: Cliente " + c.getNome() + " requisitou " + carro_pretendido.getNome());
                    c.printNoCliente("Requisição do carro " + carro_pretendido.getNome() + " efetuada com sucesso.");

                    // Adiciona-se o id carro em questão à lista de requisições atuais do cliente
                    c.addRequisicoes(carro_pretendido.getId());
                }

                // Se o carro está atribuído a outro cliente, fica reservado
                if (estado_carro_pretendido.equals("requisitado")) {

                    carro_pretendido.setEstado("reservado");

                    // O cliente fica na lista de clientes que requisitaram.
                    // No topo dessa lista estarão os que requisitaram primeiro.
                    carro_pretendido.addReserva(c.getNumero());

                    System.out.println("INFO: Cliente " + c.getNome() + " reservou " + carro_pretendido.getNome());

                    c.printNoCliente("Está agora na lista de clientes que reservaram este carro. " +
                            "\nReceberá uma mensagem assim que estiver disponível para si.");
                }

                // Se o carro já está reservado, o cliente é adicionado
                // à lista dos clientes que também já o reservaram.
                if (estado_carro_pretendido.equals("reservado")) {
                    // O cliente fica na lista de clientes que requisitaram.
                    // No topo dessa lista estarão os que requisitaram primeiro.
                    carro_pretendido.addReserva(c.getNumero());

                    System.out.println("INFO: Cliente " + c.getNome() + " reservou " + carro_pretendido.getNome());

                    c.printNoCliente("Está agora na lista de clientes que reservaram este carro. " +
                            "\nReceberá uma mensagem assim que estiver disponível para si.");
                }
            }

            updateFile();
        }
    }

    /**
     * Método remoto invocado no cliente para devolver um recurso
     * atribuído ao cliente em questão
     * @param c cliente em questão
     * @param id do recurso a ser devolvido
     * @throws RemoteException
     */
    @Override
    public void devolverRecurso(ClienteInterface c, int id) throws RemoteException {

        Carro carro_devolver = carros.get(id);
        ArrayList reservas = carro_devolver.getReservas();

        ArrayList<Integer> requisitados = c.getRequisitados();
        ArrayList<Integer> requisitados_atualizado = new ArrayList();

        // Atualiza a lista de carros_requisitados, isto é,
        // retira este carro da lista de carros requisitados
        // do cliente que o devolveu.
        for (Integer id_requisitados : requisitados) {
            if (id_requisitados != carro_devolver.getId())
                requisitados_atualizado.add(id_requisitados);
        }
        c.setRequisitados(requisitados_atualizado);

        // Mensagens informativas para o cliente
        // que devolve e o servidor, respetivamente
        c.printNoCliente("Devolução de " + carro_devolver.getNome() + " confirmada. Obrigado!");
        System.out.println("INFO: Cliente " + c.getNome() + " devolveu " + carros.get(id).getNome());

        // Verificar se algum cliente reservou este recurso
        if (reservas.size() > 0) {

            // Atualizar o estado do carro para requisitado
            // se apenas houver uma reserva
            if (reservas.size() == 1) {
                /* Atualizar o estado do carro para
                 * Requisitado por ____ (primeiro cliente na lista reservas do carro).
                 */
                carro_devolver.setEstado("requisitado");
            }

            // Obter o número do cliente que reservou este recurso.
            int id_cliente_reservou = (int) reservas.get(0);

            // Obter a ClienteInterface relativa ao número de cliente que reservou este recurso.
            ClienteInterface cliente_reservou = clientes.get(id_cliente_reservou);

            // Atualizar o dono atual do carro para
            // corresponder ao cliente que o reservou primeiro
            carro_devolver.setDono_atual(cliente_reservou.getNome());

            // Retirar esta reserva da lista de reservas.
            reservas.remove(0);

            cliente_reservou.printNoCliente("O recurso que reservou, "
                    + carro_devolver.getNome() + " acabou de ficar disponível para si!"
                    + "\nO seu estado foi atualizado.");

            // Adiciona o carro à lista de requisições atual do primeiro cliente que reservou.
            cliente_reservou.addRequisicoes(carro_devolver.getId());
        }

        // Se nenhum cliente reservou este recurso
        else {
            carro_devolver.setEstado("disponível");
            carro_devolver.setDono_atual("Sem dono");
        }

        updateFile();
    }

    /**
     * Método remoto invocado no cliente para exibição de todos
     * os carros registados.
     * @throws RemoteException
     */
    @Override
    public String todosRecursos() throws RemoteException {

        String todos = "";

        // Concatena numa só string a informação relevante
        // de todos os recursos registados.
        for (Carro carro : carros)
            todos = todos.concat(carro.toString());

        return todos;
    }

    /* Exibe ao utilizador todos os recursos que tem,
     * atualmente, no seu nome.
     * Ao mesmo tempo, cria uma lista com todos os
     * identificadores desses mesmos recursos, para
     * filtrar os identificadores que sejam válidos,
     * isto é,
     * que digam respeito a um recurso que o cliente
     * tem realmente atribuído a si mesmo, e retorna-a.
     */
    @Override
    public ArrayList<Integer> clienteCarrosRequisitados (ClienteInterface c) throws java.rmi.RemoteException {

        c.printNoCliente("Recursos atribuídos a si:");
        ArrayList<Integer> ids_validos = c.getRequisitados();

        for (Integer id_carro: ids_validos)
            c.printNoCliente(carros.get(id_carro).toString());

        return ids_validos;
    }

    /**
     * Persistência de dados:
     *  Obtém o registo de todos os carros a partir do ficheiro "carros.txt"
     */
    private void readFromFile () {
        FileInputStream f;
        ObjectInputStream ois_file;
        try {
            f = new FileInputStream("carros.txt");
            ois_file = new ObjectInputStream(f);

            carros = (ArrayList<Carro>) ois_file.readObject();

            f.close();
            ois_file.close();

        } catch (ClassNotFoundException | IOException e) {
            //System.out.println(e.getMessage());
        }
    }

    /**
     * Persistência de dados:
     *  Reescreve a lista de carros para o ficheiro "carros.txt",
     *  com o fim de o manter atualizado e de acordo com todas
     *  as alterações que forem sendo feitas à lista de carros,
     *  tais como mudanças de estado, adição de recursos, etc.
     */
    public void updateFile () {
        try {
            ObjectOutputStream oos_file = new ObjectOutputStream(new FileOutputStream("carros.txt"));

            oos_file.writeObject(carros);
            oos_file.flush();
            oos_file.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}